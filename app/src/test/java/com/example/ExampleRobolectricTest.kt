package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.populateInitialData
import com.example.data.model.OrderStatus
import com.example.data.repository.CartItemRequest
import com.example.data.repository.OrderPlacementResult
import com.example.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: RestaurantRepository

  @Before
  fun setup() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    populateInitialData(db)
    repository = RestaurantRepository(db)
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Parosa Partner", appName)
  }

  @Test
  fun `customer can place order with server-verified prices and transitions through all states`() = runBlocking {
    // 1. Verify restaurant is online
    val profile = db.restaurantDao().getProfileSync()
    assertNotNull(profile)
    assertTrue(profile!!.isOnline)

    // 2. Customer places order for Paneer Tikka (item_1, price 280) and Butter Naan (item_4, price 50, qty 2)
    // Server subtotal should be: 280*1 + 50*2 = 380
    val items = listOf(
      CartItemRequest("item_1", 1),
      CartItemRequest("item_4", 2)
    )

    val placeResult = repository.placeCustomerOrder(
      customerId = "cust_test_1",
      customerName = "Rohan Mehta",
      customerPhone = "+91 98111 22334",
      deliveryAddress = "Flat 101, Indiranagar",
      items = items,
      specialInstructions = "Make it spicy"
    )

    assertTrue(placeResult is OrderPlacementResult.Success)
    val order = (placeResult as OrderPlacementResult.Success).order

    assertEquals(OrderStatus.NEW.name, order.status)
    assertEquals(380.0, order.subtotal, 0.01)
    assertEquals("Rohan Mehta", order.customerName)

    // 3. Restaurant receives and accepts order -> ACCEPTED
    repository.acceptOrder(order.id, 20)
    var updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.ACCEPTED.name, updated?.status)

    // 4. Restaurant starts preparing -> PREPARING
    repository.startPreparing(order.id)
    updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.PREPARING.name, updated?.status)

    // 5. Restaurant marks food ready -> READY
    repository.markFoodReady(order.id)
    updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.READY.name, updated?.status)

    // 5. Delivery valet picks up -> PICKED_UP
    repository.markOrderPickedUp(order.id)
    updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.PICKED_UP.name, updated?.status)

    // 6. Delivered & Handed over -> COMPLETED
    repository.completeOrder(order.id)
    updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.COMPLETED.name, updated?.status)

    // 7. Customer submits review after completion
    repository.submitCustomerReview(
      customerId = "cust_test_1",
      customerName = "Rohan Mehta",
      orderId = order.id,
      rating = 5.0f,
      comment = "Exceptional flavours and blazing fast delivery!"
    )

    val reviews = db.reviewDao().getAllReviews().first()
    assertTrue(reviews.any { it.orderId == order.id && it.rating == 5.0f })
  }

  @Test
  fun `offline restaurant prevents customer order placement`() = runBlocking {
    // Set restaurant offline
    repository.toggleOnlineStatus(false)

    val items = listOf(CartItemRequest("item_1", 1))
    val result = repository.placeCustomerOrder(
      customerId = "cust_2",
      customerName = "Sneha K",
      customerPhone = "+91 99999 88888",
      deliveryAddress = "Indiranagar",
      items = items
    )

    assertTrue(result is OrderPlacementResult.Error)
    assertTrue((result as OrderPlacementResult.Error).message.contains("Currently unavailable", ignoreCase = true))
  }

  @Test
  fun `admin authorization prevents non-admin and allows admin to manage restaurants`() = runBlocking {
    // Default role is not ADMIN
    repository.setUserRole(com.example.data.model.UserRole.CUSTOMER)
    assertFalse(repository.checkAdminAccess())

    // Attempting admin action as customer should throw SecurityException
    var securityExceptionCaught = false
    try {
      repository.updateRestaurantStatus(1, "SUSPENDED")
    } catch (e: SecurityException) {
      securityExceptionCaught = true
    }
    assertTrue("Non-admin user must not be able to update restaurant status", securityExceptionCaught)

    // Elevate to ADMIN role
    repository.setUserRole(com.example.data.model.UserRole.ADMIN)
    assertTrue(repository.checkAdminAccess())

    // Admin suspends restaurant
    repository.updateRestaurantStatus(1, "SUSPENDED")
    val updatedProfile = db.restaurantDao().getProfileSync()
    assertEquals("SUSPENDED", updatedProfile?.status)

    // Customer attempts to place order while suspended
    val items = listOf(CartItemRequest("item_1", 1))
    val result = repository.placeCustomerOrder(
      customerId = "cust_3",
      customerName = "Vikram R",
      customerPhone = "+91 91234 56789",
      deliveryAddress = "Koramangala",
      items = items
    )
    assertTrue(result is OrderPlacementResult.Error)
    assertTrue((result as OrderPlacementResult.Error).message.contains("suspended", ignoreCase = true))

    // Admin approves restaurant back to ACTIVE/APPROVED
    repository.updateRestaurantStatus(1, "APPROVED")
    val approvedProfile = db.restaurantDao().getProfileSync()
    assertEquals("APPROVED", approvedProfile?.status)

    // Admin verifies document
    repository.updateDocumentStatus("FSSAI", "VERIFIED")
    val docs = db.documentDao().getDocuments().first()
    val fssaiDoc = docs.find { it.docType == "FSSAI" }
    assertEquals("VERIFIED", fssaiDoc?.status)
  }

  @Test
  fun `delivery partner flow updates order status and logs earnings`() = runBlocking {
    // 1. Customer places order
    val items = listOf(CartItemRequest("item_1", 1))
    val placeResult = repository.placeCustomerOrder(
      customerId = "cust_deliv",
      customerName = "Aman Verma",
      customerPhone = "+91 98888 77777",
      deliveryAddress = "HSR Layout, Sector 2",
      items = items
    )
    assertTrue(placeResult is OrderPlacementResult.Success)
    val order = (placeResult as OrderPlacementResult.Success).order

    // 2. Restaurant accepts, prepares, and marks ready
    repository.acceptOrder(order.id, 15)
    repository.startPreparing(order.id)
    repository.markFoodReady(order.id)

    // 3. Delivery rider picks up
    repository.riderPickUpOrder(order.id)
    var updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.PICKED_UP.name, updated?.status)

    // 4. Delivery rider delivers
    repository.riderDeliverOrder(order.id, "dl_1")
    updated = db.orderDao().getOrderByIdSync(order.id)
    assertEquals(OrderStatus.COMPLETED.name, updated?.status)
  }

  // ================= 10 RESTAURANT OWNER AUTHENTICATION TESTS =================

  @Test
  fun `test 1 - new restaurant registration creates user and profile in PENDING status`() = runBlocking {
    val result = repository.registerRestaurant(
      fullName = "Deepak Sharma",
      restaurantName = "Dilli Darbar",
      mobile = "+91 98111 55443",
      email = "deepak.dillidarbar@gmail.com",
      password = "Password@123",
      confirmPassword = "Password@123",
      address = "Shop 12, Hauz Khas",
      city = "New Delhi",
      state = "Delhi",
      pincode = "110016",
      cuisine = "Mughlai, North Indian",
      fssai = "10020011000999"
    )

    assertTrue("Registration should succeed and return PendingApproval", result is com.example.data.repository.AuthResult.PendingApproval)
    val pending = result as com.example.data.repository.AuthResult.PendingApproval
    assertEquals("PENDING", pending.restaurant.status)
    assertEquals("RESTAURANT_OWNER", pending.user.role)
    assertEquals("Dilli Darbar", pending.restaurant.name)
    assertFalse("Password must never be stored in plaintext", pending.user.passwordHash.contains("Password@123"))
    assertTrue("Salt must be generated", pending.user.passwordSalt.isNotEmpty())
  }

  @Test
  fun `test 2 - duplicate registration prevention by email and mobile`() = runBlocking {
    // Attempt to register with email that already exists (punjabkitchen.blr@gmail.com)
    val emailDupResult = repository.registerRestaurant(
      fullName = "Another Owner",
      restaurantName = "New Kitchen",
      mobile = "+91 99999 11111",
      email = "punjabkitchen.blr@gmail.com",
      password = "Password@123",
      confirmPassword = "Password@123",
      address = "Street 1",
      city = "Bengaluru",
      state = "Karnataka",
      pincode = "560001",
      cuisine = "South Indian",
      fssai = "10020011000888"
    )
    assertTrue("Duplicate email must return Error", emailDupResult is com.example.data.repository.AuthResult.Error)
    assertTrue((emailDupResult as com.example.data.repository.AuthResult.Error).message.contains("email address already exists", ignoreCase = true))

    // Attempt to register with mobile number that already exists (+91 98450 12891)
    val phoneDupResult = repository.registerRestaurant(
      fullName = "Another Owner",
      restaurantName = "New Kitchen 2",
      mobile = "+91 98450 12891",
      email = "unique.email@gmail.com",
      password = "Password@123",
      confirmPassword = "Password@123",
      address = "Street 2",
      city = "Bengaluru",
      state = "Karnataka",
      pincode = "560001",
      cuisine = "South Indian",
      fssai = "10020011000777"
    )
    assertTrue("Duplicate mobile must return Error", phoneDupResult is com.example.data.repository.AuthResult.Error)
    assertTrue((phoneDupResult as com.example.data.repository.AuthResult.Error).message.contains("mobile number already exists", ignoreCase = true))
  }

  @Test
  fun `test 3 - pending approval login returns PendingApproval state`() = runBlocking {
    // Spice Villa (rest_owner_pending) was seeded with status = PENDING
    val result = repository.loginRestaurant("spicevilla.pending@gmail.com", "Partner@123")
    assertTrue("Login for pending restaurant must return PendingApproval", result is com.example.data.repository.AuthResult.PendingApproval)
    val pending = result as com.example.data.repository.AuthResult.PendingApproval
    assertEquals("PENDING", pending.restaurant.status)
    assertEquals("Spice Villa", pending.restaurant.name)
  }

  @Test
  fun `test 4 - admin approval updates restaurant status to APPROVED`() = runBlocking {
    repository.setUserRole(com.example.data.model.UserRole.ADMIN)

    // Admin approves restaurant ID 2 (Spice Villa)
    repository.updateRestaurantStatus(2, "APPROVED")
    val profile = db.restaurantDao().getProfileByIdSync(2)
    assertEquals("APPROVED", profile?.status)
  }

  @Test
  fun `test 5 - successful login after approval grants access and initializes restaurant context`() = runBlocking {
    // First ensure restaurant is approved
    repository.setUserRole(com.example.data.model.UserRole.ADMIN)
    repository.updateRestaurantStatus(2, "APPROVED")

    // Now partner logs in
    val loginResult = repository.loginRestaurant("spicevilla.pending@gmail.com", "Partner@123")
    assertTrue("Login after approval must succeed", loginResult is com.example.data.repository.AuthResult.Success)
    val success = loginResult as com.example.data.repository.AuthResult.Success
    assertEquals("Spice Villa", success.restaurant.name)
    assertEquals(2, repository.currentRestaurantId.value)
    assertEquals(success.user.id, repository.currentAuthenticatedUser.value?.id)
  }

  @Test
  fun `test 6 - incorrect password returns error`() = runBlocking {
    val result = repository.loginRestaurant("punjabkitchen.blr@gmail.com", "WrongPassword!999")
    assertTrue("Incorrect password must return Error", result is com.example.data.repository.AuthResult.Error)
    assertTrue((result as com.example.data.repository.AuthResult.Error).message.contains("Incorrect password", ignoreCase = true))
  }

  @Test
  fun `test 7 - forgot password resets password securely and allows subsequent login`() = runBlocking {
    // Reset password for punjabkitchen.blr@gmail.com
    val resetResult = repository.resetPassword("punjabkitchen.blr@gmail.com", "NewSecret#2026", "NewSecret#2026")
    assertTrue("Password reset must succeed", resetResult is com.example.data.repository.AuthResult.Success)

    // Old password should fail
    val oldLoginResult = repository.loginRestaurant("punjabkitchen.blr@gmail.com", "Partner@123")
    assertTrue("Old password must fail", oldLoginResult is com.example.data.repository.AuthResult.Error)

    // New password should succeed
    val newLoginResult = repository.loginRestaurant("punjabkitchen.blr@gmail.com", "NewSecret#2026")
    assertTrue("New password must succeed", newLoginResult is com.example.data.repository.AuthResult.Success)
  }

  @Test
  fun `test 8 - suspended restaurant login prevents dashboard access and returns Suspended status`() = runBlocking {
    // Curry House (curryhouse.suspended@gmail.com) is seeded with status = SUSPENDED
    val result = repository.loginRestaurant("curryhouse.suspended@gmail.com", "Partner@123")
    assertTrue("Suspended restaurant login must return Suspended", result is com.example.data.repository.AuthResult.Suspended)
    val suspended = result as com.example.data.repository.AuthResult.Suspended
    assertTrue(suspended.message.contains("suspended", ignoreCase = true))
  }

  @Test
  fun `test 9 - logout and session expiry clears active session`() = runBlocking {
    // Log in as Restaurant 1
    val loginResult = repository.loginRestaurant("punjabkitchen.blr@gmail.com", "Partner@123")
    assertTrue(loginResult is com.example.data.repository.AuthResult.Success)
    assertNotNull(repository.currentAuthenticatedUser.value)

    // Log out
    repository.logout()
    assertNull("Authenticated user must be cleared upon logout", repository.currentAuthenticatedUser.value)
  }

  @Test
  fun `test 10 - data isolation prevents Restaurant A from modifying Restaurant B orders`() = runBlocking {
    // 1. Create order specifically belonging to Restaurant 2 (rest_2)
    val now = System.currentTimeMillis()
    val orderForRest2 = com.example.data.entity.OrderEntity(
      id = "ORD-REST2-001",
      customerName = "Customer Rest2",
      customerPhone = "+91 99999 00000",
      deliveryAddress = "Indiranagar",
      itemsSummary = "Paneer Tikka x 1",
      itemsJson = "[]",
      subtotal = 250.0,
      taxes = 12.5,
      discount = 0.0,
      deliveryFee = 35.0,
      commission = 40.0,
      netEarnings = 210.0,
      totalAmount = 297.5,
      orderTime = now,
      status = OrderStatus.NEW.name,
      restaurantId = "rest_2"
    )
    db.orderDao().insertOrder(orderForRest2)

    // 2. Authenticate as Restaurant 1 (rest_owner_1, restaurantId = 1)
    repository.loginRestaurant("punjabkitchen.blr@gmail.com", "Partner@123")
    assertEquals(1, repository.currentAuthenticatedUser.value?.restaurantId)

    // 3. Restaurant 1 attempts to accept Restaurant 2's order -> must throw SecurityException
    var securityViolationCaught = false
    try {
      repository.acceptOrder("ORD-REST2-001", 20)
    } catch (e: SecurityException) {
      securityViolationCaught = true
    }
    assertTrue("Restaurant 1 must NOT be authorized to accept order belonging to Restaurant 2", securityViolationCaught)

    // Order status in DB must remain NEW
    val orderAfterAttempt = db.orderDao().getOrderByIdSync("ORD-REST2-001")
    assertEquals(OrderStatus.NEW.name, orderAfterAttempt?.status)
  }
}
