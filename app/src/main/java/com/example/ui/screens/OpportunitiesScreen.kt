package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OpportunityEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val opportunities by viewModel.opportunities.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.testTag("opportunities_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_opportunity_fab")
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "إضافة فرصة تعاقد جديدة")
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
                    Icon(Icons.Default.Lightbulb, null, tint = LegalGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "متابعة مسار الصفقات والتعاقدات الجديدة المحتملة لشركتي لتكنولوجيا المعلومات، والمراجعة القانونية للعروض.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (opportunities.isEmpty()) {
                EmptyStateView(
                    title = "لا توجد فرص مسجلة",
                    description = "سجل الفرص والصفقات الجديدة لمتابعة التفاوض وصياغة العقود.",
                    icon = Icons.Default.Handshake
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(opportunities, key = { it.id }) { opp ->
                        OpportunityCard(opp = opp, sdf = sdf)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOpportunityDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newOpp ->
                viewModel.saveOpportunity(newOpp)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun OpportunityCard(
    opp: OpportunityEntity,
    sdf: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = opp.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = opp.stage)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "العميل: ${opp.clientName} • ${opp.serviceType}",
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
                    text = "القيمة التقديرية: ${opp.estimatedValue} ${opp.currency}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LegalGold
                )
                Text(
                    text = "المراجعة القانونية: ${opp.legalReviewStatus}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (opp.nextAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "الخطوة التالية: ${opp.nextAction}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تاريخ أول تواصل: ${sdf.format(Date(opp.firstContactAt))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المتابعة القادمة: ${sdf.format(Date(opp.nextFollowupAt))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddOpportunityDialog(
    onDismiss: () -> Unit,
    onSave: (OpportunityEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("تطوير برمجيات ومنصات سحابية") }
    var estimatedValue by remember { mutableStateOf("250000") }
    var stage by remember { mutableStateOf("فرصة جديدة") }
    var nextAction by remember { mutableStateOf("إعداد العرض الفني والمالي ومراجعته قانونياً") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل فرصة تعاقد جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الفرصة *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("اسم العميل / الجهة *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("نوع الخدمة") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estimatedValue,
                    onValueChange = { estimatedValue = it },
                    label = { Text("القيمة التقديرية (ج.م)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("الخطوة التالية") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && clientName.isNotBlank()) {
                        val num = (10..99).random()
                        onSave(
                            OpportunityEntity(
                                opportunityNumber = "OPP-2026-$num",
                                clientName = clientName,
                                title = title,
                                serviceType = serviceType,
                                estimatedValue = estimatedValue.toDoubleOrNull() ?: 0.0,
                                stage = stage,
                                nextAction = nextAction
                            )
                        )
                    }
                }
            ) {
                Text("حفظ الفرصة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
