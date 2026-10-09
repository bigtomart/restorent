package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.*
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantStatus
import com.example.data.model.StaffRole
import com.example.data.model.UserRole
import com.example.data.repository.CartItemRequest
import com.example.data.repository.OrderPlacementResult
import com.example.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AuthState {
  AUTHENTICATED,
  LOGIN,
  REGISTER,
  VERIFY_OTP,
  FORGOT_PASSWORD,
  APPROVAL_PENDING,
  APPROVAL_REJECTED,
  SUSPENDED
}

data class DashboardAnalytics(
  val todaySales: Double = 0.0,
  val todayEarnings: Double = 0.0,
  val todayOrdersCount: Int = 0,
  val newOrdersCount: Int = 0,
  val activeKitchenCount: Int = 0,
  val readyOrdersCount: Int = 0,
  val completedOrdersCount: Int = 0,
  val cancelledOrdersCount: Int = 0,
  val avgRating: Float = 4.7f,
  val avgPrepTimeMinutes: Int = 20
)

class RestaurantViewModel(private val repository: RestaurantRepository) : ViewModel() {

  // Auth & Session
  private val _authState = MutableStateFlow(AuthState.LOGIN)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  val currentAuthenticatedUser: StateFlow<UserEntity?> = repository.currentAuthenticatedUser

  private val _authErrorMessage = MutableStateFlow<String?>(null)
  val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

  private val _authSuccessMessage = MutableStateFlow<String?>(null)
  val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

  private val _authIsLoading = MutableStateFlow(false)
  val authIsLoading: StateFlow<Boolean> = _authIsLoading.asStateFlow()

  init {
    viewModelScope.launch {
      val session = repository.checkExistingSession()
      if (session is com.example.data.repository.AuthResult.Success) {
        _authState.value = AuthState.AUTHENTICATED
      }
    }
  }

  val currentStaffRole: StateFlow<StaffRole> = repository.currentStaffRole

  // Restaurant Profile
  val restaurantProfile: StateFlow<RestaurantProfileEntity?> = repository.restaurantProfile
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Orders
  val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedOrderTab = MutableStateFlow(0) // 0: All/New, 1: Kitchen, 2: Ready, 3: Completed, 4: Cancelled
  val selectedOrderTab: StateFlow<Int> = _selectedOrderTab.asStateFlow()

  private val _selectedOrderForDetails = MutableStateFlow<OrderEntity?>(null)
  val selectedOrderForDetails: StateFlow<OrderEntity?> = _selectedOrderForDetails.asStateFlow()

  // Real-time Incoming Order Alert Banner
  private val _incomingAlert = MutableStateFlow<OrderEntity?>(null)
  val incomingAlert: StateFlow<OrderEntity?> = _incomingAlert.asStateFlow()
  val incomingOrderAlert: StateFlow<OrderEntity?> = _incomingAlert.asStateFlow()

  fun simulateIncomingCustomerOrder() {
    viewModelScope.launch {
      repository.simulateIncomingCustomerOrder()
    }
  }

  init {
    viewModelScope.launch {
      repository.incomingOrderAlert.collect { order ->
        _incomingAlert.value = order
      }
    }
  }

