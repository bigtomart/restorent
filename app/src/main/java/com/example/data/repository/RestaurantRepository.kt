package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.model.OrderStatus
import com.example.data.model.StaffRole
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

import com.example.data.util.PasswordHasher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

data class CartItemRequest(
  val itemId: String,
  val quantity: Int
)

sealed class OrderPlacementResult {
  data class Success(val order: OrderEntity) : OrderPlacementResult()
  data class Error(val message: String) : OrderPlacementResult()
}

sealed class AuthResult {
  data class Success(val user: UserEntity, val restaurant: RestaurantProfileEntity) : AuthResult()
  data class PendingApproval(val user: UserEntity, val restaurant: RestaurantProfileEntity) : AuthResult()
  data class Suspended(val message: String) : AuthResult()
  data class Rejected(val message: String) : AuthResult()
  data class Error(val message: String) : AuthResult()
}

class RestaurantRepository(private val db: AppDatabase) {

  // Current active staff role for permission testing
  private val _currentStaffRole = MutableStateFlow(StaffRole.OWNER)
  val currentStaffRole: StateFlow<StaffRole> = _currentStaffRole.asStateFlow()

  // Authenticated Partner User & Dynamic Restaurant Context
  private val _currentAuthenticatedUser = MutableStateFlow<UserEntity?>(null)
  val currentAuthenticatedUser: StateFlow<UserEntity?> = _currentAuthenticatedUser.asStateFlow()

  private val _currentRestaurantId = MutableStateFlow<Int>(1)
  val currentRestaurantId: StateFlow<Int> = _currentRestaurantId.asStateFlow()

  fun setAuthenticatedUser(user: UserEntity?, restId: Int = user?.restaurantId ?: 1) {
    _currentAuthenticatedUser.value = user
    _currentRestaurantId.value = restId
  }

  // New incoming order audio/visual alert event
  private val _incomingOrderAlert = MutableSharedFlow<OrderEntity>(extraBufferCapacity = 1)
  val incomingOrderAlert: SharedFlow<OrderEntity> = _incomingOrderAlert.asSharedFlow()

  fun setStaffRole(role: StaffRole) {
    _currentStaffRole.value = role
  }

  // Profile & Online Status - dynamically bound to active restaurant
  @OptIn(ExperimentalCoroutinesApi::class)
  val restaurantProfile: Flow<RestaurantProfileEntity?> = _currentRestaurantId.flatMapLatest { id ->
    db.restaurantDao().getProfileById(id)
  }

  suspend fun toggleOnlineStatus(isOnline: Boolean, reason: String = "", durationMinutes: Int = 0) {
    val restId = _currentRestaurantId.value
    val until = if (durationMinutes > 0) System.currentTimeMillis() + (durationMinutes * 60 * 1000L) else 0L
    db.restaurantDao().updateOnlineStatusForId(restId, isOnline, reason, until)
  }

  suspend fun updateProfile(profile: RestaurantProfileEntity) {
    db.restaurantDao().updateProfile(profile)
  }

  suspend fun updateVerificationStatus(status: String) {
    val restId = _currentRestaurantId.value
    db.restaurantDao().updateRestaurantStatus(restId, status)
  }

  // Orders - dynamically scoped to authenticated restaurant
  @OptIn(ExperimentalCoroutinesApi::class)
  val allOrders: Flow<List<OrderEntity>> = _currentRestaurantId.flatMapLatest { id ->
    db.orderDao().getOrdersForRestaurant("rest_$id")
  }
  val newOrdersCount: Flow<Int> = allOrders.map { orders -> orders.count { it.status == "NEW" } }
  val activeKitchenCount: Flow<Int> = allOrders.map { orders -> orders.count { it.status == "PREPARING" || it.status == "ACCEPTED" } }
  val readyOrdersCount: Flow<Int> = allOrders.map { orders -> orders.count { it.status == "READY" } }
  val completedOrdersCount: Flow<Int> = allOrders.map { orders -> orders.count { it.status == "COMPLETED" } }
  val cancelledOrdersCount: Flow<Int> = allOrders.map { orders -> orders.count { it.status == "CANCELLED" || it.status == "REJECTED" } }
  val todaySales: Flow<Double> = allOrders.map { orders -> orders.filter { it.status == "COMPLETED" }.sumOf { it.totalAmount } }
  val todayEarnings: Flow<Double> = allOrders.map { orders -> orders.filter { it.status == "COMPLETED" }.sumOf { it.netEarnings } }

