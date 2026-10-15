package com.example.ui.screens

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
import com.example.data.model.ContractEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val contracts by viewModel.contracts.collectAsState()
    var selectedFilter by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedContractForDetails by remember { mutableStateOf<ContractEntity?>(null) }

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    val filteredContracts = remember(contracts, selectedFilter) {
        when (selectedFilter) {
            "قيد المراجعة" -> contracts.filter { it.reviewStatus == "قيد المراجعة" }
            "بانتظار التوقيع" -> contracts.filter { it.signatureStatus == "بانتظار التوقيع" }
            "ساري وموقع" -> contracts.filter { it.signatureStatus == "موقع رسمياً" }
            else -> contracts
        }
    }

    Scaffold(
        modifier = modifier.testTag("contracts_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_contract_fab")
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = "إضافة عقد جديد")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("الكل", "قيد المراجعة", "بانتظار التوقيع", "ساري وموقع")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            if (filteredContracts.isEmpty()) {
                EmptyStateView(
                    title = "لا توجد عقود مسجلة",
                    description = "يمكنك إضافة عقد جديد وتتبع دورة المراجعة القانونية والالتزامات.",
                    icon = Icons.Default.Description
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredContracts, key = { it.id }) { contract ->
                        ContractCard(
                            contract = contract,
                            sdf = sdf,
                            onClick = { selectedContractForDetails = contract }
                        )
                    }
                }
            }
        }
    }

    // Add Contract Dialog
    if (showAddDialog) {
        AddContractDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newContract ->
                viewModel.saveContract(newContract)
                showAddDialog = false
            }
        )
    }

    // Contract Details Dialog
    selectedContractForDetails?.let { contract ->
        ContractDetailDialog(
            contract = contract,
            sdf = sdf,
            viewModel = viewModel,
            onDismiss = { selectedContractForDetails = null },
            onUpdate = { updated ->
                viewModel.saveContract(updated)
                selectedContractForDetails = null
            }
        )
    }
}

@Composable
fun ContractCard(
    contract: ContractEntity,
    sdf: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("contract_card_${contract.id}"),
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
                    text = contract.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = contract.reviewStatus)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الطرف المقابل: ${contract.counterpartyName} • رقم: ${contract.contractNumber}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (contract.contractValue > 0) "${contract.contractValue} ${contract.currency}" else "اتفاقية غير مالية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LegalGold
                )
                StatusBadge(status = contract.signatureStatus)
            }

            if (contract.nextAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "الخطوة التالية: ${contract.nextAction}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (contract.riskNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات المخاطر: ${contract.riskNotes}",
                    fontSize = 11.sp,
                    color = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تاريخ السريان: ${sdf.format(Date(contract.startDate))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "مهلة الإخطار: ${sdf.format(Date(contract.noticeDeadline))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddContractDialog(
    onDismiss: () -> Unit,
    onSave: (ContractEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var counterpartyName by remember { mutableStateOf("") }
    var contractType by remember { mutableStateOf("عقد تقديم خدمات تقنية") }
    var contractValue by remember { mutableStateOf("150000") }
    var nextAction by remember { mutableStateOf("مراجعة بنود المسؤولية وغرامات التأخير") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل عقد ومستند قانوني جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم العقد *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = counterpartyName,
                    onValueChange = { counterpartyName = it },
                    label = { Text("الطرف المقابل *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contractType,
                    onValueChange = { contractType = it },
                    label = { Text("نوع العقد") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contractValue,
                    onValueChange = { contractValue = it },
                    label = { Text("قيمة العقد (ج.م)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("الإجراء التالي والموعد") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && counterpartyName.isNotBlank()) {
                        val rand = (100..999).random()
                        onSave(
                            ContractEntity(
                                contractNumber = "SHR-CTR-2026-$rand",
                                title = title,
                                counterpartyName = counterpartyName,
                                contractType = contractType,
                                contractValue = contractValue.toDoubleOrNull() ?: 0.0,
                                nextAction = nextAction
                            )
                        )
                    }
                }
            ) {
                Text("حفظ العقد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun ContractDetailDialog(
    contract: ContractEntity,
    sdf: SimpleDateFormat,
    viewModel: LegalDeskViewModel,
    onDismiss: () -> Unit,
    onUpdate: (ContractEntity) -> Unit
) {
    val obligations by viewModel.getObligationsForContract(contract.id).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(contract.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("رقم: ${contract.contractNumber} • ${contract.contractType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "الطرف المقابل: ${contract.counterpartyName}", fontWeight = FontWeight.SemiBold)
                Text(text = "القيمة: ${contract.contractValue} ${contract.currency}")
                Text(text = "حالة المراجعة: ${contract.reviewStatus}")
                Text(text = "حالة التوقيع: ${contract.signatureStatus}")
                Text(text = "نهاية العقد: ${sdf.format(Date(contract.endDate))}")
                Text(text = "مهلة الإخطار: ${sdf.format(Date(contract.noticeDeadline))}", color = DangerRed, fontWeight = FontWeight.Bold)

                if (contract.riskNotes.isNotBlank()) {
                    Divider()
                    Text(text = "المخاطر القانونية:", fontWeight = FontWeight.Bold, color = DangerRed)
                    Text(text = contract.riskNotes, fontSize = 12.sp)
                }

                if (obligations.isNotEmpty()) {
                    Divider()
                    Text(text = "الالتزامات التعاقدية:", fontWeight = FontWeight.Bold)
                    obligations.forEach { ob ->
                        Text(text = "• ${ob.description} (${ob.responsibleParty})", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(contract.copy(signatureStatus = "موقع رسمياً", reviewStatus = "تمت المراجعة"))
                }
            ) {
                Text("اعتماد وتوثيق التوقيع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
