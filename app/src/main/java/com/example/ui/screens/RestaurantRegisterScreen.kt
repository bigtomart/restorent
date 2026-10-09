package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun RestaurantRegisterScreen(
  viewModel: RestaurantViewModel,
  onNavigateToLogin: () -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var restaurantName by remember { mutableStateOf("") }
  var mobile by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }

  var address by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("Bengaluru") }
  var state by remember { mutableStateOf("Karnataka") }
  var pincode by remember { mutableStateOf("560038") }
  var cuisine by remember { mutableStateOf("North Indian, Mughlai") }
  var fssai by remember { mutableStateOf("") }

  var localValidationError by remember { mutableStateOf<String?>(null) }

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
        .padding(top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateToLogin,
        modifier = Modifier.testTag("register_back_btn")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Login", tint = ParosaTextPrimary)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text("Restaurant Partner Registration", color = ParosaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    Text(
      text = "Join our network. Your application will be reviewed and approved by the Admin.",
      color = ParosaTextMuted,
      fontSize = 12.sp,
      modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
    )

    // Error Banner
    val displayError = localValidationError ?: errorMessage
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

    // Registration Form Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ParosaSurface),
      border = BorderStroke(1.dp, ParosaBorder)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {

        // Section 1: Owner & Restaurant Information
        Text("OWNER & STORE DETAILS", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Owner Full Name
        Text("Owner Full Name *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_owner_name_input"),
          placeholder = { Text("e.g. Arjun Kumar", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Restaurant Name
        Text("Restaurant Name *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = restaurantName,
          onValueChange = { restaurantName = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_restaurant_name_input"),
          placeholder = { Text("e.g. The Punjab Kitchen", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Store, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mobile Number
        Text("Mobile Number *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = mobile,
          onValueChange = { mobile = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input"),
          placeholder = { Text("e.g. +91 98450 12891", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email Address
        Text("Email Address *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = email,
          onValueChange = { email = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
          placeholder = { Text("e.g. partner@restaurant.com", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Security & Credentials
        Text("SECURITY CREDENTIALS", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Password
        Text("Password (min 6 characters) *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
          placeholder = { Text("Create password", color = ParosaTextMuted, fontSize = 13.sp) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password
        Text("Confirm Password *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input"),
          placeholder = { Text("Repeat password", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.LockClock, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
              Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
            }
          },
          visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Location & Business Compliance
        Text("LOCATION & FSSAI COMPLIANCE", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Address
        Text("Restaurant Address *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = address,
          onValueChange = { address = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_address_input"),
          placeholder = { Text("e.g. 12th Main Road, Indiranagar", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // City, State, Pincode Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text("City *", color = ParosaTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = city,
              onValueChange = { city = it },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("reg_city_input"),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text("Pincode *", color = ParosaTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = pincode,
              onValueChange = { pincode = it },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("reg_pincode_input"),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cuisine
        Text("Cuisine Type *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = cuisine,
          onValueChange = { cuisine = it },
          modifier = Modifier.fillMaxWidth().testTag("reg_cuisine_input"),
          placeholder = { Text("e.g. Biryani, North Indian, Mughlai", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.RestaurantMenu, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // FSSAI License Number
        Text("FSSAI License Number (14 digits) *", color = ParosaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = fssai,
          onValueChange = { fssai = it; localValidationError = null; viewModel.clearAuthMessages() },
          modifier = Modifier.fillMaxWidth().testTag("reg_fssai_input"),
          placeholder = { Text("e.g. 10020011000123", color = ParosaTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp)) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ParosaOrange, unfocusedBorderColor = ParosaBorder, focusedTextColor = ParosaTextPrimary, unfocusedTextColor = ParosaTextPrimary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
          onClick = {
            if (fullName.isBlank() || restaurantName.isBlank() || mobile.isBlank() || email.isBlank() ||
              password.isBlank() || confirmPassword.isBlank() || address.isBlank() || city.isBlank() ||
              pincode.isBlank() || fssai.isBlank()) {
              localValidationError = "Please fill in all required fields."
              return@Button
            }
            if (password != confirmPassword) {
              localValidationError = "Passwords do not match."
              return@Button
            }
            if (password.length < 6) {
              localValidationError = "Password must be at least 6 characters long."
              return@Button
            }
            localValidationError = null
            viewModel.registerRestaurant(
              fullName = fullName,
              restaurantName = restaurantName,
              mobile = mobile,
              email = email,
              password = password,
              confirmPassword = confirmPassword,
              address = address,
              city = city,
              state = state,
              pincode = pincode,
              cuisine = cuisine,
              fssai = fssai
            )
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("reg_submit_btn"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = ParosaBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.AppRegistration, contentDescription = null, tint = ParosaBg, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit Registration Application", color = ParosaBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Already have account link
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Already have a partner account? ", color = ParosaTextMuted, fontSize = 13.sp)
      Text(
        text = "Sign In",
        color = ParosaOrange,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .clickable { onNavigateToLogin() }
          .testTag("navigate_back_to_login_btn")
      )
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}
