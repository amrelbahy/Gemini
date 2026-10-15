package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LegalDeskRepository(private val db: AppDatabase) {

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    val overdueTasks: Flow<List<TaskEntity>> = db.taskDao().getOverdueTasks()
    val urgentTasks: Flow<List<TaskEntity>> = db.taskDao().getUrgentTasks()
    val waitingTasks: Flow<List<TaskEntity>> = db.taskDao().getWaitingTasks()

    fun getSubTasks(taskId: String): Flow<List<SubTaskEntity>> = db.taskDao().getSubTasks(taskId)

    suspend fun saveTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        db.taskDao().insert(task)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "مهمة",
                entityId = task.id,
                actionType = "حفظ",
                summary = "حفظ مهمة: ${task.title} (الحالة: ${task.status})"
            )
        )
    }

    suspend fun completeTask(taskId: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        val status = if (isCompleted) "مكتملة" else "قيد التنفيذ"
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        db.taskDao().updateStatus(taskId, status, completedAt)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "مهمة",
                entityId = taskId,
                actionType = if (isCompleted) "إكمال" else "إعادة فتح",
                summary = "تغيير حالة المهمة إلى: $status"
            )
        )
    }

    suspend fun archiveTask(taskId: String) = withContext(Dispatchers.IO) {
        db.taskDao().archive(taskId)
    }

    suspend fun addSubTask(subTask: SubTaskEntity) = withContext(Dispatchers.IO) {
        db.taskDao().insertSubTask(subTask)
    }

    suspend fun updateSubTask(subTask: SubTaskEntity) = withContext(Dispatchers.IO) {
        db.taskDao().updateSubTask(subTask)
    }

    suspend fun deleteSubTask(id: String) = withContext(Dispatchers.IO) {
        db.taskDao().deleteSubTask(id)
    }

    // Incorporations
    val allIncorporations: Flow<List<IncorporationEntity>> = db.incorporationDao().getAllIncorporations()
    val activeIncorporations: Flow<List<IncorporationEntity>> = db.incorporationDao().getActiveIncorporations()

    fun getIncorporationParties(incId: String): Flow<List<IncorporationPartyEntity>> =
        db.incorporationDao().getParties(incId)

    fun getIncorporationChecklist(incId: String): Flow<List<IncorporationChecklistEntity>> =
        db.incorporationDao().getChecklist(incId)

    suspend fun saveIncorporation(incorporation: IncorporationEntity) = withContext(Dispatchers.IO) {
        db.incorporationDao().insert(incorporation)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "تأسيس",
                entityId = incorporation.id,
                actionType = "حفظ",
                summary = "حفظ ملف تأسيس: ${incorporation.companyName} (المرحلة ${incorporation.currentStageIndex + 1})"
            )
        )
    }

    suspend fun saveIncorporationParty(party: IncorporationPartyEntity) = withContext(Dispatchers.IO) {
        db.incorporationDao().insertParty(party)
    }

    suspend fun updateChecklistItem(item: IncorporationChecklistEntity) = withContext(Dispatchers.IO) {
        db.incorporationDao().updateChecklistItem(item)
    }

    suspend fun archiveIncorporation(id: String) = withContext(Dispatchers.IO) {
        db.incorporationDao().archive(id)
    }

    // Contracts
    val allContracts: Flow<List<ContractEntity>> = db.contractDao().getAllContracts()
    val contractsUnderReview: Flow<List<ContractEntity>> = db.contractDao().getContractsUnderReview()

    fun getContractObligations(contractId: String): Flow<List<ContractObligationEntity>> =
        db.contractDao().getObligations(contractId)

    suspend fun saveContract(contract: ContractEntity) = withContext(Dispatchers.IO) {
        db.contractDao().insert(contract)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "عقد",
                entityId = contract.id,
                actionType = "حفظ",
                summary = "حفظ العقد: ${contract.title} - ${contract.counterpartyName}"
            )
        )
    }

    suspend fun saveContractObligation(obligation: ContractObligationEntity) = withContext(Dispatchers.IO) {
        db.contractDao().insertObligation(obligation)
    }

    suspend fun updateContractObligation(obligation: ContractObligationEntity) = withContext(Dispatchers.IO) {
        db.contractDao().updateObligation(obligation)
    }

    suspend fun archiveContract(id: String) = withContext(Dispatchers.IO) {
        db.contractDao().archive(id)
    }

    // Management Requests
    val allManagementRequests: Flow<List<ManagementRequestEntity>> = db.managementRequestDao().getAllRequests()
    val openManagementRequests: Flow<List<ManagementRequestEntity>> = db.managementRequestDao().getOpenRequests()

    suspend fun saveManagementRequest(request: ManagementRequestEntity) = withContext(Dispatchers.IO) {
        db.managementRequestDao().insert(request)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "طلب إدارة",
                entityId = request.id,
                actionType = "حفظ",
                summary = "تسجيل طلب إدارة من: ${request.requesterName} - ${request.requestTitle}"
            )
        )
    }

    suspend fun convertRequestToTask(request: ManagementRequestEntity) = withContext(Dispatchers.IO) {
        val task = TaskEntity(
            title = "تكليف الإدارة: ${request.requestTitle}",
            description = "مقدم الطلب: ${request.requesterName} (${request.department})\nالقناة: ${request.sourceChannel}\n\n${request.description}",
            category = "طلبات إدارة",
            priority = request.priority,
            status = "قيد التنفيذ",
            sourceType = "طلب إدارة",
            sourceId = request.id,
            requestedBy = request.requesterName,
            dueDate = request.dueDate,
            nextAction = "البدء في دراسة وتنفيذ طلب الإدارة",
            notes = "تم إنشاؤها تلقائياً من سجل طلبات الإدارة رقم ${request.requestNumber}"
        )
        db.taskDao().insert(task)
        db.managementRequestDao().update(request.copy(status = "قيد الإجراء"))
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "طلب إدارة",
                entityId = request.id,
                actionType = "تحويل لمهمة",
                summary = "تحويل طلب الإدارة #${request.requestNumber} إلى مهمة عمل"
            )
        )
    }

    // Finance Followups
    val allFinanceFollowups: Flow<List<FinanceFollowupEntity>> = db.financeDao().getAllFinanceFollowups()
    val overdueFinanceFollowups: Flow<List<FinanceFollowupEntity>> = db.financeDao().getOverdueFollowups()
    val pendingFinanceFollowups: Flow<List<FinanceFollowupEntity>> = db.financeDao().getPendingFollowups()

    suspend fun saveFinanceFollowup(followup: FinanceFollowupEntity) = withContext(Dispatchers.IO) {
        db.financeDao().insert(followup)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "متابعة مالية",
                entityId = followup.id,
                actionType = "حفظ",
                summary = "تسجيل متابعة مالية: ${followup.counterpartyName} - ${followup.amountDue} ${followup.currency}"
            )
        )
    }

    // Opportunities
    val allOpportunities: Flow<List<OpportunityEntity>> = db.opportunityDao().getAllOpportunities()

    suspend fun saveOpportunity(opp: OpportunityEntity) = withContext(Dispatchers.IO) {
        db.opportunityDao().insert(opp)
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "فرصة تعاقد",
                entityId = opp.id,
                actionType = "حفظ",
                summary = "تسجيل فرصة تعاقد: ${opp.title} - ${opp.clientName} (${opp.stage})"
            )
        )
    }

    // Contacts
    val allContacts: Flow<List<ContactEntity>> = db.contactDao().getAllContacts()

    suspend fun saveContact(contact: ContactEntity) = withContext(Dispatchers.IO) {
        db.contactDao().insert(contact)
    }

    suspend fun archiveContact(id: String) = withContext(Dispatchers.IO) {
        db.contactDao().archive(id)
    }

    // Meetings
    val allMeetings: Flow<List<MeetingEntity>> = db.meetingDao().getAllMeetings()

    suspend fun saveMeeting(meeting: MeetingEntity) = withContext(Dispatchers.IO) {
        db.meetingDao().insert(meeting)
    }

    // Documents
    val allDocuments: Flow<List<DocumentEntity>> = db.documentDao().getAllDocuments()

    fun getDocumentsForEntity(entityType: String, entityId: String): Flow<List<DocumentEntity>> =
        db.documentDao().getDocumentsForEntity(entityType, entityId)

    suspend fun saveDocument(doc: DocumentEntity) = withContext(Dispatchers.IO) {
        db.documentDao().insert(doc)
    }

    // Activity Logs
    val recentActivityLogs: Flow<List<ActivityLogEntity>> = db.activityLogDao().getRecentLogs()

    // Backup & Restore
    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Sherkety Legal Desk")
        root.put("version", "1.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("user", "عمرو عادل أحمد - مدير الشؤون القانونية")

        // Tasks
        val tasksArray = JSONArray()
        db.taskDao().getAllTasksSnapshot().forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("category", t.category)
            obj.put("priority", t.priority)
            obj.put("status", t.status)
            obj.put("dueDate", t.dueDate)
            obj.put("assignedTo", t.assignedTo)
            obj.put("nextAction", t.nextAction)
            obj.put("blockedReason", t.blockedReason)
            obj.put("waitingFor", t.waitingFor)
            obj.put("notes", t.notes)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Contracts
        val contractsArray = JSONArray()
        db.contractDao().getAllContractsSnapshot().forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("contractNumber", c.contractNumber)
            obj.put("title", c.title)
            obj.put("contractType", c.contractType)
            obj.put("counterpartyName", c.counterpartyName)
            obj.put("contractValue", c.contractValue)
            obj.put("currency", c.currency)
            obj.put("reviewStatus", c.reviewStatus)
            obj.put("approvalStatus", c.approvalStatus)
            obj.put("signatureStatus", c.signatureStatus)
            obj.put("nextAction", c.nextAction)
            contractsArray.put(obj)
        }
        root.put("contracts", contractsArray)

        // Incorporations
        val incArray = JSONArray()
        db.incorporationDao().getAllIncorporationsSnapshot().forEach { i ->
            val obj = JSONObject()
            obj.put("id", i.id)
            obj.put("fileNumber", i.fileNumber)
            obj.put("companyName", i.companyName)
            obj.put("clientName", i.clientName)
            obj.put("currentStageIndex", i.currentStageIndex)
            obj.put("status", i.status)
            obj.put("professionalFees", i.professionalFees)
            obj.put("amountCollected", i.amountCollected)
            obj.put("missingDocuments", i.missingDocuments)
            obj.put("nextAction", i.nextAction)
            incArray.put(obj)
        }
        root.put("incorporations", incArray)

        // Management Requests
        val reqArray = JSONArray()
        db.managementRequestDao().getAllRequestsSnapshot().forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("requestNumber", r.requestNumber)
            obj.put("requesterName", r.requesterName)
            obj.put("department", r.department)
            obj.put("requestTitle", r.requestTitle)
            obj.put("status", r.status)
            obj.put("priority", r.priority)
            reqArray.put(obj)
        }
        root.put("managementRequests", reqArray)

        // Finance
        val finArray = JSONArray()
        db.financeDao().getAllFinanceFollowupsSnapshot().forEach { f ->
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("referenceNumber", f.referenceNumber)
            obj.put("counterpartyName", f.counterpartyName)
            obj.put("amountDue", f.amountDue)
            obj.put("amountPaid", f.amountPaid)
            obj.put("currency", f.currency)
            obj.put("status", f.status)
            obj.put("dueDate", f.dueDate)
            finArray.put(obj)
        }
        root.put("financeFollowups", finArray)

        root.toString(2)
    }

    suspend fun getBackupSummary(jsonString: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val root = JSONObject(jsonString)
            val tasksCount = root.optJSONArray("tasks")?.length() ?: 0
            val contractsCount = root.optJSONArray("contracts")?.length() ?: 0
            val incCount = root.optJSONArray("incorporations")?.length() ?: 0
            val reqCount = root.optJSONArray("managementRequests")?.length() ?: 0
            val finCount = root.optJSONArray("financeFollowups")?.length() ?: 0
            val date = root.optLong("exportedAt", System.currentTimeMillis())

            Result.success("تحتوي النسخة على:\n• المهام: $tasksCount\n• العقود: $contractsCount\n• ملفات التأسيس: $incCount\n• طلبات الإدارة: $reqCount\n• المتابعات المالية: $finCount")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
