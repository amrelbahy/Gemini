package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val entityType: String = "عميل", // عميل، شركة تابعة، مورد، جهة حكومية، شريك
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String = "عام", // شؤون قانونية، تأسيس، عقود، طلبات إدارة، متابعة مالية، شخصي
    val priority: String = "متوسطة", // عاجلة، عالية، متوسطة، منخفضة
    val status: String = "جديدة", // جديدة، لم تبدأ، قيد التنفيذ، متوقفة، بانتظار طرف آخر، مكتملة، ملغاة
    val sourceType: String = "", // عقد، تأسيس، طلب إدارة، فرصة، اجتماع
    val sourceId: String = "",
    val contactId: String = "",
    val parentTaskId: String? = null,
    val isSelfAssigned: Boolean = true, // هل أقوم بتنفيذها بنفسي أم أتابع تنفيذها
    val assignedTo: String = "عمرو عادل",
    val requestedBy: String = "عمرو عادل",
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 86400000L * 2, // default 2 days
    val completedAt: Long? = null,
    val nextAction: String = "",
    val blockedReason: String = "",
    val waitingFor: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)

@Entity(tableName = "sub_tasks")
data class SubTaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incorporations")
data class IncorporationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fileNumber: String,
    val clientId: String = "",
    val clientName: String,
    val companyName: String,
    val alternativeNames: String = "",
    val legalForm: String = "شركة ذات مسؤولية محدودة (ش.ذ.م.م)", // شركة مساهمة، شركة شخص واحد، تضامن، فرع شركة أجنبية
    val activityDescription: String = "تكنولوجيا المعلومات والبرمجيات",
    val jurisdiction: String = "الهيئة العامة للاستثمار - القاهرة",
    val currentStageIndex: Int = 0, // 0 to 9
    val status: String = "نشط", // نشط، مكتمل، متوقف، ملغى
    val receivedAt: Long = System.currentTimeMillis(),
    val startedAt: Long = System.currentTimeMillis(),
    val targetCompletionDate: Long = System.currentTimeMillis() + 86400000L * 14,
    val completedAt: Long? = null,
    val professionalFees: Double = 0.0,
    val governmentFeesEstimated: Double = 0.0,
    val governmentFeesActual: Double = 0.0,
    val amountCollected: Double = 0.0,
    val currency: String = "EGP",
    val missingDocuments: String = "",
    val nextAction: String = "",
    val blockedReason: String = "",
    val waitingForPerson: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)

@Entity(tableName = "incorporation_parties")
data class IncorporationPartyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val incorporationId: String,
    val partyName: String,
    val partyRole: String = "شريك", // شريك، مدير، رئيس مجلس إدارة، وكيل مؤسسين
    val ownershipPercentage: Double = 0.0,
    val managerAuthorityNotes: String = "",
    val documentStatus: String = "مكتمل", // مكتمل، ناقص توكيل، ناقص هوية
    val notes: String = ""
)

@Entity(tableName = "incorporation_checklists")
data class IncorporationChecklistEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val incorporationId: String,
    val stageIndex: Int,
    val stageTitle: String,
    val itemName: String,
    val isRequired: Boolean = true,
    val isDone: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val contractNumber: String,
    val title: String,
    val contractType: String = "عقد تقديم خدمات تقنية", // تطوير أنظمة، تراخيص، NDA، عقد مورد، عقد عمل، إنذار قانوني
    val counterpartyId: String = "",
    val counterpartyName: String,
    val requestingDepartment: String = "الإدارة التنفيذية (CEO)", // العمليات، الموارد البشرية، المبيعات، المالية
    val contractValue: Double = 0.0,
    val currency: String = "EGP",
    val reviewStatus: String = "قيد المراجعة", // مسودة، قيد المراجعة، تمت المراجعة، بانتظار تعديل
    val negotiationStatus: String = "جاري التفاوض", // لم يبدأ، جاري التفاوض، متفق عليه
    val approvalStatus: String = "بانتظار الاعتماد", // مسودة، بانتظار الاعتماد، معتمد داخلياً
    val signatureStatus: String = "غير موقع", // بانتظار التوقيع، موقع جزئياً، موقع رسمياً
    val executionStatus: String = "ساري", // ساري، متوقف، منتهٍ، مجدد، مفسوخ
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 86400000L * 365,
    val renewalDate: Long = System.currentTimeMillis() + 86400000L * 335,
    val noticeDeadline: Long = System.currentTimeMillis() + 86400000L * 300,
    val nextAction: String = "",
    val riskNotes: String = "",
    val notes: String = "",
    val isFinalVersion: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)

