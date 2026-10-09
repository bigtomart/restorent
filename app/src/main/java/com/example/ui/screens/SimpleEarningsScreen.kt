package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.TransactionEntity
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun SimpleEarningsScreen(
  viewModel: RestaurantViewModel
) {
  val transactions by viewModel.transactions.collectAsStateWithLifecycle()
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

  var selectedDateFilter by remember { mutableStateOf("Today") }
  val dateFilters = listOf("Today", "7 Days", "30 Days", "Custom Date Range")

  var selectedTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

  // Transaction details dialog
  selectedTxForDetail?.let { tx ->
    Dialog(onDismissRequest = { selectedTxForDetail = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
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
            Text(tx.orderId, color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (tx.settlementStatus) {
                "PAID" -> ParosaGreenContainer
                "PROCESSING" -> ParosaOrangeContainer
                "FAILED" -> ParosaRedContainer
                else -> ParosaOrangeContainer
              }
            ) {
              Text(
                text = tx.settlementStatus,
                color = when (tx.settlementStatus) {
                  "PAID" -> ParosaGreen
                  "FAILED" -> ParosaRed
                  else -> ParosaOrange
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text("Date: ${tx.date}", color = ParosaTextMuted, fontSize = 12.sp)

          Spacer(modifier = Modifier.height(14.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Order Amount", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("₹${tx.grossAmount.toInt()}", color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          val subtotalEst = (tx.grossAmount * 0.88).toInt()
          val discountEst = (tx.discountContribution).toInt()
          val taxEst = (tx.tax).toInt()
          val commEst = (tx.commission).toInt()

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹$subtotalEst", color = ParosaTextPrimary, fontSize = 12.sp)
          }
          if (discountEst > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Discount", color = ParosaTextSecondary, fontSize = 12.sp)
              Text("-₹$discountEst", color = ParosaRed, fontSize = 12.sp)
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tax", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("₹$taxEst", color = ParosaTextPrimary, fontSize = 12.sp)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Platform Commission", color = ParosaTextSecondary, fontSize = 12.sp)
            Text("-₹$commEst", color = ParosaRed, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(10.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Restaurant Earnings", color = ParosaGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("₹${tx.netEarning.toInt()}", color = ParosaGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { selectedTxForDetail = null },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ParosaSurfaceElevated),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Close", color = ParosaTextPrimary)
          }
        }
      }
    }
  }

  // Dynamic calculations based on orders and filter
  val completedOrders = remember(allOrders) { allOrders.filter { it.status == "COMPLETED" } }
  val completedEarningsExtra = remember(completedOrders) { completedOrders.sumOf { it.netEarnings }.toInt() }

  val todayEarnings = 2480 + completedEarningsExtra
  val thisWeekEarnings = when (selectedDateFilter) {
    "Today" -> todayEarnings
    "7 Days" -> 15800 + completedEarningsExtra
    "30 Days" -> 62400 + completedEarningsExtra
    else -> 15800 + completedEarningsExtra
  }
  val thisMonthEarnings = 62400 + completedEarningsExtra

  val grossSalesToday = 13200 + (completedOrders.sumOf { it.totalAmount }.toInt())
  val discountsToday = 1200 + (completedOrders.sumOf { it.discount }.toInt())
  val commissionToday = 1320 + (completedOrders.sumOf { it.commission }.toInt())
  val taxesToday = 200 + (completedOrders.sumOf { it.taxes }.toInt())
  val netEarningsToday = grossSalesToday - discountsToday - commissionToday + taxesToday
  val totalOrdersToday = 28 + completedOrders.size

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Heading
    item {
      Text(
        text = "Earnings",
        color = ParosaTextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // 2. Date Filters Row
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(dateFilters) { filter ->
          val isSelected = selectedDateFilter == filter
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) ParosaOrangeContainer else ParosaSurface,
            border = BorderStroke(1.dp, if (isSelected) ParosaOrange else ParosaBorder),
            modifier = Modifier.clickable { selectedDateFilter = filter }
          ) {
            Text(
              text = filter,
              color = if (isSelected) ParosaOrange else ParosaTextSecondary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }
      }
    }

    // 3. 3 Simple Top Metric Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SimpleEarningsCard(
          title = "Today's Earnings",
          amount = "₹$todayEarnings",
          modifier = Modifier.weight(1f)
        )
        SimpleEarningsCard(
          title = "This Week",
          amount = "₹$thisWeekEarnings",
          modifier = Modifier.weight(1f)
        )
        SimpleEarningsCard(
          title = "This Month",
          amount = "₹$thisMonthEarnings",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 4. Today's Summary Breakdown
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaSurface),
        border = BorderStroke(1.dp, ParosaBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (selectedDateFilter == "Today") "Today's Summary" else "$selectedDateFilter Summary",
            color = ParosaTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(12.dp))

          SummaryRow("Total Orders", "$totalOrdersToday", ParosaTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          SummaryRow("Gross Sales", "₹$grossSalesToday", ParosaTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          SummaryRow("Discounts", "-₹$discountsToday", ParosaRed)
          Spacer(modifier = Modifier.height(6.dp))
          SummaryRow("Platform Commission", "-₹$commissionToday", ParosaRed)
          Spacer(modifier = Modifier.height(6.dp))
          SummaryRow("Taxes/Adjustments", "+₹$taxesToday", ParosaTextSecondary)

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(10.dp))

          SummaryRow("Net Earnings", "₹$netEarningsToday", ParosaGreen, isBold = true)
        }
      }
    }

    // 5. Settlement Status Section
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaSurface),
        border = BorderStroke(1.dp, ParosaBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Settlement Status", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Pending Settlement", color = ParosaTextSecondary, fontSize = 11.sp)
              Text("₹2,450", color = ParosaOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Text("Processing", color = ParosaOrange, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }

            Box(modifier = Modifier.width(1.dp).height(36.dp).background(ParosaBorder))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Next Settlement", color = ParosaTextSecondary, fontSize = 11.sp)
              Text("₹5,800", color = Color(0xFFFACC15), fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Text("Tomorrow, 10 AM", color = ParosaTextMuted, fontSize = 10.sp)
            }

            Box(modifier = Modifier.width(1.dp).height(36.dp).background(ParosaBorder))

            Column(horizontalAlignment = Alignment.End) {
              Text("Paid Settlement", color = ParosaTextSecondary, fontSize = 11.sp)
              Text("₹52,400", color = ParosaGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Text("Paid", color = ParosaGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 6. Transaction History
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Transaction History",
          color = ParosaTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text("Tap for details", color = ParosaTextMuted, fontSize = 11.sp)
      }
    }

    if (transactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, ParosaBorder)
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No transactions recorded yet", color = ParosaTextMuted, fontSize = 13.sp)
          }
        }
      }
    } else {
      items(transactions, key = { it.id }) { tx ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedTxForDetail = tx },
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
            Column {
              Text(tx.orderId, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(2.dp))
              Text(tx.date, color = ParosaTextMuted, fontSize = 11.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text("Order Amount: ₹${tx.grossAmount.toInt()}", color = ParosaTextSecondary, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("₹${tx.netEarning.toInt()}", color = ParosaGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
              Text("Earnings", color = ParosaTextMuted, fontSize = 10.sp)
              Spacer(modifier = Modifier.height(3.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = when (tx.settlementStatus) {
                  "PAID" -> ParosaGreenContainer
                  "FAILED" -> ParosaRedContainer
                  else -> ParosaOrangeContainer
                }
              ) {
                Text(
                  text = tx.settlementStatus,
                  color = when (tx.settlementStatus) {
                    "PAID" -> ParosaGreen
                    "FAILED" -> ParosaRed
                    else -> ParosaOrange
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SummaryRow(title: String, value: String, valueColor: Color, isBold: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      color = if (isBold) ParosaTextPrimary else ParosaTextSecondary,
      fontSize = if (isBold) 13.sp else 12.sp,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    )
    Text(
      text = value,
      color = valueColor,
      fontSize = if (isBold) 15.sp else 13.sp,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
    )
  }
}

@Composable
fun SimpleEarningsCard(
  title: String,
  amount: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = ParosaSurface),
    border = BorderStroke(1.dp, ParosaBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(title, color = ParosaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
      Spacer(modifier = Modifier.height(6.dp))
      Text(amount, color = ParosaGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
  }
}
