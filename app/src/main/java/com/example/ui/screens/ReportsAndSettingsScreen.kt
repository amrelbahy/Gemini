package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.LegalDeskViewModel

@Composable
fun ReportsAndSettingsScreen(
    viewModel: LegalDeskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val contracts by viewModel.contracts.collectAsState()
    val incorporations by viewModel.incorporations.collectAsState()
    val finance by viewModel.financeFollowups.collectAsState()
    val requests by viewModel.managementRequests.collectAsState()
    val opportunities by viewModel.opportunities.collectAsState()

    var backupJsonPreview by remember { mutableStateOf<String?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var pinLockEnabled by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    var showCopiedToast by remember { mutableStateOf(false) }

    val completedTasksCount = tasks.count { it.status == "مكتملة" }
    val totalContractValue = contracts.sumOf { it.contractValue }
    val totalFinanceDue = finance.sumOf { it.amountDue }
    val totalFinancePaid = finance.sumOf { it.amountPaid }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: KPIs Summary
        item {
            SectionHeader(
                title = "مؤشرات الأداء والتقارير التنفيذية",
                subtitle = "تحليل رقمي شامل لحجم الأعمال والإنجاز"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "معدل إنجاز المهام", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$completedTasksCount من ${tasks.size} (${if (tasks.isNotEmpty()) (completedTasksCount * 100 / tasks.size) else 0}%)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "إجمالي القيمة التعاقدية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$totalContractValue ج.م",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LegalGold
                            )
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "المستحقات المالية المسجلة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$totalFinanceDue ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "المحصل فعلياً", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$totalFinancePaid ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ملفات التأسيس النشطة: ${incorporations.count { it.status == "نشط" }}", fontSize = 12.sp)
                        Text(text = "طلبات الإدارة المفتوحة: ${requests.count { it.status != "منجز" }}", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: Backup & Restore
        item {
            SectionHeader(
                title = "النسخ الاحتياطي وسلامة البيانات",
                subtitle = "تصدير متكامل لقاعدة البيانات المحلية واستعادتها بأمان"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يتم حفظ كافة البيانات محلياً وبشكل دائم على الجهاز عبر Room SQLite. يمكنك أخذ نسخة احتياطية مشفرة بصيغة JSON لنقلها أو حفظها في سحابة خاصة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Button(
                        onClick = {
                            viewModel.createBackup { json ->
                                backupJsonPreview = json
                                showBackupDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_backup"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء وتصدير نسخة احتياطية كاملة (JSON)")
                    }
                }
            }
        }

        // Section: Security & Privacy Settings
        item {
            SectionHeader(
                title = "الأمان والخصوصية المهنية",
                subtitle = "حماية السجلات القانونية والعقود"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "قفل التطبيق برمز مرور PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "حماية الملفات الحساسة عند ترك الهاتف",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pinLockEnabled,
                            onCheckedChange = { pinLockEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نظام تشغيل محلي آمن لا يشارك البيانات السرية تلقائياً.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // App Information Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sherkety Legal Desk – مكتب عمرو عادل v1.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "شركة شركتي لتكنولوجيا المعلومات (Sherkety for IT)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showBackupDialog && backupJsonPreview != null) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("تم إنشاء النسخة الاحتياطية بنجاح", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تحتوي النسخة على كافة الجداول والعلاقات:\n• ${tasks.size} مهمة\n• ${contracts.size} عقود قانونية\n• ${incorporations.size} ملفات تأسيس\n• ${finance.size} متابعات مالية\n• ${requests.size} تكليفات إدارة",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "يمكنك نسخ نص الـ JSON وحفظه في ملف آمن أو إرساله لسحابة شركتي الخاصة.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(backupJsonPreview!!))
                        showBackupDialog = false
                        showCopiedToast = true
                    }
                ) {
                    Text("نسخ JSON للحافظة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("إغلاق") }
            }
        )
    }

    if (showCopiedToast) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = { showCopiedToast = false }) { Text("حسناً") } }
        ) {
            Text("تم نسخ كود النسخة الاحتياطية الكامل إلى الحافظة بنجاح.")
        }
    }
}