  // Dashboard Metrics & Pipeline
  val dashboardAnalytics: StateFlow<DashboardAnalytics> = combine(
    repository.allOrders,
    repository.todaySales,
    repository.todayEarnings,
    repository.restaurantProfile
  ) { orders, sales, earnings, profile ->
    val newCount = orders.count { it.status == OrderStatus.NEW.name }
    val kitchenCount = orders.count { it.status == OrderStatus.ACCEPTED.name || it.status == OrderStatus.PREPARING.name }
    val readyCount = orders.count { it.status == OrderStatus.READY.name }
    val completedCount = orders.count { it.status == OrderStatus.COMPLETED.name || it.status == OrderStatus.PICKED_UP.name }
    val cancelledCount = orders.count { it.status == OrderStatus.CANCELLED.name || it.status == OrderStatus.REJECTED.name }

    DashboardAnalytics(
      todaySales = sales,
      todayEarnings = earnings,
      todayOrdersCount = orders.size,
      newOrdersCount = newCount,
      activeKitchenCount = kitchenCount,
      readyOrdersCount = readyCount,
      completedOrdersCount = completedCount,
      cancelledOrdersCount = cancelledCount,
      avgRating = profile?.rating ?: 4.7f,
      avgPrepTimeMinutes = profile?.avgPrepTimeMinutes ?: 20
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardAnalytics())

  // Menu, Categories & Inventory
  val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val menuItems: StateFlow<List<MenuItemEntity>> = repository.allMenuItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedMenuCategoryId = MutableStateFlow<String?>(null)
  val selectedMenuCategoryId: StateFlow<String?> = _selectedMenuCategoryId.asStateFlow()

  private val _menuSearchQuery = MutableStateFlow("")
  val menuSearchQuery: StateFlow<String> = _menuSearchQuery.asStateFlow()

  // Offers
  val offers: StateFlow<List<OfferEntity>> = repository.allOffers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Staff
  val staffList: StateFlow<List<StaffEntity>> = repository.allStaff
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Reviews
  val reviews: StateFlow<List<ReviewEntity>> = repository.allReviews
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Transactions & Settlements
  val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendingSettlement: StateFlow<Double> = repository.pendingSettlementAmount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val paidSettlement: StateFlow<Double> = repository.paidSettlementAmount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  // Notifications
  val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Support
  val supportTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Timings
  val timings: StateFlow<List<RestaurantTimingEntity>> = repository.restaurantTimings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Documents
  val documents: StateFlow<List<RestaurantDocumentEntity>> = repository.documents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Analytics Date Filter: 0=Today, 1=Yesterday, 2=7 Days, 3=30 Days
  private val _analyticsPeriod = MutableStateFlow(0)
  val analyticsPeriod: StateFlow<Int> = _analyticsPeriod.asStateFlow()

  // UI Dialog States
  var orderToAccept = MutableStateFlow<OrderEntity?>(null)
  var orderToReject = MutableStateFlow<OrderEntity?>(null)
  var showOnlineConfirmationDialog = MutableStateFlow(false)
  var showPauseStoreDialog = MutableStateFlow(false)
  var showAddCategoryDialog = MutableStateFlow(false)
  var showAddMenuItemDialog = MutableStateFlow(false)
  var showCreateOfferDialog = MutableStateFlow(false)
  var showAddStaffDialog = MutableStateFlow(false)
  var showCreateTicketDialog = MutableStateFlow(false)

  // Actions
  fun setSelectedOrderTab(tabIndex: Int) {
    _selectedOrderTab.value = tabIndex
  }

  fun setSelectedOrderForDetails(order: OrderEntity?) {
    _selectedOrderForDetails.value = order
  }

  fun setMenuSearchQuery(query: String) {
    _menuSearchQuery.value = query
  }

  fun setSelectedMenuCategoryId(catId: String?) {
    _selectedMenuCategoryId.value = catId
  }

  fun setAnalyticsPeriod(period: Int) {
    _analyticsPeriod.value = period
  }

  fun switchStaffRole(role: StaffRole) {
    repository.setStaffRole(role)
  }

  fun dismissIncomingAlert() {
    _incomingAlert.value = null
  }

  fun triggerSimulatedOrder() {
    viewModelScope.launch {
      repository.simulateIncomingCustomerOrder()
    }
  }

  fun toggleOnlineStatus(isOnline: Boolean, reason: String = "", durationMin: Int = 0) {
    viewModelScope.launch {
      repository.toggleOnlineStatus(isOnline, reason, durationMin)
    }
  }

  fun acceptOrder(orderId: String, prepTimeMin: Int = 18) {
    viewModelScope.launch {
      repository.startPreparing(orderId)
      orderToAccept.value = null
      _incomingAlert.value = null
    }
  }

  fun startPreparing(orderId: String) {
    viewModelScope.launch {
      repository.startPreparing(orderId)
    }
  }

  fun markFoodReady(orderId: String) {
    viewModelScope.launch {
      repository.markFoodReady(orderId)
    }
  }

  fun markPickedUp(orderId: String) {
    viewModelScope.launch {
      repository.markPickedUp(orderId)
    }
  }

  fun markOrderPickedUp(orderId: String) {
    viewModelScope.launch {
      repository.markOrderPickedUp(orderId)
    }
  }

  // Customer Active Order State Tracking
  private val _activeCustomerOrderId = MutableStateFlow<String?>(null)
  val activeCustomerOrderId: StateFlow<String?> = _activeCustomerOrderId.asStateFlow()

  val activeCustomerOrder: StateFlow<OrderEntity?> = combine(allOrders, _activeCustomerOrderId) { orders, id ->
    if (id != null) orders.find { it.id == id } else orders.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  fun setActiveCustomerOrderId(orderId: String?) {
    _activeCustomerOrderId.value = orderId
  }

  fun placeCustomerOrder(
    customerId: String,
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    items: List<CartItemRequest>,
    specialInstructions: String = "",
    onResult: (OrderPlacementResult) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.placeCustomerOrder(
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        deliveryAddress = deliveryAddress,
        items = items,
        specialInstructions = specialInstructions
      )
      if (result is OrderPlacementResult.Success) {
        _activeCustomerOrderId.value = result.order.id
      }
      onResult(result)
    }
  }

  fun submitCustomerReview(
    customerId: String,
    customerName: String,
    orderId: String,
    rating: Float,
    comment: String,
    onComplete: () -> Unit
  ) {
    viewModelScope.launch {
      repository.submitCustomerReview(customerId, customerName, orderId, rating, comment)
      onComplete()
    }
  }

  fun completeOrder(orderId: String) {
    viewModelScope.launch {
      repository.completeOrder(orderId)
      if (_selectedOrderForDetails.value?.id == orderId) {
        _selectedOrderForDetails.value = null
      }
    }
  }

  fun rejectOrder(orderId: String, reason: String) {
    viewModelScope.launch {
      repository.rejectOrder(orderId, reason)
      orderToReject.value = null
      _incomingAlert.value = null
      if (_selectedOrderForDetails.value?.id == orderId) {
        _selectedOrderForDetails.value = null
      }
    }
  }

  fun cancelOrder(orderId: String, reason: String) {
    viewModelScope.launch {
      repository.cancelOrder(orderId, reason)
      if (_selectedOrderForDetails.value?.id == orderId) {
        _selectedOrderForDetails.value = null
      }
    }
  }

  fun toggleItemSoldOut(itemId: String, isSoldOut: Boolean) {
    viewModelScope.launch {
      repository.toggleItemSoldOut(itemId, isSoldOut)
    }
  }

  fun toggleItemAvailability(itemId: String, isAvailable: Boolean) {
    viewModelScope.launch {
      repository.toggleItemAvailability(itemId, isAvailable)
    }
  }

  fun updateItemStock(itemId: String, stock: Int) {
    viewModelScope.launch {
      repository.updateItemStock(itemId, stock)
    }
  }

  fun addCategory(name: String) {
    viewModelScope.launch {
      repository.addCategory(name)
      showAddCategoryDialog.value = false
    }
  }

  fun updateCategory(category: CategoryEntity) {
    viewModelScope.launch {
      repository.updateCategory(category)
    }
  }

  fun renameCategory(category: CategoryEntity, newName: String) {
    viewModelScope.launch {
      repository.updateCategory(category.copy(name = newName))
    }
  }

  fun deleteCategory(category: CategoryEntity) {
    viewModelScope.launch {
      repository.deleteCategory(category)
    }
  }

  fun addMenuItem(
    name: String,
    categoryId: String,
    categoryName: String,
    price: Double,
    description: String,
    isVeg: Boolean,
    prepTime: Int,
    spicyLevel: Int
  ) {
    viewModelScope.launch {
      val newItem = MenuItemEntity(
        id = "item_" + System.currentTimeMillis().toString().takeLast(6),
        categoryId = categoryId,
        categoryName = categoryName,
        name = name,
        description = description,
        price = price,
        isVeg = isVeg,
        isBestseller = false,
        spicyLevel = spicyLevel,
        preparationTimeMinutes = prepTime,
        isAvailable = true,
        isSoldOut = false,
        stockQuantity = 40
      )
      repository.insertMenuItem(newItem)
      showAddMenuItemDialog.value = false
    }
  }

  fun duplicateMenuItem(itemId: String) {
    viewModelScope.launch {
      repository.duplicateMenuItem(itemId)
    }
  }

  fun updateMenuItem(item: MenuItemEntity) {
    viewModelScope.launch {
      repository.updateMenuItem(item)
    }
  }

  fun deleteMenuItem(item: MenuItemEntity) {
    viewModelScope.launch {
      repository.deleteMenuItem(item)
    }
  }

  fun createOffer(
    code: String,
    title: String,
    description: String,
    type: String,
    value: Double,
    minOrder: Double,
    maxDiscount: Double,
    startDate: String,
    endDate: String
  ) {
    viewModelScope.launch {
      val offer = OfferEntity(
        id = "off_" + System.currentTimeMillis().toString().takeLast(6),
        code = code,
        title = title,
        description = description,
        discountType = type,
        discountValue = value,
        minOrderValue = minOrder,
        maxDiscount = maxDiscount,
        startDate = startDate,
        endDate = endDate,
        status = "ACTIVE"
      )
      repository.insertOffer(offer)
      showCreateOfferDialog.value = false
    }
  }

  fun toggleOfferStatus(offerId: String, currentStatus: String) {
    viewModelScope.launch {
      val newStatus = if (currentStatus == "ACTIVE") "EXPIRED" else "ACTIVE"
      repository.setOfferStatus(offerId, newStatus)
    }
  }

  fun deleteOffer(offer: OfferEntity) {
    viewModelScope.launch {
      repository.deleteOffer(offer)
    }
  }

  fun addStaff(name: String, role: String, phone: String, email: String, pin: String) {
    viewModelScope.launch {
      repository.addStaff(name, role, phone, email, pin)
      showAddStaffDialog.value = false
    }
  }

  fun deleteStaff(staff: StaffEntity) {
    viewModelScope.launch {
      repository.deleteStaff(staff)
    }
  }

  fun replyToReview(reviewId: String, reply: String) {
    viewModelScope.launch {
      repository.replyToReview(reviewId, reply)
    }
  }

  fun createSupportTicket(category: String, subject: String, description: String) {
    viewModelScope.launch {
      repository.createSupportTicket(category, subject, description)
      showCreateTicketDialog.value = false
    }
  }

  fun markNotificationAsRead(id: String) {
    viewModelScope.launch {
      repository.markNotificationAsRead(id)
    }
  }

  fun markAllNotificationsAsRead() {
    viewModelScope.launch {
      repository.markAllNotificationsAsRead()
    }
  }

  fun updateTiming(timing: RestaurantTimingEntity) {
    viewModelScope.launch {
      repository.updateTiming(timing)
    }
  }

  fun updateProfile(profile: RestaurantProfileEntity) {
    viewModelScope.launch {
      repository.updateProfile(profile)
    }
  }

  fun submitRegistration(
    restaurantName: String,
    ownerName: String,
    phone: String,
    email: String,
    address: String,
    cuisines: String,
    pan: String,
    gstin: String,
    fssai: String,
    bankAcc: String,
    ifsc: String,
    upi: String
  ) {
    viewModelScope.launch {
      val updatedProfile = RestaurantProfileEntity(
        id = 1,
        name = restaurantName,
        ownerName = ownerName,
        phone = phone,
        email = email,
        address = address,
        city = "New Delhi",
        state = "Delhi",
        pincode = "110001",
        category = "Restaurant & Delivery Kitchen",
        cuisines = cuisines,
        status = RestaurantStatus.UNDER_REVIEW.name,
        pan = pan,
        gstin = gstin,
        fssai = fssai,
        bankAccount = bankAcc,
        bankIfsc = ifsc,
        bankName = "Primary Commercial Bank",
        upiId = upi
      )
      repository.updateProfile(updatedProfile)
      _authState.value = AuthState.APPROVAL_PENDING
    }
  }

  // ================= ADMIN & 4-ROLE SYSTEM INTEGRATION =================

  val currentUserRole: StateFlow<UserRole> = repository.currentUserRole

  fun setUserRole(role: UserRole) {
    repository.setUserRole(role)
  }

  // Admin Data Flows
  val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allRestaurants: StateFlow<List<RestaurantProfileEntity>> = repository.allRestaurants
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allDeliveryPartners: StateFlow<List<DeliveryPartnerEntity>> = repository.allDeliveryPartners
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val availableDeliveryPartners: StateFlow<List<DeliveryPartnerEntity>> = repository.availableDeliveryPartners
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allDeliveryAssignments: StateFlow<List<DeliveryAssignmentEntity>> = repository.allDeliveryAssignments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val platformSettings: StateFlow<PlatformSettingsEntity?> = repository.platformSettings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allDocuments: StateFlow<List<RestaurantDocumentEntity>> = repository.allDocuments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSupportTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Admin Actions
  fun updateRestaurantStatus(id: Int, status: String) {
    viewModelScope.launch {
      try {
        repository.updateRestaurantStatus(id, status)
      } catch (_: Exception) {}
    }
  }

  fun updateDocumentStatus(docType: String, status: String) {
    viewModelScope.launch {
      try {
        repository.updateDocumentStatus(docType, status)
      } catch (_: Exception) {}
    }
  }

  fun updateUserStatus(userId: String, status: String) {
    viewModelScope.launch {
      try {
        repository.updateUserStatus(userId, status)
      } catch (_: Exception) {}
    }
  }

  fun updateDeliveryPartnerStatus(id: String, status: String) {
    viewModelScope.launch {
      try {
        repository.updateDeliveryPartnerStatus(id, status)
      } catch (_: Exception) {}
    }
  }

  fun toggleDeliveryPartnerOnline(id: String, isOnline: Boolean) {
    viewModelScope.launch {
      repository.toggleDeliveryPartnerOnline(id, isOnline)
    }
  }

  fun updatePlatformSettings(settings: PlatformSettingsEntity) {
    viewModelScope.launch {
      try {
        repository.updatePlatformSettings(settings)
      } catch (_: Exception) {}
    }
  }

  fun assignOrderToRider(orderId: String, riderId: String, riderName: String) {
    viewModelScope.launch {
      repository.assignOrderToRider(orderId, riderId, riderName)
    }
  }

  fun hideReview(reviewId: String, isHidden: Boolean) {
    viewModelScope.launch {
      try {
        repository.hideReview(reviewId, isHidden)
      } catch (_: Exception) {}
    }
  }

  fun broadcastNotification(title: String, message: String, targetAudience: String) {
    viewModelScope.launch {
      try {
        repository.broadcastNotification(title, message, targetAudience)
      } catch (_: Exception) {}
    }
  }

  fun resolveSupportTicket(ticketId: String, status: String, resolution: String) {
    viewModelScope.launch {
      try {
        repository.resolveSupportTicket(ticketId, status, resolution)
      } catch (_: Exception) {}
    }
  }

  // Delivery Partner Actions
  fun riderAcceptOrder(orderId: String, riderId: String, riderName: String) {
    viewModelScope.launch {
      repository.riderAcceptOrder(orderId, riderId, riderName)
    }
  }

  fun riderPickUpOrder(orderId: String) {
    viewModelScope.launch {
      repository.riderPickUpOrder(orderId)
    }
  }

  fun riderDeliverOrder(orderId: String, riderId: String = "dl_1") {
    viewModelScope.launch {
      repository.riderDeliverOrder(orderId, riderId)
    }
  }

  // ================= RESTAURANT AUTHENTICATION & SESSION =================

  fun clearAuthMessages() {
    _authErrorMessage.value = null
    _authSuccessMessage.value = null
  }

  fun setAuthState(state: AuthState) {
    _authState.value = state
    clearAuthMessages()
  }

  fun loginRestaurant(
    identifier: String,
    password: String,
    rememberMe: Boolean = true,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _authIsLoading.value = true
      clearAuthMessages()
      val result = repository.loginRestaurant(identifier, password, rememberMe)
      _authIsLoading.value = false
      when (result) {
        is com.example.data.repository.AuthResult.Success -> {
          _authState.value = AuthState.AUTHENTICATED
          onSuccess()
        }
        is com.example.data.repository.AuthResult.PendingApproval -> {
          _authState.value = AuthState.APPROVAL_PENDING
        }
        is com.example.data.repository.AuthResult.Suspended -> {
          _authState.value = AuthState.SUSPENDED
          _authErrorMessage.value = result.message
        }
        is com.example.data.repository.AuthResult.Rejected -> {
          _authState.value = AuthState.APPROVAL_REJECTED
          _authErrorMessage.value = result.message
        }
        is com.example.data.repository.AuthResult.Error -> {
          _authErrorMessage.value = result.message
        }
      }
    }
  }

