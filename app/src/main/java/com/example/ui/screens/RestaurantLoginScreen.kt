package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuthState
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun RestaurantLoginScreen(
  viewModel: RestaurantViewModel,
  onNavigateToRegister: () -> Unit,
  onNavigateToForgot: () -> Unit,
  onSwitchMode: (String) -> Unit
) {
  var identifier by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var rememberMe by remember { mutableStateOf(true) }

  val errorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
  val successMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
  val isLoading by viewModel.authIsLoading.collectAsStateWithLifecycle()

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .verticalScroll(scrollState)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar Mode Switchers
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ParosaOrangeContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.SoupKitchen, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("PAROSA", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }

      // Quick Switchers Pill
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = { onSwitchMode("CUSTOMER") },
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.6f)),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(26.dp)
        ) {
          Text("Customer", color = ParosaOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
          onClick = { onSwitchMode("ADMIN") },
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.6f)),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(26.dp)
        ) {
          Text("Admin", color = ParosaGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Header Title
    Text(
      text = "Partner Portal Login",
      color = ParosaTextPrimary,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Manage kitchen orders, live menu & store operations",
      color = ParosaTextMuted,
      fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Error / Success Message Banners
    AnimatedVisibility(visible = errorMessage != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaRedContainer),
        border = BorderStroke(1.dp, ParosaRed.copy(alpha = 0.6f))
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ParosaRed, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(errorMessage ?: "", color = ParosaTextPrimary, fontSize = 12.sp)
        }
      }
    }

    AnimatedVisibility(visible = successMessage != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ParosaGreenContainer),
        border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.6f))
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = ParosaGreen, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(successMessage ?: "", color = ParosaTextPrimary, fontSize = 12.sp)
        }
      }
    }

    // Login Form Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {

        // Identifier Input
        Text("Email or Mobile Number", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = identifier,
          onValueChange = {
            identifier = it
            viewModel.clearAuthMessages()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_identifier_input"),
          placeholder = { Text("e.g. punjabkitchen.blr@gmail.com", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder,
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        Text("Password", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            viewModel.clearAuthMessages()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input"),
          placeholder = { Text("Enter your password", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = ParosaTextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder,
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Remember Me & Forgot Password
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { rememberMe = !rememberMe }
          ) {
            Checkbox(
              checked = rememberMe,
              onCheckedChange = { rememberMe = it },
              colors = CheckboxDefaults.colors(
                checkedColor = ParosaOrange,
                checkmarkColor = ParosaBg
              ),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Remember me", color = ParosaTextSecondary, fontSize = 12.sp)
          }

          Text(
            text = "Forgot Password?",
            color = ParosaOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .clickable { onNavigateToForgot() }
              .testTag("forgot_password_btn")
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Login Button
        Button(
          onClick = {
            viewModel.loginRestaurant(identifier, password, rememberMe)
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("login_submit_btn"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = ParosaBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.Login, contentDescription = null, tint = ParosaBg, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login to Dashboard", color = ParosaBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Register Navigation Link
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("New restaurant partner? ", color = ParosaTextMuted, fontSize = 13.sp)
      Text(
        text = "Register Restaurant",
        color = ParosaOrange,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .clickable { onNavigateToRegister() }
          .testTag("navigate_to_register_btn")
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Quick Test Credentials Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurfaceElevated),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Key, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Quick Test Credentials", color = ParosaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tap any credential below to auto-fill and test real authentication states:", color = ParosaTextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Approved Partner Chip
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaGreenContainer,
          border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              identifier = "punjabkitchen.blr@gmail.com"
              password = "Partner@123"
              viewModel.clearAuthMessages()
            }
            .padding(bottom = 6.dp)
            .testTag("chip_approved_partner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("✅ The Punjab Kitchen (APPROVED)", color = ParosaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text("punjabkitchen.blr@gmail.com • Partner@123", color = ParosaTextSecondary, fontSize = 10.sp)
            }
            Text("Tap to fill", color = ParosaGreen, fontSize = 10.sp)
          }
        }

        // Pending Approval Partner Chip
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaOrangeContainer,
          border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              identifier = "spicevilla.pending@gmail.com"
              password = "Partner@123"
              viewModel.clearAuthMessages()
            }
            .padding(bottom = 6.dp)
            .testTag("chip_pending_partner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("⏳ Spice Villa (PENDING APPROVAL)", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text("spicevilla.pending@gmail.com • Partner@123", color = ParosaTextSecondary, fontSize = 10.sp)
            }
            Text("Tap to fill", color = ParosaOrange, fontSize = 10.sp)
          }
        }

        // Suspended Partner Chip
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaRedContainer,
          border = BorderStroke(1.dp, ParosaRed.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              identifier = "curryhouse.suspended@gmail.com"
              password = "Partner@123"
              viewModel.clearAuthMessages()
            }
            .testTag("chip_suspended_partner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("⛔ Curry House (SUSPENDED)", color = ParosaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text("curryhouse.suspended@gmail.com • Partner@123", color = ParosaTextSecondary, fontSize = 10.sp)
            }
            Text("Tap to fill", color = ParosaRed, fontSize = 10.sp)
          }
        }
      }
    }
  }
}
