package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.database.populateInitialData
import com.example.data.repository.RestaurantRepository
import com.example.ui.AuthState
import com.example.ui.RestaurantViewModel
import com.example.ui.components.SimpleSidebar
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = RestaurantRepository(database)

    CoroutineScope(Dispatchers.IO).launch {
      if (database.restaurantDao().getProfileSync() == null) {
        populateInitialData(database)
      }
    }

    setContent {
      MyApplicationTheme(darkTheme = true) {
        val viewModel = remember { RestaurantViewModel(repository) }
        var appMode by remember { mutableStateOf("PARTNER") }

        when (appMode) {
          "CUSTOMER" -> {
            BackHandler { appMode = "PARTNER" }
            CustomerAppScreen(
              viewModel = viewModel,
              onSwitchMode = { newMode -> appMode = newMode }
            )
          }
          "DELIVERY" -> {
            BackHandler { appMode = "PARTNER" }
            DeliveryPartnerAppScreen(
              viewModel = viewModel,
              onSwitchMode = { newMode -> appMode = newMode }
            )
          }
          "ADMIN" -> {
            BackHandler { appMode = "PARTNER" }
            AdminPanelScreen(
              viewModel = viewModel,
              onSwitchMode = { newMode -> appMode = newMode }
            )
          }
          else -> {
            val authState by viewModel.authState.collectAsStateWithLifecycle()
            when (authState) {
              AuthState.LOGIN -> {
                RestaurantLoginScreen(
                  viewModel = viewModel,
                  onNavigateToRegister = { viewModel.setAuthState(AuthState.REGISTER) },
                  onNavigateToForgot = { viewModel.setAuthState(AuthState.FORGOT_PASSWORD) },
                  onSwitchMode = { newMode -> appMode = newMode }
                )
              }
              AuthState.REGISTER -> {
                BackHandler { viewModel.setAuthState(AuthState.LOGIN) }
                RestaurantRegisterScreen(
                  viewModel = viewModel,
                  onNavigateToLogin = { viewModel.setAuthState(AuthState.LOGIN) }
                )
              }
              AuthState.FORGOT_PASSWORD -> {
                BackHandler { viewModel.setAuthState(AuthState.LOGIN) }
                RestaurantForgotPasswordScreen(
                  viewModel = viewModel,
                  onNavigateToLogin = { viewModel.setAuthState(AuthState.LOGIN) }
                )
              }
              AuthState.APPROVAL_PENDING -> {
                BackHandler { viewModel.logout() }
                RestaurantPendingApprovalScreen(
                  viewModel = viewModel,
                  onLogout = { viewModel.logout() },
                  onSwitchMode = { newMode -> appMode = newMode }
                )
              }
              AuthState.SUSPENDED, AuthState.APPROVAL_REJECTED -> {
                BackHandler { viewModel.logout() }
                RestaurantSuspendedScreen(
                  viewModel = viewModel,
                  onLogout = { viewModel.logout() },
                  onSwitchMode = { newMode -> appMode = newMode }
                )
              }
              AuthState.AUTHENTICATED, AuthState.VERIFY_OTP -> {
                SimplePartnerApp(
                  viewModel = viewModel,
                  onSwitchMode = { newMode -> appMode = newMode }
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplePartnerApp(
  viewModel: RestaurantViewModel,
  onSwitchMode: (String) -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.restaurantProfile.collectAsStateWithLifecycle()
  val restaurantName = profile?.name ?: "The Punjab Kitchen"
  val isOnline = profile?.isOnline ?: true

  var currentTab by remember { mutableStateOf("Dashboard") }
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val configuration = LocalConfiguration.current
  val isWideScreen = configuration.screenWidthDp >= 720

  // Handle Back Button
  if (currentTab != "Dashboard") {
    BackHandler {
      currentTab = "Dashboard"
    }
  }

  // Modal Drawer for mobile/tablet portrait
  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = !isWideScreen,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = ParosaSurface,
        modifier = Modifier.width(260.dp)
      ) {
        SimpleSidebar(
          restaurantName = restaurantName,
          currentTab = currentTab,
          onSelectTab = { selected ->
            currentTab = selected
            scope.launch { drawerState.close() }
          },
          onLogout = {
            scope.launch { drawerState.close() }
            viewModel.logout()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
          },
          onSwitchToCustomerApp = {
            scope.launch { drawerState.close() }
            onSwitchMode("CUSTOMER")
          },
          onSwitchMode = { mode ->
            scope.launch { drawerState.close() }
            onSwitchMode(mode)
          },
          modifier = Modifier.fillMaxHeight()
        )
      }
    }
  ) {
    Scaffold(
      containerColor = ParosaBg,
      topBar = {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                restaurantName,
                color = ParosaTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.width(12.dp))

              // Restaurant status: ONLINE / OFFLINE
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOnline) ParosaGreenContainer else ParosaRedContainer,
                border = BorderStroke(1.dp, if (isOnline) ParosaGreenDark else ParosaRed),
                modifier = Modifier.clickable {
                  val newStatus = !isOnline
                  viewModel.toggleOnlineStatus(newStatus)
                  Toast.makeText(
                    context,
                    if (newStatus) "Restaurant is now ONLINE" else "Restaurant is now OFFLINE",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (isOnline) ParosaGreen else ParosaRed)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = if (isOnline) "ONLINE" else "OFFLINE",
                    color = if (isOnline) ParosaGreen else ParosaRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          },
          navigationIcon = {
            if (!isWideScreen) {
              IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ParosaTextPrimary)
              }
            }
          },
          actions = {
            // Switch to Customer App Pill
            OutlinedButton(
              onClick = { onSwitchMode("CUSTOMER") },
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, ParosaOrange.copy(alpha = 0.7f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp).testTag("top_switch_to_customer_btn")
            ) {
              Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ParosaOrange, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Customer", color = ParosaOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Switch to Delivery Rider Pill
            OutlinedButton(
              onClick = { onSwitchMode("DELIVERY") },
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, ParosaBlue.copy(alpha = 0.7f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp).testTag("top_switch_to_delivery_btn")
            ) {
              Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = ParosaBlue, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Rider", color = ParosaBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Switch to Admin Panel Pill
            OutlinedButton(
              onClick = { onSwitchMode("ADMIN") },
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, ParosaGreen.copy(alpha = 0.7f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp).testTag("top_switch_to_admin_btn")
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ParosaGreen, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Admin", color = ParosaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Profile Icon on top right
            IconButton(
              onClick = {
                currentTab = "Settings"
                Toast.makeText(context, "Logged in as ${profile?.ownerName ?: "Arjun Kumar"}", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.testTag("top_profile_icon")
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(ParosaSurfaceElevated),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Outlined.Person,
                  contentDescription = "Profile",
                  tint = ParosaOrange,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = ParosaSurface)
        )
      },
      bottomBar = {
        if (!isWideScreen) {
          NavigationBar(
            containerColor = ParosaSurface,
            tonalElevation = 4.dp
          ) {
            val navItems = listOf(
              Triple("Dashboard", "Dashboard", Icons.Outlined.Dashboard),
              Triple("Orders", "Orders", Icons.Outlined.ReceiptLong),
              Triple("Menu", "Menu", Icons.Outlined.RestaurantMenu),
              Triple("Earnings", "Earnings", Icons.Outlined.Payments),
              Triple("Reviews", "Reviews", Icons.Outlined.StarOutline),
              Triple("Settings", "Settings", Icons.Outlined.Settings)
            )

            navItems.forEach { (tabId, label, icon) ->
              val isSelected = currentTab == tabId
              NavigationBarItem(
                selected = isSelected,
                onClick = { currentTab = tabId },
                icon = {
                  Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isSelected) ParosaOrange else ParosaTextMuted
                  )
                },
                label = {
                  Text(
                    label,
                    fontSize = 10.sp,
                    color = if (isSelected) ParosaOrange else ParosaTextMuted,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = ParosaOrangeContainer
                )
              )
            }
          }
        }
      }
    ) { padding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .background(ParosaBg)
          .padding(padding)
      ) {
        // Desktop / Wide Screen Permanent Sidebar
        if (isWideScreen) {
          SimpleSidebar(
            restaurantName = restaurantName,
            currentTab = currentTab,
            onSelectTab = { currentTab = it },
            onLogout = {
              viewModel.logout()
              Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            },
            onSwitchToCustomerApp = { onSwitchMode("CUSTOMER") },
            onSwitchMode = onSwitchMode
          )
        }

        // Main Content Area
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          when (currentTab) {
            "Dashboard" -> {
              SimpleDashboardScreen(
                viewModel = viewModel,
                onNavigateToTab = { tab -> currentTab = tab }
              )
            }

            "Orders" -> {
              SimpleOrdersScreen(viewModel = viewModel)
            }

            "Menu" -> {
              SimpleMenuScreen(viewModel = viewModel)
            }

            "Earnings" -> {
              SimpleEarningsScreen(viewModel = viewModel)
            }

            "Reviews" -> {
              SimpleReviewsScreen(viewModel = viewModel)
            }

            "Settings" -> {
              SimpleSettingsScreen(
                viewModel = viewModel,
                onLogout = {
                  viewModel.logout()
                  Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                }
              )
            }

            else -> {
              SimpleDashboardScreen(
                viewModel = viewModel,
                onNavigateToTab = { tab -> currentTab = tab }
              )
            }
          }
        }
      }
    }
  }
}
