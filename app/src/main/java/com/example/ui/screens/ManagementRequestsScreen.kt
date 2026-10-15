package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ManagementRequestEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementRequestsScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.managementRequests.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedReq by remember { mutableStateOf<ManagementRequestEntity?>(null) }
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.testTag("management_requests_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_request_fab")
            ) {
                Icon(Icons.Default.AddComment, contentDescription = "تسجيل طلب إدارة جديد")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سجل التكليفات الواردة من المدير التنفيذي (CEO) والمدير المالي ومديري الإدارات، بما في ذلك التكليفات الشفهية.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (requests.isEmpty()) {
                EmptyStateView(
                    title = "لا توجد طلبات إدارة مفتوحة",
                    description = "يمكنك توثيق أي طلب شفهي أو رسمي من الإدارة العليا هنا.",
                    icon = Icons.Default.AssignmentInd
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(requests, key = { it.id }) { req ->
                        RequestCard(
                            request = req,
                            sdf = sdf,
                            onConvertToTask = { viewModel.convertRequestToTask(req) },
                            onDetails = { selectedReq = req }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddManagementRequestDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newReq ->
                viewModel.saveManagementRequest(newReq)
                showAddDialog = false
            }
        )
    }

    selectedReq?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedReq = null },
            title = { Text(req.requestTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("مقدم الطلب: ${req.requesterName} (${req.department})", fontWeight = FontWeight.SemiBold)
                    Text("القناة: ${req.sourceChannel} • الأولوية: ${req.priority}")
                    Text("الموعد المطلوب: ${sdf.format(Date(req.dueDate))}")
                    Divider()
                    Text("الوصف:", fontWeight = FontWeight.Bold)
                    Text(req.description, fontSize = 13.sp)
                    if (req.responseNotes.isNotBlank()) {
                        Divider()
                        Text("الإجراء المتخذ: ${req.responseNotes}", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedReq = null }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
fun RequestCard(
    request: ManagementRequestEntity,
    sdf: SimpleDateFormat,
    onConvertToTask: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_card_${request.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.requestTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                PriorityBadge(priority = request.priority)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${request.requesterName} • ${request.department} (${request.sourceChannel})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = request.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = request.status)

                Row {
                    OutlinedButton(
                        onClick = onConvertToTask,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_convert_to_task_${request.id}")
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تحويل لمهمة", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onDetails, modifier = Modifier.height(34.dp)) {
                        Text("التفاصيل", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddManagementRequestDialog(
    onDismiss: () -> Unit,
    onSave: (ManagementRequestEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var requester by remember { mutableStateOf("الرئيس التنفيذي (CEO - م. أحمد عبد العزيز)") }
    var department by remember { mutableStateOf("الإدارة التنفيذية") }
    var description by remember { mutableStateOf("") }
    var sourceChannel by remember { mutableStateOf("شفهي") }
    var priority by remember { mutableStateOf("عالية") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("توثيق طلب أو تكليف إداري جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الطلب / التكليف *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = requester,
                    onValueChange = { requester = it },
                    label = { Text("مقدم الطلب (الاسم والمنصب) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("الإدارة") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sourceChannel,
                        onValueChange = { sourceChannel = it },
                        label = { Text("القناة (شفهي/إيميل)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("نص وتفاصيل التكليف *") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && requester.isNotBlank()) {
                        val num = (10..99).random()
                        onSave(
                            ManagementRequestEntity(
                                requestNumber = "REQ-2026-$num",
                                requesterName = requester,
                                department = department,
                                requestTitle = title,
                                description = description,
                                sourceChannel = sourceChannel,
                                priority = priority
                            )
                        )
                    }
                }
            ) {
                Text("حفظ التكليف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
