package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.OrderEntity
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun DeliveryPartnerAppScreen(
  viewModel: RestaurantViewModel,
  onSwitchMode: (String) -> Unit
) {
  val context = LocalContext.current
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
  val allDeliveryPartners by viewModel.allDeliveryPartners.collectAsStateWithLifecycle()

  val currentRider = remember(allDeliveryPartners) {
    allDeliveryPartners.firstOrNull { it.id == "dl_1" } ?: allDeliveryPartners.firstOrNull()
  }

  var isOnline by remember(currentRider) { mutableStateOf(currentRider?.isOnline ?: true) }

  // Orders available for delivery or actively being delivered
  val activeDeliveryOrders = remember(allOrders) {
    allOrders.filter { it.status == "READY" || it.status == "PICKED_UP" }
  }

  val completedDeliveries = remember(allOrders) {
    allOrders.filter { it.status == "COMPLETED" }
  }

  val riderEarningsToday = 1420 + (completedDeliveries.size * 35)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
  ) {
    // 1. Top Bar with Profile and Mode Navigation
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
              .background(Color(0xFF1E3A8A)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Delivery Partner App", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${currentRider?.name ?: "Vikram Sharma"} • ${currentRider?.vehicle ?: "Honda Activa"}", color = ParosaTextMuted, fontSize = 11.sp)
          }
        }

        // Mode Navigation Switcher
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = { onSwitchMode("PARTNER") },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("👨‍🍳 Partner", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onSwitchMode("CUSTOMER") },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("🛍️ Customer", color = ParosaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onSwitchMode("ADMIN") },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParosaRed.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("🛡️ Admin", color = ParosaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 2. Rider Dashboard
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Rider Status & Online Toggle Card
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
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(currentRider?.name ?: "Vikram Sharma", color = ParosaTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Rating: 4.8 ★", color = Color(0xFFFACC15), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("• ${currentRider?.vehicleNumber ?: "KA 03 EQ 4521"}", color = ParosaTextMuted, fontSize = 11.sp)
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isOnline) "ONLINE" else "OFFLINE", color = if (isOnline) ParosaGreen else ParosaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                  checked = isOnline,
                  onCheckedChange = {
                    isOnline = it
                    viewModel.toggleDeliveryPartnerOnline("dl_1", it)
                    Toast.makeText(context, if (it) "You are now ONLINE for deliveries" else "You are now OFFLINE", Toast.LENGTH_SHORT).show()
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = ParosaGreen, checkedTrackColor = ParosaGreenContainer)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Column {
                Text("Today's Earnings", color = ParosaTextSecondary, fontSize = 11.sp)
                Text("₹$riderEarningsToday", color = ParosaGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Completed Trips", color = ParosaTextSecondary, fontSize = 11.sp)
                Text("${completedDeliveries.size}", color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Rate per Drop", color = ParosaTextSecondary, fontSize = 11.sp)
                Text("₹35", color = Color(0xFF60A5FA), fontSize = 18.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Active / Ready Orders for Pickup
      item {
        Text("Active Delivery Tasks (${activeDeliveryOrders.size})", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }

      if (activeDeliveryOrders.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ParosaSurface),
            border = BorderStroke(1.dp, ParosaBorder)
          ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
              Text("No orders ready for pickup right now.", color = ParosaTextMuted, fontSize = 13.sp)
            }
          }
        }
      } else {
        items(activeDeliveryOrders, key = { it.id }) { order ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ParosaSurface),
            border = BorderStroke(1.dp, if (order.status == "PICKED_UP") Color(0xFF60A5FA) else ParosaOrange)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(order.id, color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (order.status == "PICKED_UP") Color(0xFF1E3A8A) else ParosaOrangeContainer
                ) {
                  Text(
                    if (order.status == "PICKED_UP") "OUT FOR DELIVERY" else "READY FOR PICKUP",
                    color = if (order.status == "PICKED_UP") Color(0xFF60A5FA) else ParosaOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text("Pickup: The Punjab Kitchen • Indiranagar", color = ParosaTextSecondary, fontSize = 12.sp)
              Text("Drop: ${order.customerName} • ${order.deliveryAddress}", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
              Spacer(modifier = Modifier.height(4.dp))
              Text(order.itemsSummary, color = ParosaTextMuted, fontSize = 11.sp)

              Spacer(modifier = Modifier.height(12.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (order.status == "READY") {
                  Button(
                    onClick = {
                      viewModel.riderPickUpOrder(order.id)
                      Toast.makeText(context, "Order ${order.id} PICKED UP! Status: Out for Delivery", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text("PICK UP ORDER", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                } else if (order.status == "PICKED_UP") {
                  Button(
                    onClick = {
                      viewModel.riderDeliverOrder(order.id, "dl_1")
                      Toast.makeText(context, "Order ${order.id} DELIVERED! ₹35 added to earnings.", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text("MARK DELIVERED", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }

      // Recent Completed Deliveries
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Recent Delivered Orders (${completedDeliveries.size})", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }

      items(completedDeliveries.take(5)) { doneOrder ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaSurface,
          border = BorderStroke(1.dp, ParosaBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("${doneOrder.id} • ${doneOrder.customerName}", color = ParosaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
              Text("Delivered to ${doneOrder.deliveryAddress}", color = ParosaTextMuted, fontSize = 10.sp)
            }
            Text("+₹35", color = ParosaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
