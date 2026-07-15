package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MarbleProduct
import com.example.data.SlabDimension
import com.example.ui.MarbleViewModel
import com.example.ui.components.MarbleTexture
import com.example.ui.components.QrCodeDrawer
import com.example.ui.theme.*

enum class AppScreen {
    EXPLORER,
    DETAILS,
    OWNER_DASHBOARD,
    ADD_EDIT_PRODUCT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowroomApp(
    viewModel: MarbleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.EXPLORER) }
    var selectedProduct by remember { mutableStateOf<MarbleProduct?>(null) }
    var isEditMode by remember { mutableStateOf(false) }

    // Observers
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val isOwnerMode by viewModel.isOwnerMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val logs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteProducts.collectAsStateWithLifecycle()

    // Filters categories
    val categories by viewModel.categories.collectAsStateWithLifecycle(initialValue = listOf("All"))
    val colors by viewModel.colors.collectAsStateWithLifecycle(initialValue = listOf("All"))
    val finishes by viewModel.finishes.collectAsStateWithLifecycle(initialValue = listOf("All"))
    val thicknesses by viewModel.thicknesses.collectAsStateWithLifecycle(initialValue = listOf("All"))
    val origins by viewModel.origins.collectAsStateWithLifecycle(initialValue = listOf("All"))

    // Active filters
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedColor by viewModel.selectedColor.collectAsStateWithLifecycle()
    val selectedFinish by viewModel.selectedFinish.collectAsStateWithLifecycle()
    val selectedThickness by viewModel.selectedThickness.collectAsStateWithLifecycle()
    val selectedOrigin by viewModel.selectedOrigin.collectAsStateWithLifecycle()
    val onlyInStock by viewModel.onlyInStock.collectAsStateWithLifecycle()

    // Dialog flags
    var showPinDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showCompanyInfoDialog by remember { mutableStateOf(false) }

    // Intercept back actions
    BackHandler(enabled = currentScreen != AppScreen.EXPLORER) {
        when (currentScreen) {
            AppScreen.DETAILS -> currentScreen = AppScreen.EXPLORER
            AppScreen.OWNER_DASHBOARD -> currentScreen = AppScreen.EXPLORER
            AppScreen.ADD_EDIT_PRODUCT -> {
                currentScreen = if (isEditMode) AppScreen.DETAILS else AppScreen.EXPLORER
                isEditMode = false
            }
            else -> currentScreen = AppScreen.EXPLORER
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LuxuryBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MOONSTONES",
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Serif
                        )
                        Box(
                            modifier = Modifier
                                .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IMPERIAL",
                                color = GoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentScreen != AppScreen.EXPLORER) {
                        IconButton(
                            onClick = {
                                when (currentScreen) {
                                    AppScreen.DETAILS -> currentScreen = AppScreen.EXPLORER
                                    AppScreen.OWNER_DASHBOARD -> currentScreen = AppScreen.EXPLORER
                                    AppScreen.ADD_EDIT_PRODUCT -> {
                                        currentScreen = if (isEditMode) AppScreen.DETAILS else AppScreen.EXPLORER
                                        isEditMode = false
                                    }
                                    else -> currentScreen = AppScreen.EXPLORER
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Gold
                            )
                        }
                    } else {
                        IconButton(onClick = { showCompanyInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Company Info",
                                tint = Gold
                            )
                        }
                    }
                },
                actions = {
                    // Mode selector pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isOwnerMode) SuccessGreen.copy(alpha = 0.15f)
                                else Gold.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (isOwnerMode) SuccessGreen.copy(alpha = 0.5f)
                                else Gold.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                if (isOwnerMode) {
                                    viewModel.logoutOwner()
                                    Toast
                                        .makeText(
                                            context,
                                            "Switched to Read-Only Guest Mode",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                } else {
                                    showPinDialog = true
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOwnerMode) SuccessGreen else Gold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOwnerMode) "OWNER MODE" else "GUEST VIEW",
                            color = if (isOwnerMode) SuccessGreen else MarbleWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isOwnerMode && currentScreen == AppScreen.EXPLORER) {
                        IconButton(
                            onClick = { currentScreen = AppScreen.OWNER_DASHBOARD },
                            modifier = Modifier.testTag("dashboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Owner Dashboard",
                                tint = Gold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuxuryDarkGray,
                    titleContentColor = Gold,
                    actionIconContentColor = Gold
                )
            )
        },
        bottomBar = {
            if (currentScreen == AppScreen.EXPLORER) {
                NavigationBar(
                    containerColor = LuxuryDarkGray,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Showroom", tint = Gold) },
                        label = { Text("Showroom", color = Gold, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Gold.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { showCompanyInfoDialog = true },
                        icon = { Icon(Icons.Default.ContactPhone, contentDescription = "Contact", tint = TextGray) },
                        label = { Text("Contact", color = TextGray) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (isOwnerMode && currentScreen == AppScreen.EXPLORER) {
                FloatingActionButton(
                    onClick = {
                        isEditMode = false
                        selectedProduct = null
                        currentScreen = AppScreen.ADD_EDIT_PRODUCT
                    },
                    containerColor = Gold,
                    contentColor = LuxuryBlack,
                    modifier = Modifier.testTag("add_product_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Stone")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LuxuryBlack)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.EXPLORER -> {
                        ExplorerScreen(
                            products = products,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.updateSearchQuery(it) },
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelect = { viewModel.selectCategory(it) },
                            onProductClick = { p ->
                                selectedProduct = p
                                currentScreen = AppScreen.DETAILS
                            },
                            onFilterClick = { showFilterSheet = true },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) }
                        )
                    }

                    AppScreen.DETAILS -> {
                        selectedProduct?.let { product ->
                            // Always fetch latest product updates
                            val latestProduct = products.find { it.id == product.id } ?: product
                            DetailsScreen(
                                product = latestProduct,
                                isOwner = isOwnerMode,
                                onBack = { currentScreen = AppScreen.EXPLORER },
                                onEdit = {
                                    isEditMode = true
                                    currentScreen = AppScreen.ADD_EDIT_PRODUCT
                                },
                                onDelete = {
                                    viewModel.deleteProduct(latestProduct)
                                    currentScreen = AppScreen.EXPLORER
                                },
                                onFavoriteToggle = { viewModel.toggleFavorite(latestProduct) }
                            )
                        }
                    }

                    AppScreen.OWNER_DASHBOARD -> {
                        OwnerDashboardScreen(
                            products = products,
                            logs = logs,
                            notifications = notifications,
                            onBack = { currentScreen = AppScreen.EXPLORER }
                        )
                    }

                    AppScreen.ADD_EDIT_PRODUCT -> {
                        AddEditProductScreen(
                            product = if (isEditMode) selectedProduct else null,
                            onSave = { updatedProduct ->
                                if (isEditMode) {
                                    viewModel.updateProduct(updatedProduct)
                                } else {
                                    viewModel.insertProduct(updatedProduct)
                                }
                                currentScreen = AppScreen.EXPLORER
                                isEditMode = false
                            },
                            onCancel = {
                                currentScreen = if (isEditMode) AppScreen.DETAILS else AppScreen.EXPLORER
                                isEditMode = false
                            }
                        )
                    }
                }
            }
        }

        // 1. PIN LOCK DIALOG
        if (showPinDialog) {
            OwnerPinDialog(
                onDismiss = { showPinDialog = false },
                onVerify = { pin ->
                    val success = viewModel.verifyOwnerPin(pin)
                    if (success) {
                        showPinDialog = false
                        Toast.makeText(context, "Welcome Owner! Full control unlocked.", Toast.LENGTH_SHORT).show()
                    }
                },
                errorMsg = viewModel.ownerPinError.collectAsStateWithLifecycle().value
            )
        }

        // 2. DETAILED SEARCH FILTERS BOTTOM DIALOG
        if (showFilterSheet) {
            FilterDialog(
                colors = colors,
                finishes = finishes,
                thicknesses = thicknesses,
                origins = origins,
                selectedColor = selectedColor,
                selectedFinish = selectedFinish,
                selectedThickness = selectedThickness,
                selectedOrigin = selectedOrigin,
                onlyInStock = onlyInStock,
                onColorSelect = { viewModel.selectColor(it) },
                onFinishSelect = { viewModel.selectFinish(it) },
                onThicknessSelect = { viewModel.selectThickness(it) },
                onOriginSelect = { viewModel.selectOrigin(it) },
                onStockToggle = { viewModel.toggleOnlyInStock(it) },
                onDismiss = { showFilterSheet = false }
            )
        }

        // 3. COMPANY INFORMATION DIALOG
        if (showCompanyInfoDialog) {
            CompanyInfoDialog(onDismiss = { showCompanyInfoDialog = false })
        }
    }
}

