package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLogEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: LegalDeskViewModel,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val urgentTasks by viewModel.urgentTasks.collectAsState()
    val waitingTasks by viewModel.waitingTasks.collectAsState()
    val incorporations by viewModel.incorporations.collectAsState()
    val activeInc by viewModel.activeIncorporations.collectAsState()
    val contracts by viewModel.contracts.collectAsState()
    val contractsUnderReview by viewModel.contractsUnderReview.collectAsState()
    val requests by viewModel.managementRequests.collectAsState()
    val openRequests by viewModel.openManagementRequests.collectAsState()
    val finance by viewModel.financeFollowups.collectAsState()
    val overdueFin by viewModel.overdueFinanceFollowups.collectAsState()
    val opportunities by viewModel.opportunities.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Profile Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("executive_profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عمرو عادل أحمد",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "مدير الشؤون القانونية • شركتي لتكنولوجيا المعلومات",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«كل ملف تحت السيطرة، وكل مهمة في موعدها»",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        // Section: "ماذا يجب أن أفعل الآن؟" (What should I do right now?)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("what_to_do_now_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ماذا يجب أن أفعل الآن؟ (الأولويات الحرجة)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${urgentTasks.size + overdueTasks.size} إجراءات",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val topPriorities = (urgentTasks + overdueTasks).distinctBy { it.id }.take(3)
                    if (topPriorities.isEmpty()) {
                        Text(
                            text = "رائع! لا توجد مهام عاجلة أو متأخرة متراكمة حالياً.",
                            fontSize = 13.sp,
                            color = SuccessGreen,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        topPriorities.forEach { task ->
                            PriorityActionItem(
                                task = task,
                                onCheck = { viewModel.toggleTaskComplete(task) },
                                onClick = { onNavigateTo("tasks") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Executive Metrics Grid (2 columns)
        item {
            SectionHeader(
                title = "لوحة المؤشرات وحالة العمل",
                subtitle = "نظرة فورية على الملفات والتعاقدات النشطة"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExecutiveStatCard(
                        title = "مهام اليوم والمتأخرات",
                        count = "${overdueTasks.size + urgentTasks.size}",
                        icon = Icons.Default.Warning,
                        iconColor = DangerRed,
                        subtitle = "${overdueTasks.size} متأخرة • ${urgentTasks.size} عاجلة",
                        onClick = { onNavigateTo("tasks") },
                        modifier = Modifier.weight(1f)
                    )
                    ExecutiveStatCard(
                        title = "تأسيسات نشطة",
                        count = "${activeInc.size}",
                        icon = Icons.Default.Domain,
                        iconColor = ExecutiveBlue,
                        subtitle = "من إجمالي ${incorporations.size} ملفات",
                        onClick = { onNavigateTo("incorporations") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExecutiveStatCard(
                        title = "عقود قيد المراجعة",
                        count = "${contractsUnderReview.size}",
                        icon = Icons.Default.Description,
                        iconColor = LegalGold,
                        subtitle = "${contracts.size} عقود مسجلة",
                        onClick = { onNavigateTo("contracts") },
                        modifier = Modifier.weight(1f)
                    )
                    ExecutiveStatCard(
                        title = "طلبات الإدارة المفتوحة",
                        count = "${openRequests.size}",
                        icon = Icons.Default.AssignmentInd,
                        iconColor = PurpleStatus,
                        subtitle = "من CEO ومدراء الإدارات",
                        onClick = { onNavigateTo("management_requests") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExecutiveStatCard(
                        title = "متابعات مالية مستحقة",
                        count = "${overdueFin.size}",
                        icon = Icons.Default.AccountBalance,
                        iconColor = if (overdueFin.isNotEmpty()) DangerRed else SuccessGreen,
                        subtitle = "${finance.size} عمليات متابعة",
                        onClick = { onNavigateTo("finance") },
                        modifier = Modifier.weight(1f)
                    )
                    ExecutiveStatCard(
                        title = "فرص تعاقد جارية",
                        count = "${opportunities.size}",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconColor = InfoCyan,
                        subtitle = "قيد التفاوض والمراجعة",
                        onClick = { onNavigateTo("opportunities") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Navigation Buttons Row
        item {
            SectionHeader(title = "الوصول السريع للأقسام")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { onNavigateTo("ai_assistant") },
                        label = { Text("المساعد الذكي 🤖", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigateTo("tasks") },
                        label = { Text("إدارة المهام") },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigateTo("incorporations") },
                        label = { Text("تأسيس الشركات") },
                        leadingIcon = { Icon(Icons.Default.BusinessCenter, null) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigateTo("contracts") },
                        label = { Text("العقود القانونية") },
                        leadingIcon = { Icon(Icons.Default.Gavel, null) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigateTo("finance") },
                        label = { Text("المتابعة المالية") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigateTo("reports") },
                        label = { Text("التقارير") },
                        leadingIcon = { Icon(Icons.Default.BarChart, null) }
                    )
                }
            }
        }

        // Activity Logs Stream
        item {
            SectionHeader(
                title = "آخر التحديثات المسجلة على الملفات",
                subtitle = "سجل الإجراءات والأنشطة الإدارية والقانونية"
            )
        }

        items(logs.take(5)) { log ->
            ActivityLogCard(log = log, sdf = sdf)
        }
    }
}

@Composable
private fun PriorityActionItem(
    task: TaskEntity,
    onCheck: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == "مكتملة",
                onCheckedChange = { onCheck() },
                modifier = Modifier.testTag("checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.nextAction.isNotBlank()) {
                    Text(
                        text = "الخطوة التالية: ${task.nextAction}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            PriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun ActivityLogCard(log: ActivityLogEntity, sdf: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.entityType) {
                        "عقد" -> Icons.Default.Description
                        "تأسيس" -> Icons.Default.Domain
                        "مهمة" -> Icons.Default.Check
                        "متابعة مالية" -> Icons.Default.AccountBalance
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.summary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${log.entityType} • ${sdf.format(Date(log.timestamp))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
