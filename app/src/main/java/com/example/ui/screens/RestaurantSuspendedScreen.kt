package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun RestaurantSuspendedScreen(
  viewModel: RestaurantViewModel,
  onLogout: () -> Unit,
  onSwitchMode: (String) -> Unit
) {
  val profile by viewModel.restaurantProfile.collectAsStateWithLifecycle()
  val errorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Suspended Shield Icon
    Box(
      modifier = Modifier
        .size(90.dp)
        .clip(CircleShape)
        .background(ParosaRedContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Block,
        contentDescription = "Account Suspended",
        tint = ParosaRed,
        modifier = Modifier.size(48.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Restaurant Account Suspended",
      color = ParosaTextPrimary,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = ParosaRedContainer,
      border = BorderStroke(1.dp, ParosaRed.copy(alpha = 0.6f))
    ) {
      Text(
        text = "⛔ STATUS: SUSPENDED BY ADMIN",
        color = ParosaRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text("REASON FOR SUSPENSION", color = ParosaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = profile?.rejectionReason?.ifBlank { errorMessage ?: "Compliance or safety review initiated by platform administrator." } ?: "Compliance audit initiated by platform administrator.",
          color = ParosaTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "While suspended, incoming customer orders are completely halted, menu editing is locked, and restaurant operations are paused.",
          color = ParosaTextSecondary,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Admin Panel Switcher (For Testing & Reactivation)
    OutlinedButton(
      onClick = { onSwitchMode("ADMIN") },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("suspended_goto_admin_btn"),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ParosaGreen, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Open Admin Panel (To Reactivate)", color = ParosaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Logout
    TextButton(
      onClick = onLogout,
      modifier = Modifier.testTag("suspended_logout_btn")
    ) {
      Icon(Icons.Default.Logout, contentDescription = null, tint = ParosaRed, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Log Out & Return to Login", color = ParosaRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
  }
}
