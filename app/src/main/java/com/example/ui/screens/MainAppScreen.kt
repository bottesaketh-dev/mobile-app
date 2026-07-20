package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Display status messages using snackbar
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.uiMessage.value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (currentUser == null) {
            LoginScreen(viewModel = viewModel)
        } else {
            NavigationShell(viewModel = viewModel)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// ==========================================
// 1. GORGEOUS PREMIUM LOGIN SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val initialSyncDone by viewModel.initialSyncDone.collectAsStateWithLifecycle()
    val isConnecting = isSyncing && !initialSyncDone

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Large Brand Card with Logo
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepNavy),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Brand Image Logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                        .padding(2.dp)
                ) {
                    val drawableResId = LocalContext.current.resources.getIdentifier(
                        "bluefox_logo", "drawable", LocalContext.current.packageName
                    )
                    if (drawableResId != 0) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = drawableResId),
                            contentDescription = "BlueFox Ledger Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.RestaurantMenu,
                            contentDescription = "Fallback Logo",
                            tint = DeepNavy,
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BLUEFOX LEDGER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Flavors of India • Revenue & Cashflow Ledger",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Input Fields Card
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Sign In to Console",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon", tint = DeepNavy) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = DeepNavy
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = DeepNavy) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = DeepNavy
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Show sync status on login screen
                if (isConnecting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connecting to cloud database...",
                            fontSize = 12.sp,
                            color = DeepNavy.copy(alpha = 0.7f)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Access Dashboard", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Quick Testing Access Shortcuts (Polished Design Constraint)
        Text(
            text = "⚡ QUICK TESTING ACCOUNTS (1-CLICK ACCESS)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DeepNavy.copy(alpha = 0.6f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val roles = listOf(
                Triple("owner", "Owner", Icons.Filled.SupervisorAccount),
                Triple("branch_manager", "Manager", Icons.Filled.Storefront),
                Triple("biller", "Biller POS", Icons.Filled.PointOfSale)
            )

            for (role in roles) {
                val roleId = role.first
                val label = role.second
                val icon = role.third
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.bypassLogin(roleId) },
                    colors = CardDefaults.cardColors(containerColor = DeepNavy.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = DeepNavy,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. MAIN APPLICATION SHELL
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationShell(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Dashboard") }

    // Filtered tabs depending on user role
    val availableTabs = remember(currentUser) {
        when (currentUser?.role) {
            "owner" -> listOf("Dashboard", "POS Seating", "Menu Catalog", "Staff Registry", "Grocery Procure", "Expenses Log", "Financial Reports", "AI Assistant")
            "branch_manager" -> listOf("Dashboard", "Menu Catalog", "Grocery Procure", "Expenses Log", "Financial Reports", "AI Assistant")
            else -> listOf("POS Seating", "Menu Catalog", "AI Assistant") // Biller
        }
    }

    // Set fallback tab if current active is not allowed
    LaunchedEffect(availableTabs) {
        if (activeTab !in availableTabs) {
            activeTab = availableTabs.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeTab.uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentUser?.username?.uppercase() ?: "",
                            fontSize = 11.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    // Branch Filter (Owner only)
                    if (currentUser?.role == "owner" && activeTab in listOf("Dashboard", "Financial Reports")) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            TextButton(
                                onClick = { dropdownExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                            ) {
                                val currentBranchName = branches.find { it.branch_id == selectedBranchId }?.name ?: "All Branches"
                                Text(
                                    text = currentBranchName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 140.dp)
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Branch Filter")
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Branches") },
                                    onClick = {
                                        viewModel.selectedBranchId.value = null
                                        dropdownExpanded = false
                                    }
                                )
                                branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch.name) },
                                        onClick = {
                                            viewModel.selectedBranchId.value = branch.branch_id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Role indicator pill
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (currentUser?.role) {
                                    "owner" -> GoldAccent
                                    "branch_manager" -> Color.White.copy(alpha = 0.2f)
                                    else -> EmeraldGreen
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentUser?.role?.replace("_", " ")?.uppercase() ?: "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentUser?.role == "owner") DeepNavy else Color.White
                        )
                    }

                    // Logout Button
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy)
            )
        },
        bottomBar = {
            // Horizontal Lazy Navigation bar for flexible role access
            Surface(
                tonalElevation = 8.dp,
                color = DeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Solves system button overlapping
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(availableTabs) { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldAccent else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = tab,
                                    tint = if (isSelected) DeepNavy else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DeepNavy else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Dashboard" -> DashboardConsoleScreen(viewModel = viewModel)
                "POS Seating" -> PosBillingScreen(viewModel = viewModel)
                "Menu Catalog" -> MenuCatalogScreen(viewModel = viewModel)
                "Staff Registry" -> StaffRegistryScreen(viewModel = viewModel)
                "Grocery Procure" -> GroceryProcureScreen(viewModel = viewModel)
                "Expenses Log" -> ExpensesLogScreen(viewModel = viewModel)
                "Financial Reports" -> FinancialReportsScreen(viewModel = viewModel)
                "AI Assistant" -> AiAssistantScreen(viewModel = viewModel)
            }
        }
    }
}

