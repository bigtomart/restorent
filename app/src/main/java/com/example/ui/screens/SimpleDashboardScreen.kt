package com.example.ui.screens

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.entity.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimpleDashboardScreen(
  viewModel: RestaurantViewModel,
  onNavigateToTab: (String) -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.restaurantProfile.collectAsStateWithLifecycle()
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
  val incomingOrder by viewModel.incomingOrderAlert.collectAsStateWithLifecycle(initialValue = null)

  var rejectDialogOrder by remember { mutableStateOf<OrderEntity?>(null) }
  var rejectReason by remember { mutableStateOf("Item out of stock") }
  var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }
  var newOrderAlertOrder by remember { mutableStateOf<OrderEntity?>(null) }

  // Sound & Vibration helper for new orders
  LaunchedEffect(incomingOrder) {
    incomingOrder?.let { order ->
      newOrderAlertOrder = order
      try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(400)
        }
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(context, notificationUri)
        r?.play()
      } catch (_: Exception) {}
    }
  }

  // 1. Prominent New Order Received Notification Dialog
  newOrderAlertOrder?.let { order ->
    Dialog(onDismissRequest = { newOrderAlertOrder = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = ParosaSurface,
        border = BorderStroke(2.dp, ParosaOrange),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ParosaOrangeContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("New Order Received!", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Text("Immediate action required", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(order.id, color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("₹${order.totalAmount.toInt()}", color = ParosaGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text("Customer: ${order.customerName}", color = ParosaTextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(order.itemsSummary, color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                val orderToReject = order
                newOrderAlertOrder = null
                rejectDialogOrder = orderToReject
              },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, ParosaRed),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ParosaRed),
              modifier = Modifier.weight(1f).height(40.dp)
            ) {
              Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
              onClick = {
                viewModel.acceptOrder(order.id)
                newOrderAlertOrder = null
                Toast.makeText(context, "Order ${order.id} accepted! Moved to Preparing", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
              modifier = Modifier.weight(1f).height(40.dp)
            ) {
              Text("ACCEPT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }

  // 2. Reject Reason Dialog
  rejectDialogOrder?.let { order ->
    Dialog(onDismissRequest = { rejectDialogOrder = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Reject Order ${order.id}", color = ParosaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Select rejection reason:", color = ParosaTextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(10.dp))

          val reasons = listOf("Item out of stock", "Kitchen too busy", "Store closing soon", "Delivery address far")
          reasons.forEach { r ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { rejectReason = r }
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = rejectReason == r,
                onClick = { rejectReason = r },
                colors = RadioButtonDefaults.colors(selectedColor = ParosaOrange)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(r, color = ParosaTextPrimary, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { rejectDialogOrder = null }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                viewModel.rejectOrder(order.id, rejectReason)
                Toast.makeText(context, "Order ${order.id} rejected ($rejectReason)", Toast.LENGTH_SHORT).show()
                rejectDialogOrder = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaRed)
            ) {
              Text("Reject Order", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // 3. Order Details Dialog
  selectedOrderForDetail?.let { order ->
    OrderDetailsDialog(
      order = order,
      onDismiss = { selectedOrderForDetail = null },
      onAccept = {
        viewModel.acceptOrder(order.id)
        selectedOrderForDetail = null
        Toast.makeText(context, "Order ${order.id} moved to Preparing", Toast.LENGTH_SHORT).show()
      },
      onReject = {
        val o = order
        selectedOrderForDetail = null
        rejectDialogOrder = o
      },
      onMarkReady = {
        viewModel.markFoodReady(order.id)
        selectedOrderForDetail = null
        Toast.makeText(context, "Order ${order.id} marked Ready", Toast.LENGTH_SHORT).show()
      },
      onMarkCompleted = {
        viewModel.completeOrder(order.id)
        selectedOrderForDetail = null
        Toast.makeText(context, "Order ${order.id} Completed!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  val isOnline = profile?.isOnline ?: true

  // Order state filtering
  val newOrders = remember(allOrders) { allOrders.filter { it.status == OrderStatus.NEW.name } }
  val preparingOrders = remember(allOrders) {
    allOrders.filter { it.status == OrderStatus.PREPARING.name || it.status == OrderStatus.ACCEPTED.name }
  }
  val readyOrders = remember(allOrders) { allOrders.filter { it.status == OrderStatus.READY.name } }
  val completedOrders = remember(allOrders) { allOrders.filter { it.status == OrderStatus.COMPLETED.name } }

  val todaysOrdersCount = remember(completedOrders.size) {
    28 + completedOrders.size
  }

  val todaysEarningsAmount = remember(completedOrders) {
    val extra = completedOrders.sumOf { it.netEarnings }
    2480 + extra.toInt()
  }

  val averageOrderValue = remember(todaysEarningsAmount, todaysOrdersCount) {
    if (todaysOrdersCount > 0) todaysEarningsAmount / todaysOrdersCount else 446
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Top Section: Greeting & Online Switch + Quick Simulate Order
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
              Text(
                text = "Good Morning, ${profile?.name ?: "The Punjab Kitchen"}",
                color = ParosaTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(2.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) ParosaGreen else ParosaRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isOnline) "Online • Ready for orders" else "Offline • Not taking orders",
                  color = if (isOnline) ParosaGreen else ParosaTextMuted,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            // Simple ON/OFF Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isOnline) "ON" else "OFF",
                color = if (isOnline) ParosaGreen else ParosaTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Switch(
                checked = isOnline,
                onCheckedChange = { newState ->
                  viewModel.toggleOnlineStatus(newState)
                  Toast.makeText(
                    context,
                    if (newState) "Restaurant is now ONLINE" else "Restaurant is now OFFLINE",
                    Toast.LENGTH_SHORT
                  ).show()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = ParosaGreen,
                  checkedTrackColor = ParosaGreenContainer,
                  uncheckedThumbColor = ParosaTextMuted,
                  uncheckedTrackColor = ParosaBorder
                ),
                modifier = Modifier.testTag("online_toggle_switch")
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder.copy(alpha = 0.5f)))
          Spacer(modifier = Modifier.height(8.dp))

          // Quick Simulation Button for Testing Incoming Orders
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Simulate live order dispatch:", color = ParosaTextMuted, fontSize = 11.sp)
            OutlinedButton(
              onClick = {
                viewModel.simulateIncomingCustomerOrder()
                Toast.makeText(context, "New incoming order simulated!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp).testTag("simulate_order_btn")
            ) {
              Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Simulate Order", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // 2. Four Simple Compact Metric Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SimpleMetricCard(
          title = "New Orders",
          count = if (newOrders.size < 10) "0${newOrders.size}" else "${newOrders.size}",
          countColor = ParosaOrange,
          modifier = Modifier.weight(1f).clickable { onNavigateToTab("Orders") }
        )
        SimpleMetricCard(
          title = "Preparing",
          count = if (preparingOrders.size < 10) "0${preparingOrders.size}" else "${preparingOrders.size}",
          countColor = Color(0xFFFACC15),
          modifier = Modifier.weight(1f).clickable { onNavigateToTab("Orders") }
        )
        SimpleMetricCard(
          title = "Ready",
          count = if (readyOrders.size < 10) "0${readyOrders.size}" else "${readyOrders.size}",
          countColor = ParosaGreen,
          modifier = Modifier.weight(1f).clickable { onNavigateToTab("Orders") }
        )
        SimpleMetricCard(
          title = "Today's Orders",
          count = "$todaysOrdersCount",
          countColor = ParosaTextPrimary,
          modifier = Modifier.weight(1f).clickable { onNavigateToTab("Orders") }
        )
      }
    }

    // 3. Today's Earnings Summary Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToTab("Earnings") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaSurface),
        border = BorderStroke(1.dp, ParosaBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Today's Earnings", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹$todaysEarningsAmount", color = ParosaGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
          }

          Box(
            modifier = Modifier
              .width(1.dp)
              .height(40.dp)
              .background(ParosaBorder)
          )

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Orders Today", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("$todaysOrdersCount", color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }

          Box(
            modifier = Modifier
              .width(1.dp)
              .height(40.dp)
              .background(ParosaBorder)
          )

          Column(horizontalAlignment = Alignment.End) {
            Text("Average Order", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹$averageOrderValue", color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 4. New Orders Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "New Orders (${newOrders.size})",
          color = ParosaTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        if (newOrders.isNotEmpty()) {
          Text("Tap to view details", color = ParosaTextMuted, fontSize = 11.sp)
        }
      }
    }

    if (newOrders.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, ParosaBorder)
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            Text("No new incoming orders right now", color = ParosaTextMuted, fontSize = 13.sp)
          }
        }
      }
    } else {
      items(newOrders, key = { it.id }) { order ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { selectedOrderForDetail = order },
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, ParosaBorderHighlight)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(order.id, color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
              Text("₹${order.totalAmount.toInt()}", color = ParosaGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Customer: ${order.customerName}", color = ParosaTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Text(order.itemsSummary, color = ParosaTextPrimary, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { rejectDialogOrder = order },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ParosaBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParosaRed),
                modifier = Modifier.height(36.dp)
              ) {
                Text("REJECT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }

              Spacer(modifier = Modifier.width(8.dp))

              Button(
                onClick = {
                  viewModel.acceptOrder(order.id)
                  Toast.makeText(context, "Order ${order.id} moved to Preparing", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
                modifier = Modifier.height(36.dp).testTag("accept_${order.id}")
              ) {
                Text("ACCEPT", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // 5. Preparing Orders Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Preparing Orders (${preparingOrders.size})",
          color = ParosaTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        if (preparingOrders.isNotEmpty()) {
          Text("Tap to view details", color = ParosaTextMuted, fontSize = 11.sp)
        }
      }
    }

    if (preparingOrders.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, ParosaBorder)
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            Text("No orders currently preparing in kitchen", color = ParosaTextMuted, fontSize = 13.sp)
          }
        }
      }
    } else {
      items(preparingOrders, key = { it.id }) { order ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { selectedOrderForDetail = order },
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
              Text(order.id, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(2.dp))
              Text("Customer: ${order.customerName}", color = ParosaTextSecondary, fontSize = 11.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text(order.itemsSummary, color = ParosaTextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Spacer(modifier = Modifier.height(4.dp))
              Text("⏱ Due in ${order.estimatedPrepTimeMinutes} min", color = Color(0xFFFACC15), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Button(
              onClick = {
                viewModel.markFoodReady(order.id)
                Toast.makeText(context, "Order ${order.id} is now Ready for Pickup!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
              modifier = Modifier.height(34.dp).testTag("mark_ready_${order.id}")
            ) {
              Text("MARK READY", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 6. Ready for Pickup Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ready for Pickup (${readyOrders.size})",
          color = ParosaTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        if (readyOrders.isNotEmpty()) {
          Text("Tap to view details", color = ParosaTextMuted, fontSize = 11.sp)
        }
      }
    }

    if (readyOrders.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, ParosaBorder)
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            Text("No orders waiting for pickup", color = ParosaTextMuted, fontSize = 13.sp)
          }
        }
      }
    } else {
      items(readyOrders, key = { it.id }) { order ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { selectedOrderForDetail = order },
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
                Text(order.id, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("• ${order.customerName}", color = ParosaTextSecondary, fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(order.itemsSummary, color = ParosaTextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Spacer(modifier = Modifier.height(2.dp))
              Text("Amount: ₹${order.totalAmount.toInt()}", color = ParosaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                viewModel.completeOrder(order.id)
                Toast.makeText(context, "Order ${order.id} marked Completed!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
              modifier = Modifier.height(34.dp).testTag("pickup_${order.id}")
            ) {
              Text("MARK COMPLETED", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun SimpleMetricCard(
  title: String,
  count: String,
  countColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = ParosaSurface),
    border = BorderStroke(1.dp, ParosaBorder)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
      Text(
        text = title,
        color = ParosaTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = count,
        color = countColor,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun OrderDetailsDialog(
  order: OrderEntity,
  onDismiss: () -> Unit,
  onAccept: () -> Unit,
  onReject: () -> Unit,
  onMarkReady: () -> Unit,
  onMarkCompleted: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = ParosaSurface,
      border = BorderStroke(1.dp, ParosaBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
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
          Text(order.id, color = ParosaTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (order.status) {
              "NEW" -> ParosaOrangeContainer
              "READY" -> ParosaGreenContainer
              "COMPLETED" -> ParosaSurfaceElevated
              "CANCELLED", "REJECTED" -> ParosaRedContainer
              else -> ParosaSurfaceElevated
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

        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
        Spacer(modifier = Modifier.height(10.dp))

        Text("Customer Details", color = ParosaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(order.customerName, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(order.customerPhone, color = ParosaTextSecondary, fontSize = 12.sp)
        Text(order.deliveryAddress, color = ParosaTextMuted, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))
        Text("Ordered Items", color = ParosaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(order.itemsSummary, color = ParosaTextPrimary, fontSize = 13.sp)

        if (order.specialInstructions.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = ParosaOrangeContainer.copy(alpha = 0.5f)
          ) {
            Text(
              "Note: ${order.specialInstructions}",
              color = ParosaOrange,
              fontSize = 11.sp,
              modifier = Modifier.padding(6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
        Spacer(modifier = Modifier.height(10.dp))

        // Price breakdown
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Subtotal", color = ParosaTextSecondary, fontSize = 12.sp)
          Text("₹${order.subtotal.toInt()}", color = ParosaTextPrimary, fontSize = 12.sp)
        }
        if (order.discount > 0) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Discount", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("-₹${order.discount.toInt()}", color = ParosaRed, fontSize = 12.sp)
          }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Taxes & Packaging", color = ParosaTextSecondary, fontSize = 12.sp)
          Text("₹${order.taxes.toInt()}", color = ParosaTextPrimary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Total Amount", color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text("₹${order.totalAmount.toInt()}", color = ParosaGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Placed on: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.orderTime))}",
          color = ParosaTextMuted,
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Dynamic State Actions
        when (order.status) {
          "NEW" -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = onReject,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ParosaRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParosaRed),
                modifier = Modifier.weight(1f)
              ) {
                Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Button(
                onClick = onAccept,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
                modifier = Modifier.weight(1f)
              ) {
                Text("ACCEPT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
          "ACCEPTED" -> {
            Button(
              onClick = onAccept,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("START PREPARING", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
          "PREPARING" -> {
            Button(
              onClick = onMarkReady,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("MARK READY", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
          "READY" -> {
            Button(
              onClick = onMarkCompleted,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("MARK COMPLETED", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
          else -> {
            TextButton(
              onClick = onDismiss,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Close", color = ParosaTextSecondary)
            }
          }
        }
      }
    }
  }
}