// ================= SHOWROOM EXPLORER VIEW =================
@Composable
fun ExplorerScreen(
    products: List<MarbleProduct>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onProductClick: (MarbleProduct) -> Unit,
    onFilterClick: () -> Unit,
    onFavoriteToggle: (MarbleProduct) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Branding visual header card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Brush.horizontalGradient(listOf(Gold, Color.Transparent)), RoundedCornerShape(12.dp))
                .background(Brush.verticalGradient(listOf(LuxuryDarkGray, LuxuryBlack)))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "MOONSTONES SHOWROOM",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Kishangarh, Rajasthan • Exquisite Marbles & Stones",
                    color = TextGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search and filter button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by name, SKU, type...", color = TextGray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Gold) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MarbleWhite,
                    unfocusedTextColor = MarbleWhite,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = LuxuryLightGray,
                    focusedContainerColor = LuxuryDarkGray,
                    unfocusedContainerColor = LuxuryDarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LuxuryDarkGray)
                    .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("filter_icon")
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Advanced Filters",
                    tint = Gold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Categories chips
        Text(text = "Categories", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Gold else LuxuryDarkGray)
                        .border(1.dp, if (isSelected) GoldLight else LuxuryLightGray, RoundedCornerShape(16.dp))
                        .clickable { onCategorySelect(cat) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) LuxuryBlack else MarbleWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Products Grid
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "No Marbles Found",
                        tint = Gold.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Stones Match Your Filters",
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing your filters or searching another keyword.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("products_grid")
            ) {
                items(products) { product ->
                    MarbleItemCard(
                        product = product,
                        onClick = { onProductClick(product) },
                        onFavoriteClick = { onFavoriteToggle(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarbleItemCard(
    product: MarbleProduct,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable(onClick = onClick)
            .testTag("marble_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.25f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Procedural texture drawing placeholder with custom canvas
                MarbleTexture(
                    marbleColor = product.color,
                    category = product.category,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Description Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuxuryMediumGray)
                        .padding(8.dp)
                ) {
                    Text(
                        text = product.name,
                        color = MarbleWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category,
                            color = Gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (product.availableSlabs > 0) SuccessGreen.copy(alpha = 0.15f)
                                    else ErrorRed.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (product.availableSlabs > 0) "${product.availableSlabs} Slabs Left" else "OUT OF STOCK",
                                color = if (product.availableSlabs > 0) SuccessGreen else ErrorRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Thk: ${product.thickness}",
                            color = TextGray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = product.origin,
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Floating Favorite Heart toggle
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(32.dp)
                    .background(LuxuryBlack.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Toggle",
                    tint = if (product.isFavorite) ErrorRed else Gold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ================= PRODUCT DETAILS SCREEN =================
@Composable
fun DetailsScreen(
    product: MarbleProduct,
    isOwner: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    var showFullQR by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val slabs = remember(product.slabsJson) { product.getSlabDimensionsList() }
    val totalAreaSqFt = remember(slabs) { product.calculateTotalAreaSqFt() }
    val totalAreaSqM = remember(slabs) { product.calculateTotalAreaSqM() }
    val averageSize = remember(slabs) { product.calculateAverageSizeString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Giant beautiful header procedural texture card with Zoom instruction
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            MarbleTexture(
                marbleColor = product.color,
                category = product.category,
                modifier = Modifier.fillMaxSize()
            )

            // Overlap header details with glassmorphism
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, LuxuryBlack.copy(alpha = 0.85f), LuxuryBlack)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.name,
                            color = MarbleWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )

                        IconButton(onClick = onFavoriteToggle) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (product.isFavorite) ErrorRed else Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = "Collection: ${product.collectionName}",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Action Panel for Owners
            if (isOwner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = LuxuryBlack)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Stone", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Info Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBadge(label = "Category", value = product.category)
                InfoBadge(label = "Type", value = product.marbleType)
                InfoBadge(label = "Finish", value = product.finish)
                InfoBadge(label = "Thickness", value = product.thickness)
                InfoBadge(label = "Origin", value = product.origin)
            }

            // QR code & SKU / Price block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuxuryDarkGray)
                    .border(0.5.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("IDENTIFICATION", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("SKU: ${product.skuNumber}", color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Batch: ${product.batchNumber}", color = TextGray, fontSize = 12.sp)
                    Text("Lot: ${product.lotNumber}", color = TextGray, fontSize = 12.sp)
                    if (product.price > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Est. Price: ₹${product.price}/sq.ft", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showFullQR = true }
                ) {
                    QrCodeDrawer(sku = product.skuNumber, qrSize = 90.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap to Zoom", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inventory Slabs section
            Text("INVENTORY & SIZES", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuxuryDarkGray)
                    .border(0.5.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                // Stock indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StockMetric(label = "Total Slabs", count = product.totalSlabs, color = MarbleWhite)
                    StockMetric(label = "Available", count = product.availableSlabs, color = SuccessGreen)
                    StockMetric(label = "Reserved", count = product.reservedSlabs, color = ReservedOrange)
                    StockMetric(label = "Sold", count = product.soldSlabs, color = ErrorRed)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = LuxuryLightGray)

                // Advanced metrics calculated
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Average Size", color = TextGray, fontSize = 10.sp)
                        Text(averageSize, color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Area (sq.ft)", color = TextGray, fontSize = 10.sp)
                        Text(String.format("%.1f", totalAreaSqFt), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Area (sq.m)", color = TextGray, fontSize = 10.sp)
                        Text(String.format("%.1f", totalAreaSqM), color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = LuxuryLightGray)

                // Individual slabs dimension table
                Text("Individual Slabs (L x W in inches):", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                if (slabs.isEmpty()) {
                    Text("No individual slab dimensions recorded.", color = TextGray, fontSize = 12.sp)
                } else {
                    slabs.forEachIndexed { index, slab ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Slab #${index + 1}", color = MarbleWhite, fontSize = 12.sp)
                            Text(
                                text = "${slab.length}\" × ${slab.width}\" (${String.format("%.1f", slab.areaSqFt)} sq.ft)",
                                color = TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extra Warehouse locations
            Text("LOGISTICS & LOCATION", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuxuryDarkGray)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Block Quantity", color = TextGray, fontSize = 10.sp)
                    Text("${product.blockQuantity} Blocks", color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Container #", color = TextGray, fontSize = 10.sp)
                    Text(product.containerNumber.ifBlank { "N/A" }, color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Warehouse Location", color = TextGray, fontSize = 10.sp)
                    Text(product.warehouseLocation.ifBlank { "N/A" }, color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Rack / Slot #", color = TextGray, fontSize = 10.sp)
                    Text(product.rackNumber.ifBlank { "N/A" }, color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Showroom buttons
            Text("CONTACT SHOWROOM", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuxuryDarkGray)
                    .border(0.5.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Interested in ${product.name}?",
                    color = MarbleWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Opposite Mohanpura Road, Kishangarh, Rajasthan",
                    color = TextGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919980583621"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryBlack),
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call", color = Gold)
                    }

                    Button(
                        onClick = {
                            val url = "https://api.whatsapp.com/send?phone=919928360001&text=Hello%20MOONSTONES,%20I%20am%20interested%20in%20${Uri.encode(product.name)}%20(SKU:%20${product.skuNumber})%20with%20available%20size%20${Uri.encode(averageSize)}."
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", color = Color.White)
                    }
                }
            }
        }
    }

    // Zoomed QR Code representation modal
    if (showFullQR) {
        Dialog(onDismissRequest = { showFullQR = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = product.name,
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Scan to Open Stone Specifications",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    QrCodeDrawer(sku = product.skuNumber, qrSize = 190.dp)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SKU: ${product.skuNumber}", color = MarbleWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Lot: ${product.lotNumber} • Batch: ${product.batchNumber}", color = TextGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showFullQR = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Gold)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Stone?", color = ErrorRed) },
            text = { Text("Are you absolutely sure you want to delete '${product.name}'? This cannot be undone.", color = MarbleWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Gold)
                }
            },
            containerColor = LuxuryDarkGray
        )
    }
}

@Composable
fun InfoBadge(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LuxuryDarkGray)
            .border(0.5.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = TextGray, fontSize = 9.sp)
        Text(text = value, color = MarbleWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StockMetric(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextGray, fontSize = 10.sp)
        Text(text = count.toString(), color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

// ================= OWNER ADMIN DASHBOARD SCREEN =================
@Composable
fun OwnerDashboardScreen(
    products: List<MarbleProduct>,
    logs: List<String>,
    notifications: List<String>,
    onBack: () -> Unit
) {
    val totalProducts = products.size
    val totalSlabs = products.sumOf { it.totalSlabs }
    val availableSlabs = products.sumOf { it.availableSlabs }
    val reservedSlabs = products.sumOf { it.reservedSlabs }
    val soldSlabs = products.sumOf { it.soldSlabs }
    val outOfStock = products.count { it.availableSlabs == 0 }
    val lowStock = products.count { it.availableSlabs in 1..1 }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Activity Logs, 1 = Low Stock Alerts

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Dashboard Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OWNER DASHBOARD",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Serif
                )
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkGray, contentColor = Gold),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                ) {
                    Text("Exit Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // Grid of core statistics
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(label = "Total Stones", value = totalProducts.toString(), icon = Icons.Default.Category, modifier = Modifier.weight(1f))
                    StatCard(label = "Total Slabs", value = totalSlabs.toString(), icon = Icons.Default.Inbox, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(label = "Slabs Available", value = availableSlabs.toString(), icon = Icons.Default.CheckCircle, color = SuccessGreen, modifier = Modifier.weight(1f))
                    StatCard(label = "Slabs Reserved", value = reservedSlabs.toString(), icon = Icons.Default.Pending, color = ReservedOrange, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(label = "Slabs Sold", value = soldSlabs.toString(), icon = Icons.Default.MonetizationOn, color = ErrorRed, modifier = Modifier.weight(1f))
                    StatCard(label = "Out of Stock", value = outOfStock.toString(), icon = Icons.Default.Cancel, color = if (outOfStock > 0) ErrorRed else TextGray, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            // Live Alerts Bar
            if (outOfStock > 0 || lowStock > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorRed.copy(alpha = 0.15f))
                        .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Alert", tint = ErrorRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Critical stock alerts: $outOfStock stones are Out of Stock and $lowStock stones are Low on Slabs.",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tab selection
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = LuxuryDarkGray,
                contentColor = Gold
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Activity Audit Logs", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Live Notifications", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        if (selectedTab == 0) {
            // Activity logs list
            if (logs.isEmpty()) {
                item {
                    Text("No activities logged yet.", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(logs) { log ->
                    LogItem(text = log)
                }
            }
        } else {
            // Notification lists
            if (notifications.isEmpty()) {
                item {
                    Text("No live notifications received.", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(notifications) { alert ->
                    AlertNotificationItem(text = alert)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = Gold,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
        border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, color = TextGray, fontSize = 10.sp)
                Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LogItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LuxuryMediumGray)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = MarbleWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun AlertNotificationItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LuxuryMediumGray)
            .border(0.5.dp, if (text.contains("⚠️")) ErrorRed.copy(alpha = 0.4f) else Gold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = MarbleWhite, fontSize = 11.sp)
    }
}

// ================= ADD / EDIT PRODUCT FORM =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    product: MarbleProduct?,
    onSave: (MarbleProduct) -> Unit,
    onCancel: () -> Unit
) {
    // Form fields state
    var name by remember { mutableStateOf(product?.name ?: "") }
    var collectionName by remember { mutableStateOf(product?.collectionName ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Italian Marble") }
    var marbleType by remember { mutableStateOf(product?.marbleType ?: "") }
    var finish by remember { mutableStateOf(product?.finish ?: "Polished") }
    var thickness by remember { mutableStateOf(product?.thickness ?: "18mm") }
    var color by remember { mutableStateOf(product?.color ?: "White") }
    var origin by remember { mutableStateOf(product?.origin ?: "") }
    var skuNumber by remember { mutableStateOf(product?.skuNumber ?: "MS-SKU-${(100..999).random()}") }
    var batchNumber by remember { mutableStateOf(product?.batchNumber ?: "B-2026") }
    var lotNumber by remember { mutableStateOf(product?.lotNumber ?: "LOT-") }

    // Inventory details
    var storeName by remember { mutableStateOf(product?.storeName ?: "Kishangarh Showroom") }
    var blockQuantity by remember { mutableStateOf(product?.blockQuantity?.toString() ?: "0") }
    var containerNumber by remember { mutableStateOf(product?.containerNumber ?: "") }
    var warehouseLocation by remember { mutableStateOf(product?.warehouseLocation ?: "") }
    var rackNumber by remember { mutableStateOf(product?.rackNumber ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "0.0") }

    // Interactive individual Slabs Dimension state
    val slabDimensions = remember {
        mutableStateListOf<SlabDimension>().apply {
            product?.getSlabDimensionsList()?.let { addAll(it) }
        }
    }

    var tempLength by remember { mutableStateOf("") }
    var tempWidth by remember { mutableStateOf("") }

    // Automatically calculate stats based on slabDimensions list
    val calculatedTotalSlabs = slabDimensions.size
    val availableSlabsCount = remember(slabDimensions.size) { slabDimensions.size } // Initially map size

    // Basic quantities manual entry
    var availableSlabs by remember { mutableStateOf(product?.availableSlabs?.toString() ?: "0") }
    var reservedSlabs by remember { mutableStateOf(product?.reservedSlabs?.toString() ?: "0") }
    var soldSlabs by remember { mutableStateOf(product?.soldSlabs?.toString() ?: "0") }

    // Dynamic calculator totals
    val computedTotalAreaSqFt = slabDimensions.sumOf { it.areaSqFt }

    // Sync counts automatically if dims exist
    LaunchedEffect(slabDimensions.size) {
        if (slabDimensions.isNotEmpty()) {
            availableSlabs = slabDimensions.size.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title
        Text(
            text = if (product == null) "ADD NEW PREMIUM STONE" else "EDIT STONE: ${product.name}",
            color = Gold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Serif
        )

        // General Information
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("SPECIFICATION INFO", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name", color = TextGray) },
                    modifier = Modifier.fillMaxWidth().testTag("name_field"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                )

                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Collection Name", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = marbleType,
                        onValueChange = { marbleType = it },
                        label = { Text("Marble Type", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = finish,
                        onValueChange = { finish = it },
                        label = { Text("Finish", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = thickness,
                        onValueChange = { thickness = it },
                        label = { Text("Thickness (e.g. 18mm)", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color (Primary)", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Origin (e.g. Italy)", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = skuNumber,
                        onValueChange = { skuNumber = it },
                        label = { Text("SKU Number", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price/sq.ft (₹)", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch Number", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = lotNumber,
                        onValueChange = { lotNumber = it },
                        label = { Text("Lot Number", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }
            }
        }

        // Slab sizes manager
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SLAB SIZES MANAGER", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Add slabs individually. Total slabs and area are automatically calculated in real-time.",
                    color = TextGray,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempLength,
                        onValueChange = { tempLength = it },
                        label = { Text("Length (in)", color = TextGray, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = tempWidth,
                        onValueChange = { tempWidth = it },
                        label = { Text("Width (in)", color = TextGray, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    Button(
                        onClick = {
                            val l = tempLength.toDoubleOrNull()
                            val w = tempWidth.toDoubleOrNull()
                            if (l != null && w != null) {
                                slabDimensions.add(SlabDimension(l, w))
                                tempLength = ""
                                tempWidth = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = LuxuryBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Add")
                    }
                }

                // Slabs dimension tags
                if (slabDimensions.isNotEmpty()) {
                    Text("Recorded Slabs:", color = MarbleWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        slabDimensions.forEachIndexed { index, slab ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LuxuryMediumGray)
                                    .border(0.5.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}: ${slab.length}\"×${slab.width}\"",
                                    color = MarbleWhite,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = ErrorRed,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { slabDimensions.removeAt(index) }
                                )
                            }
                        }
                    }

                    // Dynamic Area calculation display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Gold.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Auto-Calculated Area: ${String.format("%.1f", computedTotalAreaSqFt)} sq.ft (${String.format("%.1f", computedTotalAreaSqFt * 0.092903)} sq.m) • $calculatedTotalSlabs Slabs Total",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Detailed Quantities
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("STOCK & QUANTITY COUNTS", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = availableSlabs,
                        onValueChange = { availableSlabs = it },
                        label = { Text("Available Slabs", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = reservedSlabs,
                        onValueChange = { reservedSlabs = it },
                        label = { Text("Reserved Slabs", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = soldSlabs,
                        onValueChange = { soldSlabs = it },
                        label = { Text("Sold Slabs", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = blockQuantity,
                        onValueChange = { blockQuantity = it },
                        label = { Text("Block Quantity", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }
            }
        }

        // Logistics
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("LOGISTICS & LOCATION INFO", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Showroom / Store Name", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = containerNumber,
                        onValueChange = { containerNumber = it },
                        label = { Text("Container Number", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                    OutlinedTextField(
                        value = warehouseLocation,
                        onValueChange = { warehouseLocation = it },
                        label = { Text("Warehouse Yard", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                    )
                }

                OutlinedTextField(
                    value = rackNumber,
                    onValueChange = { rackNumber = it },
                    label = { Text("Rack / Slot Location", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold)
                )
            }
        }

        // Form Action row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkGray, contentColor = Gold),
                border = BorderStroke(1.dp, Gold)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (name.isBlank()) return@Button

                    val slabString = slabDimensions.joinToString(";") { "${it.length}x${it.width}" }
                    val totalSlabsCount = (availableSlabs.toIntOrNull() ?: 0) +
                            (reservedSlabs.toIntOrNull() ?: 0) +
                            (soldSlabs.toIntOrNull() ?: 0)

                    val updatedProduct = MarbleProduct(
                        id = product?.id ?: 0,
                        name = name,
                        collectionName = collectionName.ifBlank { "Classic" },
                        category = category.ifBlank { "Marble" },
                        marbleType = marbleType.ifBlank { "Generic" },
                        finish = finish.ifBlank { "Polished" },
                        thickness = thickness.ifBlank { "18mm" },
                        color = color.ifBlank { "White" },
                        origin = origin.ifBlank { "India" },
                        skuNumber = skuNumber,
                        batchNumber = batchNumber,
                        lotNumber = lotNumber.ifBlank { "N/A" },
                        storeName = storeName,
                        totalSlabs = if (slabDimensions.isNotEmpty()) slabDimensions.size else totalSlabsCount,
                        availableSlabs = availableSlabs.toIntOrNull() ?: 0,
                        reservedSlabs = reservedSlabs.toIntOrNull() ?: 0,
                        soldSlabs = soldSlabs.toIntOrNull() ?: 0,
                        slabsJson = slabString,
                        blockQuantity = blockQuantity.toIntOrNull() ?: 0,
                        containerNumber = containerNumber,
                        warehouseLocation = warehouseLocation,
                        rackNumber = rackNumber,
                        coverImageUrl = product?.coverImageUrl ?: "",
                        galleryImagesJson = product?.galleryImagesJson ?: "",
                        bookmatchImageUrl = product?.bookmatchImageUrl ?: "",
                        videoUrl = product?.videoUrl ?: "",
                        isFavorite = product?.isFavorite ?: false,
                        price = price.toDoubleOrNull() ?: 0.0,
                        timestamp = product?.timestamp ?: System.currentTimeMillis()
                    )
                    onSave(updatedProduct)
                },
                modifier = Modifier.weight(1f).testTag("save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = LuxuryBlack)
            ) {
                Text("Save Stone", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= DIALOG COMPONENTS =================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

@Composable
fun OwnerPinDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit,
    errorMsg: String?
) {
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            border = BorderStroke(1.dp, Gold)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock PIN",
                    tint = Gold,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = "Owner Verification PIN",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please enter your 4-digit security PIN to unlock Owner Editing control.\n(Try: '1989' or '9980')",
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    placeholder = { Text("••••", color = TextGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .testTag("pin_field"),
                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MarbleWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MarbleWhite, unfocusedTextColor = MarbleWhite, focusedBorderColor = Gold, focusedContainerColor = LuxuryBlack, unfocusedContainerColor = LuxuryBlack),
                    singleLine = true
                )

                if (errorMsg != null) {
                    Text(text = errorMsg, color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextGray)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onVerify(pin) },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = LuxuryBlack),
                        modifier = Modifier.testTag("verify_pin_submit")
                    ) {
                        Text("Verify PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    colors: List<String>,
    finishes: List<String>,
    thicknesses: List<String>,
    origins: List<String>,
    selectedColor: String,
    selectedFinish: String,
    selectedThickness: String,
    selectedOrigin: String,
    onlyInStock: Boolean,
    onColorSelect: (String) -> Unit,
    onFinishSelect: (String) -> Unit,
    onThicknessSelect: (String) -> Unit,
    onOriginSelect: (String) -> Unit,
    onStockToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            border = BorderStroke(0.5.dp, Gold.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ADVANCED FILTERS", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Gold)
                    }
                }

                // In stock filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Only In-Stock", color = MarbleWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Hide out of stock items", color = TextGray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = onlyInStock,
                        onCheckedChange = onStockToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = Gold.copy(alpha = 0.4f))
                    )
                }

                HorizontalDivider(color = LuxuryLightGray)

                // Color Selector
                FilterSection(title = "Color Family", items = colors, selectedItem = selectedColor, onSelect = onColorSelect)

                // Finish Selector
                FilterSection(title = "Finish", items = finishes, selectedItem = selectedFinish, onSelect = onFinishSelect)

                // Thickness Selector
                FilterSection(title = "Thickness", items = thicknesses, selectedItem = selectedThickness, onSelect = onThicknessSelect)

                // Origin Selector
                FilterSection(title = "Origin", items = origins, selectedItem = selectedOrigin, onSelect = onOriginSelect)

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = LuxuryBlack)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    items: List<String>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(title, color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItem == item
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Gold else LuxuryBlack)
                        .border(1.dp, if (isSelected) GoldLight else LuxuryLightGray, RoundedCornerShape(6.dp))
                        .clickable { onSelect(item) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) LuxuryBlack else MarbleWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CompanyInfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkGray),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            border = BorderStroke(1.dp, Gold)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Procedural Gold Shield Logo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.15f))
                        .border(1.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                }

                Text(
                    text = "MOONSTONES",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    text = "Premium Showroom & Marble Gallery",
                    color = TextGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = LuxuryLightGray)

                // Details Specs list
                CompanyDetailRow(icon = Icons.Default.LocationOn, label = "Address", value = "Opposite Mohanpura Road, KH No. 4712, Makrana Road, Kali Dungri, Kishangarh, Rajasthan, India")
                CompanyDetailRow(icon = Icons.Default.Phone, label = "Support Desk", value = "+91 9980583621")
                CompanyDetailRow(icon = Icons.Default.Chat, label = "Sales Desk", value = "+91 9928360001")

                Spacer(modifier = Modifier.height(6.dp))

                // Direct Click interactions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919980583621"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryBlack),
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Text("Call Helpline", color = Gold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val mapUri = Uri.parse("geo:0,0?q=Makrana+Road,+Kishangarh,+Rajasthan,+India")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = LuxuryBlack)
                    ) {
                        Text("Find Location", fontSize = 12.sp)
                    }
                }

                TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Gold)) {
                    Text("Close Panel", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CompanyDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = MarbleWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
