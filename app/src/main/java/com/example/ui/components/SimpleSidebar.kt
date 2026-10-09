package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class SimpleSidebarItem(
  val id: String,
  val label: String,
  val icon: ImageVector
)

@Composable
fun SimpleSidebar(
  restaurantName: String,
  currentTab: String,
  onSelectTab: (String) -> Unit,
  onLogout: () -> Unit,
  onSwitchToCustomerApp: (() -> Unit)? = null,
  onSwitchMode: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val menuItems = listOf(
    SimpleSidebarItem("Dashboard", "Dashboard", Icons.Outlined.Dashboard),
    SimpleSidebarItem("Orders", "Orders", Icons.Outlined.ReceiptLong),
    SimpleSidebarItem("Menu", "Menu", Icons.Outlined.RestaurantMenu),
    SimpleSidebarItem("Earnings", "Earnings", Icons.Outlined.Payments),
    SimpleSidebarItem("Reviews", "Reviews", Icons.Outlined.StarOutline),
    SimpleSidebarItem("Settings", "Settings", Icons.Outlined.Settings)
  )

  Column(
    modifier = modifier
      .width(240.dp)
      .fillMaxHeight()
      .background(ParosaSurface)
      .border(1.dp, ParosaBorder)
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Restaurant Logo & Name
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ParosaOrangeContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.SoupKitchen,
            contentDescription = null,
            tint = ParosaOrange,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = restaurantName,
            color = ParosaTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = "Partner Portal",
            color = ParosaTextMuted,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Navigation Links
      menuItems.forEach { item ->
        val isSelected = currentTab == item.id
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSelected) ParosaOrangeContainer else Color.Transparent,
          border = if (isSelected) BorderStroke(1.dp, ParosaBorderHighlight) else null,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onSelectTab(item.id) }
            .testTag("nav_${item.id}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = item.icon,
              contentDescription = item.label,
              tint = if (isSelected) ParosaOrange else ParosaTextSecondary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = item.label,
              color = if (isSelected) ParosaTextPrimary else ParosaTextSecondary,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    // App Switcher section
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
      Text(
        text = "SWITCH APPLICATION",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = ParosaTextMuted,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
      )

      // Customer App
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ParosaOrangeContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onSwitchMode?.invoke("CUSTOMER") ?: onSwitchToCustomerApp?.invoke()
          }
          .padding(bottom = 4.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Customer App", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      // Delivery Partner
      if (onSwitchMode != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaBlueContainer.copy(alpha = 0.35f),
          border = BorderStroke(1.dp, ParosaBlue.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSwitchMode("DELIVERY") }
            .padding(bottom = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = ParosaBlue, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delivery Rider", color = ParosaBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Admin Panel
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaGreenContainer.copy(alpha = 0.35f),
          border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSwitchMode("ADMIN") }
            .padding(bottom = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ParosaGreen, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Admin Panel", color = ParosaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Logout Item at bottom
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color.Transparent,
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onLogout() }
        .testTag("nav_logout")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Logout,
          contentDescription = "Logout",
          tint = ParosaRed,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Logout",
          color = ParosaRed,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}
