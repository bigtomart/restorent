package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    RestaurantProfileEntity::class,
    OrderEntity::class,
    CategoryEntity::class,
    MenuItemEntity::class,
    OfferEntity::class,
    StaffEntity::class,
    ReviewEntity::class,
    TransactionEntity::class,
    NotificationEntity::class,
    SupportTicketEntity::class,
    RestaurantTimingEntity::class,
    RestaurantDocumentEntity::class,
    UserEntity::class,
    DeliveryPartnerEntity::class,
    DeliveryAssignmentEntity::class,
    PlatformSettingsEntity::class
  ],
  version = 5,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun restaurantDao(): RestaurantDao
  abstract fun orderDao(): OrderDao
  abstract fun menuDao(): MenuDao
  abstract fun offerDao(): OfferDao
  abstract fun staffDao(): StaffDao
  abstract fun reviewDao(): ReviewDao
  abstract fun transactionDao(): TransactionDao
  abstract fun notificationDao(): NotificationDao
  abstract fun supportTicketDao(): SupportTicketDao
  abstract fun timingDao(): TimingDao
  abstract fun documentDao(): DocumentDao
  abstract fun userDao(): UserDao
  abstract fun deliveryPartnerDao(): DeliveryPartnerDao
  abstract fun deliveryAssignmentDao(): DeliveryAssignmentDao
  abstract fun platformSettingsDao(): PlatformSettingsDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "restaurant_partner_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

