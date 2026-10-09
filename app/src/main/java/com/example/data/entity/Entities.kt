package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_profile")
data class RestaurantProfileEntity(
  @PrimaryKey val id: Int = 1,
  val name: String,
  val ownerName: String,
  val phone: String,
  val email: String,
  val address: String,
  val city: String,
  val state: String,
  val pincode: String,
  val latitude: Double = 28.6139,
  val longitude: Double = 77.2090,
  val category: String,
  val cuisines: String,
  val logoUrl: String = "",
  val coverUrl: String = "",
  val isOnline: Boolean = true,
  val offlineReason: String = "",
  val offlineUntil: Long = 0L,
  val autoAccept: Boolean = false,
  val avgPrepTimeMinutes: Int = 20,
  val minOrderAmount: Double = 149.0,
  val deliveryRadiusKm: Double = 8.5,
  val rating: Float = 4.6f,
  val totalReviews: Int = 428,
  val status: String = "APPROVED", // APPROVED, UNDER_REVIEW, PENDING, REJECTED
  val rejectionReason: String = "",
  val pan: String = "ABCDE1234F",
  val gstin: String = "07AAAAA0000A1Z5",
  val fssai: String = "10020011000123",
  val bankAccount: String = "987654321012",
  val bankIfsc: String = "HDFC0001234",
  val bankName: String = "HDFC Bank Ltd",
  val upiId: String = "bitepartner@okhdfcbank",
  val joinedDate: String = "15 Jan 2026",
  val ownerUserId: String = "rest_owner_1"
)

@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey val id: String,
  val customerName: String,
  val customerPhone: String,
  val deliveryAddress: String,
  val itemsSummary: String,
  val itemsJson: String,
  val subtotal: Double,
  val taxes: Double,
  val discount: Double,
  val deliveryFee: Double,
  val commission: Double,
  val netEarnings: Double,
  val totalAmount: Double,
  val specialInstructions: String = "",
  val orderTime: Long,
  val status: String, // NEW, ACCEPTED, PREPARING, READY, PICKED_UP, COMPLETED, CANCELLED, REJECTED
  val estimatedPrepTimeMinutes: Int = 20,
  val acceptedAt: Long = 0L,
  val preparedAt: Long = 0L,
  val completedAt: Long = 0L,
  val rejectionReason: String = "",
  val cancellationReason: String = "",
  val paymentMethod: String = "Online / Prepaid",
  val deliveryPartnerName: String = "Vikram Sharma",
  val deliveryPartnerPhone: String = "+91 98765 43210",
  val deliveryPartnerVehicle: String = "Honda Activa (DL 4S 8192)",
  val restaurantId: String = "rest_1",
  val customerId: String = "cust_1",
  val paymentStatus: String = "PAID", // PENDING, PAID, FAILED, REFUNDED
  val createdAt: Long = 0L,
  val updatedAt: Long = 0L
)

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val id: String,
  val name: String,
  val displayOrder: Int = 0,
  val itemCount: Int = 0,
  val isActive: Boolean = true
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
  @PrimaryKey val id: String,
  val categoryId: String,
  val categoryName: String,
  val name: String,
  val description: String,
  val price: Double,
  val discountedPrice: Double = 0.0,
  val isVeg: Boolean,
  val isBestseller: Boolean = false,
  val spicyLevel: Int = 1, // 0 = None, 1 = Mild, 2 = Medium, 3 = Extra Hot
  val preparationTimeMinutes: Int = 15,
  val gstPercent: Double = 5.0,
  val isAvailable: Boolean = true,
  val isSoldOut: Boolean = false,
  val stockQuantity: Int = 40,
  val imageUrl: String = "",
  val variantsJson: String = "[]",
  val addonsJson: String = "[]"
)

@Entity(tableName = "offers")
data class OfferEntity(
  @PrimaryKey val id: String,
  val code: String,
  val title: String,
  val description: String,
  val discountType: String, // PERCENTAGE, FLAT, BOGO
  val discountValue: Double,
  val minOrderValue: Double,
  val maxDiscount: Double,
  val startDate: String,
  val endDate: String,
  val usageLimit: Int = 100,
  val usageCount: Int = 0,
  val status: String = "ACTIVE" // ACTIVE, SCHEDULED, EXPIRED
)

@Entity(tableName = "staff")
data class StaffEntity(
  @PrimaryKey val id: String,
  val name: String,
  val role: String, // OWNER, MANAGER, KITCHEN_STAFF, CASHIER
  val phone: String,
  val email: String,
  val pinCode: String = "1234",
  val isActive: Boolean = true,
  val joinedDate: String = "15 Jan 2024"
)

