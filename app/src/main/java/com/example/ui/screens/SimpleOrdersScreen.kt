package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun SimpleOrdersScreen(
  viewModel: RestaurantViewModel
) {
  val context = LocalContext.current
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
  var selectedTab by remember { mutableStateOf("All") }

  var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }
  var rejectDialogOrder by remember { mutableStateOf<OrderEntity?>(null) }
  var rejectReason by remember { mutableStateOf("Item out of stock") }

  val tabs = listOf("All", "New", "Preparing", "Ready", "Completed", "Cancelled")

  // Reject dialog
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

  // Detailed Order Dialog
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

  val filteredOrders = remember(allOrders, selectedTab) {
    when (selectedTab) {
      "New" -> allOrders.filter { it.status == OrderStatus.NEW.name }
      "Preparing" -> allOrders.filter { it.status == OrderStatus.PREPARING.name || it.status == OrderStatus.ACCEPTED.name }
      "Ready" -> allOrders.filter { it.status == OrderStatus.READY.name }
      "Completed" -> allOrders.filter { it.status == OrderStatus.COMPLETED.name }
      "Cancelled" -> allOrders.filter { it.status == OrderStatus.CANCELLED.name || it.status == OrderStatus.REJECTED.name }
      else -> allOrders
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .padding(16.dp)
  ) {
    Text(
      text = "Orders",
      color = ParosaTextPrimary,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Filter Tabs
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(tabs) { tab ->
        val count = when (tab) {
          "New" -> allOrders.count { it.status == OrderStatus.NEW.name }
          "Preparing" -> allOrders.count { it.status == OrderStatus.PREPARING.name || it.status == OrderStatus.ACCEPTED.name }
          "Ready" -> allOrders.count { it.status == OrderStatus.READY.name }
          "Completed" -> allOrders.count { it.status == OrderStatus.COMPLETED.name }
          "Cancelled" -> allOrders.count { it.status == OrderStatus.CANCELLED.name || it.status == OrderStatus.REJECTED.name }
          else -> allOrders.size
        }

        val isSelected = selectedTab == tab
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSelected) ParosaOrangeContainer else ParosaSurface,
          border = BorderStroke(1.dp, if (isSelected) ParosaOrange else ParosaBorder),
          modifier = Modifier.clickable { selectedTab = tab }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = tab,
              color = if (isSelected) ParosaOrange else ParosaTextSecondary,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (count > 0) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) ParosaOrange else ParosaSurfaceElevated
              ) {
                Text(
                  text = "$count",
                  color = if (isSelected) Color.Black else ParosaTextMuted,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (filteredOrders.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No orders in '$selectedTab'", color = ParosaTextMuted, fontSize = 14.sp)
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredOrders, key = { it.id }) { order ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedOrderForDetail = order },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ParosaSurface),
            border = BorderStroke(1.dp, ParosaBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(order.id, color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("• ${order.customerName}", color = ParosaTextSecondary, fontSize = 13.sp)
                }

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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              Text(order.itemsSummary, color = ParosaTextPrimary, fontSize = 13.sp)

              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Amount: ₹${order.totalAmount.toInt()}",
                  color = ParosaGreen,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )

                Text(
                  text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.orderTime)),
                  color = ParosaTextMuted,
                  fontSize = 11.sp
                )
              }

              // State Actions based on exact order status
              Spacer(modifier = Modifier.height(10.dp))
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder.copy(alpha = 0.5f)))
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                when (order.status) {
                  "NEW" -> {
                    OutlinedButton(
                      onClick = { rejectDialogOrder = order },
                      shape = RoundedCornerShape(6.dp),
                      border = BorderStroke(1.dp, ParosaBorder),
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = ParosaRed),
                      modifier = Modifier.height(32.dp)
                    ) {
                      Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                      onClick = {
                        viewModel.acceptOrder(order.id)
                        Toast.makeText(context, "Order ${order.id} accepted! Moved to Preparing", Toast.LENGTH_SHORT).show()
                      },
                      shape = RoundedCornerShape(6.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
                      modifier = Modifier.height(32.dp).testTag("accept_${order.id}")
                    ) {
                      Text("ACCEPT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                  "ACCEPTED" -> {
                    Button(
                      onClick = {
                        viewModel.startPreparing(order.id)
                        Toast.makeText(context, "Kitchen started preparing ${order.id}", Toast.LENGTH_SHORT).show()
                      },
                      shape = RoundedCornerShape(6.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
                      modifier = Modifier.height(32.dp)
                    ) {
                      Text("START PREPARING", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                  "PREPARING" -> {
                    Button(
                      onClick = {
                        viewModel.markFoodReady(order.id)
                        Toast.makeText(context, "Order ${order.id} marked Ready for Pickup!", Toast.LENGTH_SHORT).show()
                      },
                      shape = RoundedCornerShape(6.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
                      modifier = Modifier.height(32.dp).testTag("mark_ready_${order.id}")
                    ) {
                      Text("MARK READY", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                  "READY" -> {
                    Button(
                      onClick = {
                        viewModel.completeOrder(order.id)
                        Toast.makeText(context, "Order ${order.id} Completed & Handed Over!", Toast.LENGTH_SHORT).show()
                      },
                      shape = RoundedCornerShape(6.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = ParosaGreen),
                      modifier = Modifier.height(32.dp).testTag("pickup_${order.id}")
                    ) {
                      Text("MARK COMPLETED", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                  "CANCELLED", "REJECTED" -> {
                    Text(
                      text = "Reason: ${order.rejectionReason.ifBlank { "Cancelled" }}",
                      color = ParosaRed,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                  "COMPLETED" -> {
                    Text(
                      text = "Completed & Settled",
                      color = ParosaGreen,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