  fun getOrderById(orderId: String): Flow<OrderEntity?> = db.orderDao().getOrderById(orderId)

  suspend fun verifyOrderOwnership(orderId: String) {
    val order = db.orderDao().getOrderByIdSync(orderId)
      ?: throw IllegalArgumentException("Order '$orderId' does not exist.")
    val user = _currentAuthenticatedUser.value
    if (user != null && user.role != UserRole.ADMIN.name) {
      val expectedRestId = "rest_${user.restaurantId}"
      if (order.restaurantId != expectedRestId) {
        throw SecurityException("Security Violation: Restaurant partner (ID: ${user.restaurantId}) is not authorized to modify order '$orderId' belonging to ${order.restaurantId}.")
      }
    }
  }

  suspend fun acceptOrder(orderId: String, prepTimeMinutes: Int = 20) {
    verifyOrderOwnership(orderId)
    val now = System.currentTimeMillis()
    db.orderDao().acceptOrder(orderId, OrderStatus.ACCEPTED.name, prepTimeMinutes, now)
  }

  suspend fun startPreparing(orderId: String) {
    verifyOrderOwnership(orderId)
    val now = System.currentTimeMillis()
    db.orderDao().updateOrderStatusTime(orderId, OrderStatus.PREPARING.name, now)
  }

  suspend fun markFoodReady(orderId: String) {
    verifyOrderOwnership(orderId)
    val now = System.currentTimeMillis()
    db.orderDao().updateOrderStatusTime(orderId, OrderStatus.READY.name, now)
  }

  suspend fun markPickedUp(orderId: String) {
    val now = System.currentTimeMillis()
    db.orderDao().updateOrderStatusTime(orderId, OrderStatus.PICKED_UP.name, now)
  }

