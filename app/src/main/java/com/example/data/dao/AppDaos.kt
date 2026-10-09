package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
  @Query("SELECT * FROM restaurant_profile WHERE id = :id LIMIT 1")
  fun getProfileById(id: Int): Flow<RestaurantProfileEntity?>

  @Query("SELECT * FROM restaurant_profile WHERE id = :id LIMIT 1")
  suspend fun getProfileByIdSync(id: Int): RestaurantProfileEntity?

  @Query("SELECT * FROM restaurant_profile WHERE id = 1 LIMIT 1")
  fun getProfile(): Flow<RestaurantProfileEntity?>

  @Query("SELECT * FROM restaurant_profile WHERE id = 1 LIMIT 1")
  suspend fun getProfileSync(): RestaurantProfileEntity?

  @Query("SELECT * FROM restaurant_profile WHERE ownerUserId = :userId LIMIT 1")
  fun getProfileByOwnerUserId(userId: String): Flow<RestaurantProfileEntity?>

  @Query("SELECT * FROM restaurant_profile WHERE ownerUserId = :userId LIMIT 1")
  suspend fun getProfileByOwnerUserIdSync(userId: String): RestaurantProfileEntity?

  @Query("SELECT * FROM restaurant_profile")
  fun getAllRestaurants(): Flow<List<RestaurantProfileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfile(profile: RestaurantProfileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRestaurants(profiles: List<RestaurantProfileEntity>)

  @Update
  suspend fun updateProfile(profile: RestaurantProfileEntity)

  @Query("UPDATE restaurant_profile SET isOnline = :isOnline, offlineReason = :reason, offlineUntil = :until WHERE id = :id")
  suspend fun updateOnlineStatusForId(id: Int, isOnline: Boolean, reason: String = "", until: Long = 0L)

  @Query("UPDATE restaurant_profile SET isOnline = :isOnline, offlineReason = :reason, offlineUntil = :until WHERE id = 1")
  suspend fun updateOnlineStatus(isOnline: Boolean, reason: String = "", until: Long = 0L)

  @Query("UPDATE restaurant_profile SET status = :status WHERE id = :id")
  suspend fun updateRestaurantStatus(id: Int, status: String)

  @Query("UPDATE restaurant_profile SET status = :status WHERE id = 1")
  suspend fun updateVerificationStatus(status: String)

  @Query("SELECT MAX(id) FROM restaurant_profile")
  suspend fun getMaxRestaurantId(): Int?
}