@Entity(tableName = "contract_obligations")
data class ContractObligationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val contractId: String,
    val description: String,
    val responsibleParty: String = "شركتي لتكنولوجيا المعلومات", // شركتي أو الطرف الآخر
    val dueDate: Long = System.currentTimeMillis() + 86400000L * 30,
    val status: String = "قيد التنفيذ", // قيد التنفيذ، منجز، متأخر
    val notes: String = ""
)

@Entity(tableName = "management_requests")
data class ManagementRequestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestNumber: String,
    val requesterName: String, // الرئيس التنفيذي (CEO)، المدير المالي (CFO)، مدير العمليات (COO)...
    val department: String = "الإدارة التنفيذية",
    val requestTitle: String,
    val description: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val sourceChannel: String = "شفهي", // شفهي، بريد إلكتروني، اجتماع رسمي، واتساب
    val priority: String = "عالية", // عاجلة، عالية، متوسطة، منخفضة
    val dueDate: Long = System.currentTimeMillis() + 86400000L * 3,
    val status: String = "مفتوح", // مفتوح، قيد الإجراء، بانتظار رد، منجز، مغلق
    val clarityLevel: String = "واضح", // واضح، يحتاج تفاصيل، غامض
    val relatedContractId: String? = null,
    val relatedIncorporationId: String? = null,
    val responseNotes: String = "",
    val waitingForDepartment: String = "",
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "finance_followups")
data class FinanceFollowupEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val referenceNumber: String,
    val counterpartyId: String = "",
    val counterpartyName: String,
    val contractId: String = "",
    val transactionType: String = "مستحقات عميل", // مستحقات عميل، دفعة متأخرة، دفعة تعاقدية مستحقة، رسوم حكومية، أتعاب استشارة، مراجعة مع المحاسب
    val invoiceReference: String = "",
    val amountDue: Double = 0.0,
    val amountPaid: Double = 0.0,
    val currency: String = "EGP",
    val dueDate: Long = System.currentTimeMillis() + 86400000L * 7,
    val status: String = "مستحق", // مستحق، مستحق قريباً، متأخر، قيد المراجعة، بانتظار تأكيد مالي، مسدد بالكامل، محل نزاع
    val financeConfirmationStatus: String = "بانتظار التأكيد", // مؤكد رسمياً، بانتظار التأكيد، غير مطابق
    val disputeNotes: String = "",
    val responsibleFollowupPerson: String = "عمرو عادل",
    val nextAction: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val opportunityNumber: String,
    val contactId: String = "",
    val clientName: String,
    val companyName: String = "",
    val title: String,
    val serviceType: String = "تطوير أنظمة سحابية", // تطوير أنظمة، استشارات تقنية، رخص برمجيات، عقود صيانة
    val source: String = "تواصل مباشر", // تواصل مباشر، توصية، مؤتمر تقني، موقع الشركة
    val estimatedValue: Double = 0.0,
    val currency: String = "EGP",
    val stage: String = "فرصة جديدة", // فرصة جديدة، تواصل أولي، جمع متطلبات، إعداد عرض، تفاوض، مراجعة قانونية وتجارية، انتظار اعتماد، تم التعاقد، أغلقت دون تعاقد
    val firstContactAt: Long = System.currentTimeMillis(),
    val nextFollowupAt: Long = System.currentTimeMillis() + 86400000L * 3,
    val proposalStatus: String = "مسودة", // مسودة، تم الإرسال، معتمد، مرفوض
    val legalReviewStatus: String = "قيد المراجعة", // لم تبدأ، قيد المراجعة، معتمد
    val expectedCloseDate: Long = System.currentTimeMillis() + 86400000L * 30,
    val closedReason: String = "",
    val nextAction: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val meetingDate: Long = System.currentTimeMillis(),
    val participants: String = "عمرو عادل",
    val summary: String = "",
    val decisions: String = "",
    val nextAction: String = "",
    val relatedEntityId: String = "",
    val relatedEntityType: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val fileType: String = "PDF",
    val fileSizeKb: Int = 120,
    val documentCategory: String = "عقد", // عقد، تأسيس، توكيل، سجل تجاري، بطاقة ضريبية، إنذار، محضر، قرار إداري
    val relatedEntityType: String = "عقد", // عقد، تأسيس، طلب إدارة، عميل
    val relatedEntityId: String = "",
    val relatedEntityTitle: String = "",
    val versionNumber: String = "v1.0",
    val isFinalVersion: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val actionType: String, // إنشاء، تعديل، إكمال، أرشفة، استعادة
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)