  suspend fun completeOrder(orderId: String) {
    verifyOrderOwnership(orderId)
    val now = System.currentTimeMillis()
    val order = db.orderDao().getOrderByIdSync(orderId)
    if (order != null) {
      db.orderDao().updateOrder(order.copy(status = OrderStatus.COMPLETED.name, completedAt = now))
      // Add settlement transaction
      val tx = TransactionEntity(
        id = "TX-" + UUID.randomUUID().toString().take(8).uppercase(),
        orderId = orderId,
        date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(now)),
        timestamp = now,
        grossAmount = order.totalAmount,
        commission = order.commission,
        tax = order.taxes,
        discountContribution = order.discount,
        netEarning = order.netEarnings,
        settlementStatus = "PENDING",
        settlementReference = "QUEUED_PAYOUT"
      )
      db.transactionDao().insertTransaction(tx)
    }
  }

  suspend fun rejectOrder(orderId: String, reason: String) {
    verifyOrderOwnership(orderId)
    db.orderDao().rejectOrder(orderId, OrderStatus.REJECTED.name, reason)
  }

  suspend fun cancelOrder(orderId: String, reason: String) {
    verifyOrderOwnership(orderId)
    val order = db.orderDao().getOrderByIdSync(orderId)
    if (order != null) {
      db.orderDao().updateOrder(order.copy(status = OrderStatus.CANCELLED.name, cancellationReason = reason))
    }
  }

  suspend fun markOrderPickedUp(orderId: String) {
    val order = db.orderDao().getOrderByIdSync(orderId)
    if (order != null) {
      val now = System.currentTimeMillis()
      db.orderDao().updateOrder(order.copy(status = OrderStatus.PICKED_UP.name, updatedAt = now))
      
      val notif = NotificationEntity(
        id = "notif_" + UUID.randomUUID().toString().take(6),
        title = "🛵 Order Picked Up: $orderId",
        message = "Delivery partner ${order.deliveryPartnerName} has picked up order $orderId.",
        type = "ORDER",
        timestamp = now,
        isRead = false,
        referenceId = orderId
      )
      db.notificationDao().insertNotification(notif)
    }
  }

  suspend fun placeCustomerOrder(
    customerId: String,
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    items: List<CartItemRequest>,
    specialInstructions: String = "",
    paymentMethod: String = "Online UPI (Prepaid)"
  ): OrderPlacementResult {
    val profile = db.restaurantDao().getProfileSync()
      ?: return OrderPlacementResult.Error("Restaurant not found")

    if (profile.status != "APPROVED") {
      return OrderPlacementResult.Error("Currently unavailable - Restaurant account is ${profile.status.lowercase()} by Admin.")
    }

    if (!profile.isOnline) {
      return OrderPlacementResult.Error("Currently unavailable - Restaurant is offline and cannot accept orders.")
    }

    if (items.isEmpty()) {
      return OrderPlacementResult.Error("Your cart is empty.")
    }

    var serverSubtotal = 0.0
    val itemsSummaryList = mutableListOf<String>()
    val validatedItemsJsonList = mutableListOf<String>()

    for (itemReq in items) {
      if (itemReq.quantity <= 0) {
        return OrderPlacementResult.Error("Invalid quantity for items.")
      }
      val menuItem = db.menuDao().getMenuItemById(itemReq.itemId)
        ?: return OrderPlacementResult.Error("Item '${itemReq.itemId}' does not exist.")

      if (!menuItem.isAvailable || menuItem.isSoldOut) {
        return OrderPlacementResult.Error("'${menuItem.name}' is sold out / currently unavailable.")
      }

      val effectivePrice = if (menuItem.discountedPrice > 0 && menuItem.discountedPrice < menuItem.price) {
        menuItem.discountedPrice
      } else {
        menuItem.price
      }

      val itemTotal = effectivePrice * itemReq.quantity
      serverSubtotal += itemTotal
      itemsSummaryList.add("${itemReq.quantity}x ${menuItem.name}")
      validatedItemsJsonList.add("""{"name":"${menuItem.name}","quantity":${itemReq.quantity},"price":$effectivePrice}""")
    }

    val discount = if (serverSubtotal >= 500) 50.0 else 0.0
    val deliveryFee = 35.0
    val tax = (serverSubtotal * 0.05)
    val totalAmount = serverSubtotal - discount + deliveryFee + tax
    val commission = (serverSubtotal * 0.16)
    val netEarnings = serverSubtotal - discount - commission

    val now = System.currentTimeMillis()
    val orderId = "#ORD-" + Random.nextInt(1000, 9999)

    val newOrder = OrderEntity(
      id = orderId,
      customerName = customerName,
      customerPhone = customerPhone,
      deliveryAddress = deliveryAddress,
      itemsSummary = itemsSummaryList.joinToString(", "),
      itemsJson = "[" + validatedItemsJsonList.joinToString(",") + "]",
      subtotal = serverSubtotal,
      taxes = tax,
      discount = discount,
      deliveryFee = deliveryFee,
      commission = commission,
      netEarnings = netEarnings,
      totalAmount = totalAmount,
      specialInstructions = specialInstructions,
      orderTime = now,
      status = OrderStatus.NEW.name,
      estimatedPrepTimeMinutes = profile.avgPrepTimeMinutes,
      paymentMethod = paymentMethod,
      restaurantId = "rest_${profile.id}",
      customerId = customerId,
      paymentStatus = "PAID",
      createdAt = now,
      updatedAt = now
    )

    db.orderDao().insertOrder(newOrder)

    // Notify Restaurant Partner
    val notif = NotificationEntity(
      id = "notif_" + UUID.randomUUID().toString().take(6),
      title = "🔔 New Order: $orderId",
      message = "New order for ₹${totalAmount.toInt()} from $customerName.",
      type = "ORDER",
      timestamp = now,
      isRead = false,
      referenceId = orderId
    )
    db.notificationDao().insertNotification(notif)
    _incomingOrderAlert.tryEmit(newOrder)

    return OrderPlacementResult.Success(newOrder)
  }

  suspend fun submitCustomerReview(
    customerId: String,
    customerName: String,
    orderId: String,
    rating: Float,
    comment: String
  ) {
    val order = db.orderDao().getOrderByIdSync(orderId)
    val now = System.currentTimeMillis()
    val reviewId = "rev_" + UUID.randomUUID().toString().take(6)
    val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(now))
    val itemsSummary = order?.itemsSummary ?: "Meal order"

    val review = ReviewEntity(
      id = reviewId,
      orderId = orderId,
      customerName = customerName,
      rating = rating,
      foodRating = rating,
      deliveryRating = 5.0f,
      comment = comment,
      itemsReviewed = itemsSummary,
      date = dateStr,
      customerId = customerId,
      restaurantId = "rest_1",
      createdAt = now
    )
    db.reviewDao().insertReview(review)
  }

  // Simulated Real-time incoming order generator
  suspend fun simulateIncomingCustomerOrder() {
    val randomId = "ORD-" + (9825 + Random.nextInt(100))
    val names = listOf("Arjun Kapoor", "Simran Kaur", "Aditya Roy", "Neha Sharma", "Kartik Iyer", "Pooja Hegde")
    val selectedName = names.random()
    val now = System.currentTimeMillis()

    val newOrder = OrderEntity(
      id = randomId,
      customerName = selectedName,
      customerPhone = "+91 98" + Random.nextInt(10000000, 99999999),
      deliveryAddress = "Flat ${Random.nextInt(101, 804)}, Tower ${listOf("A", "B", "C").random()}, Express Greens, CP",
      itemsSummary = "1x Hyderabadi Chicken Biryani, 2x Garlic Butter Naan",
      itemsJson = """[{"name":"Hyderabadi Chicken Biryani","quantity":1,"price":320.0,"addons":[]},{"name":"Garlic Butter Naan","quantity":2,"price":70.0,"addons":[]}]""",
      subtotal = 460.0,
      taxes = 23.0,
      discount = 30.0,
      deliveryFee = 35.0,
      commission = 73.6,
      netEarnings = 379.4,
      totalAmount = 488.0,
      specialInstructions = "Please deliver before 30 mins if possible.",
      orderTime = now,
      status = OrderStatus.NEW.name,
      estimatedPrepTimeMinutes = 20,
      paymentMethod = "Online UPI (Prepaid)"
    )

    db.orderDao().insertOrder(newOrder)

    // Add notification
    val notif = NotificationEntity(
      id = "notif_" + UUID.randomUUID().toString().take(6),
      title = "🔔 New Order: $randomId",
      message = "New order for ₹488.00 from $selectedName. Please accept within 3 minutes.",
      type = "ORDER",
      timestamp = now,
      isRead = false,
      referenceId = randomId
    )
    db.notificationDao().insertNotification(notif)
    _incomingOrderAlert.tryEmit(newOrder)
  }

  // Menu & Categories
  val allCategories: Flow<List<CategoryEntity>> = db.menuDao().getAllCategories()
  val allMenuItems: Flow<List<MenuItemEntity>> = db.menuDao().getAllMenuItems()

  suspend fun addCategory(name: String) {
    val id = "cat_" + UUID.randomUUID().toString().take(6)
    db.menuDao().insertCategory(CategoryEntity(id, name, displayOrder = 10, itemCount = 0, isActive = true))
  }

  suspend fun updateCategory(category: CategoryEntity) {
    db.menuDao().updateCategory(category)
  }

  suspend fun deleteCategory(category: CategoryEntity) {
    db.menuDao().deleteCategory(category)
  }

  suspend fun toggleItemSoldOut(itemId: String, isSoldOut: Boolean) {
    db.menuDao().setItemSoldOut(itemId, isSoldOut)
  }

  suspend fun toggleItemAvailability(itemId: String, isAvailable: Boolean) {
    db.menuDao().setItemAvailability(itemId, isAvailable)
  }

  suspend fun updateItemStock(itemId: String, stock: Int) {
    db.menuDao().updateItemStock(itemId, stock)
  }

  suspend fun insertMenuItem(item: MenuItemEntity) {
    db.menuDao().insertMenuItem(item)
  }

  suspend fun updateMenuItem(item: MenuItemEntity) {
    db.menuDao().updateMenuItem(item)
  }

  suspend fun deleteMenuItem(item: MenuItemEntity) {
    db.menuDao().deleteMenuItem(item)
  }

  suspend fun duplicateMenuItem(itemId: String) {
    val original = db.menuDao().getMenuItemById(itemId)
    if (original != null) {
      val duplicate = original.copy(
        id = "item_" + UUID.randomUUID().toString().take(6),
        name = "${original.name} (Copy)"
      )
      db.menuDao().insertMenuItem(duplicate)
    }
  }

  // Offers
  val allOffers: Flow<List<OfferEntity>> = db.offerDao().getAllOffers()

  suspend fun insertOffer(offer: OfferEntity) {
    db.offerDao().insertOffer(offer)
  }

  suspend fun deleteOffer(offer: OfferEntity) {
    db.offerDao().deleteOffer(offer)
  }

  suspend fun setOfferStatus(offerId: String, status: String) {
    db.offerDao().setOfferStatus(offerId, status)
  }

  // Staff
  val allStaff: Flow<List<StaffEntity>> = db.staffDao().getAllStaff()

  suspend fun addStaff(name: String, role: String, phone: String, email: String, pinCode: String) {
    val id = "st_" + UUID.randomUUID().toString().take(6)
    val now = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    db.staffDao().insertStaff(StaffEntity(id, name, role, phone, email, pinCode, true, now))
  }

  suspend fun deleteStaff(staff: StaffEntity) {
    db.staffDao().deleteStaff(staff)
  }

  // Reviews
  val allReviews: Flow<List<ReviewEntity>> = db.reviewDao().getAllReviews()

  suspend fun replyToReview(reviewId: String, reply: String) {
    val formattedDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
    db.reviewDao().replyToReview(reviewId, reply, formattedDate)
  }

  // Transactions & Settlements
  val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
  val pendingSettlementAmount: Flow<Double> = db.transactionDao().getPendingSettlementAmount()
  val paidSettlementAmount: Flow<Double> = db.transactionDao().getPaidSettlementAmount()

  // Notifications
  val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
  val unreadNotificationsCount: Flow<Int> = db.notificationDao().getUnreadCount()

  suspend fun markNotificationAsRead(id: String) {
    db.notificationDao().markAsRead(id)
  }

  suspend fun markAllNotificationsAsRead() {
    db.notificationDao().markAllAsRead()
  }

  // Support Tickets
  val allTickets: Flow<List<SupportTicketEntity>> = db.supportTicketDao().getAllTickets()

  suspend fun createSupportTicket(category: String, subject: String, description: String) {
    val id = "TICK-" + Random.nextInt(1000, 9999)
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    db.supportTicketDao().insertTicket(
      SupportTicketEntity(id, category, subject, description, "OPEN", date, "Assigned to Partner Support Representative")
    )
  }

  // Timings
  val restaurantTimings: Flow<List<RestaurantTimingEntity>> = db.timingDao().getTimings()

  suspend fun updateTiming(timing: RestaurantTimingEntity) {
    db.timingDao().updateTiming(timing)
  }

  // Documents
  val documents: Flow<List<RestaurantDocumentEntity>> = db.documentDao().getDocuments()

  suspend fun updateDocument(doc: RestaurantDocumentEntity) {
    db.documentDao().updateDocument(doc)
  }

  // ================= ADMIN & 4-ROLE BACKEND INTEGRATION =================

  private val _currentUserRole = MutableStateFlow(UserRole.ADMIN)
  val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

  fun setUserRole(role: UserRole) {
    _currentUserRole.value = role
  }

  fun checkAdminAccess(): Boolean = _currentUserRole.value == UserRole.ADMIN

  // Platform Settings & Commission Rules
  val platformSettings: Flow<PlatformSettingsEntity?> = db.platformSettingsDao().getSettings()

  suspend fun updatePlatformSettings(settings: PlatformSettingsEntity) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to modify platform commission.")
    db.platformSettingsDao().insertSettings(settings)
  }

  // Admin Restaurant Management
  val allRestaurants: Flow<List<RestaurantProfileEntity>> = db.restaurantDao().getAllRestaurants()

  suspend fun updateRestaurantStatus(id: Int, status: String) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to update restaurant status.")
    db.restaurantDao().updateRestaurantStatus(id, status)
  }

  // Admin Document Verification
  val allDocuments: Flow<List<RestaurantDocumentEntity>> = db.documentDao().getDocuments()

  suspend fun updateDocumentStatus(docType: String, status: String) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to verify documents.")
    db.documentDao().updateDocumentStatus(docType, status)
  }

  // Admin Customer / User Management
  val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()

  suspend fun updateUserStatus(userId: String, status: String) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to update user status.")
    db.userDao().updateUserStatus(userId, status)
  }

  // Delivery Partners
  val allDeliveryPartners: Flow<List<DeliveryPartnerEntity>> = db.deliveryPartnerDao().getAllDeliveryPartners()
  val availableDeliveryPartners: Flow<List<DeliveryPartnerEntity>> = db.deliveryPartnerDao().getAvailablePartners()

  suspend fun updateDeliveryPartnerStatus(id: String, status: String) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to update partner status.")
    db.deliveryPartnerDao().updatePartnerStatus(id, status)
  }

  suspend fun toggleDeliveryPartnerOnline(id: String, isOnline: Boolean) {
    db.deliveryPartnerDao().updatePartnerOnlineStatus(id, isOnline)
  }

  // Order Assignment (Automatic or Manual by Admin)
  val allDeliveryAssignments: Flow<List<DeliveryAssignmentEntity>> = db.deliveryAssignmentDao().getAllAssignments()

  suspend fun assignOrderToRider(orderId: String, riderId: String, riderName: String) {
    val assignment = DeliveryAssignmentEntity(
      orderId = orderId,
      deliveryPartnerId = riderId,
      deliveryPartnerName = riderName,
      status = "ASSIGNED"
    )
    db.deliveryAssignmentDao().insertAssignment(assignment)
    val order = db.orderDao().getOrderByIdSync(orderId)
    if (order != null) {
      db.orderDao().updateOrder(order.copy(deliveryPartnerName = riderName, updatedAt = System.currentTimeMillis()))
    }
  }

  // Rider Order Lifecycle Actions
  suspend fun riderAcceptOrder(orderId: String, riderId: String, riderName: String) {
    assignOrderToRider(orderId, riderId, riderName)
    db.deliveryAssignmentDao().updateAssignmentStatus(orderId, "ACCEPTED")
  }

  suspend fun riderPickUpOrder(orderId: String) {
    markOrderPickedUp(orderId)
    db.deliveryAssignmentDao().updateAssignmentStatus(orderId, "PICKED_UP")
  }

  suspend fun riderDeliverOrder(orderId: String, riderId: String = "dl_1") {
    completeOrder(orderId)
    db.deliveryAssignmentDao().updateAssignmentStatus(orderId, "DELIVERED")
    db.deliveryPartnerDao().recordDeliveryCompletion(riderId, 35.0)
  }

  // Reviews Moderation (Admin can hide or restore reviews)
  suspend fun hideReview(reviewId: String, isHidden: Boolean) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to moderate reviews.")
    db.reviewDao().hideReview(reviewId, isHidden)
  }

  // Admin Broadcast Notification
  suspend fun broadcastNotification(title: String, message: String, targetAudience: String) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to broadcast notifications.")
    val notif = NotificationEntity(
      id = "notif_" + UUID.randomUUID().toString().take(6),
      title = title,
      message = message,
      type = "SYSTEM",
      timestamp = System.currentTimeMillis(),
      isRead = false,
      targetAudience = targetAudience
    )
    db.notificationDao().insertNotification(notif)
  }

  // Support Ticket Resolution
  suspend fun resolveSupportTicket(ticketId: String, status: String, resolution: String) {
    if (!checkAdminAccess()) throw SecurityException("Unauthorized: Admin role required to update support tickets.")
    db.supportTicketDao().resolveTicket(ticketId, status, resolution)
  }

  // ================= RESTAURANT AUTHENTICATION & SESSION MANAGEMENT =================

  suspend fun loginRestaurant(identifier: String, password: String, rememberMe: Boolean = true): AuthResult {
    val trimmed = identifier.trim()
    if (trimmed.isEmpty() || password.isEmpty()) {
      return AuthResult.Error("Please enter both email/mobile and password.")
    }

    val user = db.userDao().getUserByEmailOrPhone(trimmed)
      ?: return AuthResult.Error("No account found with this email or mobile number.")

    // Role Verification: Only RESTAURANT_OWNER, RESTAURANT_STAFF or ADMIN
    if (user.role != UserRole.RESTAURANT_OWNER.name && user.role != UserRole.RESTAURANT_STAFF.name && user.role != UserRole.ADMIN.name) {
      return AuthResult.Error("Access denied: This login is exclusively for Restaurant Partners. Normal customers and riders cannot access this portal.")
    }

    // Secure Password Verification using salted hash
    if (!PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
      return AuthResult.Error("Incorrect password. Please try again or use Forgot Password.")
    }

    // Check user account status
    if (user.status == "SUSPENDED") {
      return AuthResult.Suspended("Your owner account has been suspended by Admin. Contact partner support.")
    }

    // Load associated restaurant profile
    val profile = db.restaurantDao().getProfileByIdSync(user.restaurantId)
      ?: db.restaurantDao().getProfileByOwnerUserIdSync(user.id)
      ?: return AuthResult.Error("No restaurant profile associated with this account.")

    // Check restaurant approval status
    when (profile.status) {
      "PENDING", "UNDER_REVIEW" -> {
        _currentAuthenticatedUser.value = user
        _currentRestaurantId.value = profile.id
        return AuthResult.PendingApproval(user, profile)
      }
      "REJECTED" -> {
        return AuthResult.Rejected("Restaurant application was rejected by Admin: ${profile.rejectionReason.ifBlank { "Documentation does not meet platform requirements." }}")
      }
      "SUSPENDED" -> {
        return AuthResult.Suspended("Restaurant '${profile.name}' has been suspended by Admin. Access to restaurant operations is temporarily locked.")
      }
      else -> {
        // APPROVED / ACTIVE
        _currentAuthenticatedUser.value = user
        _currentRestaurantId.value = profile.id
        if (rememberMe) {
          val sessionToken = UUID.randomUUID().toString()
          val expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L) // 30 days session
          db.userDao().updateSession(user.id, sessionToken, expiry)
        }
        return AuthResult.Success(user, profile)
      }
    }
  }

  suspend fun registerRestaurant(
    fullName: String,
    restaurantName: String,
    mobile: String,
    email: String,
    password: String,
    confirmPassword: String,
    address: String,
    city: String,
    state: String,
    pincode: String,
    cuisine: String,
    fssai: String
  ): AuthResult {
    // 1. Required fields validation
    if (fullName.isBlank() || restaurantName.isBlank() || mobile.isBlank() || email.isBlank() ||
      password.isBlank() || confirmPassword.isBlank() || address.isBlank() || city.isBlank() ||
      state.isBlank() || pincode.isBlank() || cuisine.isBlank() || fssai.isBlank()) {
      return AuthResult.Error("All fields are required. Please fill in the entire form.")
    }

    if (!email.contains("@") || !email.contains(".")) {
      return AuthResult.Error("Please enter a valid email address.")
    }

    val cleanMobile = mobile.replace("+", "").replace(" ", "").replace("-", "")
    if (cleanMobile.length < 10) {
      return AuthResult.Error("Please enter a valid 10-digit mobile number.")
    }

    if (password.length < 6) {
      return AuthResult.Error("Password must be at least 6 characters long.")
    }

    if (password != confirmPassword) {
      return AuthResult.Error("Passwords do not match. Please re-enter your password.")
    }

    if (fssai.trim().length < 6) {
      return AuthResult.Error("Please enter a valid FSSAI license number.")
    }

    // 2. Duplicate Account Prevention
    val existingEmail = db.userDao().getUserByEmail(email.trim().lowercase())
    if (existingEmail != null) {
      return AuthResult.Error("An account with this email address already exists. Please log in.")
    }

    val existingPhone = db.userDao().getUserByPhone(mobile.trim())
    if (existingPhone != null) {
      return AuthResult.Error("An account with this mobile number already exists. Please log in.")
    }

    // 3. Cryptographic Password Hashing
    val salt = PasswordHasher.generateSalt()
    val hash = PasswordHasher.hashPassword(password, salt)

    // 4. Create new Restaurant Profile with PENDING status
    val maxId = db.restaurantDao().getMaxRestaurantId() ?: 1
    val nextRestId = maxId + 1
    val userId = "rest_owner_$nextRestId"
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    val newUser = UserEntity(
      id = userId,
      name = fullName.trim(),
      phone = mobile.trim(),
      email = email.trim().lowercase(),
      role = UserRole.RESTAURANT_OWNER.name,
      status = "ACTIVE",
      registrationDate = dateStr,
      passwordHash = hash,
      passwordSalt = salt,
      restaurantId = nextRestId
    )
    db.userDao().insertUser(newUser)

    val newProfile = RestaurantProfileEntity(
      id = nextRestId,
      name = restaurantName.trim(),
      ownerName = fullName.trim(),
      phone = mobile.trim(),
      email = email.trim().lowercase(),
      address = address.trim(),
      city = city.trim(),
      state = state.trim(),
      pincode = pincode.trim(),
      category = "Dine-in & Cloud Kitchen",
      cuisines = cuisine.trim(),
      status = "PENDING",
      fssai = fssai.trim(),
      joinedDate = dateStr,
      ownerUserId = userId,
      isOnline = false
    )
    db.restaurantDao().insertProfile(newProfile)

    // Insert FSSAI Document for Admin Verification
    val fssaiDoc = RestaurantDocumentEntity(
      docType = "FSSAI_$nextRestId",
      title = "FSSAI License - $restaurantName",
      docNumber = fssai.trim(),
      status = "PENDING",
      uploadedDate = dateStr,
      notes = "New restaurant application for $restaurantName"
    )
    db.documentDao().insertDocuments(listOf(fssaiDoc))

    _currentAuthenticatedUser.value = newUser
    _currentRestaurantId.value = nextRestId

    return AuthResult.PendingApproval(newUser, newProfile)
  }

  suspend fun resetPassword(identifier: String, newPassword: String, confirmPassword: String = newPassword): AuthResult {
    val trimmed = identifier.trim()
    if (trimmed.isBlank() || newPassword.isBlank()) {
      return AuthResult.Error("Please provide your email/mobile and new password.")
    }
    if (newPassword.length < 6) {
      return AuthResult.Error("New password must be at least 6 characters long.")
    }
    if (newPassword != confirmPassword) {
      return AuthResult.Error("Passwords do not match.")
    }

    val user = db.userDao().getUserByEmailOrPhone(trimmed)
      ?: return AuthResult.Error("No account found with this email or mobile number.")

    if (user.role != UserRole.RESTAURANT_OWNER.name && user.role != UserRole.RESTAURANT_STAFF.name && user.role != UserRole.ADMIN.name) {
      return AuthResult.Error("Password reset here is only available for restaurant partner accounts.")
    }

    val salt = PasswordHasher.generateSalt()
    val hash = PasswordHasher.hashPassword(newPassword, salt)
    db.userDao().updatePassword(user.id, hash, salt)

    val profile = db.restaurantDao().getProfileByIdSync(user.restaurantId)
      ?: db.restaurantDao().getProfileSync()
      ?: RestaurantProfileEntity(id = user.restaurantId, name = "Partner Restaurant", ownerName = user.name, phone = user.phone, email = user.email, address = "", city = "", state = "", pincode = "", category = "", cuisines = "")

    return AuthResult.Success(user, profile)
  }

  suspend fun logout() {
    val user = _currentAuthenticatedUser.value
    if (user != null) {
      db.userDao().updateSession(user.id, null, 0L)
    }
    _currentAuthenticatedUser.value = null
    _currentRestaurantId.value = 1
  }

  suspend fun checkExistingSession(): AuthResult? {
    val user = _currentAuthenticatedUser.value ?: return null
    val profile = db.restaurantDao().getProfileByIdSync(user.restaurantId) ?: return null
    return if (profile.status == "APPROVED" || profile.status == "ACTIVE") {
      AuthResult.Success(user, profile)
    } else if (profile.status == "PENDING") {
      AuthResult.PendingApproval(user, profile)
    } else if (profile.status == "SUSPENDED") {
      AuthResult.Suspended("Your restaurant is suspended by Admin.")
    } else {
      null
    }
  }
}
