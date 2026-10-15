package com.example.ui.screens

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
import com.example.data.model.FinanceFollowupEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val financeList by viewModel.financeFollowups.collectAsState()
    var selectedFilter by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    val filteredList = remember(financeList, selectedFilter) {
        when (selectedFilter) {
            "متأخر" -> financeList.filter { it.status == "متأخر" }
            "مستحق" -> financeList.filter { it.status == "مستحق" || it.status == "مستحق قريباً" }
            "بانتظار التأكيد" -> financeList.filter { it.financeConfirmationStatus == "بانتظار التأكيد" }
            else -> financeList
        }
    }

    Scaffold(
        modifier = modifier.testTag("finance_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_finance_fab")
            ) {
                Icon(Icons.Default.AddCard, contentDescription = "تسجيل متابعة مالية")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Explanatory note from user prompt
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = GoldContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBalance, null, tint = LegalGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "متابعة قانونية وإدارية للمستحقات والدفعات المرتبطة بالعقود والتأسيسات وليست بديلاً عن برنامج المحاسبة الرسمي.",
                        fontSize = 11.sp,
                        color = GoldOnContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("الكل", "متأخر", "مستحق", "بانتظار التأكيد")
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
                    title = "لا توجد مستحقات في هذا التصنيف",
                    description = "يمكنك تسجيل متابعة دفعة تعاقدية أو مطالبة مالية لعميل أو مورد.",
                    icon = Icons.Default.AttachMoney
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        FinanceFollowupCard(item = item, sdf = sdf)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFinanceDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newFin ->
                viewModel.saveFinanceFollowup(newFin)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FinanceFollowupCard(
    item: FinanceFollowupEntity,
    sdf: SimpleDateFormat
) {
    val remaining = item.amountDue - item.amountPaid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finance_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.status == "متأخر") DangerContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.counterpartyName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.transactionType} • مرجع: ${item.referenceNumber}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "المبلغ المطلوب:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${item.amountDue} ${item.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "المسدد / المحصل:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${item.amountPaid} ${item.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Column {
                        Text(text = "المتبقي:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "$remaining ${item.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (remaining > 0) DangerRed else SuccessGreen)
                    }
                }
            }

            if (item.disputeNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات النزاع / التعطيل: ${item.disputeNotes}",
                    fontSize = 11.sp,
                    color = DangerRed
                )
            }

            if (item.nextAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الإجراء التالي: ${item.nextAction}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "حالة التأكيد المالي: ${item.financeConfirmationStatus}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "الاستحقاق: ${sdf.format(Date(item.dueDate))}",
                    fontSize = 11.sp,
                    color = if (item.status == "متأخر") DangerRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddFinanceDialog(
    onDismiss: () -> Unit,
    onSave: (FinanceFollowupEntity) -> Unit
) {
    var counterparty by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("دفعة تعاقدية مستحقة") }
    var amountDue by remember { mutableStateOf("50000") }
    var amountPaid by remember { mutableStateOf("0") }
    var nextAction by remember { mutableStateOf("متابعة التحويل البنكي مع الحسابات") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل متابعة مالية جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    label = { Text("الطرف (العميل أو المورد) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = transactionType,
                    onValueChange = { transactionType = it },
                    label = { Text("نوع المعاملة") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountDue,
                        onValueChange = { amountDue = it },
                        label = { Text("المبلغ المستحق (ج.م) *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = { amountPaid = it },
                        label = { Text("المسدد حالياً") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("الإجراء التالي المحدد") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (counterparty.isNotBlank()) {
                        val num = (10..99).random()
                        onSave(
                            FinanceFollowupEntity(
                                referenceNumber = "FIN-2026-$num",
                                counterpartyName = counterparty,
                                transactionType = transactionType,
                                amountDue = amountDue.toDoubleOrNull() ?: 0.0,
                                amountPaid = amountPaid.toDoubleOrNull() ?: 0.0,
                                nextAction = nextAction
                            )
                        )
                    }
                }
            ) {
                Text("حفظ المتابعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
