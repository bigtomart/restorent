package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CategoryEntity
import com.example.data.entity.MenuItemEntity
import com.example.ui.RestaurantViewModel
import com.example.ui.theme.*

@Composable
fun VegNonVegIcon(isVeg: Boolean) {
  Box(
    modifier = Modifier
      .size(14.dp)
      .border(1.dp, if (isVeg) VegGreen else NonVegRed, RoundedCornerShape(3.dp))
      .padding(2.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(5.dp)
        .clip(CircleShape)
        .background(if (isVeg) VegGreen else NonVegRed)
    )
  }
}

@Composable
fun SimpleMenuScreen(
  viewModel: RestaurantViewModel
) {
  val context = LocalContext.current
  val categories by viewModel.categories.collectAsStateWithLifecycle()
  val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()

  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  var showAddDialog by remember { mutableStateOf(false) }
  var itemToEdit by remember { mutableStateOf<MenuItemEntity?>(null) }
  var itemToDelete by remember { mutableStateOf<MenuItemEntity?>(null) }

  // Category management dialogs
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var newCategoryName by remember { mutableStateOf("") }
  var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
  var editCategoryName by remember { mutableStateOf("") }
  var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

  // Add Category Dialog
  if (showAddCategoryDialog) {
    Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Add New Category", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = newCategoryName,
            onValueChange = { newCategoryName = it },
            label = { Text("Category Name", color = ParosaTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showAddCategoryDialog = false }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (newCategoryName.isNotBlank()) {
                  viewModel.addCategory(newCategoryName.trim())
                  Toast.makeText(context, "Category '$newCategoryName' added", Toast.LENGTH_SHORT).show()
                  newCategoryName = ""
                  showAddCategoryDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
            ) {
              Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Rename Category Dialog
  categoryToEdit?.let { cat ->
    Dialog(onDismissRequest = { categoryToEdit = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Rename Category", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editCategoryName,
            onValueChange = { editCategoryName = it },
            label = { Text("Category Name", color = ParosaTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { categoryToEdit = null }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (editCategoryName.isNotBlank()) {
                  viewModel.renameCategory(cat, editCategoryName.trim())
                  Toast.makeText(context, "Category renamed to '$editCategoryName'", Toast.LENGTH_SHORT).show()
                  categoryToEdit = null
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
            ) {
              Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Delete Category Dialog (Checks if contains items)
  categoryToDelete?.let { cat ->
    val itemsInCat = remember(cat, menuItems) { menuItems.count { it.categoryId == cat.id || it.categoryName == cat.name } }
    Dialog(onDismissRequest = { categoryToDelete = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Delete Category '${cat.name}'?", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          if (itemsInCat > 0) {
            Text(
              "Cannot delete category: contains $itemsInCat food item(s). Please delete or reassign food items first.",
              color = ParosaRed,
              fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              Button(
                onClick = { categoryToDelete = null },
                colors = ButtonDefaults.buttonColors(containerColor = ParosaSurfaceElevated)
              ) {
                Text("OK", color = ParosaTextPrimary)
              }
            }
          } else {
            Text("Are you sure you want to delete this empty category?", color = ParosaTextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              TextButton(onClick = { categoryToDelete = null }) {
                Text("Cancel", color = ParosaTextSecondary)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  viewModel.deleteCategory(cat)
                  Toast.makeText(context, "Category '${cat.name}' deleted", Toast.LENGTH_SHORT).show()
                  if (selectedCategory == cat.name) selectedCategory = null
                  categoryToDelete = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = ParosaRed)
              ) {
                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  // Delete Food Item Confirmation Dialog
  itemToDelete?.let { item ->
    Dialog(onDismissRequest = { itemToDelete = null }) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ParosaSurface,
        border = BorderStroke(1.dp, ParosaBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Delete this food item?", color = ParosaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Are you sure you want to remove '${item.name}' from your menu?", color = ParosaTextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(16.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { itemToDelete = null }) {
              Text("Cancel", color = ParosaTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                viewModel.deleteMenuItem(item)
                Toast.makeText(context, "Item '${item.name}' deleted.", Toast.LENGTH_SHORT).show()
                itemToDelete = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = ParosaRed)
            ) {
              Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Add / Edit Food Item Dialog
  if (showAddDialog || itemToEdit != null) {
    SimpleAddFoodDialog(
      item = itemToEdit,
      categories = categories.map { it.name }.ifEmpty { listOf("Starters", "Main Course", "Biryani", "Breads", "Beverages", "Desserts") },
      onDismiss = {
        showAddDialog = false
        itemToEdit = null
      },
      onSave = { name, category, price, discountPrice, desc, isVeg, prepTime, isAvailable ->
        if (itemToEdit != null) {
          viewModel.updateMenuItem(
            itemToEdit!!.copy(
              name = name,
              categoryName = category,
              price = price,
              discountedPrice = discountPrice,
              description = desc,
              isVeg = isVeg,
              preparationTimeMinutes = prepTime,
              isAvailable = isAvailable,
              isSoldOut = !isAvailable
            )
          )
          Toast.makeText(context, "Food item updated successfully.", Toast.LENGTH_SHORT).show()
        } else {
          val catEntity = categories.find { it.name == category } ?: categories.firstOrNull()
          val catId = catEntity?.id ?: "cat_1"
          viewModel.addMenuItem(name, catId, category, price, desc, isVeg, prepTime, 1)
          Toast.makeText(context, "Food item added successfully.", Toast.LENGTH_SHORT).show()
        }
        showAddDialog = false
        itemToEdit = null
      }
    )
  }

  // Filter by category and search query
  val filteredItems = remember(menuItems, selectedCategory, searchQuery) {
    var list = menuItems
    if (selectedCategory != null) {
      list = list.filter { it.categoryName.equals(selectedCategory, ignoreCase = true) }
    }
    if (searchQuery.isNotBlank()) {
      list = list.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
    list
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ParosaBg)
      .padding(16.dp)
  ) {
    // Top Section: Heading & + Add Food Item button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Menu",
        color = ParosaTextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )

      Button(
        onClick = {
          itemToEdit = null
          showAddDialog = true
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange),
        modifier = Modifier.testTag("add_food_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("+ Add Food Item", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search food items...", color = ParosaTextMuted, fontSize = 13.sp) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear", tint = ParosaTextSecondary, modifier = Modifier.size(16.dp))
          }
        }
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ParosaTextPrimary,
        unfocusedTextColor = ParosaTextPrimary,
        focusedBorderColor = ParosaOrange,
        unfocusedBorderColor = ParosaBorder
      ),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.fillMaxWidth().height(50.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Categories chip row with Add Category option
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      item {
        val isAll = selectedCategory == null
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isAll) ParosaOrangeContainer else ParosaSurface,
          border = BorderStroke(1.dp, if (isAll) ParosaOrange else ParosaBorder),
          modifier = Modifier.clickable { selectedCategory = null }
        ) {
          Text(
            text = "All",
            color = if (isAll) ParosaOrange else ParosaTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          )
        }
      }

      items(categories) { cat ->
        val isSelected = selectedCategory == cat.name
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSelected) ParosaOrangeContainer else ParosaSurface,
          border = BorderStroke(1.dp, if (isSelected) ParosaOrange else ParosaBorder),
          modifier = Modifier.clickable { selectedCategory = cat.name }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = cat.name,
              color = if (isSelected) ParosaOrange else ParosaTextSecondary,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            // Options to edit or delete category
            if (isSelected) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                Icons.Default.Edit,
                contentDescription = "Rename",
                tint = ParosaOrange,
                modifier = Modifier
                  .size(13.dp)
                  .clickable {
                    categoryToEdit = cat
                    editCategoryName = cat.name
                  }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = ParosaRed,
                modifier = Modifier
                  .size(13.dp)
                  .clickable {
                    categoryToDelete = cat
                  }
              )
            }
          }
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ParosaSurfaceElevated,
          border = BorderStroke(1.dp, ParosaBorder),
          modifier = Modifier.clickable { showAddCategoryDialog = true }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = ParosaTextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Category", color = ParosaTextSecondary, fontSize = 12.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Food Items List
    if (filteredItems.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          if (searchQuery.isNotBlank()) "No items matching '$searchQuery'" else "No items in this category",
          color = ParosaTextMuted,
          fontSize = 14.sp
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredItems, key = { it.id }) { item ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ParosaSurface),
            border = BorderStroke(1.dp, ParosaBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.Top,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(ParosaSurfaceElevated),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      Icons.Default.Restaurant,
                      contentDescription = null,
                      tint = if (!item.isSoldOut) ParosaOrange else ParosaTextMuted,
                      modifier = Modifier.size(22.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      VegNonVegIcon(item.isVeg)
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(item.name, color = ParosaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    if (item.description.isNotBlank()) {
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(item.description, color = ParosaTextSecondary, fontSize = 11.sp, maxLines = 1)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text("₹${item.price.toInt()}", color = ParosaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                      if (item.discountedPrice > 0 && item.discountedPrice < item.price) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("₹${item.discountedPrice.toInt()}", color = ParosaTextMuted, fontSize = 11.sp)
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("• ${item.categoryName}", color = ParosaTextMuted, fontSize = 11.sp)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ParosaBorder.copy(alpha = 0.5f)))
              Spacer(modifier = Modifier.height(8.dp))

              // Availability Toggle and Actions (Edit, Delete)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Availability Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Switch(
                    checked = !item.isSoldOut,
                    onCheckedChange = { isAvailable ->
                      viewModel.toggleItemSoldOut(item.id, !isAvailable)
                      Toast.makeText(
                        context,
                        if (isAvailable) "${item.name} is now Available" else "${item.name} marked Sold Out",
                        Toast.LENGTH_SHORT
                      ).show()
                    },
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = ParosaGreen,
                      checkedTrackColor = ParosaGreenContainer,
                      uncheckedThumbColor = ParosaRed,
                      uncheckedTrackColor = ParosaBorder
                    ),
                    modifier = Modifier.testTag("toggle_item_${item.id}")
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (!item.isSoldOut) "Available" else "Sold Out",
                    color = if (!item.isSoldOut) ParosaGreen else ParosaRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                // Edit and Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                  OutlinedButton(
                    onClick = { itemToEdit = item },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParosaBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ParosaTextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = ParosaTextSecondary, fontSize = 11.sp)
                  }

                  Spacer(modifier = Modifier.width(8.dp))

                  OutlinedButton(
                    onClick = { itemToDelete = item },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParosaRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                  ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ParosaRed, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = ParosaRed, fontSize = 11.sp)
                  }
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
fun SimpleAddFoodDialog(
  item: MenuItemEntity?,
  categories: List<String>,
  onDismiss: () -> Unit,
  onSave: (name: String, category: String, price: Double, discountPrice: Double, desc: String, isVeg: Boolean, prepTime: Int, isAvailable: Boolean) -> Unit
) {
  var name by remember { mutableStateOf(item?.name ?: "") }
  var category by remember { mutableStateOf(item?.categoryName ?: categories.firstOrNull() ?: "Starters") }
  var priceStr by remember { mutableStateOf(item?.price?.toInt()?.toString() ?: "240") }
  var discountPriceStr by remember { mutableStateOf(if ((item?.discountedPrice ?: 0.0) > 0) item?.discountedPrice?.toInt().toString() else "") }
  var desc by remember { mutableStateOf(item?.description ?: "") }
  var isVeg by remember { mutableStateOf(item?.isVeg ?: true) }
  var prepTimeStr by remember { mutableStateOf(item?.preparationTimeMinutes?.toString() ?: "15") }
  var isAvailable by remember { mutableStateOf(item?.let { !it.isSoldOut } ?: true) }

  var validationError by remember { mutableStateOf("") }
  var categoryDropdownExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = ParosaSurface,
      border = BorderStroke(1.dp, ParosaBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(androidx.compose.foundation.rememberScrollState())
      ) {
        Text(
          text = if (item != null) "Edit Food Item" else "Add Food Item",
          color = ParosaTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Food Name
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            validationError = ""
          },
          label = { Text("Food Name *", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth().testTag("food_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category", color = ParosaTextSecondary) },
            trailingIcon = {
              IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ParosaOrange)
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.fillMaxWidth().clickable { categoryDropdownExpanded = true }
          )

          DropdownMenu(
            expanded = categoryDropdownExpanded,
            onDismissRequest = { categoryDropdownExpanded = false },
            modifier = Modifier.background(ParosaSurface)
          ) {
            categories.forEach { catName ->
              DropdownMenuItem(
                text = { Text(catName, color = ParosaTextPrimary) },
                onClick = {
                  category = catName
                  categoryDropdownExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price and Optional Discount Price
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = priceStr,
            onValueChange = {
              priceStr = it
              validationError = ""
            },
            label = { Text("Price (₹) *", color = ParosaTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ParosaTextPrimary,
              unfocusedTextColor = ParosaTextPrimary,
              focusedBorderColor = ParosaOrange,
              unfocusedBorderColor = ParosaBorder
            ),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = discountPriceStr,
            onValueChange = { discountPriceStr = it },
            label = { Text("Discounted (₹)", color = ParosaTextSecondary) },
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

        Spacer(modifier = Modifier.height(10.dp))

        // Veg / Non-Veg toggle & Prep Time
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(ParosaSurfaceElevated)
              .clickable { isVeg = !isVeg }
              .padding(8.dp)
          ) {
            VegNonVegIcon(isVeg)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isVeg) "Veg" else "Non-Veg", color = ParosaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedTextField(
            value = prepTimeStr,
            onValueChange = { prepTimeStr = it },
            label = { Text("Prep Time (min)", color = ParosaTextSecondary, fontSize = 11.sp) },
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

        Spacer(modifier = Modifier.height(10.dp))

        // Description
        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description", color = ParosaTextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ParosaTextPrimary,
            unfocusedTextColor = ParosaTextPrimary,
            focusedBorderColor = ParosaOrange,
            unfocusedBorderColor = ParosaBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Availability Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Available for ordering", color = ParosaTextPrimary, fontSize = 13.sp)
          Switch(
            checked = isAvailable,
            onCheckedChange = { isAvailable = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = ParosaGreen,
              checkedTrackColor = ParosaGreenContainer
            )
          )
        }

        if (validationError.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(validationError, color = ParosaRed, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text("Cancel", color = ParosaTextSecondary) }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (name.isBlank()) {
                validationError = "Food name is required."
                return@Button
              }
              val p = priceStr.toDoubleOrNull()
              if (p == null || p <= 0) {
                validationError = "Valid price is required."
                return@Button
              }
              val dp = discountPriceStr.toDoubleOrNull() ?: 0.0
              val pt = prepTimeStr.toIntOrNull() ?: 15
              onSave(name.trim(), category, p, dp, desc.trim(), isVeg, pt, isAvailable)
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ParosaOrange)
          ) {
            Text("Save Food", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
