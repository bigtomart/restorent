package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class DayTiming(
  val day: String,
  var isOpen: Boolean,
  var openTime: String,
  var closeTime: String
)

@Composable
fun SimpleSettingsScreen(
  viewModel: RestaurantViewModel,
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.restaurantProfile.collectAsStateWithLifecycle()

  // Profile fields
  var name by remember(profile) { mutableStateOf(profile?.name ?: "The Punjab Kitchen") }
  var ownerName by remember(profile) { mutableStateOf(profile?.ownerName ?: "Arjun Kumar") }
  var phone by remember(profile) { mutableStateOf(profile?.phone ?: "+91 98450 12891") }
  var email by remember(profile) { mutableStateOf(profile?.email ?: "punjabkitchen.blr@gmail.com") }
  var description by remember { mutableStateOf("Authentic North Indian, tandoori grills, and Mughlai rich gravies.") }
  var cuisineType by remember(profile) { mutableStateOf(profile?.cuisines ?: "North Indian, Mughlai, Tandoor") }
  var address by remember(profile) { mutableStateOf(profile?.address ?: "12th Main Road, Indiranagar") }
  var city by remember(profile) { mutableStateOf(profile?.city ?: "Bengaluru") }
  var state by remember(profile) { mutableStateOf(profile?.state ?: "Karnataka") }
  var pincode by remember(profile) { mutableStateOf(profile?.pincode ?: "560038") }
  var isOnline by remember(profile) { mutableStateOf(profile?.isOnline ?: true) }

  // Logo & Cover presets
  var selectedLogoIcon by remember { mutableStateOf("🍲") }
  var selectedCoverColor by remember { mutableStateOf(ParosaGreenDark) }

  // Dialogs
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showChangePasswordDialog by remember { mutableStateOf(false) }
  var showNotificationSettingsDialog by remember { mutableStateOf(false) }
  var showLogoutConfirmDialog by remember { mutableStateOf(false) }
  var showDeactivateConfirmDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var selectedLanguage by remember { mutableStateOf("English (India)") }

  // Notification toggles state
  var notifNewOrder by remember { mutableStateOf(true) }
  var notifOrderStatus by remember { mutableStateOf(true) }
  var notifPayments by remember { mutableStateOf(true) }
  var notifReviews by remember { mutableStateOf(true) }
  var notifPromotional by remember { mutableStateOf(false) }

  // Change Password state
  var currentPassword by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmNewPassword by remember { mutableStateOf("") }
  var passwordError by remember { mutableStateOf("") }

  // Opening Hours
  val daysList = remember {
    mutableStateListOf(
      DayTiming("Monday", true, "11:00 AM", "11:00 PM"),
      DayTiming("Tuesday", true, "11:00 AM", "11:00 PM"),
      DayTiming("Wednesday", true, "11:00 AM", "11:00 PM"),
      DayTiming("Thursday", true, "11:00 AM", "11:00 PM"),
      DayTiming("Friday", true, "11:00 AM", "11:30 PM"),
      DayTiming("Saturday", true, "11:00 AM", "11:30 PM"),
      DayTiming("Sunday", true, "11:00 AM", "11:00 PM")
    )
  }

  // 1. Change Password Dialog
  if (showChangePasswordDialog) {
    Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Change Password", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password", color = ParosaTextSecondary) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = newPassword,
            onValueChange = {
              newPassword = it
              passwordError = ""
            },
            label = { Text("New Password", color = ParosaTextSecondary) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = confirmNewPassword,
            onValueChange = {
              confirmNewPassword = it
              passwordError = ""
            },
            label = { Text("Confirm New Password", color = ParosaTextSecondary) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )

          if (passwordError.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(passwordError, color = ParosaRed, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showChangePasswordDialog = false }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (newPassword.isBlank()) {
                  passwordError = "New password cannot be empty."
                  return@Button
                }
                if (newPassword != confirmNewPassword) {
                  passwordError = "Passwords do not match."
                  return@Button
                }
                Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                showChangePasswordDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
            ) {
              Text("Save Password", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // 2. Notification Settings Dialog
  if (showNotificationSettingsDialog) {
    Dialog(onDismissRequest = { showNotificationSettingsDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Notification Settings", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(14.dp))

          NotificationToggleRow("New Order Notifications", notifNewOrder) { notifNewOrder = it }
          NotificationToggleRow("Order Status Notifications", notifOrderStatus) { notifOrderStatus = it }
          NotificationToggleRow("Payment Notifications", notifPayments) { notifPayments = it }
          NotificationToggleRow("Review Notifications", notifReviews) { notifReviews = it }
          NotificationToggleRow("Promotional Notifications", notifPromotional) { notifPromotional = it }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              Toast.makeText(context, "Notification preferences saved.", Toast.LENGTH_SHORT).show()
              showNotificationSettingsDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // 3. Logout Confirmation Dialog
  if (showLogoutConfirmDialog) {
    Dialog(onDismissRequest = { showLogoutConfirmDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Are you sure you want to logout?", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text("You will need to sign in again to access the partner dashboard.", color = ParosaTextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(18.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showLogoutConfirmDialog = false }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                showLogoutConfirmDialog = false
                onLogout()
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaRed)
            ) {
              Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // 4. Deactivate Account Confirmation Dialog
  if (showDeactivateConfirmDialog) {
    Dialog(onDismissRequest = { showDeactivateConfirmDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaRed),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Deactivate Restaurant Account", color = ParosaRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "This will pause your restaurant listing, stop all incoming orders, and set your account to inactive. Your data will be safely stored and can be reactivated by support.",
            color = ParosaTextSecondary,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(18.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showDeactivateConfirmDialog = false }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                viewModel.toggleOnlineStatus(false)
                Toast.makeText(context, "Restaurant account deactivated successfully. Status set to Offline.", Toast.LENGTH_LONG).show()
                showDeactivateConfirmDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaRed)
            ) {
              Text("Confirm Deactivation", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // 5. Language Dialog
  if (showLanguageDialog) {
    Dialog(onDismissRequest = { showLanguageDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Select Language", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))
          val languages = listOf("English (India)", "Hindi (हिंदी)", "Punjabi (ਪੰਜਾਬੀ)", "Kannada (ಕನ್ನಡ)")
          languages.forEach { lang ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedLanguage = lang
                  Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                  showLanguageDialog = false
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedLanguage == lang,
                onClick = {
                  selectedLanguage = lang
                  showLanguageDialog = false
                },
                colors = RadioButtonDefaults.colors(selectedColor = ParosaOrange)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(lang, color = ParosaTextPrimary, fontSize = 13.sp)
            }
          }
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Text(
      text = "Settings",
      color = ParosaTextPrimary,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 1. Top Section: Restaurant Profile Header Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Restaurant Profile", color = ParosaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Restaurant Logo preview
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(selectedCoverColor),
              contentAlignment = Alignment.Center
            ) {
              Text(selectedLogoIcon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(name, color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                if (isOnline) "🟢 ONLINE" else "🔴 OFFLINE",
                color = if (isOnline) ParosaGreen else ParosaRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          OutlinedButton(
            onClick = {
              // Quick preset toggle for logo icon & cover
              val icons = listOf("🍲", "🍛", "🥘", "🍗", "🍚")
              val nextIcon = icons[(icons.indexOf(selectedLogoIcon) + 1) % icons.size]
              selectedLogoIcon = nextIcon
              Toast.makeText(context, "Logo updated to $nextIcon", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, ParosaBorder),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit Logo", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Restaurant Availability Section
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
            Text("Restaurant Availability", color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              if (isOnline) "Your restaurant is accepting orders." else "Your restaurant is currently unavailable for new orders.",
              color = if (isOnline) ParosaGreen else ParosaRed,
              fontSize = 12.sp
            )
          }

          Switch(
            checked = isOnline,
            onCheckedChange = {
              isOnline = it
              viewModel.toggleOnlineStatus(it)
              Toast.makeText(
                context,
                if (it) "Your restaurant is accepting orders." else "Your restaurant is currently unavailable for new orders.",
                Toast.LENGTH_SHORT
              ).show()
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = ParosaGreen,
              checkedTrackColor = ParosaGreenContainer
            ),
            modifier = Modifier.testTag("settings_online_switch")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Restaurant Information Form
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Restaurant Information", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Restaurant Name", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = ownerName,
          onValueChange = { ownerName = it },
          label = { Text("Owner Name", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number", color = ParosaTextSecondary) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address", color = ParosaTextSecondary) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Restaurant Description", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = cuisineType,
          onValueChange = { cuisineType = it },
          label = { Text("Cuisine Type", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Restaurant Address", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City", color = ParosaTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State", color = ParosaTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = pincode,
            onValueChange = { pincode = it },
            label = { Text("Pincode", color = ParosaTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            if (profile != null) {
              viewModel.updateProfile(
                profile!!.copy(
                  name = name.trim(),
                  ownerName = ownerName.trim(),
                  phone = phone.trim(),
                  email = email.trim(),
                  cuisines = cuisineType.trim(),
                  address = address.trim(),
                  city = city.trim(),
                  state = state.trim(),
                  pincode = pincode.trim(),
                  isOnline = isOnline
                )
              )
              Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            }
          },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
          modifier = Modifier.fillMaxWidth().height(44.dp).testTag("save_settings_btn")
        ) {
          Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. Opening Hours Section
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
          Text("Opening Hours", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          TextButton(onClick = {
            // Quick toggle for "Closed Today"
            val todayDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
            val idx = daysList.indexOfFirst { it.day.equals(todayDay, ignoreCase = true) }
            if (idx != -1) {
              daysList[idx] = daysList[idx].copy(isOpen = !daysList[idx].isOpen)
              Toast.makeText(context, "${todayDay} marked ${if (daysList[idx].isOpen) "Open" else "Closed Today"}", Toast.LENGTH_SHORT).show()
            }
          }) {
            Text("Toggle Today", color = ParosaOrange, fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        daysList.forEachIndexed { idx, dayTiming ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.width(90.dp)) {
              Text(dayTiming.day, color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            if (dayTiming.isOpen) {
              Text("${dayTiming.openTime} - ${dayTiming.closeTime}", color = ParosaTextSecondary, fontSize = 12.sp)
            } else {
              Text("Closed Today", color = ParosaRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Switch(
              checked = dayTiming.isOpen,
              onCheckedChange = { openState ->
                daysList[idx] = dayTiming.copy(isOpen = openState)
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = ParosaGreen,
                checkedTrackColor = ParosaGreenContainer,
                uncheckedThumbColor = ParosaRed,
                uncheckedTrackColor = ParosaBorder
              )
            )
          }
          if (idx < daysList.size - 1) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder.copy(alpha = 0.5f)))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 5. Account Settings (Password, Notifications, Language)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Account Settings", color = ParosaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        AccountSettingsRow("Change Password", Icons.Default.Lock) { showChangePasswordDialog = true }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))

        AccountSettingsRow("Notification Settings", Icons.Default.Notifications) { showNotificationSettingsDialog = true }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder))

        AccountSettingsRow("Language: $selectedLanguage", Icons.Default.Language) { showLanguageDialog = true }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 6. Logout and Deactivate
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        OutlinedButton(
          onClick = { showLogoutConfirmDialog = true },
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = ParosaOrange),
          modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
          Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Logout", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
          onClick = { showDeactivateConfirmDialog = true },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Deactivate Restaurant Account", color = ParosaRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun AccountSettingsRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(12.dp))
      Text(title, color = ParosaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
  }
}

@Composable
fun NotificationToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(title, color = ParosaTextPrimary, fontSize = 13.sp)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = ParosaGreen,
        checkedTrackColor = ParosaGreenContainer
      )
    )
  }
}