suspend fun populateInitialData(db: AppDatabase) {
  val currentTime = System.currentTimeMillis()

  // 1. Restaurant Profiles
  val defaultSalt = "a1b2c3d4e5f60718"
  val defaultOwnerHash = com.example.data.util.PasswordHasher.hashPassword("Partner@123", defaultSalt)
  val defaultAdminHash = com.example.data.util.PasswordHasher.hashPassword("Admin@123", defaultSalt)

  val initialRestaurants = listOf(
    RestaurantProfileEntity(
      id = 1,
      name = "The Punjab Kitchen",
      ownerName = "Arjun Kumar",
      phone = "+91 98450 12891",
      email = "punjabkitchen.blr@gmail.com",
      address = "12th Main Road, Indiranagar, Bengaluru",
      city = "Bengaluru",
      state = "Karnataka",
      pincode = "560038",
      category = "Dine-in & Cloud Kitchen",
      cuisines = "North Indian, Mughlai, Tandoor",
      isOnline = true,
      avgPrepTimeMinutes = 18,
      minOrderAmount = 199.0,
      deliveryRadiusKm = 7.0,
      rating = 4.5f,
      totalReviews = 840,
      status = "APPROVED",
      ownerUserId = "rest_owner_1"
    ),
    RestaurantProfileEntity(
      id = 2,
      name = "Spice Villa",
      ownerName = "Kavita Rao",
      phone = "+91 98450 99999",
      email = "spicevilla.pending@gmail.com",
      address = "45 Commercial Street, Bengaluru",
      city = "Bengaluru",
      state = "Karnataka",
      pincode = "560001",
      category = "Fine Dine",
      cuisines = "South Indian, Coastal",
      isOnline = false,
      rating = 4.2f,
      totalReviews = 0,
      status = "PENDING",
      ownerUserId = "rest_owner_pending"
    ),
    RestaurantProfileEntity(
      id = 3,
      name = "Curry House",
      ownerName = "Manish Sethi",
      phone = "+91 98450 88888",
      email = "curryhouse.suspended@gmail.com",
      address = "88 MG Road, Bengaluru",
      city = "Bengaluru",
      state = "Karnataka",
      pincode = "560025",
      category = "Cloud Kitchen",
      cuisines = "North Indian, Fast Food",
      isOnline = false,
      rating = 3.6f,
      totalReviews = 50,
      status = "SUSPENDED",
      rejectionReason = "Safety audit compliance issue flagged by food inspector.",
      ownerUserId = "rest_owner_suspended"
    )
  )
  db.restaurantDao().insertRestaurants(initialRestaurants)

  // 2. Categories
  val categories = listOf(
    CategoryEntity("cat_1", "Starters", 1, 3, true),
    CategoryEntity("cat_2", "Main Course", 2, 4, true),
    CategoryEntity("cat_3", "Biryani", 3, 3, true),
    CategoryEntity("cat_4", "Breads", 4, 3, true),
    CategoryEntity("cat_5", "Beverages", 5, 3, true),
    CategoryEntity("cat_6", "Desserts", 6, 2, true)
  )
  db.menuDao().insertCategories(categories)

  // 3. Menu Items
  val menuItems = listOf(
    MenuItemEntity(
      id = "item_1",
      categoryId = "cat_1",
      categoryName = "Starters",
      name = "Paneer Tikka",
      description = "Fresh cottage cheese marinated in spiced yogurt and grilled in clay oven.",
      price = 280.0,
      isVeg = true,
      isBestseller = true,
      preparationTimeMinutes = 15,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_2",
      categoryId = "cat_2",
      categoryName = "Main Course",
      name = "Paneer Butter Masala",
      description = "Paneer cubes in a rich tomato, butter, and cashew gravy.",
      price = 240.0,
      isVeg = true,
      isBestseller = true,
      preparationTimeMinutes = 15,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_3",
      categoryId = "cat_2",
      categoryName = "Main Course",
      name = "Dal Makhani",
      description = "Slow cooked black lentils with fresh cream and butter.",
      price = 220.0,
      isVeg = true,
      isBestseller = true,
      preparationTimeMinutes = 12,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_4",
      categoryId = "cat_4",
      categoryName = "Breads",
      name = "Butter Naan",
      description = "Traditional refined flour bread baked in clay tandoor with fresh butter.",
      price = 50.0,
      isVeg = true,
      isBestseller = true,
      preparationTimeMinutes = 6,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_5",
      categoryId = "cat_4",
      categoryName = "Breads",
      name = "Garlic Naan",
      description = "Tandoor baked refined flour bread with minced garlic and melted butter.",
      price = 65.0,
      isVeg = true,
      isBestseller = true,
      preparationTimeMinutes = 8,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_6",
      categoryId = "cat_3",
      categoryName = "Biryani",
      name = "Chicken Biryani",
      description = "Tender spiced chicken cooked in fragrant long-grain basmati rice and saffron.",
      price = 340.0,
      isVeg = false,
      isBestseller = true,
      preparationTimeMinutes = 20,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_7",
      categoryId = "cat_3",
      categoryName = "Biryani",
      name = "Veg Biryani",
      description = "Fresh seasonal vegetables and paneer cooked with aromatic basmati rice.",
      price = 260.0,
      isVeg = true,
      isBestseller = false,
      preparationTimeMinutes = 18,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_8",
      categoryId = "cat_5",
      categoryName = "Beverages",
      name = "Masala Tea",
      description = "Traditional Indian spiced milk tea brewed with cardamom and ginger.",
      price = 40.0,
      isVeg = true,
      isBestseller = false,
      preparationTimeMinutes = 5,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_9",
      categoryId = "cat_5",
      categoryName = "Beverages",
      name = "Cold Drink",
      description = "Chilled soft drink (300ml bottle).",
      price = 45.0,
      isVeg = true,
      isBestseller = false,
      preparationTimeMinutes = 2,
      isAvailable = true,
      isSoldOut = false
    ),
    MenuItemEntity(
      id = "item_10",
      categoryId = "cat_6",
      categoryName = "Desserts",
      name = "Gulab Jamun",
      description = "Golden fried milk dumplings in cardamom sugar syrup (2 pieces).",
      price = 90.0,
      isVeg = true,
      isBestseller = true,
      preparationTimeMinutes = 5,
      isAvailable = true,
      isSoldOut = false
    )
  )
  db.menuDao().insertMenuItems(menuItems)

  // 4. Initial Orders
  val orders = listOf(
    // New Orders (3)
    OrderEntity(
      id = "Order #1048",
      customerName = "Ananya S.",
      customerPhone = "+91 98451 22910",
      deliveryAddress = "Flat 302, Palm Heights, Indiranagar",
      itemsSummary = "1 × Paneer Butter Masala, 2 × Garlic Naan",
      itemsJson = "[]",
      subtotal = 440.0,
      taxes = 22.0,
      discount = 0.0,
      deliveryFee = 30.0,
      commission = 70.4,
      netEarnings = 391.6,
      totalAmount = 640.0,
      specialInstructions = "Medium spice for paneer",
      orderTime = currentTime - 3 * 60 * 1000L,
      status = "NEW"
    ),
    OrderEntity(
      id = "Order #1049",
      customerName = "Rahul M.",
      customerPhone = "+91 99120 44812",
      deliveryAddress = "Villa 14, Defence Colony",
      itemsSummary = "2 × Paneer Tikka, 1 × Jeera Rice",
      itemsJson = "[]",
      subtotal = 640.0,
      taxes = 32.0,
      discount = 0.0,
      deliveryFee = 35.0,
      commission = 102.4,
      netEarnings = 569.6,
      totalAmount = 680.0,
      specialInstructions = "No cutlery, please",
      orderTime = currentTime - 6 * 60 * 1000L,
      status = "NEW"
    ),
    OrderEntity(
      id = "Order #1050",
      customerName = "Priya K.",
      customerPhone = "+91 97112 88491",
      deliveryAddress = "504, Windsor Court",
      itemsSummary = "1 × Dal Makhani, 3 × Butter Naan",
      itemsJson = "[]",
      subtotal = 380.0,
      taxes = 19.0,
      discount = 0.0,
      deliveryFee = 25.0,
      commission = 60.8,
      netEarnings = 338.2,
      totalAmount = 420.0,
      specialInstructions = "Less oil please",
      orderTime = currentTime - 9 * 60 * 1000L,
      status = "NEW"
    ),

    // Preparing (4)
    OrderEntity(
      id = "Order #1044",
      customerName = "Vikram B.",
      customerPhone = "+91 98111 22334",
      deliveryAddress = "Indiranagar 6th Main",
      itemsSummary = "1 × Butter Chicken, 2 × Garlic Naan",
      itemsJson = "[]",
      subtotal = 540.0,
      taxes = 27.0,
      discount = 0.0,
      deliveryFee = 30.0,
      commission = 86.4,
      netEarnings = 480.6,
      totalAmount = 570.0,
      specialInstructions = "Due in 8 min",
      orderTime = currentTime - 15 * 60 * 1000L,
      status = "PREPARING",
      estimatedPrepTimeMinutes = 8
    ),
    OrderEntity(
      id = "Order #1045",
      customerName = "Neha G.",
      customerPhone = "+91 98222 33445",
      deliveryAddress = "Domlur 2nd Stage",
      itemsSummary = "1 × Dal Makhani, 1 × Jeera Rice",
      itemsJson = "[]",
      subtotal = 400.0,
      taxes = 20.0,
      discount = 0.0,
      deliveryFee = 25.0,
      commission = 64.0,
      netEarnings = 356.0,
      totalAmount = 425.0,
      specialInstructions = "Due in 12 min",
      orderTime = currentTime - 18 * 60 * 1000L,
      status = "PREPARING",
      estimatedPrepTimeMinutes = 12
    ),
    OrderEntity(
      id = "Order #1046",
      customerName = "Siddharth R.",
      customerPhone = "+91 98333 44556",
      deliveryAddress = "Koramangala 4th Block",
      itemsSummary = "2 × Paneer Tikka",
      itemsJson = "[]",
      subtotal = 560.0,
      taxes = 28.0,
      discount = 0.0,
      deliveryFee = 35.0,
      commission = 89.6,
      netEarnings = 498.4,
      totalAmount = 595.0,
      specialInstructions = "Due in 15 min",
      orderTime = currentTime - 22 * 60 * 1000L,
      status = "PREPARING",
      estimatedPrepTimeMinutes = 15
    ),
    OrderEntity(
      id = "Order #1047",
      customerName = "Sneha K.",
      customerPhone = "+91 98444 55667",
      deliveryAddress = "Ulsoor Lake Road",
      itemsSummary = "1 × Paneer Butter Masala",
      itemsJson = "[]",
      subtotal = 320.0,
      taxes = 16.0,
      discount = 0.0,
      deliveryFee = 25.0,
      commission = 51.2,
      netEarnings = 284.8,
      totalAmount = 345.0,
      specialInstructions = "Due in 18 min",
      orderTime = currentTime - 25 * 60 * 1000L,
      status = "PREPARING",
      estimatedPrepTimeMinutes = 18
    ),

    // Ready (2)
    OrderEntity(
      id = "Order #1042",
      customerName = "Ravi M.",
      customerPhone = "+91 98555 66778",
      deliveryAddress = "HAL 3rd Stage",
      itemsSummary = "2 × Paneer Butter Masala, 4 × Naan",
      itemsJson = "[]",
      subtotal = 880.0,
      taxes = 44.0,
      discount = 0.0,
      deliveryFee = 35.0,
      commission = 140.8,
      netEarnings = 783.2,
      totalAmount = 920.0,
      orderTime = currentTime - 32 * 60 * 1000L,
      status = "READY"
    ),
    OrderEntity(
      id = "Order #1043",
      customerName = "Sameer T.",
      customerPhone = "+91 98666 77889",
      deliveryAddress = "Indiranagar Metro",
      itemsSummary = "1 × Paneer Tikka, 2 × Naan",
      itemsJson = "[]",
      subtotal = 400.0,
      taxes = 20.0,
      discount = 0.0,
      deliveryFee = 30.0,
      commission = 64.0,
      netEarnings = 356.0,
      totalAmount = 400.0,
      orderTime = currentTime - 36 * 60 * 1000L,
      status = "READY"
    )
  )
  db.orderDao().insertOrders(orders)

  // 5. Reviews (4.5 Rating)
  val reviews = listOf(
    ReviewEntity("rev_1", "#1035", "Aditi Rao", 5.0f, 5.0f, 5.0f, "The Paneer Butter Masala was creamy, velvety and piping hot! Best in Indiranagar.", "Paneer Butter Masala, Naan", "Today, 7:15 PM"),
    ReviewEntity("rev_2", "#1029", "Tanmay Bhat", 4.0f, 4.0f, 4.0f, "Great tandoori flavor in Paneer Tikka. Arrived on time.", "Paneer Tikka", "Today, 6:40 PM"),
    ReviewEntity("rev_3", "#1021", "Sneha Rao", 5.0f, 5.0f, 5.0f, "Dal Makhani slow cooked to perfection! Highly recommended.", "Dal Makhani", "Yesterday, 8:20 PM"),
    ReviewEntity("rev_4", "#1015", "Rohan Mehta", 4.0f, 4.0f, 4.0f, "Good food quality and clean packaging.", "Dum Biryani", "Yesterday, 7:10 PM")
  )
  db.reviewDao().insertReviews(reviews)

  // 6. Transactions
  val transactions = listOf(
    TransactionEntity("tx_1", "Order #1041", "Today, 02:40 PM", currentTime - 40 * 60 * 1000L, 560.0, 89.6, 28.0, 0.0, 470.4, "PAID"),
    TransactionEntity("tx_2", "Order #1038", "Today, 01:15 PM", currentTime - 70 * 60 * 1000L, 920.0, 147.2, 46.0, 0.0, 772.8, "PAID"),
    TransactionEntity("tx_3", "Order #1034", "Today, 12:45 PM", currentTime - 110 * 60 * 1000L, 680.0, 108.8, 34.0, 0.0, 571.2, "PAID"),
    TransactionEntity("tx_4", "Order #1030", "Today, 11:30 AM", currentTime - 150 * 60 * 1000L, 440.0, 70.4, 22.0, 0.0, 369.6, "PAID")
  )
  db.transactionDao().insertTransactions(transactions)

  // 7. Platform Users
  val users = listOf(
    UserEntity("usr_admin", "Admin System", "+91 99000 00001", "admin@foodplatform.com", "ADMIN", "ACTIVE", "01 Jan 2026", 0, 0.0, passwordHash = defaultAdminHash, passwordSalt = defaultSalt, restaurantId = 1),
    UserEntity("cust_1", "Rahul Verma", "+91 98450 77123", "rahul.verma@gmail.com", "CUSTOMER", "ACTIVE", "10 Feb 2026", 14, 6420.0),
    UserEntity("cust_2", "Sneha Kapoor", "+91 98444 55667", "sneha.k@outlook.com", "CUSTOMER", "ACTIVE", "14 Feb 2026", 8, 3240.0),
    UserEntity("cust_3", "Tanmay Bhat", "+91 98777 66554", "tanmay.bhat@gmail.com", "CUSTOMER", "ACTIVE", "01 Mar 2026", 22, 11200.0),
    UserEntity("rest_owner_1", "Arjun Kumar", "+91 98450 12891", "punjabkitchen.blr@gmail.com", "RESTAURANT_OWNER", "ACTIVE", "15 Jan 2026", 0, 0.0, passwordHash = defaultOwnerHash, passwordSalt = defaultSalt, restaurantId = 1),
    UserEntity("rest_owner_pending", "Kavita Rao", "+91 98450 99999", "spicevilla.pending@gmail.com", "RESTAURANT_OWNER", "ACTIVE", "01 Oct 2026", 0, 0.0, passwordHash = defaultOwnerHash, passwordSalt = defaultSalt, restaurantId = 2),
    UserEntity("rest_owner_suspended", "Manish Sethi", "+91 98450 88888", "curryhouse.suspended@gmail.com", "RESTAURANT_OWNER", "ACTIVE", "05 Sep 2026", 0, 0.0, passwordHash = defaultOwnerHash, passwordSalt = defaultSalt, restaurantId = 3),
    UserEntity("rider_1", "Vikram Sharma", "+91 98765 43210", "vikram.delivery@foodplatform.com", "DELIVERY_PARTNER", "ACTIVE", "20 Jan 2026", 142, 0.0)
  )
  db.userDao().insertUsers(users)

  // 8. Delivery Partners
  val deliveryPartners = listOf(
    DeliveryPartnerEntity(
      id = "dl_1",
      name = "Vikram Sharma",
      phone = "+91 98765 43210",
      vehicle = "Honda Activa 6G",
      vehicleNumber = "KA 03 EQ 4521",
      isOnline = true,
      status = "APPROVED",
      totalDeliveries = 142,
      rating = 4.8f,
      totalEarnings = 8450.0
    ),
    DeliveryPartnerEntity(
      id = "dl_2",
      name = "Amit Deshmukh",
      phone = "+91 98222 33445",
      vehicle = "Hero Splendor Plus",
      vehicleNumber = "KA 04 HJ 7812",
      isOnline = true,
      status = "APPROVED",
      totalDeliveries = 98,
      rating = 4.7f,
      totalEarnings = 5900.0
    ),
    DeliveryPartnerEntity(
      id = "dl_3",
      name = "Karthik Raja",
      phone = "+91 98333 44556",
      vehicle = "TVS Jupiter",
      vehicleNumber = "KA 05 MN 9034",
      isOnline = false,
      status = "PENDING",
      totalDeliveries = 0,
      rating = 5.0f,
      totalEarnings = 0.0
    )
  )
  db.deliveryPartnerDao().insertDeliveryPartners(deliveryPartners)

  // 9. Platform Settings
  val settings = PlatformSettingsEntity(
    id = 1,
    restaurantCommissionPercent = 16.0,
    deliveryCommissionPercent = 10.0,
    taxPercent = 5.0,
    deliveryFeeFixed = 35.0
  )
  db.platformSettingsDao().insertSettings(settings)

  // 10. Documents
  val documents = listOf(
    RestaurantDocumentEntity("PAN", "Permanent Account Number (PAN)", "ABCDE1234F", "VERIFIED", "15 Jan 2026", "Verified via NSDL"),
    RestaurantDocumentEntity("GSTIN", "Goods and Services Tax (GSTIN)", "07AAAAA0000A1Z5", "VERIFIED", "15 Jan 2026", "Active 5% GST on Restaurant Services"),
    RestaurantDocumentEntity("FSSAI", "FSSAI Food Safety License", "10020011000123", "VERIFIED", "16 Jan 2026", "Valid through 2028"),
    RestaurantDocumentEntity("BANK", "Bank Account Passbook/Cancelled Cheque", "HDFC - 987654321012", "VERIFIED", "16 Jan 2026", "Verified via penny drop")
  )
  db.documentDao().insertDocuments(documents)

  // 11. Support Tickets
  val tickets = listOf(
    SupportTicketEntity("TICK-1021", "Payment & Settlement", "Settlement payout inquiry", "Weekly settlement processed to HDFC account.", "RESOLVED", "03 Oct 2026", "Processed on 04 Oct", "Arjun Kumar", "RESTAURANT_OWNER"),
    SupportTicketEntity("TICK-1044", "Order Support", "Missing cutlery reported", "Customer mentioned missing spoons in biryani order.", "OPEN", "Today, 01:20 PM", "", "Rahul Verma", "CUSTOMER"),
    SupportTicketEntity("TICK-1052", "Delivery Issue", "GPS navigation route discrepancy", "Rider flagged closed road near Indiranagar flyover.", "IN_PROGRESS", "Today, 02:45 PM", "Assigned to maps support", "Vikram Sharma", "DELIVERY_PARTNER")
  )
  db.supportTicketDao().insertTickets(tickets)
}
