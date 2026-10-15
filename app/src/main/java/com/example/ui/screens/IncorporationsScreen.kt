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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncorporationChecklistEntity
import com.example.data.model.IncorporationEntity
import com.example.data.model.IncorporationPartyEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncorporationsScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val incorporations by viewModel.incorporations.collectAsState()
    var selectedFilter by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedIncForDetails by remember { mutableStateOf<IncorporationEntity?>(null) }

    val filteredList = remember(incorporations, selectedFilter) {
        when (selectedFilter) {
            "نشط" -> incorporations.filter { it.status == "نشط" }
            "متوقف" -> incorporations.filter { it.status == "متوقف" }
            "مكتمل" -> incorporations.filter { it.status == "مكتمل" }
            else -> incorporations
        }
    }

    Scaffold(
        modifier = modifier.testTag("incorporations_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_incorporation_fab")
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = "تأسيس شركة جديدة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("الكل", "نشط", "متوقف", "مكتمل")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "لا توجد ملفات تأسيس",
                    description = "يمكنك فتح ملف تأسيس جديد لعميل وتسجيل الشركاء والمراحل.",
                    icon = Icons.Default.Domain
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { inc ->
                        IncorporationCard(
                            inc = inc,
                            onClick = { selectedIncForDetails = inc }
                        )
                    }
                }
            }
        }
    }

    // Add Incorporation Dialog
    if (showAddDialog) {
        AddIncorporationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newInc ->
                viewModel.saveIncorporation(newInc)
                showAddDialog = false
            }
        )
    }

    // Incorporation Detail & 10 Stages Checklist Dialog
    selectedIncForDetails?.let { inc ->
        IncorporationDetailDialog(
            inc = inc,
            viewModel = viewModel,
            onDismiss = { selectedIncForDetails = null },
            onStageChanged = { newStage ->
                viewModel.updateIncorporationStage(inc, newStage)
            }
        )
    }
}

@Composable
fun IncorporationCard(
    inc: IncorporationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("inc_card_${inc.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inc.companyName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = inc.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "العميل: ${inc.clientName} • ملف: ${inc.fileNumber}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "الشكل القانوني: ${inc.legalForm}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stage Progress Bar (10 Stages)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "المرحلة الحالية: ${inc.currentStageIndex + 1} من 10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${((inc.currentStageIndex + 1) * 10)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (inc.currentStageIndex + 1) / 10f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (inc.status == "متوقف") DangerRed else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (inc.missingDocuments.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DangerContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "نواقص: ${inc.missingDocuments}",
                            fontSize = 11.sp,
                            color = DangerRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (inc.nextAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الخطوة التالية: ${inc.nextAction}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Fees summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "أتعاب الشركة: ${inc.professionalFees} ${inc.currency}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المحصل: ${inc.amountCollected} ${inc.currency}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun AddIncorporationDialog(
    onDismiss: () -> Unit,
    onSave: (IncorporationEntity) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var legalForm by remember { mutableStateOf("شركة ذات مسؤولية محدودة (ش.ذ.م.م)") }
    var activityDescription by remember { mutableStateOf("تكنولوجيا المعلومات والبرمجيات") }
    var professionalFees by remember { mutableStateOf("35000") }
    var nextAction by remember { mutableStateOf("مراجعة بيانات الشركاء واستلام التوكيلات") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فتح ملف تأسيس شركة جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("الاسم المقترح للشركة *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("اسم العميل / المفوض *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = legalForm,
                    onValueChange = { legalForm = it },
                    label = { Text("الشكل القانوني") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = professionalFees,
                    onValueChange = { professionalFees = it },
                    label = { Text("أتعاب الشركة (ج.م)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("الخطوة التالية المحددة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isNotBlank() && clientName.isNotBlank()) {
                        val randomNum = (100..999).random()
                        onSave(
                            IncorporationEntity(
                                fileNumber = "INC-2026-$randomNum",
                                companyName = companyName,
                                clientName = clientName,
                                legalForm = legalForm,
                                activityDescription = activityDescription,
                                professionalFees = professionalFees.toDoubleOrNull() ?: 0.0,
                                nextAction = nextAction
                            )
                        )
                    }
                }
            ) {
                Text("إنشاء الملف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun IncorporationDetailDialog(
    inc: IncorporationEntity,
    viewModel: LegalDeskViewModel,
    onDismiss: () -> Unit,
    onStageChanged: (Int) -> Unit
) {
    val checklist by viewModel.getChecklistForInc(inc.id).collectAsState(initial = emptyList())
    val parties by viewModel.getPartiesForInc(inc.id).collectAsState(initial = emptyList())

    val stagesNames = listOf(
        "١. استلام الطلب والبيانات الأولية",
        "٢. مراجعة النشاط وتحديد الشكل القانوني",
        "٣. حجز ومراجعة أسماء الشركات المقترحة",
        "٤. استلام المستندات والتوكيلات الرسمية",
        "٥. إعداد وتوثيق عقود ومستندات التأسيس",
        "٦. تقديم الملف ومتابعة الهيئة المختصة",
        "٧. استخراج السجل التجاري وشهادة التأسيس",
        "٨. فتح الملف الضريبي والبطاقة الضريبية",
        "٩. متطلبات الفاتورة الإلكترونية والتأمينات",
        "١٠. مراجعة اكتمال الملف وتسليم المستندات"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(inc.companyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("ملف: ${inc.fileNumber} • ${inc.legalForm}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("المراحل الـ 10 للتأسيس وقائمة التحقق:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                items(stagesNames.mapIndexed { idx, name -> Pair(idx, name) }) { (idx, stageTitle) ->
                    val isCurrent = inc.currentStageIndex == idx
                    val isPassed = inc.currentStageIndex > idx

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStageChanged(idx) },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                isPassed -> SuccessContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isPassed -> Icons.Default.CheckCircle
                                    isCurrent -> Icons.Default.PlayCircle
                                    else -> Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = null,
                                tint = when {
                                    isPassed -> SuccessGreen
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stageTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (parties.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("الشركاء والمديرون المسجلون:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    items(parties) { party ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(party.partyName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${party.ownershipPercentage}%", fontWeight = FontWeight.Bold, color = LegalGold)
                                }
                                Text("الصفة: ${party.partyRole}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (party.managerAuthorityNotes.isNotBlank()) {
                                    Text("الصلاحيات: ${party.managerAuthorityNotes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
