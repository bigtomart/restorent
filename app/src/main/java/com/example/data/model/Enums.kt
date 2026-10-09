package com.example.data.model

enum class OrderStatus(val title: String) {
  NEW("New"),
  ACCEPTED("Accepted"),
  PREPARING("Preparing"),
  READY("Food Ready"),
  PICKED_UP("Picked Up"),
  COMPLETED("Completed"),
  CANCELLED("Cancelled"),
  REJECTED("Rejected")
}

enum class UserRole(val title: String) {
  CUSTOMER("Customer"),
  RESTAURANT_OWNER("Restaurant Owner"),
  RESTAURANT_STAFF("Restaurant Staff"),
  DELIVERY_PARTNER("Delivery Partner"),
  ADMIN("Platform Admin")
}

enum class RestaurantStatus(val title: String) {
  PENDING("Pending"),
  APPROVED("Approved"),
  REJECTED("Rejected"),
  SUSPENDED("Suspended"),
  ACTIVE("Active"),
  INACTIVE("Inactive"),
  UNDER_REVIEW("Under Review")
}

enum class StaffRole(val title: String, val description: String) {
  OWNER("Owner", "Full access to dashboard, menu, orders, earnings, staff & settings"),
  MANAGER("Manager", "Can manage orders, menu, inventory, reviews & view analytics"),
  KITCHEN_STAFF("Kitchen Staff", "Can view orders & update preparation statuses"),
  CASHIER("Cashier", "Can view orders, accept payments & print receipts")
}

enum class DiscountType {
  PERCENTAGE,
  FLAT,
  BOGO
}

enum class OfferStatus(val title: String) {
  ACTIVE("Active"),
  SCHEDULED("Scheduled"),
  EXPIRED("Expired")
}

enum class SettlementStatus(val title: String) {
  PENDING("Pending"),
  PROCESSING("Processing"),
  PAID("Paid"),
  FAILED("Failed")
}

enum class DocumentStatus(val title: String) {
  VERIFIED("Verified"),
  PENDING("Pending Review"),
  REJECTED("Rejected"),
  EXPIRED("Expired")
}

enum class TicketStatus(val title: String) {
  OPEN("Open"),
  IN_PROGRESS("In Progress"),
  RESOLVED("Resolved"),
  CLOSED("Closed")
}
