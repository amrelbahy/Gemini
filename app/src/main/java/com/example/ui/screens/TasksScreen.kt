package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    var selectedFilter by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTaskForDetails by remember { mutableStateOf<TaskEntity?>(null) }

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val now = System.currentTimeMillis()

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "عاجلة" -> tasks.filter { it.priority == "عاجلة" && it.status != "مكتملة" }
            "متأخرة" -> tasks.filter { it.dueDate < now && it.status != "مكتملة" }
            "قيد التنفيذ" -> tasks.filter { it.status == "قيد التنفيذ" }
            "بانتظار طرف آخر" -> tasks.filter { it.status == "بانتظار طرف آخر" }
            "مكتملة" -> tasks.filter { it.status == "مكتملة" }
            else -> tasks
        }
    }

    Scaffold(
        modifier = modifier.testTag("tasks_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة جديدة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("الكل", "عاجلة", "متأخرة", "قيد التنفيذ", "بانتظار طرف آخر", "مكتملة")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                EmptyStateView(
                    title = "لا توجد مهام تطابق هذا التصنيف",
                    description = "يمكنك إضافة مهمة جديدة باستخدام زر الإضافة أسفل الشاشة.",
                    icon = Icons.Default.Checklist
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            sdf = sdf,
                            isOverdue = task.dueDate < now && task.status != "مكتملة",
                            onToggleComplete = { viewModel.toggleTaskComplete(task) },
                            onClick = { selectedTaskForDetails = task }
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newTask ->
                viewModel.saveTask(newTask)
                showAddDialog = false
            }
        )
    }

    // Task Details Bottom Sheet / Dialog
    selectedTaskForDetails?.let { task ->
        TaskDetailsDialog(
            task = task,
            sdf = sdf,
            onDismiss = { selectedTaskForDetails = null },
            onToggleComplete = {
                viewModel.toggleTaskComplete(task)
                selectedTaskForDetails = null
            },
            onArchive = {
                viewModel.archiveTask(task.id)
                selectedTaskForDetails = null
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    sdf: SimpleDateFormat,
    isOverdue: Boolean,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) DangerContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = task.status == "مكتملة",
                        onCheckedChange = { onToggleComplete() },
                        modifier = Modifier.testTag("task_checkbox_${task.id}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }

                PriorityBadge(priority = task.priority)
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(start = 38.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = task.status)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "قسم: ${task.category}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (isOverdue) "متأخرة! (${sdf.format(Date(task.dueDate))})" else sdf.format(Date(task.dueDate)),
                    fontSize = 11.sp,
                    color = if (isOverdue) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (task.nextAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 38.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "الخطوة التالية: ${task.nextAction}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            if (task.blockedReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سبب التعطل: ${task.blockedReason}",
                    fontSize = 11.sp,
                    color = DangerRed,
                    modifier = Modifier.padding(start = 38.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("شؤون قانونية") }
    var priority by remember { mutableStateOf("عالية") }
    var status by remember { mutableStateOf("قيد التنفيذ") }
    var nextAction by remember { mutableStateOf("") }
    var waitingFor by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مهمة وتكليف جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل المهمة") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("الأولوية (عاجلة/عالية/متوسطة)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("الخطوة التالية المحددة") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = waitingFor,
                    onValueChange = { waitingFor = it },
                    label = { Text("بانتظار طرف آخر (إن وجد)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            TaskEntity(
                                title = title,
                                description = description,
                                category = category,
                                priority = priority,
                                status = status,
                                nextAction = nextAction,
                                waitingFor = waitingFor
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("btn_save_task")
            ) {
                Text("حفظ المهمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun TaskDetailsDialog(
    task: TaskEntity,
    sdf: SimpleDateFormat,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onArchive: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                PriorityBadge(priority = task.priority)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "الحالة: ${task.status}", fontWeight = FontWeight.SemiBold)
                Text(text = "الموعد النهائي: ${sdf.format(Date(task.dueDate))}")
                Text(text = "المسؤول عن التنفيذ: ${task.assignedTo}")
                Text(text = "مقدم الطلب: ${task.requestedBy}")

                if (task.description.isNotBlank()) {
                    Divider()
                    Text(text = "التفاصيل:", fontWeight = FontWeight.Bold)
                    Text(text = task.description, fontSize = 13.sp)
                }

                if (task.nextAction.isNotBlank()) {
                    Divider()
                    Text(text = "الخطوة التالية: ${task.nextAction}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

                if (task.waitingFor.isNotBlank()) {
                    Text(text = "بانتظار: ${task.waitingFor}", color = WarningOrange)
                }

                if (task.blockedReason.isNotBlank()) {
                    Text(text = "سبب التعطل: ${task.blockedReason}", color = DangerRed)
                }
            }
        },
        confirmButton = {
            Button(onClick = onToggleComplete) {
                Text(if (task.status == "مكتملة") "إعادة فتح" else "إكمال المهمة")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onArchive) {
                    Text("أرشفة", color = DangerRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("إغلاق")
                }
            }
        }
    )
}
