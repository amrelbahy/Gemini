package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE archivedAt IS NULL ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Update
    suspend fun update(contact: ContactEntity)

    @Query("UPDATE contacts SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun archive(id: String, archivedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM contacts")
    suspend fun getAllContactsSnapshot(): List<ContactEntity>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL AND dueDate < :now AND status != 'مكتملة' AND status != 'ملغاة' ORDER BY dueDate ASC")
    fun getOverdueTasks(now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL AND priority = 'عاجلة' AND status != 'مكتملة' AND status != 'ملغاة' ORDER BY dueDate ASC")
    fun getUrgentTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL AND status = 'بانتظار طرف آخر' ORDER BY dueDate ASC")
    fun getWaitingTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, completedAt: Long?)

    @Query("UPDATE tasks SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun archive(id: String, archivedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSnapshot(): List<TaskEntity>

    // Subtasks
    @Query("SELECT * FROM sub_tasks WHERE taskId = :taskId ORDER BY createdAt ASC")
    fun getSubTasks(taskId: String): Flow<List<SubTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity)

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Query("DELETE FROM sub_tasks WHERE id = :id")
    suspend fun deleteSubTask(id: String)
}

@Dao
interface IncorporationDao {
    @Query("SELECT * FROM incorporations WHERE archivedAt IS NULL ORDER BY receivedAt DESC")
    fun getAllIncorporations(): Flow<List<IncorporationEntity>>

    @Query("SELECT * FROM incorporations WHERE id = :id LIMIT 1")
    suspend fun getIncorporationById(id: String): IncorporationEntity?

    @Query("SELECT * FROM incorporations WHERE archivedAt IS NULL AND status = 'نشط' ORDER BY receivedAt DESC")
    fun getActiveIncorporations(): Flow<List<IncorporationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incorporation: IncorporationEntity)

    @Update
    suspend fun update(incorporation: IncorporationEntity)

    @Query("UPDATE incorporations SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun archive(id: String, archivedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM incorporations")
    suspend fun getAllIncorporationsSnapshot(): List<IncorporationEntity>

    // Parties
    @Query("SELECT * FROM incorporation_parties WHERE incorporationId = :incId")
    fun getParties(incId: String): Flow<List<IncorporationPartyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: IncorporationPartyEntity)

    @Delete
    suspend fun deleteParty(party: IncorporationPartyEntity)

    // Checklists
    @Query("SELECT * FROM incorporation_checklists WHERE incorporationId = :incId ORDER BY stageIndex ASC")
    fun getChecklist(incId: String): Flow<List<IncorporationChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<IncorporationChecklistEntity>)

    @Update
    suspend fun updateChecklistItem(item: IncorporationChecklistEntity)
}

@Dao
interface ContractDao {
    @Query("SELECT * FROM contracts WHERE archivedAt IS NULL ORDER BY createdAt DESC")
    fun getAllContracts(): Flow<List<ContractEntity>>

    @Query("SELECT * FROM contracts WHERE id = :id LIMIT 1")
    suspend fun getContractById(id: String): ContractEntity?

    @Query("SELECT * FROM contracts WHERE archivedAt IS NULL AND reviewStatus = 'قيد المراجعة' ORDER BY createdAt DESC")
    fun getContractsUnderReview(): Flow<List<ContractEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contract: ContractEntity)

    @Update
    suspend fun update(contract: ContractEntity)

    @Query("UPDATE contracts SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun archive(id: String, archivedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM contracts")
    suspend fun getAllContractsSnapshot(): List<ContractEntity>

    // Obligations
    @Query("SELECT * FROM contract_obligations WHERE contractId = :contractId ORDER BY dueDate ASC")
    fun getObligations(contractId: String): Flow<List<ContractObligationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObligation(obligation: ContractObligationEntity)

    @Update
    suspend fun updateObligation(obligation: ContractObligationEntity)
}

@Dao
interface ManagementRequestDao {
    @Query("SELECT * FROM management_requests ORDER BY receivedAt DESC")
    fun getAllRequests(): Flow<List<ManagementRequestEntity>>

    @Query("SELECT * FROM management_requests WHERE status != 'منجز' AND status != 'مغلق' ORDER BY dueDate ASC")
    fun getOpenRequests(): Flow<List<ManagementRequestEntity>>

    @Query("SELECT * FROM management_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): ManagementRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: ManagementRequestEntity)

    @Update
    suspend fun update(request: ManagementRequestEntity)

    @Query("SELECT * FROM management_requests")
    suspend fun getAllRequestsSnapshot(): List<ManagementRequestEntity>
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_followups ORDER BY dueDate ASC")
    fun getAllFinanceFollowups(): Flow<List<FinanceFollowupEntity>>

    @Query("SELECT * FROM finance_followups WHERE status = 'متأخر' OR (dueDate < :now AND status != 'مسدد بالكامل' AND status != 'ملغى') ORDER BY dueDate ASC")
    fun getOverdueFollowups(now: Long = System.currentTimeMillis()): Flow<List<FinanceFollowupEntity>>

    @Query("SELECT * FROM finance_followups WHERE status = 'مستحق' OR status = 'مستحق قريباً' ORDER BY dueDate ASC")
    fun getPendingFollowups(): Flow<List<FinanceFollowupEntity>>

    @Query("SELECT * FROM finance_followups WHERE id = :id LIMIT 1")
    suspend fun getFollowupById(id: String): FinanceFollowupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(followup: FinanceFollowupEntity)

    @Update
    suspend fun update(followup: FinanceFollowupEntity)

    @Query("SELECT * FROM finance_followups")
    suspend fun getAllFinanceFollowupsSnapshot(): List<FinanceFollowupEntity>
}

@Dao
interface OpportunityDao {
    @Query("SELECT * FROM opportunities ORDER BY firstContactAt DESC")
    fun getAllOpportunities(): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE id = :id LIMIT 1")
    suspend fun getOpportunityById(id: String): OpportunityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(opportunity: OpportunityEntity)

    @Update
    suspend fun update(opportunity: OpportunityEntity)

    @Query("SELECT * FROM opportunities")
    suspend fun getAllOpportunitiesSnapshot(): List<OpportunityEntity>
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY meetingDate DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meeting: MeetingEntity)

    @Update
    suspend fun update(meeting: MeetingEntity)

    @Query("SELECT * FROM meetings")
    suspend fun getAllMeetingsSnapshot(): List<MeetingEntity>
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE relatedEntityType = :entityType AND relatedEntityId = :entityId")
    fun getDocumentsForEntity(entityType: String, entityId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity)

    @Update
    suspend fun update(document: DocumentEntity)

    @Query("SELECT * FROM documents")
    suspend fun getAllDocumentsSnapshot(): List<DocumentEntity>
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity)

    @Query("SELECT * FROM activity_logs")
    suspend fun getAllLogsSnapshot(): List<ActivityLogEntity>
}
