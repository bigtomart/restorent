package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun RestaurantPendingApprovalScreen(
  viewModel: RestaurantViewModel,
  onLogout: () -> Unit,
  onSwitchMode: (String) -> Unit
) {
  val profile by viewModel.restaurantProfile.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentAuthenticatedUser.collectAsStateWithLifecycle()
  val isLoading by viewModel.authIsLoading.collectAsStateWithLifecycle()

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .verticalScroll(scrollState)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    // Pending Icon Circle
    Box(
      modifier = Modifier
        .size(90.dp)
        .clip(CircleShape)
        .background(ParosaOrangeContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.HourglassTop,
        contentDescription = "Pending Approval",
        tint = ParosaOrange,
        modifier = Modifier.size(46.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Title
    Text(
      text = "Application Under Review",
      color = ParosaTextPrimary,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Status Badge
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = ParosaOrangeContainer,
      border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.6f))
    ) {
      Text(
        text = "⏳ STATUS: PENDING ADMIN APPROVAL",
        color = ParosaOrange,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Details Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text("APPLICATION SUMMARY", color = ParosaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        DetailRow("Restaurant Name", profile?.name ?: "Spice Villa")
        DetailRow("Owner Name", profile?.ownerName ?: currentUser?.name ?: "Partner Owner")
        DetailRow("Contact Phone", profile?.phone ?: currentUser?.phone ?: "")
        DetailRow("Contact Email", profile?.email ?: currentUser?.email ?: "")
        DetailRow("Cuisine Types", profile?.cuisines ?: "North Indian, Fast Food")
        DetailRow("FSSAI License", profile?.fssai ?: "Submitted for Verification")
        DetailRow("Submitted Date", profile?.joinedDate ?: "Today")

        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Your store details and FSSAI license are under verification by the platform administration team. Once approved, your restaurant dashboard will unlock automatically.",
          color = ParosaTextSecondary,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Refresh Approval Status Button
    Button(
      onClick = { viewModel.refreshApprovalStatus() },
      enabled = !isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("refresh_approval_status_btn"),
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
    ) {
      Icon(Icons.Default.Refresh, contentDescription = null, tint = ParosaBg, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Check Approval Status", color = ParosaBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Quick Test Helper: Open Admin Panel to Approve
    OutlinedButton(
      onClick = { onSwitchMode("ADMIN") },
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
        .testTag("goto_admin_approval_btn"),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ParosaGreen, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Open Admin Panel (To Approve This Restaurant)", color = ParosaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Logout
    TextButton(
      onClick = onLogout,
      modifier = Modifier.testTag("pending_logout_btn")
    ) {
      Icon(Icons.Default.Logout, contentDescription = null, tint = ParosaRed, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Log Out & Return to Login", color = ParosaRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, color = ParosaTextSecondary, fontSize = 12.sp)
    Text(value, color = ParosaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
  }
}
