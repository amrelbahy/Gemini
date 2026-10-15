package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.LegalAiAdvisor
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.model.*
import com.example.data.repository.LegalDeskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LegalDeskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = LegalDeskRepository(db)

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedInitialDataIfEmpty(db)
        }
    }

    // Core Data Streams
    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val overdueTasks: StateFlow<List<TaskEntity>> = repository.overdueTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val urgentTasks: StateFlow<List<TaskEntity>> = repository.urgentTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val waitingTasks: StateFlow<List<TaskEntity>> = repository.waitingTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val incorporations: StateFlow<List<IncorporationEntity>> = repository.allIncorporations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val activeIncorporations: StateFlow<List<IncorporationEntity>> = repository.activeIncorporations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val contracts: StateFlow<List<ContractEntity>> = repository.allContracts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val contractsUnderReview: StateFlow<List<ContractEntity>> = repository.contractsUnderReview.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val managementRequests: StateFlow<List<ManagementRequestEntity>> = repository.allManagementRequests.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val openManagementRequests: StateFlow<List<ManagementRequestEntity>> = repository.openManagementRequests.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val financeFollowups: StateFlow<List<FinanceFollowupEntity>> = repository.allFinanceFollowups.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val overdueFinanceFollowups: StateFlow<List<FinanceFollowupEntity>> = repository.overdueFinanceFollowups.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val opportunities: StateFlow<List<OpportunityEntity>> = repository.allOpportunities.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val meetings: StateFlow<List<MeetingEntity>> = repository.allMeetings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val documents: StateFlow<List<DocumentEntity>> = repository.allDocuments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.recentActivityLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Global Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // AI Assistant State
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "مرحباً بك أستاذ عمرو، أنا المساعد القانوني والمهني الذكي لمكتب الشؤون القانونية بشركتي لتكنولوجيا المعلومات. كيف أساعدك اليوم في إدارة وتدقيق ملفاتك؟"
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun askAi(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = query)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = LegalAiAdvisor.analyzeAndRespond(
                query = query,
                tasks = tasks.value,
                contracts = contracts.value,
                incorporations = incorporations.value,
                financeList = financeFollowups.value,
                requests = managementRequests.value
            )
            _aiMessages.value = _aiMessages.value + ChatMessage(isUser = false, text = response)
            _isAiLoading.value = false
        }
    }

    // Task Actions
    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            val isNowCompleted = task.status != "مكتملة"
            repository.completeTask(task.id, isNowCompleted)
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.saveTask(task)
        }
    }

    fun archiveTask(taskId: String) {
        viewModelScope.launch {
            repository.archiveTask(taskId)
        }
    }

    // Incorporation Actions
    fun saveIncorporation(inc: IncorporationEntity) {
        viewModelScope.launch {
            repository.saveIncorporation(inc)
        }
    }

    fun updateIncorporationStage(inc: IncorporationEntity, newStage: Int) {
        viewModelScope.launch {
            val updated = inc.copy(currentStageIndex = newStage)
            repository.saveIncorporation(updated)
        }
    }

    fun saveIncorporationParty(party: IncorporationPartyEntity) {
        viewModelScope.launch {
            repository.saveIncorporationParty(party)
        }
    }

    fun getChecklistForInc(incId: String) = repository.getIncorporationChecklist(incId)
    fun getPartiesForInc(incId: String) = repository.getIncorporationParties(incId)

    fun updateChecklistItem(item: IncorporationChecklistEntity) {
        viewModelScope.launch {
            repository.updateChecklistItem(item)
        }
    }

    // Contract Actions
    fun saveContract(contract: ContractEntity) {
        viewModelScope.launch {
            repository.saveContract(contract)
        }
    }

    fun getObligationsForContract(contractId: String) = repository.getContractObligations(contractId)

    fun saveContractObligation(obligation: ContractObligationEntity) {
        viewModelScope.launch {
            repository.saveContractObligation(obligation)
        }
    }

    // Management Request Actions
    fun saveManagementRequest(request: ManagementRequestEntity) {
        viewModelScope.launch {
            repository.saveManagementRequest(request)
        }
    }

    fun convertRequestToTask(request: ManagementRequestEntity) {
        viewModelScope.launch {
            repository.convertRequestToTask(request)
        }
    }

    // Finance Actions
    fun saveFinanceFollowup(followup: FinanceFollowupEntity) {
        viewModelScope.launch {
            repository.saveFinanceFollowup(followup)
        }
    }

    // Opportunities Actions
    fun saveOpportunity(opp: OpportunityEntity) {
        viewModelScope.launch {
            repository.saveOpportunity(opp)
        }
    }

    // Contacts & Meetings & Documents Actions
    fun saveContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.saveContact(contact)
        }
    }

    fun saveMeeting(meeting: MeetingEntity) {
        viewModelScope.launch {
            repository.saveMeeting(meeting)
        }
    }

    fun saveDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.saveDocument(doc)
        }
    }

    // Backup & Restore
    private val _backupJson = MutableStateFlow<String?>(null)
    val backupJson: StateFlow<String?> = _backupJson.asStateFlow()

    fun createBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportFullBackupJson()
            _backupJson.value = json
            onReady(json)
        }
    }
}