  fun registerRestaurant(
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
    fssai: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _authIsLoading.value = true
      clearAuthMessages()
      val result = repository.registerRestaurant(
        fullName, restaurantName, mobile, email, password, confirmPassword,
        address, city, state, pincode, cuisine, fssai
      )
      _authIsLoading.value = false
      when (result) {
        is com.example.data.repository.AuthResult.PendingApproval -> {
          _authState.value = AuthState.APPROVAL_PENDING
          _authSuccessMessage.value = "Application submitted! Your restaurant is pending admin approval."
          onSuccess()
        }
        is com.example.data.repository.AuthResult.Error -> {
          _authErrorMessage.value = result.message
        }
        else -> {}
      }
    }
  }

  fun resetPassword(
    identifier: String,
    newPassword: String,
    confirmPassword: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _authIsLoading.value = true
      clearAuthMessages()
      val result = repository.resetPassword(identifier, newPassword, confirmPassword)
      _authIsLoading.value = false
      when (result) {
        is com.example.data.repository.AuthResult.Success -> {
          _authSuccessMessage.value = "Password updated successfully! Please log in."
          _authState.value = AuthState.LOGIN
          onSuccess()
        }
        is com.example.data.repository.AuthResult.Error -> {
          _authErrorMessage.value = result.message
        }
        else -> {}
      }
    }
  }

  fun logout() {
    viewModelScope.launch {
      repository.logout()
      _authState.value = AuthState.LOGIN
      clearAuthMessages()
    }
  }

  fun refreshApprovalStatus() {
    viewModelScope.launch {
      _authIsLoading.value = true
      val user = repository.currentAuthenticatedUser.value
      val profile = repository.restaurantProfile.firstOrNull()
      _authIsLoading.value = false
      if (profile != null) {
        if (profile.status == "APPROVED" || profile.status == "ACTIVE") {
          _authState.value = AuthState.AUTHENTICATED
        } else if (profile.status == "SUSPENDED") {
          _authState.value = AuthState.SUSPENDED
          _authErrorMessage.value = "Restaurant is currently suspended by Admin."
        } else if (profile.status == "REJECTED") {
          _authState.value = AuthState.APPROVAL_REJECTED
          _authErrorMessage.value = "Restaurant application was rejected."
        }
      }
    }
  }
}