@Entity(tableName = "reviews")
data class ReviewEntity(
  @PrimaryKey val id: String,
  val orderId: String,
  val customerName: String,
  val rating: Float,
  val foodRating: Float = 5f,
  val deliveryRating: Float = 5f,
  val comment: String,
  val itemsReviewed: String,
  val date: String,
  val replyText: String? = null,
  val repliedAt: String? = null,
  val customerId: String = "cust_1",
  val restaurantId: String = "rest_1",
  val createdAt: Long = 0L,
  val isHidden: Boolean = false,
  val reviewType: String = "RESTAURANT" // RESTAURANT, DELIVERY_PARTNER, CUSTOMER
)

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey val id: String,
  val orderId: String,
  val date: String,
  val timestamp: Long,
  val grossAmount: Double,
  val commission: Double,
  val tax: Double,
  val discountContribution: Double,
  val netEarning: Double,
  val settlementStatus: String, // PENDING, PROCESSING, PAID, FAILED
  val settlementReference: String = "",
  val customerName: String = "Rahul Verma",
  val restaurantName: String = "The Punjab Kitchen",
  val deliveryPartnerName: String = "Vikram Sharma",
  val paymentMethod: String = "Online UPI",
  val paymentStatus: String = "PAID", // PENDING, PAID, FAILED, REFUNDED
  val platformEarning: Double = 73.6,
  val deliveryPartnerEarning: Double = 35.0
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val title: String,
  val message: String,
  val type: String, // ORDER, CANCEL, REVIEW, SETTLEMENT, SYSTEM
  val timestamp: Long,
  val isRead: Boolean = false,
  val referenceId: String = "",
  val targetAudience: String = "ALL" // ALL, CUSTOMERS, RESTAURANTS, RIDERS
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
  @PrimaryKey val id: String,
  val category: String,
  val subject: String,
  val description: String,
  val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
  val createdAt: String,
  val resolution: String = "",
  val userName: String = "Rahul Verma",
  val userRole: String = "CUSTOMER" // CUSTOMER, RESTAURANT_OWNER, DELIVERY_PARTNER
)

@Entity(tableName = "restaurant_timings")
data class RestaurantTimingEntity(
  @PrimaryKey val dayOfWeek: Int, // 1 to 7 (Mon to Sun)
  val dayName: String,
  val isClosed: Boolean = false,
  val shift1Start: String = "11:00 AM",
  val shift1End: String = "04:00 PM",
  val shift2Start: String = "06:30 PM",
  val shift2End: String = "11:30 PM"
)

@Entity(tableName = "restaurant_documents")
data class RestaurantDocumentEntity(
  @PrimaryKey val docType: String,
  val title: String,
  val docNumber: String,
  val status: String, // VERIFIED, PENDING, REJECTED, EXPIRED
  val uploadedDate: String,
  val notes: String = ""
)

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val email: String,
  val role: String, // CUSTOMER, RESTAURANT_OWNER, RESTAURANT_STAFF, DELIVERY_PARTNER, ADMIN
  val status: String = "ACTIVE", // ACTIVE, SUSPENDED, PENDING
  val registrationDate: String = "01 Oct 2026",
  val totalOrders: Int = 0,
  val totalSpending: Double = 0.0,
  val passwordHash: String = "",
  val passwordSalt: String = "",
  val restaurantId: Int = 1,
  val sessionToken: String? = null,
  val sessionExpiry: Long = 0L
)

@Entity(tableName = "delivery_partners")
data class DeliveryPartnerEntity(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val vehicle: String,
  val vehicleNumber: String,
  val isOnline: Boolean = true,
  val status: String = "APPROVED", // PENDING, APPROVED, REJECTED, SUSPENDED, ACTIVE, INACTIVE
  val totalDeliveries: Int = 142,
  val rating: Float = 4.8f,
  val totalEarnings: Double = 8450.0,
  val currentOrderId: String? = null
)

@Entity(tableName = "delivery_assignments")
data class DeliveryAssignmentEntity(
  @PrimaryKey val orderId: String,
  val deliveryPartnerId: String,
  val deliveryPartnerName: String,
  val status: String = "ASSIGNED", // ASSIGNED, ACCEPTED, PICKED_UP, DELIVERED, CANCELLED
  val assignedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val restaurantCommissionPercent: Double = 16.0,
  val deliveryCommissionPercent: Double = 10.0,
  val taxPercent: Double = 5.0,
  val deliveryFeeFixed: Double = 35.0
)
