package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.MenuItemEntity
import com.example.data.entity.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.repository.CartItemRequest
import com.example.data.repository.OrderPlacementResult
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerAppScreen(
  viewModel: RestaurantViewModel,
  onSwitchMode: (String) -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.restaurantProfile.collectAsStateWithLifecycle()
  val categories by viewModel.categories.collectAsStateWithLifecycle()
  val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
  val activeCustomerOrder by viewModel.activeCustomerOrder.collectAsStateWithLifecycle()

  val isOnline = profile?.isOnline ?: true

  // Customer Cart: Map of ItemId -> Quantity
  val cart = remember { mutableStateMapOf<String, Int>() }
  var showCartSheet by remember { mutableStateOf(false) }
  var specialInstructions by remember { mutableStateOf("") }
  var isPlacingOrder by remember { mutableStateOf(false) }

  // Search and Category filters
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  // Customer View Mode: "MENU" or "TRACKING"
  var customerViewMode by remember { mutableStateOf("MENU") }

  // Rating review state
  var ratingScore by remember { mutableFloatStateOf(5f) }
  var reviewComment by remember { mutableStateOf("") }
  var reviewSubmitted by remember { mutableStateOf(false) }

  val totalCartItemCount = cart.values.sum()
  val cartSubtotal = cart.entries.sumOf { (itemId, qty) ->
    val item = menuItems.find { it.id == itemId }
    val price = if (item != null) {
      if (item.discountedPrice > 0 && item.discountedPrice < item.price) item.discountedPrice else item.price
    } else 0.0
    price * qty
  }

  // Filtered menu items
  val displayedItems = remember(menuItems, selectedCategory, searchQuery) {
    var list = menuItems
    if (selectedCategory != null) {
      list = list.filter { it.categoryName.equals(selectedCategory, ignoreCase = true) }
    }
    if (searchQuery.isNotBlank()) {
      list = list.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
    list
  }

  // Cart & Checkout Sheet Dialog
  if (showCartSheet) {
    Dialog(onDismissRequest = { showCartSheet = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp)
      ) {
        Column(
          modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Your Cart", color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showCartSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = ParosaTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(12.dp))

          // Items in cart
          cart.entries.forEach { (itemId, qty) ->
            val item = menuItems.find { it.id == itemId }
            if (item != null) {
              val itemPrice = if (item.discountedPrice > 0 && item.discountedPrice < item.price) item.discountedPrice else item.price
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    VegNonVegIcon(item.isVeg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(item.name, color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                  }
                  Text("₹${itemPrice.toInt()} each", color = ParosaTextMuted, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Decrement
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ParosaSurfaceElevated,
                    border = BorderStroke(1.dp, ParosaBorder),
                    modifier = Modifier.clickable {
                      if (qty > 1) {
                        cart[itemId] = qty - 1
                      } else {
                        cart.remove(itemId)
                      }
                    }
                  ) {
                    Text("-", color = ParosaOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                  }

                  Text("$qty", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))

                  // Increment
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ParosaSurfaceElevated,
                    border = BorderStroke(1.dp, ParosaBorder),
                    modifier = Modifier.clickable {
                      if (item.isAvailable && !item.isSoldOut) {
                        cart[itemId] = qty + 1
                      }
                    }
                  ) {
                    Text("+", color = ParosaGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                  }

                  Spacer(modifier = Modifier.width(12.dp))
                  Text("₹${(itemPrice * qty).toInt()}", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Special instructions input
          OutlinedTextField(
            value = specialInstructions,
            onValueChange = { specialInstructions = it },
            placeholder = { Text("Special instructions (e.g. Less spicy, extra sauce)", color = ParosaTextMuted, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(10.dp))

          // Bill Details
          val deliveryFee = 35.0
          val tax = (cartSubtotal * 0.05).toInt()
          val discount = if (cartSubtotal >= 500) 50.0 else 0.0
          val finalTotal = cartSubtotal - discount + deliveryFee + tax

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Item Subtotal", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹${cartSubtotal.toInt()}", color = ParosaTextPrimary, fontSize = 12.sp)
          }
          if (discount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Discount Applied", color = ParosaTextSecondary, fontSize = 12.sp)
              Text("-₹${discount.toInt()}", color = ParosaRed, fontSize = 12.sp)
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Delivery Fee", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹${deliveryFee.toInt()}", color = ParosaTextPrimary, fontSize = 12.sp)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Taxes & Restaurant Charges", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹$tax", color = ParosaTextPrimary, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(6.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(6.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Amount", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("₹${finalTotal.toInt()}", color = ParosaGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Delivery Address
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ParosaSurfaceElevated,
            border = BorderStroke(1.dp, ParosaBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Deliver to Home", color = ParosaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Flat 402, Tower B, Indiranagar, Bengaluru", color = ParosaTextMuted, fontSize = 10.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          if (!isOnline) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ParosaRedContainer,
              border = BorderStroke(1.dp, ParosaRed),
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
              Text(
                "Currently unavailable - The restaurant is offline and cannot accept orders right now.",
                color = ParosaRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Button(
            onClick = {
              if (!isOnline) {
                Toast.makeText(context, "Cannot place order: Restaurant is currently OFFLINE", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (cart.isEmpty()) return@Button

              isPlacingOrder = true
              val requestItems = cart.map { (id, qty) -> CartItemRequest(id, qty) }

              viewModel.placeCustomerOrder(
                customerId = "cust_rahul",
                customerName = "Rahul Verma",
                customerPhone = "+91 98450 77123",
                deliveryAddress = "Flat 402, Tower B, Indiranagar, Bengaluru",
                items = requestItems,
                specialInstructions = specialInstructions
              ) { result ->
                isPlacingOrder = false
                when (result) {
                  is OrderPlacementResult.Success -> {
                    Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
                    cart.clear()
                    showCartSheet = false
                    customerViewMode = "TRACKING"
                    reviewSubmitted = false
                  }
                  is OrderPlacementResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                  }
                }
              }
            },
            enabled = isOnline && cart.isNotEmpty() && !isPlacingOrder,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("place_order_btn")
          ) {
            Text(
              if (isPlacingOrder) "Placing Order..." else "Place Order • ₹${finalTotal.toInt()}",
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
  ) {
    // 1. Top Bar: App switcher & mode indicator
    Surface(
      color = ParosaSurface,
      border = BorderStroke(1.dp, ParosaBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(ParosaOrangeContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Customer App", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Food Ordering Mode", color = ParosaTextMuted, fontSize = 11.sp)
          }
        }

        // Mode Navigation Switcher
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = { onSwitchMode("PARTNER") },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.6f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp).testTag("switch_to_partner_btn")
          ) {
            Text("👨‍🍳 Partner", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onSwitchMode("DELIVERY") },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.6f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp).testTag("switch_to_rider_btn")
          ) {
            Text("🛵 Rider", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onSwitchMode("ADMIN") },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParosaRed.copy(alpha = 0.6f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp).testTag("switch_to_admin_btn")
          ) {
            Text("🛡️ Admin", color = ParosaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Secondary sub-tab for Customer (Browse Menu vs Track My Order)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(ParosaSurfaceElevated)
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (customerViewMode == "MENU") ParosaOrangeContainer else Color.Transparent,
        modifier = Modifier.clickable { customerViewMode = "MENU" }
      ) {
        Text(
          "🍲 Browse Restaurant & Menu",
          color = if (customerViewMode == "MENU") ParosaOrange else ParosaTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (customerViewMode == "TRACKING") ParosaOrangeContainer else Color.Transparent,
        modifier = Modifier.clickable { customerViewMode = "TRACKING" }
      ) {
        Text(
          "📍 Track My Order",
          color = if (customerViewMode == "TRACKING") ParosaOrange else ParosaTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }

    // 2. Main Content
    if (customerViewMode == "TRACKING") {
      // ORDER TRACKING SCREEN
      CustomerOrderTrackingSection(
        order = activeCustomerOrder,
        allOrders = allOrders,
        onSelectOrder = { o -> viewModel.setActiveCustomerOrderId(o.id) },
        ratingScore = ratingScore,
        onRatingChanged = { ratingScore = it },
        reviewComment = reviewComment,
        onCommentChanged = { reviewComment = it },
        reviewSubmitted = reviewSubmitted,
        onSubmitReview = {
          if (activeCustomerOrder != null) {
            viewModel.submitCustomerReview(
              customerId = "cust_rahul",
              customerName = "Rahul Verma",
              orderId = activeCustomerOrder!!.id,
              rating = ratingScore,
              comment = reviewComment.ifBlank { "Great experience, delicious authentic food!" }
            ) {
              reviewSubmitted = true
              Toast.makeText(context, "Review submitted! Shared with Restaurant.", Toast.LENGTH_SHORT).show()
            }
          }
        },
        onSimulateStatusTransition = { o ->
          // Demo delivery helper to advance order state
          when (o.status) {
            "NEW" -> viewModel.acceptOrder(o.id)
            "ACCEPTED" -> viewModel.startPreparing(o.id)
            "PREPARING" -> viewModel.markFoodReady(o.id)
            "READY" -> viewModel.markOrderPickedUp(o.id)
            "PICKED_UP" -> viewModel.completeOrder(o.id)
          }
        },
        onBackToMenu = { customerViewMode = "MENU" }
      )
    } else {
      // MENU BROWSER SCREEN
      Box(modifier = Modifier.weight(1f)) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Restaurant Profile Card
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = ParosaSurface),
              border = BorderStroke(1.dp, ParosaBorder)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = profile?.name ?: "The Punjab Kitchen",
                      color = ParosaTextPrimary,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile?.cuisines ?: "North Indian, Mughlai, Tandoor", color = ParosaTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile?.address ?: "12th Main Road, Indiranagar, Bengaluru", color = ParosaTextMuted, fontSize = 11.sp)
                  }

                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(ParosaGreenDark),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("🍲", fontSize = 24.sp)
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("4.5 ★", color = Color(0xFFFACC15), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("(128+ ratings)", color = ParosaTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("• 20-25 mins", color = ParosaTextSecondary, fontSize = 11.sp)
                  }

                  // Live Availability Status Pill
                  val isAvailable = isOnline && (profile?.status == "APPROVED")
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAvailable) ParosaGreenContainer else ParosaRedContainer,
                    border = BorderStroke(1.dp, if (isAvailable) ParosaGreenDark else ParosaRed)
                  ) {
                    val statusText = when (profile?.status) {
                      "SUSPENDED" -> "⛔ SUSPENDED BY ADMIN"
                      "PENDING" -> "⏳ PENDING APPROVAL"
                      "REJECTED" -> "❌ REJECTED"
                      else -> if (isOnline) "🟢 OPEN NOW" else "🔴 CURRENTLY UNAVAILABLE"
                    }
                    Text(
                      text = statusText,
                      color = if (isAvailable) ParosaGreen else ParosaRed,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                }
              }
            }
          }

          // Search Bar
          item {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search dishes in ${profile?.name ?: "The Punjab Kitchen"}...", color = ParosaTextMuted, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
              },
              trailingIcon = {
                if (searchQuery.isNotBlank()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(16.dp))
                  }
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ParosaTextPrimary,
                unfocusedTextColor = ParosaTextPrimary,
                focusedBorderColor = ParosaOrange,
                unfocusedBorderColor = ParosaBorder
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().height(48.dp)
            )
          }

          // Category Chips Row
          item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              item {
                val isAll = selectedCategory == null
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isAll) ParosaOrangeContainer else ParosaSurface,
                  border = BorderStroke(1.dp, if (isAll) ParosaOrange else ParosaBorder),
                  modifier = Modifier.clickable { selectedCategory = null }
                ) {
                  Text(
                    text = "All Dishes",
                    color = if (isAll) ParosaOrange else ParosaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }

              items(categories) { cat ->
                val isSelected = selectedCategory == cat.name
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) ParosaOrangeContainer else ParosaSurface,
                  border = BorderStroke(1.dp, if (isSelected) ParosaOrange else ParosaBorder),
                  modifier = Modifier.clickable { selectedCategory = cat.name }
                ) {
                  Text(
                    text = cat.name,
                    color = if (isSelected) ParosaOrange else ParosaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          // Menu Items List
          if (displayedItems.isEmpty()) {
            item {
              Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                Text("No items found matching your filter.", color = ParosaTextMuted, fontSize = 13.sp)
              }
            }
          } else {
            items(displayedItems, key = { it.id }) { item ->
              val inCartQty = cart[item.id] ?: 0
              val isItemDisabled = item.isSoldOut || !item.isAvailable

              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ParosaSurface),
                border = BorderStroke(1.dp, ParosaBorder)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      VegNonVegIcon(item.isVeg)
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(item.name, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    if (item.description.isNotBlank()) {
                      Text(item.description, color = ParosaTextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                      Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      val price = if (item.discountedPrice > 0 && item.discountedPrice < item.price) item.discountedPrice else item.price
                      Text("₹${price.toInt()}", color = ParosaGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                      if (item.discountedPrice > 0 && item.discountedPrice < item.price) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("₹${item.price.toInt()}", color = ParosaTextMuted, fontSize = 11.sp)
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("• ${item.categoryName}", color = ParosaTextMuted, fontSize = 11.sp)
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  // Add to Cart Button or Stepper
                  if (isItemDisabled) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = ParosaRedContainer,
                      border = BorderStroke(1.dp, ParosaRed)
                    ) {
                      Text(
                        "Sold Out",
                        color = ParosaRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                      )
                    }
                  } else if (inCartQty > 0) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = ParosaSurfaceElevated,
                      border = BorderStroke(1.dp, ParosaGreen)
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          "-",
                          color = ParosaGreen,
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier
                            .clickable {
                              if (inCartQty > 1) cart[item.id] = inCartQty - 1 else cart.remove(item.id)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                        Text("$inCartQty", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                          "+",
                          color = ParosaGreen,
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier
                            .clickable { cart[item.id] = inCartQty + 1 }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                      }
                    }
                  } else {
                    OutlinedButton(
                      onClick = {
                        if (!isOnline) {
                          Toast.makeText(context, "Restaurant is OFFLINE - cannot add items", Toast.LENGTH_SHORT).show()
                        } else {
                          cart[item.id] = 1
                        }
                      },
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, ParosaGreen),
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = ParosaGreen),
                      modifier = Modifier.height(34.dp).testTag("add_item_${item.id}")
                    ) {
                      Text("ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }
                }
              }
            }
          }

          item { Spacer(modifier = Modifier.height(60.dp)) }
        }

        // Floating Bottom Cart Bar
        if (totalCartItemCount > 0) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ParosaGreen,
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(16.dp)
              .clickable { showCartSheet = true }
              .testTag("view_cart_floating_bar")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("$totalCartItemCount ITEM${if (totalCartItemCount > 1) "S" else ""}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("₹${cartSubtotal.toInt()}", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View Cart", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CustomerOrderTrackingSection(
  order: OrderEntity?,
  allOrders: List<OrderEntity>,
  onSelectOrder: (OrderEntity) -> Unit,
  ratingScore: Float,
  onRatingChanged: (Float) -> Unit,
  reviewComment: String,
  onCommentChanged: (String) -> Unit,
  reviewSubmitted: Boolean,
  onSubmitReview: () -> Unit,
  onSimulateStatusTransition: (OrderEntity) -> Unit,
  onBackToMenu: () -> Unit
) {
  if (order == null) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = ParosaTextMuted, modifier = Modifier.size(48.dp))
      Spacer(modifier = Modifier.height(12.dp))
      Text("No active customer order to track", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text("Place an order from the menu or select from past orders below.", color = ParosaTextSecondary, fontSize = 12.sp)

      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = onBackToMenu, colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)) {
        Text("Browse Menu", color = Color.Black, fontWeight = FontWeight.Bold)
      }

      if (allOrders.isNotEmpty()) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent orders:", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        allOrders.take(3).forEach { prevOrder ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ParosaSurface,
            border = BorderStroke(1.dp, ParosaBorder),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectOrder(prevOrder) }
              .padding(vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("${prevOrder.id} • ${prevOrder.status}", color = ParosaTextPrimary, fontSize = 12.sp)
              Text("₹${prevOrder.totalAmount.toInt()}", color = ParosaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
    return
  }

  // Active Order Pipeline Tracker
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaSurface),
        border = BorderStroke(1.dp, ParosaBorderHighlight)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Live Order Tracking", color = ParosaOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text(order.id, color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (order.status) {
                "NEW" -> ParosaOrangeContainer
                "READY" -> ParosaGreenContainer
                "COMPLETED" -> ParosaSurfaceElevated
                "CANCELLED", "REJECTED" -> ParosaRedContainer
                else -> Color(0xFF3B2E15)
              }
            ) {
              Text(
                text = order.status,
                color = when (order.status) {
                  "NEW" -> ParosaOrange
                  "READY", "COMPLETED" -> ParosaGreen
                  "CANCELLED", "REJECTED" -> ParosaRed
                  else -> Color(0xFFFACC15)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text("The Punjab Kitchen • Indiranagar", color = ParosaTextSecondary, fontSize = 12.sp)
          Text("Estimated delivery time: 20-30 mins", color = ParosaTextMuted, fontSize = 11.sp)

          Spacer(modifier = Modifier.height(14.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(14.dp))

          // 6-step progress pipeline
          val currentStatusStep = when (order.status) {
            "NEW" -> 1
            "ACCEPTED" -> 2
            "PREPARING" -> 3
            "READY" -> 4
            "PICKED_UP" -> 5
            "COMPLETED" -> 6
            "CANCELLED", "REJECTED" -> -1
            else -> 1
          }

          TrackingStepRow(stepNum = 1, title = "Order Placed", subtitle = "Received by restaurant", isCompleted = currentStatusStep >= 1, isCurrent = currentStatusStep == 1)
          TrackingStepConnector(isCompleted = currentStatusStep > 1)

          TrackingStepRow(stepNum = 2, title = "Order Accepted", subtitle = "Restaurant confirmed order", isCompleted = currentStatusStep >= 2, isCurrent = currentStatusStep == 2)
          TrackingStepConnector(isCompleted = currentStatusStep > 2)

          TrackingStepRow(stepNum = 3, title = "Preparing in Kitchen", subtitle = "Chef is cooking your fresh meal", isCompleted = currentStatusStep >= 3, isCurrent = currentStatusStep == 3)
          TrackingStepConnector(isCompleted = currentStatusStep > 3)

          TrackingStepRow(stepNum = 4, title = "Food Ready", subtitle = "Packed and waiting for pickup", isCompleted = currentStatusStep >= 4, isCurrent = currentStatusStep == 4)
          TrackingStepConnector(isCompleted = currentStatusStep > 4)

          TrackingStepRow(stepNum = 5, title = "Picked Up", subtitle = "Valet Vikram Sharma is on the way", isCompleted = currentStatusStep >= 5, isCurrent = currentStatusStep == 5)
          TrackingStepConnector(isCompleted = currentStatusStep > 5)

          TrackingStepRow(stepNum = 6, title = "Delivered", subtitle = "Order delivered & meal enjoyed", isCompleted = currentStatusStep >= 6, isCurrent = currentStatusStep == 6)

          if (order.status != "COMPLETED" && order.status != "CANCELLED" && order.status != "REJECTED") {
            Spacer(modifier = Modifier.height(16.dp))
            // Quick advance button for seamless demo testing
            OutlinedButton(
              onClick = { onSimulateStatusTransition(order) },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
              Text("Advance Order State (Demo Mode)", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // Rate & Review Section (Active when order is COMPLETED)
    if (order.status == "COMPLETED") {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, if (reviewSubmitted) ParosaGreen else ParosaOrange)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Rate Your Experience", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reviewSubmitted) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ParosaGreenContainer,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  "✓ Your review has been submitted to the restaurant. Thank you for your feedback!",
                  color = ParosaGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(12.dp)
                )
              }
            } else {
              Text("How was your meal from ${order.customerName}?", color = ParosaTextSecondary, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(8.dp))

              // 5 Star rating selector
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                  Icon(
                    imageVector = if (star <= ratingScore) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = "$star stars",
                    tint = Color(0xFFFACC15),
                    modifier = Modifier
                      .size(32.dp)
                      .clickable { onRatingChanged(star.toFloat()) }
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = reviewComment,
                onValueChange = onCommentChanged,
                placeholder = { Text("Write your feedback (food taste, packaging, delivery)...", color = ParosaTextMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = ParosaTextPrimary,
                  unfocusedTextColor = ParosaTextPrimary,
                  focusedBorderColor = ParosaOrange,
                  unfocusedBorderColor = ParosaBorder
                ),
                modifier = Modifier.fillMaxWidth().height(80.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = onSubmitReview,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("submit_review_btn")
              ) {
                Text("Submit Review to Restaurant", color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Order Summary Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaSurface),
        border = BorderStroke(1.dp, ParosaBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Order Summary", color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(order.itemsSummary, color = ParosaTextSecondary, fontSize = 13.sp)

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(10.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", color = ParosaTextMuted, fontSize = 12.sp)
            Text("₹${order.subtotal.toInt()}", color = ParosaTextPrimary, fontSize = 12.sp)
          }
          if (order.discount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Discount", color = ParosaTextMuted, fontSize = 12.sp)
              Text("-₹${order.discount.toInt()}", color = ParosaRed, fontSize = 12.sp)
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Delivery & Taxes", color = ParosaTextMuted, fontSize = 12.sp)
            Text("₹${(order.deliveryFee + order.taxes).toInt()}", color = ParosaTextPrimary, fontSize = 12.sp)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Paid", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("₹${order.totalAmount.toInt()}", color = ParosaGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun TrackingStepRow(stepNum: Int, title: String, subtitle: String, isCompleted: Boolean, isCurrent: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(
          if (isCompleted) ParosaGreen else if (isCurrent) ParosaOrange else ParosaSurfaceElevated
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isCompleted) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
      } else {
        Text("$stepNum", color = if (isCurrent) Color.Black else ParosaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column {
      Text(
        text = title,
        color = if (isCompleted || isCurrent) ParosaTextPrimary else ParosaTextMuted,
        fontSize = 13.sp,
        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
      )
      Text(
        text = subtitle,
        color = ParosaTextMuted,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
fun TrackingStepConnector(isCompleted: Boolean) {
  Box(
    modifier = Modifier
      .padding(start = 11.dp)
      .width(2.dp)
      .height(18.dp)
      .background(if (isCompleted) ParosaGreen else ParosaBorder)
  )
}