private fun getTabIcon(tab: String): ImageVector {
    return when (tab) {
        "Dashboard" -> Icons.Filled.Dashboard
        "POS Seating" -> Icons.Filled.PointOfSale
        "Menu Catalog" -> Icons.Filled.RestaurantMenu
        "Staff Registry" -> Icons.Filled.People
        "Grocery Procure" -> Icons.Filled.Kitchen
        "Expenses Log" -> Icons.Filled.MoneyOff
        "Financial Reports" -> Icons.Filled.BarChart
        else -> Icons.Filled.Chat
    }
}

// ==========================================
// 3. TAB VIEW: DASHBOARD CONSOLE (P&L FOCUS)
// ==========================================
@Composable
fun DashboardConsoleScreen(viewModel: AppViewModel) {
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val opexExpenses by viewModel.allGeneralExpenses.collectAsStateWithLifecycle()
    val groceryPurchases by viewModel.allGroceryPurchases.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.allExpenseCategories.collectAsStateWithLifecycle()
    val salaryPayments by viewModel.allSalaryPayments.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    // Filter data relative to selected branch
    val filteredBills = remember(bills, selectedBranchId) {
        bills.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }
    val filteredOpex = remember(opexExpenses, selectedBranchId) {
        opexExpenses.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }
    val filteredGroceries = remember(groceryPurchases, selectedBranchId) {
        groceryPurchases.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    // Profit Calculations
    val cashInflow = filteredBills.sumOf { it.total_amount }
    val costOfGoods = filteredGroceries.sumOf { it.total_price }
    val generalOpex = filteredOpex.sumOf { it.amount }
    val salaryOpex = salaryPayments.filter { selectedBranchId == null || it.branch_id == selectedBranchId }.sumOf { it.net_salary } // global opex for simplicty in seeds
    val totalOutflow = costOfGoods + generalOpex + salaryOpex
    val netCashflow = cashInflow - totalOutflow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Header
        Text(
            text = "Monthly Performance Console",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = getAdaptiveText()
        )

        // 3-Card Operating Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cash Inflow
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = getAdaptiveLightGreen()),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CASH INFLOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    Text("₹${String.format("%.1f", cashInflow)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
                }
            }

            // Cash Outflow
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = getAdaptiveLightRed()),
                border = BorderStroke(1.dp, SoftCrimson.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CASH OUTFLOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftCrimson)
                    Text("₹${String.format("%.1f", totalOutflow)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
                }
            }

            // Operating Net Profit
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = getAdaptiveLightGold()),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("NET CASHFLOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    Text("₹${String.format("%.1f", netCashflow)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
                }
            }
        }

        // Custom High-Fidelity Compose Canvas Trajectory Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = getAdaptiveCardBg()),
            border = BorderStroke(1.dp, getAdaptiveBorder().copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Sales Trajectory (Last 30 Days)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = getAdaptiveText(),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render dynamic canvas graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // Combine seed sales trend with live sales
                    val salesTrend = remember(filteredBills) {
                        listOf(12000.0, 15000.0, 18000.0, 11000.0, 14000.0, 21000.0, 19000.0, 23000.0, 25000.0, cashInflow)
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val maxVal = salesTrend.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                        // Draw Grid Lines
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = height * i / gridCount
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Generate Line Coordinates
                        val points = salesTrend.mapIndexed { index, value ->
                            val x = width * index / (salesTrend.size - 1)
                            val y = height - (height * (value / maxVal).toFloat() * 0.8f) // buffer space top
                            Offset(x, y)
                        }

                        // Draw Gradient Area Under Curve
                        val fillPath = Path().apply {
                            moveTo(points.first().x, height)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(GoldAccent.copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw Main Trend Line
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = GoldAccent,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Point Indicators
                        val navyColor = DeepNavy // Keep constant or adaptive inside DrawScope
                        points.forEach { point ->
                            drawCircle(
                                color = navyColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = GoldAccent,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
        }

        // Outflow allocation doughnut/bar chart representation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = getAdaptiveCardBg()),
            border = BorderStroke(1.dp, getAdaptiveBorder().copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Outflow Allocation Ratio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = getAdaptiveText(),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (totalOutflow == 0.0) {
                    Text("No expenses logged yet for this period.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    val cogsRatio = (costOfGoods / totalOutflow).toFloat()
                    val opexRatio = (generalOpex / totalOutflow).toFloat()
                    val salaryRatio = (salaryOpex / totalOutflow).toFloat()

                    // Horizontal stacked ratio bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        if (cogsRatio > 0) {
                            Box(modifier = Modifier.weight(cogsRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(EmeraldGreen))
                        }
                        if (salaryRatio > 0) {
                            Box(modifier = Modifier.weight(salaryRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(GoldAccent))
                        }
                        if (opexRatio > 0) {
                            Box(modifier = Modifier.weight(opexRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(DeepNavy))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendItem(color = EmeraldGreen, name = "Kitchen Groceries", amount = costOfGoods)
                        LegendItem(color = GoldAccent, name = "Payroll Staff", amount = salaryOpex)
                        LegendItem(color = DeepNavy, name = "Operating Opex", amount = generalOpex)
                    }
                }
            }
        }

        // Two Section Split: Recent Bills vs Recent Opex
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recent Checkouts Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = getAdaptiveCardBg()),
                border = BorderStroke(1.dp, getAdaptiveBorder().copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Recent Bills", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredBills.isEmpty()) {
                        Text("No checkouts recorded.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        filteredBills.take(5).forEach { bill ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(bill.table_id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
                                    Text(bill.payment_mode, fontSize = 9.sp, color = Color.Gray)
                                }
                                Text("₹${bill.total_amount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            HorizontalDivider(color = getAdaptiveBorder().copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // Recent Expenses Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = getAdaptiveCardBg()),
                border = BorderStroke(1.dp, getAdaptiveBorder().copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Recent Opex Log", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredOpex.isEmpty()) {
                        Text("No expenses logged.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        filteredOpex.take(5).forEach { exp ->
                            val catName = expenseCategories.find { it.expense_category_id == exp.category_id }?.name ?: "Expense"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(catName, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = getAdaptiveText())
                                    Text(exp.description, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 80.dp))
                                }
                                Text("₹${exp.amount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SoftCrimson)
                            }
                            HorizontalDivider(color = getAdaptiveBorder().copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, name: String, amount: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Column {
            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Text("₹${String.format("%.1f", amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = getAdaptiveText())
        }
    }
}

// ==========================================
// 4. TAB VIEW: POS BILLING GRID
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosBillingScreen(viewModel: AppViewModel) {
    val tables by viewModel.allTables.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()
    val selectedTable by viewModel.selectedTable.collectAsStateWithLifecycle()

    val filteredTables = remember(tables, selectedBranchId) {
        tables.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    if (selectedTable == null) {
        // Table Seating Layout Grid
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dining Floor Grid Layout",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTables) { table ->
                    val isOccupied = table.status == "OCCUPIED"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clickable { viewModel.selectTable(table) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOccupied) LightRed else LightGreen
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isOccupied) SoftCrimson.copy(alpha = 0.4f) else EmeraldGreen.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = table.table_id,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isOccupied) SoftCrimson else EmeraldGreen)
                                        .size(10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "Cap: ${table.capacity} guests",
                                    fontSize = 11.sp,
                                    color = DeepNavy.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (isOccupied) "SEATED" else "VACANT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOccupied) SoftCrimson else EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // POS Ordering & Checkout Interface
        ActiveBillingScreen(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveBillingScreen(viewModel: AppViewModel) {
    val table by viewModel.selectedTable.collectAsStateWithLifecycle()
    val menuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val cart by viewModel.posCart.collectAsStateWithLifecycle()
    val notes by viewModel.posCartNotes.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.posCategoryFilter.collectAsStateWithLifecycle()

    var discountInput by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("UPI") }

    val categories = listOf("Starters", "Main Course", "Breads", "Rice", "Beverages", "Desserts")
    val filteredMenu = remember(menuItems, selectedCategory, table) {
        menuItems.filter { it.category == selectedCategory && it.is_available && it.branch_id == table?.branch_id }
    }

    val cartTotal = cart.entries.sumOf { it.key.price * it.value }
    val discountVal = discountInput.toDoubleOrNull() ?: 0.0
    val finalTotal = (cartTotal - discountVal).coerceAtLeast(0.0)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Menu Items Selector
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${table?.table_id} Ordering",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                TextButton(onClick = { viewModel.releaseTable() }) {
                    Text("Exit / Release", color = SoftCrimson, fontWeight = FontWeight.Bold)
                }
            }

            // Category Pill Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isCatSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCatSelected) DeepNavy else Color.LightGray.copy(alpha = 0.5f))
                            .clickable { viewModel.posCategoryFilter.value = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCatSelected) GoldAccent else DeepNavy
                        )
                    }
                }
            }

            // Food Grid list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMenu.isEmpty()) {
                    item {
                        Text(
                            "No items in this category currently.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    items(filteredMenu) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addToCart(item) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (item.is_vegetarian) EmeraldGreen else SoftCrimson)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DeepNavy
                                        )
                                    }
                                    Text(
                                        item.description ?: "",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    "₹${item.price}",
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Live Cart & Invoice Settle Form
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cart Items list
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Ticket Itemized Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DeepNavy,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (cart.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "No items selected. Click menu items on left to add.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(cart.entries.toList()) { (item, qty) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.removeFromCart(item) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Filled.Remove, contentDescription = "Reduce", modifier = Modifier.size(14.dp))
                                            }
                                            Text("$qty", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = { viewModel.addToCart(item) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(
                                            "₹${item.price * qty}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }

                                    // Special instructions input
                                    var itemNotes by remember(item.menu_item_id) { mutableStateOf(notes[item.menu_item_id] ?: "") }
                                    BasicTextField(
                                        value = itemNotes,
                                        onValueChange = {
                                            itemNotes = it
                                            viewModel.updateCartNotes(item.menu_item_id, it)
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LightGrayBg)
                                            .padding(4.dp),
                                        decorationBox = { innerTextField ->
                                            if (itemNotes.isEmpty()) {
                                                Text("Add cooking instruction...", fontSize = 9.sp, color = Color.Gray)
                                            }
                                            innerTextField()
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color.LightGray.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }
                }

                // Checkout Settle Panel
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = Color.LightGray)

                    // Checkout Pricing Columns
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 11.sp, color = Color.Gray)
                        Text("₹$cartTotal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Discount (₹)", fontSize = 11.sp, color = Color.Gray)
                        BasicTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = DeepNavy),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(60.dp)
                                .background(LightGrayBg)
                                .padding(2.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Payable", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        Text("₹$finalTotal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }

                    // Payment Mode Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("UPI", "CASH", "CARD").forEach { mode ->
                            val isModeSelected = paymentMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isModeSelected) GoldAccent else LightGrayBg)
                                    .clickable { paymentMode = mode }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.settleBill(discountVal, paymentMode) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text("Settle & Settle Invoice", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. TAB VIEW: MENU CATALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCatalogScreen(viewModel: AppViewModel) {
    val menuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    val filteredMenu = remember(menuItems, selectedBranchId) {
        menuItems.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    var isAddingNew by remember { mutableStateOf(false) }

    // Add Form variables
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Main Course") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Food Catalog Inventory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            // Show 'Add Dish' button for Owners and Managers only
            if (currentUser?.role in listOf("owner", "branch_manager")) {
                Button(
                    onClick = { isAddingNew = !isAddingNew },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(if (isAddingNew) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add Icon", tint = GoldAccent)
                        Text(if (isAddingNew) "Close Form" else "Add Dish", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isAddingNew) {
            // Sliding/Collapsing Insertion Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Register New Dish", fontWeight = FontWeight.Bold, color = DeepNavy)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Dish Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Short Ingredient Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Base Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Category Pill Selector representation
                        Box(modifier = Modifier.weight(1.2f)) {
                            var expCatDropdown by remember { mutableStateOf(false) }
                            Button(
                                onClick = { expCatDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(top = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightGrayBg, contentColor = DeepNavy),
                                border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(selectedCategory, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(expanded = expCatDropdown, onDismissRequest = { expCatDropdown = false }) {
                                listOf("Starters", "Main Course", "Breads", "Rice", "Beverages", "Desserts").forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat) }, onClick = {
                                        selectedCategory = cat
                                        expCatDropdown = false
                                    })
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            RadioButton(selected = isVeg, onClick = { isVeg = true })
                            Text("Veg (🟢)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(10.dp))
                            RadioButton(selected = !isVeg, onClick = { isVeg = false })
                            Text("Non-Veg (🔴)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val price = priceStr.toDoubleOrNull() ?: 0.0
                                if (name.isNotEmpty() && price > 0.0) {
                                    viewModel.addMenuItem(name, desc, price, selectedCategory, isVeg)
                                    name = ""
                                    desc = ""
                                    priceStr = ""
                                    isAddingNew = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                        ) {
                            Text("Submit Dish", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // List Grid of active catalog
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredMenu) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (item.is_vegetarian) EmeraldGreen else SoftCrimson)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DeepNavy
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LightGrayBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(item.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }
                            Text(item.description ?: "", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "₹${item.price}",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 14.sp
                            )

                            // Availability switch + Delete for authority
                            if (currentUser?.role in listOf("owner", "branch_manager")) {
                                Switch(
                                    checked = item.is_available,
                                    onCheckedChange = { viewModel.toggleMenuItemAvailability(item) },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (item.is_available) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = "Availability Status",
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                )

                                IconButton(onClick = { viewModel.deleteMenuItem(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove item", tint = SoftCrimson)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. TAB VIEW: STAFF REGISTRY
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffRegistryScreen(viewModel: AppViewModel) {
    val employees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    val filteredStaff = remember(employees, selectedBranchId) {
        employees.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    var isAddingNew by remember { mutableStateOf(false) }

    // Add variables
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("Chef") }
    var baseSalaryStr by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Staff Register Directory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            Button(
                onClick = { isAddingNew = !isAddingNew },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(if (isAddingNew) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add Staff", tint = GoldAccent)
                    Text(if (isAddingNew) "Close" else "Register Staff", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isAddingNew) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Register New Staff Profile", fontWeight = FontWeight.Bold, color = DeepNavy)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = baseSalaryStr,
                            onValueChange = { baseSalaryStr = it },
                            label = { Text("Base Salary (₹/m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        // Role Selector button
                        Box(modifier = Modifier.weight(1f)) {
                            var roleDropdownExpanded by remember { mutableStateOf(false) }
                            Button(
                                onClick = { roleDropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(top = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightGrayBg, contentColor = DeepNavy),
                                border = BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text(position)
                            }
                            DropdownMenu(expanded = roleDropdownExpanded, onDismissRequest = { roleDropdownExpanded = false }) {
                                listOf("Chef", "Cashier", "Waiter", "Cleaner").forEach { pos ->
                                    DropdownMenuItem(text = { Text(pos) }, onClick = {
                                        position = pos
                                        roleDropdownExpanded = false
                                    })
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val sal = baseSalaryStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty() && sal > 0) {
                                val parts = name.trim().split(" ", limit = 2)
                                val first = parts.getOrNull(0) ?: name
                                val last = parts.getOrNull(1) ?: ""
                                viewModel.addEmployee(first, last, email, phone, position, sal)
                                name = ""
                                email = ""
                                phone = ""
                                baseSalaryStr = ""
                                isAddingNew = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Save Employee", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List staff with payroll settlement action triggers
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStaff) { emp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${emp.first_name} ${emp.last_name}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepNavy)
                                Text("${emp.position} • Base: ₹${emp.monthly_salary}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Direct Quick Salary Payment Button
                                var showPayoutDialog by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showPayoutDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Pay Salary", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                if (showPayoutDialog) {
                                    SalaryPayoutDialog(
                                        employee = emp,
                                        onDismiss = { showPayoutDialog = false },
                                        onConfirm = { bonus, deduction, mode ->
                                            viewModel.paySalary(emp.employee_id, "${emp.first_name} ${emp.last_name}", 7, 2026, bonus, deduction, emp.monthly_salary, mode)
                                            showPayoutDialog = false
                                        }
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteEmployee(emp) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Staff", tint = SoftCrimson)
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
fun SalaryPayoutDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String) -> Unit
) {
    var bonusStr by remember { mutableStateOf("0") }
    var deductionStr by remember { mutableStateOf("0") }
    var payMode by remember { mutableStateOf("UPI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Process Salary: ${employee.first_name} ${employee.last_name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Base Pay: ₹${employee.monthly_salary}/month", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(
                    value = bonusStr,
                    onValueChange = { bonusStr = it },
                    label = { Text("Performance Bonus (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deductionStr,
                    onValueChange = { deductionStr = it },
                    label = { Text("Deductions / Penalties (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mode
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "CASH", "BANK_TRANSFER").forEach { m ->
                        val isSel = payMode == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) GoldAccent else LightGrayBg)
                                .clickable { payMode = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(m, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bonus = bonusStr.toDoubleOrNull() ?: 0.0
                    val ded = deductionStr.toDoubleOrNull() ?: 0.0
                    onConfirm(bonus, ded, payMode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("Authorise Payout", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// 7. TAB VIEW: GROCERY PURCHASES
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryProcureScreen(viewModel: AppViewModel) {
    val profiles by viewModel.allGroceryItems.collectAsStateWithLifecycle()
    val purchases by viewModel.allGroceryPurchases.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    val filteredProfiles = profiles
    val filteredPurchases = remember(purchases, selectedBranchId) {
        purchases.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    var isAddingNewProfile by remember { mutableStateOf(false) }
    var isAddingPurchase by remember { mutableStateOf(false) }

    // Forms states
    var ingName by remember { mutableStateOf("") }
    var ingUnit by remember { mutableStateOf("kg") }

    var selectedProfileId by remember { mutableStateOf("") }
    var selectedProfileName by remember { mutableStateOf("Tomato") }
    var qtyStr by remember { mutableStateOf("") }
    var unitPriceStr by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kitchen Groceries COGS Log",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
        }

        // Action Buttons Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    isAddingNewProfile = !isAddingNewProfile
                    isAddingPurchase = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("Register Stock Item", color = GoldAccent, fontSize = 11.sp)
            }

            Button(
                onClick = {
                    isAddingPurchase = !isAddingPurchase
                    isAddingNewProfile = false
                    // Preselect profile if list is not empty
                    if (filteredProfiles.isNotEmpty() && selectedProfileId.isEmpty()) {
                        selectedProfileId = filteredProfiles.first().grocery_item_id
                        selectedProfileName = filteredProfiles.first().product_name
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("Log Procurement", color = GoldAccent, fontSize = 11.sp)
            }
        }

        if (isAddingNewProfile) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Register Raw Stock Ingredient Profile", fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = ingName, onValueChange = { ingName = it }, label = { Text("Ingredient Name (e.g. Paneer)") }, modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            listOf("kg", "L", "packet", "count").forEach { u ->
                                val sel = ingUnit == u
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) GoldAccent else LightGrayBg)
                                        .clickable { ingUnit = u }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(u, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (ingName.isNotEmpty()) {
                                    viewModel.addGroceryItem(ingName, 1, ingUnit)
                                    ingName = ""
                                    isAddingNewProfile = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                        ) {
                            Text("Save Profile", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isAddingPurchase) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Log Fresh Procurement Cost", fontWeight = FontWeight.Bold)

                    // Profile Dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Button(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LightGrayBg, contentColor = DeepNavy),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Profile: $selectedProfileName")
                        }
                        DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            filteredProfiles.forEach { prof ->
                                DropdownMenuItem(text = { Text(prof.product_name) }, onClick = {
                                    selectedProfileId = prof.grocery_item_id
                                    selectedProfileName = prof.product_name
                                    dropdownExpanded = false
                                })
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text("Quantity Purchased") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = unitPriceStr,
                            onValueChange = { unitPriceStr = it },
                            label = { Text("Unit Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(value = vendor, onValueChange = { vendor = it }, label = { Text("Vendor Name / Market Location") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            val qty = qtyStr.toDoubleOrNull() ?: 0.0
                            val uPrice = unitPriceStr.toDoubleOrNull() ?: 0.0
                            if (selectedProfileId.isNotEmpty() && qty > 0.0 && uPrice > 0.0) {
                                viewModel.logGroceryPurchase(selectedProfileId, qty, uPrice, vendor, "")
                                qtyStr = ""
                                unitPriceStr = ""
                                vendor = ""
                                isAddingPurchase = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Record Cost Log", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List Purchases history
        Text("Procurement Cost History Ledger", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

        if (filteredPurchases.isEmpty()) {
            Text("No groceries purchased logged yet for this branch.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
        } else {
            filteredPurchases.forEach { pur ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val prodName = profiles.find { it.grocery_item_id == pur.grocery_item_id }?.product_name ?: "Grocery Item"
                            Text(prodName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Vendor: ${pur.vendor_name ?: ""} • Qty: ${pur.quantity}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Text("₹${pur.total_price}", fontWeight = FontWeight.Bold, color = SoftCrimson, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. TAB VIEW: OPERATIONAL EXPENSES LOG (OPEX)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesLogScreen(viewModel: AppViewModel) {
    val categories by viewModel.allExpenseCategories.collectAsStateWithLifecycle()
    val expenses by viewModel.allGeneralExpenses.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    val filteredCats = categories
    val filteredExps = remember(expenses, selectedBranchId) {
        expenses.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    var isAddingCat by remember { mutableStateOf(false) }
    var isAddingExpense by remember { mutableStateOf(false) }

    var catName by remember { mutableStateOf("") }

    var selectedCatId by remember { mutableStateOf(0) }
    var selectedCatName by remember { mutableStateOf("Rent") }
    var amountStr by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var receiptNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Operational Utility costs (Opex)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    isAddingCat = !isAddingCat
                    isAddingExpense = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("Add Opex category", color = GoldAccent, fontSize = 11.sp)
            }

            Button(
                onClick = {
                    isAddingExpense = !isAddingExpense
                    isAddingCat = false
                    if (filteredCats.isNotEmpty() && selectedCatId == 0) {
                        selectedCatId = filteredCats.first().expense_category_id
                        selectedCatName = filteredCats.first().name
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("Record Opex Cost", color = GoldAccent, fontSize = 11.sp)
            }
        }

        if (isAddingCat) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create New Operating Classification", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Category (e.g. Utility Bills, Rent)") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (catName.isNotEmpty()) {
                                viewModel.addExpenseCategory(catName, "")
                                catName = ""
                                isAddingCat = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Add", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isAddingExpense) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Log Utility, Rent or AC repairs opex", fontWeight = FontWeight.Bold)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Button(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LightGrayBg, contentColor = DeepNavy),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Opex Category: $selectedCatName")
                        }
                        DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            filteredCats.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                                    selectedCatId = cat.expense_category_id
                                    selectedCatName = cat.name
                                    dropdownExpanded = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Operating Cost Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Service Description / Particular details") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = receiptNumber, onValueChange = { receiptNumber = it }, label = { Text("Vendor Invoice Receipt Number") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (selectedCatId != 0 && amt > 0) {
                                viewModel.logGeneralExpense(selectedCatId, amt, desc, "UPI", "", receiptNumber, "")
                                amountStr = ""
                                desc = ""
                                receiptNumber = ""
                                isAddingExpense = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Log Cost Ledger", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("Utility Operational Costs Ledger History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

        if (filteredExps.isEmpty()) {
            Text("No operating costs logs registered.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
        } else {
            filteredExps.forEach { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val catName = categories.find { it.expense_category_id == exp.category_id }?.name ?: "Expense"
                            Text(catName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${exp.description} • Receipt: ${exp.receipt_number ?: ""}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Text("₹${exp.amount}", fontWeight = FontWeight.Bold, color = SoftCrimson, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. TAB VIEW: FINANCIAL ANALYTICS (P&L FOCUS)
// ==========================================
@Composable
fun FinancialReportsScreen(viewModel: AppViewModel) {
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val opexExpenses by viewModel.allGeneralExpenses.collectAsStateWithLifecycle()
    val groceryPurchases by viewModel.allGroceryPurchases.collectAsStateWithLifecycle()
    val salaryPayments by viewModel.allSalaryPayments.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

    val filteredBills = remember(bills, selectedBranchId) {
        bills.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }
    val filteredOpex = remember(opexExpenses, selectedBranchId) {
        opexExpenses.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }
    val filteredGroceries = remember(groceryPurchases, selectedBranchId) {
        groceryPurchases.filter { selectedBranchId == null || it.branch_id == selectedBranchId }
    }

    // Profit Calculations
    val grossSales = filteredBills.sumOf { it.total_amount }
    val costOfGoods = filteredGroceries.sumOf { it.total_price }
    val grossOperatingProfit = grossSales - costOfGoods
    val generalOpex = filteredOpex.sumOf { it.amount }
    val salaryOpex = salaryPayments.filter { selectedBranchId == null || it.branch_id == selectedBranchId }.sumOf { it.net_salary }
    val totalOpex = generalOpex + salaryOpex
    val netProfit = grossOperatingProfit - totalOpex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Reconciled P&L Statement", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Profit & Loss Ledger Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )

                HorizontalDivider(color = Color.LightGray)

                // Itemized Columns
                ProfitLossRow(label = "Gross Customer Billing (A)", value = grossSales, isPositive = true)
                ProfitLossRow(label = "Cost of Goods Sold (COGS - B)", value = costOfGoods, isPositive = false)

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                ProfitLossRow(
                    label = "Gross Operating Margin (C = A - B)",
                    value = grossOperatingProfit,
                    isPositive = grossOperatingProfit >= 0,
                    isBold = true
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                ProfitLossRow(label = "General Utility Expenses", value = generalOpex, isPositive = false)
                ProfitLossRow(label = "Payroll Salaries Processed", value = salaryOpex, isPositive = false)
                ProfitLossRow(label = "Total Operating OPEX (D)", value = totalOpex, isPositive = false, isBold = true)

                HorizontalDivider(color = Color.DarkGray, thickness = 2.dp)

                ProfitLossRow(
                    label = "Net Operating Income (E = C - D)",
                    value = netProfit,
                    isPositive = netProfit >= 0,
                    isBold = true,
                    highlightBackground = true
                )
            }
        }
    }
}

@Composable
fun ProfitLossRow(
    label: String,
    value: Double,
    isPositive: Boolean,
    isBold: Boolean = false,
    highlightBackground: Boolean = false
) {
    val formattedValue = if (isPositive || value == 0.0) "₹${String.format("%.2f", value)}" else "-₹${String.format("%.2f", kotlin.math.abs(value))}"
    val contentColor = if (isBold) {
        if (highlightBackground) {
            if (isPositive) EmeraldGreen else SoftCrimson
        } else DeepNavy
    } else Color.DarkGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlightBackground) LightGold else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = if (highlightBackground) 8.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) DeepNavy else Color.Gray
        )
        Text(
            text = formattedValue,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

// ==========================================
// 10. TAB VIEW: AI ASSISTANT CHAT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: AppViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isChatTyping.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Smooth scroll to latest messages
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
    ) {
        // Chat Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepNavy),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                        .padding(2.dp)
                ) {
                    val context = LocalContext.current
                    val drawableResId = context.resources.getIdentifier(
                        "bluefox_logo", "drawable", context.packageName
                    )
                    if (drawableResId != 0) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = drawableResId),
                            contentDescription = "AI Head",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "AI Icon", tint = DeepNavy, modifier = Modifier.size(24.dp).align(Alignment.Center))
                    }
                }

                Column {
                    Text("BlueFox Ledger AI", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Command Center Insights Bot", fontSize = 11.sp, color = GoldAccent)
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { (sender, text) ->
                val isUser = sender == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) DeepNavy else Color.White
                        ),
                        border = if (isUser) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                color = if (isUser) Color.White else DeepNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thinking", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        var dotCount by remember { mutableStateOf(1) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                kotlinx.coroutines.delay(500)
                                dotCount = if (dotCount < 3) dotCount + 1 else 1
                            }
                        }
                        Text(".".repeat(dotCount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    }
                }
            }
        }

        // Suggestion Quick Pills Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "operating profitability (P&L)",
                "how many tables are occupied?",
                "total revenue collected",
                "list chef salaries"
            )
            items(suggestions) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.sendChatMessage(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(prompt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                }
            }
        }

        // Input Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask BlueFox AI...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (input.trim().isNotEmpty()) {
                            viewModel.sendChatMessage(input)
                            input = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message", tint = DeepNavy)
                }
            }
        }
    }
}