@Dao
interface OrderDao {
  @Query("SELECT * FROM orders ORDER BY orderTime DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderTime DESC")
  fun getOrdersForCustomer(customerId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId ORDER BY orderTime DESC")
  fun getOrdersForRestaurant(restaurantId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderTime DESC")
  fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
  fun getOrderById(orderId: String): Flow<OrderEntity?>

  @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
  suspend fun getOrderByIdSync(orderId: String): OrderEntity?

  @Query("SELECT COUNT(*) FROM orders WHERE status = 'NEW'")
  fun getNewOrdersCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM orders WHERE status = 'PREPARING' OR status = 'ACCEPTED'")
  fun getActiveKitchenCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM orders WHERE status = 'READY'")
  fun getReadyOrdersCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'")
  fun getCompletedOrdersCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM orders WHERE status = 'CANCELLED' OR status = 'REJECTED'")
  fun getCancelledOrdersCount(): Flow<Int>

  @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM orders WHERE status = 'COMPLETED' OR status = 'PICKED_UP' OR status = 'READY' OR status = 'PREPARING' OR status = 'ACCEPTED'")
  fun getTodaySales(): Flow<Double>

  @Query("SELECT COALESCE(SUM(netEarnings), 0.0) FROM orders WHERE status = 'COMPLETED' OR status = 'PICKED_UP' OR status = 'READY' OR status = 'PREPARING' OR status = 'ACCEPTED'")
  fun getTodayEarnings(): Flow<Double>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrders(orders: List<OrderEntity>)

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET status = :status, estimatedPrepTimeMinutes = :prepTimeMinutes, acceptedAt = :timestamp WHERE id = :orderId")
  suspend fun acceptOrder(orderId: String, status: String, prepTimeMinutes: Int, timestamp: Long)

  @Query("UPDATE orders SET status = :status, preparedAt = :timestamp WHERE id = :orderId")
  suspend fun updateOrderStatusTime(orderId: String, status: String, timestamp: Long)

  @Query("UPDATE orders SET status = :status, rejectionReason = :reason WHERE id = :orderId")
  suspend fun rejectOrder(orderId: String, status: String, reason: String)
}

@Dao
interface MenuDao {
  @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
  fun getAllCategories(): Flow<List<CategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryEntity>)

  @Update
  suspend fun updateCategory(category: CategoryEntity)

  @Delete
  suspend fun deleteCategory(category: CategoryEntity)

  @Query("SELECT * FROM menu_items ORDER BY name ASC")
  fun getAllMenuItems(): Flow<List<MenuItemEntity>>

  @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId ORDER BY name ASC")
  fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItemEntity>>

  @Query("SELECT * FROM menu_items WHERE id = :itemId LIMIT 1")
  suspend fun getMenuItemById(itemId: String): MenuItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenuItem(item: MenuItemEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenuItems(items: List<MenuItemEntity>)

  @Update
  suspend fun updateMenuItem(item: MenuItemEntity)

  @Delete
  suspend fun deleteMenuItem(item: MenuItemEntity)

  @Query("UPDATE menu_items SET isSoldOut = :isSoldOut WHERE id = :itemId")
  suspend fun setItemSoldOut(itemId: String, isSoldOut: Boolean)

  @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :itemId")
  suspend fun setItemAvailability(itemId: String, isAvailable: Boolean)

  @Query("UPDATE menu_items SET stockQuantity = :stock WHERE id = :itemId")
  suspend fun updateItemStock(itemId: String, stock: Int)
}

@Dao
interface OfferDao {
  @Query("SELECT * FROM offers ORDER BY startDate DESC")
  fun getAllOffers(): Flow<List<OfferEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOffer(offer: OfferEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOffers(offers: List<OfferEntity>)

  @Update
  suspend fun updateOffer(offer: OfferEntity)

  @Delete
  suspend fun deleteOffer(offer: OfferEntity)

  @Query("UPDATE offers SET status = :status WHERE id = :offerId")
  suspend fun setOfferStatus(offerId: String, status: String)
}

@Dao
interface StaffDao {
  @Query("SELECT * FROM staff ORDER BY name ASC")
  fun getAllStaff(): Flow<List<StaffEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStaff(staff: StaffEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStaffList(staffList: List<StaffEntity>)

  @Update
  suspend fun updateStaff(staff: StaffEntity)

  @Delete
  suspend fun deleteStaff(staff: StaffEntity)
}

@Dao
interface ReviewDao {
  @Query("SELECT * FROM reviews ORDER BY date DESC")
  fun getAllReviews(): Flow<List<ReviewEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReview(review: ReviewEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReviews(reviews: List<ReviewEntity>)

  @Query("UPDATE reviews SET replyText = :reply, repliedAt = :repliedAt WHERE id = :reviewId")
  suspend fun replyToReview(reviewId: String, reply: String, repliedAt: String)

  @Query("UPDATE reviews SET isHidden = :isHidden WHERE id = :reviewId")
  suspend fun hideReview(reviewId: String, isHidden: Boolean)
}

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<TransactionEntity>)

  @Query("SELECT COALESCE(SUM(netEarning), 0.0) FROM transactions WHERE settlementStatus = 'PENDING'")
  fun getPendingSettlementAmount(): Flow<Double>

  @Query("SELECT COALESCE(SUM(netEarning), 0.0) FROM transactions WHERE settlementStatus = 'PAID'")
  fun getPaidSettlementAmount(): Flow<Double>
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
  fun getUnreadCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotifications(notifications: List<NotificationEntity>)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: String)

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllAsRead()
}

@Dao
interface SupportTicketDao {
  @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
  fun getAllTickets(): Flow<List<SupportTicketEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTicket(ticket: SupportTicketEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTickets(tickets: List<SupportTicketEntity>)

  @Update
  suspend fun updateTicket(ticket: SupportTicketEntity)

  @Query("UPDATE support_tickets SET status = :status, resolution = :resolution WHERE id = :ticketId")
  suspend fun resolveTicket(ticketId: String, status: String, resolution: String)
}

@Dao
interface TimingDao {
  @Query("SELECT * FROM restaurant_timings ORDER BY dayOfWeek ASC")
  fun getTimings(): Flow<List<RestaurantTimingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTimings(timings: List<RestaurantTimingEntity>)

  @Update
  suspend fun updateTiming(timing: RestaurantTimingEntity)
}

@Dao
interface DocumentDao {
  @Query("SELECT * FROM restaurant_documents")
  fun getDocuments(): Flow<List<RestaurantDocumentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocuments(documents: List<RestaurantDocumentEntity>)

  @Update
  suspend fun updateDocument(document: RestaurantDocumentEntity)

  @Query("UPDATE restaurant_documents SET status = :status WHERE docType = :docType")
  suspend fun updateDocumentStatus(docType: String, status: String)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY registrationDate DESC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE role = :role")
  fun getUsersByRole(role: String): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserById(id: String): UserEntity?

  @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
  suspend fun getUserByPhone(phone: String): UserEntity?

  @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR phone = :identifier LIMIT 1")
  suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET status = :status WHERE id = :userId")
  suspend fun updateUserStatus(userId: String, status: String)

  @Query("UPDATE users SET passwordHash = :hash, passwordSalt = :salt WHERE id = :userId")
  suspend fun updatePassword(userId: String, hash: String, salt: String)

  @Query("UPDATE users SET sessionToken = :token, sessionExpiry = :expiry WHERE id = :userId")
  suspend fun updateSession(userId: String, token: String?, expiry: Long)

  @Query("SELECT * FROM users WHERE sessionToken = :token AND sessionExpiry > :now LIMIT 1")
  suspend fun getUserBySessionToken(token: String, now: Long): UserEntity?
}

@Dao
interface DeliveryPartnerDao {
  @Query("SELECT * FROM delivery_partners")
  fun getAllDeliveryPartners(): Flow<List<DeliveryPartnerEntity>>

  @Query("SELECT * FROM delivery_partners WHERE isOnline = 1 AND status = 'APPROVED'")
  fun getAvailablePartners(): Flow<List<DeliveryPartnerEntity>>

  @Query("SELECT * FROM delivery_partners WHERE id = :id LIMIT 1")
  fun getDeliveryPartnerById(id: String): Flow<DeliveryPartnerEntity?>

  @Query("SELECT * FROM delivery_partners WHERE id = :id LIMIT 1")
  suspend fun getDeliveryPartnerByIdSync(id: String): DeliveryPartnerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDeliveryPartner(partner: DeliveryPartnerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDeliveryPartners(partners: List<DeliveryPartnerEntity>)

  @Update
  suspend fun updateDeliveryPartner(partner: DeliveryPartnerEntity)

  @Query("UPDATE delivery_partners SET status = :status WHERE id = :id")
  suspend fun updatePartnerStatus(id: String, status: String)

  @Query("UPDATE delivery_partners SET isOnline = :isOnline WHERE id = :id")
  suspend fun updatePartnerOnlineStatus(id: String, isOnline: Boolean)

  @Query("UPDATE delivery_partners SET totalDeliveries = totalDeliveries + 1, totalEarnings = totalEarnings + :earning WHERE id = :id")
  suspend fun recordDeliveryCompletion(id: String, earning: Double)
}

@Dao
interface DeliveryAssignmentDao {
  @Query("SELECT * FROM delivery_assignments WHERE orderId = :orderId LIMIT 1")
  fun getAssignmentForOrder(orderId: String): Flow<DeliveryAssignmentEntity?>

  @Query("SELECT * FROM delivery_assignments WHERE orderId = :orderId LIMIT 1")
  suspend fun getAssignmentForOrderSync(orderId: String): DeliveryAssignmentEntity?

  @Query("SELECT * FROM delivery_assignments")
  fun getAllAssignments(): Flow<List<DeliveryAssignmentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAssignment(assignment: DeliveryAssignmentEntity)

  @Query("UPDATE delivery_assignments SET status = :status WHERE orderId = :orderId")
  suspend fun updateAssignmentStatus(orderId: String, status: String)
}

@Dao
interface PlatformSettingsDao {
  @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<PlatformSettingsEntity?>

  @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
  suspend fun getSettingsSync(): PlatformSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSettings(settings: PlatformSettingsEntity)
}
