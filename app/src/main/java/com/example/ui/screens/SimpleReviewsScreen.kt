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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ReviewEntity
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun SimpleReviewsScreen(
  viewModel: RestaurantViewModel
) {
  val context = LocalContext.current
  val reviews by viewModel.reviews.collectAsStateWithLifecycle()

  var selectedFilter by remember { mutableStateOf("All") }
  val filters = listOf("All", "5 Star", "4 Star", "3 Star", "2 Star", "1 Star")

  var reviewForDetail by remember { mutableStateOf<ReviewEntity?>(null) }
  var reviewToReply by remember { mutableStateOf<ReviewEntity?>(null) }
  var replyText by remember { mutableStateOf("") }

  // Review Details Dialog
  reviewForDetail?.let { review ->
    Dialog(onDismissRequest = { reviewForDetail = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(review.customerName, color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            val stars = "★".repeat(review.rating.toInt().coerceIn(1, 5))
            Text(stars, color = Color(0xFFFACC15), fontSize = 15.sp)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text("Order: ${review.orderId} • Date: ${review.date}", color = ParosaTextMuted, fontSize = 11.sp)

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
          Spacer(modifier = Modifier.height(10.dp))

          Text("Food Ordered:", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Text(review.itemsReviewed, color = ParosaTextPrimary, fontSize = 13.sp)

          Spacer(modifier = Modifier.height(10.dp))
          Text("Customer Feedback:", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Text("\"${review.comment}\"", color = ParosaTextPrimary, fontSize = 13.sp)

          if (!review.replyText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ParosaOrangeContainer.copy(alpha = 0.4f),
              border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Restaurant Reply:", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(review.replyText, color = ParosaTextPrimary, fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (review.replyText.isNullOrBlank()) {
              Button(
                onClick = {
                  val r = review
                  reviewForDetail = null
                  reviewToReply = r
                },
                colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
              ) {
                Text("Reply", color = Color.Black, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(8.dp))
            }
            TextButton(onClick = { reviewForDetail = null }) {
              Text("Close", color = ParosaTextSecondary)
            }
          }
        }
      }
    }
  }

  // Reply to Review Dialog
  reviewToReply?.let { review ->
    Dialog(onDismissRequest = { reviewToReply = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Reply to ${review.customerName}", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text("\"${review.comment}\"", color = ParosaTextSecondary, fontSize = 12.sp, maxLines = 2)

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            placeholder = { Text("Write your reply...", color = ParosaTextMuted, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth().height(100.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
              replyText = ""
              reviewToReply = null
            }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (replyText.isNotBlank()) {
                  viewModel.replyToReview(review.id, replyText.trim())
                  Toast.makeText(context, "Reply sent to customer.", Toast.LENGTH_SHORT).show()
                  replyText = ""
                  reviewToReply = null
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
            ) {
              Text("Send Reply", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Filtered reviews
  val filteredReviews = remember(reviews, selectedFilter) {
    when (selectedFilter) {
      "5 Star" -> reviews.filter { it.rating >= 4.5f }
      "4 Star" -> reviews.filter { it.rating in 3.5f..4.4f }
      "3 Star" -> reviews.filter { it.rating in 2.5f..3.4f }
      "2 Star" -> reviews.filter { it.rating in 1.5f..2.4f }
      "1 Star" -> reviews.filter { it.rating < 1.5f }
      else -> reviews
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Heading
    item {
      Text(
        text = "Reviews & Ratings",
        color = ParosaTextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // 2. Rating Summary Card & Breakdown
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
              Text("Overall Rating", color = ParosaTextSecondary, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("4.5", color = ParosaTextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("★", color = Color(0xFFFACC15), fontSize = 26.sp)
              }
              Text("Based on 128 reviews", color = ParosaTextMuted, fontSize = 11.sp)
            }

            // Simple rating breakdown bars
            Column(modifier = Modifier.width(170.dp)) {
              RatingBarRow("5 ★", 82, 128)
              Spacer(modifier = Modifier.height(3.dp))
              RatingBarRow("4 ★", 31, 128)
              Spacer(modifier = Modifier.height(3.dp))
              RatingBarRow("3 ★", 9, 128)
              Spacer(modifier = Modifier.height(3.dp))
              RatingBarRow("2 ★", 4, 128)
              Spacer(modifier = Modifier.height(3.dp))
              RatingBarRow("1 ★", 2, 128)
            }
          }
        }
      }
    }

    // 3. Review Filters Row
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filters) { filter ->
          val isSelected = selectedFilter == filter
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) ParosaOrangeContainer else ParosaSurface,
            border = BorderStroke(1.dp, if (isSelected) ParosaOrange else ParosaBorder),
            modifier = Modifier.clickable { selectedFilter = filter }
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

    // 4. Review List Heading
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Customer Reviews", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Tap for full details", color = ParosaTextMuted, fontSize = 11.sp)
      }
    }

    // 5. Review Cards
    if (filteredReviews.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = ParosaSurface),
          border = BorderStroke(1.dp, ParosaBorder)
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No reviews found in '$selectedFilter'", color = ParosaTextMuted, fontSize = 13.sp)
          }
        }
      }
    } else {
      items(filteredReviews, key = { it.id }) { review ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { reviewForDetail = review },
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
              Text(review.customerName, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              Text(review.date, color = ParosaTextMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              val stars = "★".repeat(review.rating.toInt().coerceIn(1, 5))
              Text(stars, color = Color(0xFFFACC15), fontSize = 13.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Order ${review.orderId}", color = ParosaTextMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("\"${review.comment}\"", color = ParosaTextSecondary, fontSize = 13.sp)

            // Show Restaurant Reply if present
            if (!review.replyText.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = ParosaOrangeContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text("Restaurant Reply:", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(review.replyText, color = ParosaTextPrimary, fontSize = 12.sp)
                }
              }
            } else {
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                  onClick = {
                    replyText = ""
                    reviewToReply = review
                  },
                  shape = RoundedCornerShape(6.dp),
                  border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp)
                ) {
                  Icon(Icons.Default.Reply, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reply", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun RatingBarRow(starLabel: String, count: Int, total: Int) {
  val frac = if (total > 0) (count.toFloat() / total).coerceIn(0.04f, 1f) else 0.04f
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(starLabel, color = ParosaTextSecondary, fontSize = 10.sp, modifier = Modifier.width(22.dp))
    Spacer(modifier = Modifier.width(4.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(5.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(ParosaBorder)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(frac)
          .fillMaxHeight()
          .clip(RoundedCornerShape(3.dp))
          .background(Color(0xFFFACC15))
      )
    }
    Spacer(modifier = Modifier.width(6.dp))
    Text("$count", color = ParosaTextMuted, fontSize = 10.sp, modifier = Modifier.width(20.dp))
  }
}
