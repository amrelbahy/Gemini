package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.SherketyLegalTheme
import com.example.ui.viewmodel.LegalDeskViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LegalDeskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SherketyLegalTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: LegalDeskViewModel) {
    var currentScreen by remember { mutableStateOf("dashboard") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showGlobalSearch by remember { mutableStateOf(false) }

    // Back handling
    BackHandler(enabled = currentScreen != "dashboard") {
        currentScreen = "dashboard"
    }

    val screenTitle = when (currentScreen) {
        "dashboard" -> "لوحة التحكم الرئيسية"
        "tasks" -> "إدارة المهام والتكليفات"
        "incorporations" -> "تأسيس الشركات"
        "contracts" -> "العقود والشؤون القانونية"
        "management_requests" -> "طلبات وتكليفات الإدارة"
        "finance" -> "المتابعة المالية والمحاسبية"
        "opportunities" -> "فرص التعاقد الجديدة"
        "ai_assistant" -> "المساعد القانوني الذكي"
        "reports" -> "التقارير ومؤشرات الأداء"
        else -> "Sherkety Legal Desk"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(310.dp)
                    .testTag("navigation_drawer")
            ) {
                // Drawer Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Gavel, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sherkety Legal Desk",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "مكتب عمرو عادل • شركتي لتكنولوجيا المعلومات",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val menuItems = listOf(
                    Triple("dashboard", "لوحة التحكم الرئيسية", Icons.Default.Dashboard),
                    Triple("tasks", "إدارة المهام والتكليفات", Icons.Default.Checklist),
                    Triple("incorporations", "تأسيس الشركات", Icons.Default.Domain),
                    Triple("contracts", "العقود والشؤون القانونية", Icons.Default.Description),
                    Triple("management_requests", "طلبات وتكليفات الإدارة", Icons.Default.AssignmentInd),
                    Triple("finance", "المتابعة المالية والمحاسبية", Icons.Default.AccountBalance),
                    Triple("opportunities", "فرص التعاقد الجديدة", Icons.AutoMirrored.Filled.TrendingUp),
                    Triple("ai_assistant", "المساعد القانوني الذكي 🤖", Icons.Default.AutoAwesome),
                    Triple("reports", "التقارير والنسخ الاحتياطي", Icons.Default.BarChart)
                )

                menuItems.forEach { (route, label, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(label, fontSize = 13.sp, fontWeight = if (currentScreen == route) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentScreen == route,
                        onClick = {
                            currentScreen = route
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("nav_item_$route")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = screenTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "مكتب عمرو عادل • شركتي لتكنولوجيا المعلومات",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة الرئيسية")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showGlobalSearch = true },
                            modifier = Modifier.testTag("top_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "البحث السريع")
                        }
                        IconButton(
                            onClick = { currentScreen = "ai_assistant" },
                            modifier = Modifier.testTag("top_ai_button")
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "المساعد الذكي",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val bottomTabs = listOf(
                        Triple("dashboard", "الرئيسية", Icons.Default.Dashboard),
                        Triple("tasks", "المهام", Icons.Default.Checklist),
                        Triple("incorporations", "التأسيس", Icons.Default.Domain),
                        Triple("contracts", "العقود", Icons.Default.Description),
                        Triple("ai_assistant", "المساعد", Icons.Default.AutoAwesome)
                    )

                    bottomTabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            selected = currentScreen == route,
                            onClick = { currentScreen = route },
                            modifier = Modifier.testTag("bottom_tab_$route")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(viewModel = viewModel, onNavigateTo = { currentScreen = it })
                    "tasks" -> TasksScreen(viewModel = viewModel)
                    "incorporations" -> IncorporationsScreen(viewModel = viewModel)
                    "contracts" -> ContractsScreen(viewModel = viewModel)
                    "management_requests" -> ManagementRequestsScreen(viewModel = viewModel)
                    "finance" -> FinanceScreen(viewModel = viewModel)
                    "opportunities" -> OpportunitiesScreen(viewModel = viewModel)
                    "ai_assistant" -> AiAssistantScreen(viewModel = viewModel)
                    "reports" -> ReportsAndSettingsScreen(viewModel = viewModel)
                    else -> DashboardScreen(viewModel = viewModel, onNavigateTo = { currentScreen = it })
                }
            }
        }
    }

    // Global Search Modal Dialog
    if (showGlobalSearch) {
        GlobalSearchDialog(
            viewModel = viewModel,
            onDismiss = { showGlobalSearch = false },
            onNavigate = { route ->
                currentScreen = route
                showGlobalSearch = false
            }
        )
    }
}

@Composable
fun GlobalSearchDialog(
    viewModel: LegalDeskViewModel,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val tasks by viewModel.tasks.collectAsState()
    val contracts by viewModel.contracts.collectAsState()
    val incorporations by viewModel.incorporations.collectAsState()
    val finance by viewModel.financeFollowups.collectAsState()

    val matchingTasks = remember(query, tasks) {
        if (query.isBlank()) emptyList() else tasks.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }
    val matchingContracts = remember(query, contracts) {
        if (query.isBlank()) emptyList() else contracts.filter { it.title.contains(query, ignoreCase = true) || it.counterpartyName.contains(query, ignoreCase = true) }
    }
    val matchingInc = remember(query, incorporations) {
        if (query.isBlank()) emptyList() else incorporations.filter { it.companyName.contains(query, ignoreCase = true) || it.clientName.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("البحث الشامل في ملفات المكتب", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("ابحث عن شركة، عقد، مهمة، أو عميل...") },
                    modifier = Modifier.fillMaxWidth().testTag("global_search_input"),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                if (query.isNotBlank()) {
                    Text(
                        text = "نتائج البحث (${matchingTasks.size + matchingContracts.size + matchingInc.size} نتيجة):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    matchingTasks.take(3).forEach { t ->
                        Text(
                            text = "• مهمة: ${t.title}",
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate("tasks") }
                        )
                    }
                    matchingContracts.take(3).forEach { c ->
                        Text(
                            text = "• عقد: ${c.title} (${c.counterpartyName})",
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate("contracts") }
                        )
                    }
                    matchingInc.take(3).forEach { i ->
                        Text(
                            text = "• تأسيس: ${i.companyName} (${i.clientName})",
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate("incorporations") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
