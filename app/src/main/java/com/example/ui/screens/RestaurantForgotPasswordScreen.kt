package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun RestaurantForgotPasswordScreen(
  viewModel: RestaurantViewModel,
  onNavigateToLogin: () -> Unit
) {
  var identifier by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var localError by remember { mutableStateOf<String?>(null) }
  val errorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
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
    // Top Bar with Back Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateToLogin,
        modifier = Modifier.testTag("forgot_back_btn")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ParosaTextPrimary)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text("Reset Password", color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    Text(
      text = "Enter your registered partner email or mobile number to set a new password securely.",
      color = ParosaTextMuted,
      fontSize = 12.sp,
      modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
    )

    val displayError = localError ?: errorMessage
    AnimatedVisibility(visible = displayError != null) {
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
          Text(displayError ?: "", color = ParosaTextPrimary, fontSize = 12.sp)
        }
      }
    }

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {

        Text("Registered Email or Mobile Number *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = identifier,
          onValueChange = { identifier = it; localError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("forgot_identifier_input"),
          placeholder = { Text("e.g. punjabkitchen.blr@gmail.com", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("New Password (min 6 characters) *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = newPassword,
          onValueChange = { newPassword = it; localError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("forgot_new_password_input"),
          placeholder = { Text("Enter new password", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Confirm New Password *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it; localError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("forgot_confirm_password_input"),
          placeholder = { Text("Re-enter new password", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.LockClock, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            if (identifier.isBlank() || newPassword.isBlank()) {
              localError = "Please enter all fields."
              return@Button
            }
            if (newPassword.length < 6) {
              localError = "Password must be at least 6 characters long."
              return@Button
            }
            if (newPassword != confirmPassword) {
              localError = "Passwords do not match."
              return@Button
            }
            localError = null
            viewModel.resetPassword(identifier, newPassword, confirmPassword)
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("forgot_submit_btn"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = ParosaBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.LockReset, contentDescription = null, tint = ParosaBg, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update Password & Return to Login", color = ParosaBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    TextButton(onClick = onNavigateToLogin) {
      Text("Back to Login", color = ParosaOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
  }
}
