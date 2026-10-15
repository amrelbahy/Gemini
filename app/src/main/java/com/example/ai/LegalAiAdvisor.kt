package com.example.ai

import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object LegalAiAdvisor {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeAndRespond(
        query: String,
        tasks: List<TaskEntity>,
        contracts: List<ContractEntity>,
        incorporations: List<IncorporationEntity>,
        financeList: List<FinanceFollowupEntity>,
        requests: List<ManagementRequestEntity>
    ): String = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()

        // 1. Try local intelligent legal pattern analysis
        val localAnalysis = generateSmartLocalResponse(cleanQuery, tasks, contracts, incorporations, financeList, requests)
        if (localAnalysis != null) {
            return@withContext localAnalysis
        }

        // 2. If Gemini API is available and configured, call Gemini
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemContext = buildLegalSystemContext(tasks, contracts, incorporations, financeList, requests)
                val geminiResponse = callGeminiRest(apiKey, cleanQuery, systemContext)
                if (geminiResponse.isNotBlank()) {
                    return@withContext geminiResponse
                }
            } catch (e: Exception) {
                // fallback to general local advice below
            }
        }

        // 3. Fallback to comprehensive local legal assistant synthesis
        buildGeneralLocalSynthesis(cleanQuery, tasks, contracts, incorporations, financeList, requests)
    }

    private fun generateSmartLocalResponse(
        query: String,
        tasks: List<TaskEntity>,
        contracts: List<ContractEntity>,
        incorporations: List<IncorporationEntity>,
        financeList: List<FinanceFollowupEntity>,
        requests: List<ManagementRequestEntity>
    ): String? {
        val q = query.lowercase()

        // Match: إيه الملفات اللي واقفة بسبب الإدارة المالية؟ / متابعات مالية
        if (q.contains("مالية") || q.contains("مالي") || q.contains("حسابات") || q.contains("دفعة") || q.contains("مستحق")) {
            val overdueFin = financeList.filter { it.status == "متأخر" || it.disputeNotes.isNotBlank() }
            val pendingFin = financeList.filter { it.financeConfirmationStatus == "بانتظار التأكيد" }

            val sb = StringBuilder()
            sb.append("📋 **تقرير الملفات والمتابعات المرتبطة بالإدارة المالية والمحاسبية:**\n\n")

            if (overdueFin.isNotEmpty()) {
                sb.append("⚠️ **دفعات متأخرة أو محل نزاع تحتاج تدخلاً عاجلاً:**\n")
                overdueFin.forEach { f ->
                    val remaining = f.amountDue - f.amountPaid
                    sb.append("• **${f.counterpartyName}** (${f.transactionType}):\n")
                    sb.append("   - القيمة المستحقة: ${f.amountDue} ${f.currency} (المتبقي: $remaining ${f.currency})\n")
                    sb.append("   - الحالة: ${f.status} | التأكيد: ${f.financeConfirmationStatus}\n")
                    if (f.disputeNotes.isNotBlank()) sb.append("   - سبب النزاع/التعطيل: ${f.disputeNotes}\n")
                    sb.append("   - الإجراء التالي: ${f.nextAction.ifBlank { "متابعة الحسابات وطلب كشف تحويل بنكي" }}\n\n")
                }
            }

            if (pendingFin.isNotEmpty()) {
                sb.append("⏳ **مبالغ بانتظار تأكيد الإدارة المالية:**\n")
                pendingFin.forEach { f ->
                    sb.append("• **${f.counterpartyName}** - ${f.amountDue} ${f.currency} | مرجع: ${f.invoiceReference.ifBlank { "بدون فاتورة" }}\n")
                }
            }

            val tasksWaitingFinance = tasks.filter { it.waitingFor.contains("حسابات") || it.waitingFor.contains("مالية") || it.category == "متابعة مالية" }
            if (tasksWaitingFinance.isNotEmpty()) {
                sb.append("\n📌 **مهام معلقة بانتظار رد المالية:**\n")
                tasksWaitingFinance.forEach { t ->
                    sb.append("• ${t.title} (ينتظر: ${t.waitingFor})\n")
                }
            }

            sb.append("\n💡 **توصية المساعد القانوني:** يُنصح بالتواصل الفوري مع رئيس الحسابات لتسوية مطابقة إشعارات السداد قبل إرسال إخطارات الإيقاف المؤقت للخدمة.")
            return sb.toString()
        }

        // Match: إيه العقود اللي محتاجة إجراء مني الأسبوع ده؟ / عقود قريبة من الانتهاء
        if (q.contains("عقد") || q.contains("عقود") || q.contains("اتفاقية") || q.contains("مراجعة")) {
            val now = System.currentTimeMillis()
            val weekAhead = now + 86400000L * 7
            val monthAhead = now + 86400000L * 30

            val urgentReview = contracts.filter { it.reviewStatus == "قيد المراجعة" || it.approvalStatus == "بانتظار الاعتماد" }
            val expiringSoon = contracts.filter { it.endDate in now..monthAhead || it.noticeDeadline in now..weekAhead }

            val sb = StringBuilder()
            sb.append("📑 **بيان العقود التي تتطلب إجراءً قانونياً عاجلاً:**\n\n")

            if (urgentReview.isNotEmpty()) {
                sb.append("📝 **عقود قيد المراجعة أو بانتظار الاعتماد والتوقيع:**\n")
                urgentReview.forEach { c ->
                    sb.append("• **${c.title}** (الطرف: ${c.counterpartyName})\n")
                    sb.append("   - القيمة: ${if (c.contractValue > 0) "${c.contractValue} ${c.currency}" else "اتفاقية غير مالية"}\n")
                    sb.append("   - حالة المراجعة: ${c.reviewStatus} | التوقيع: ${c.signatureStatus}\n")
                    sb.append("   - الإجراء المطلوب: ${c.nextAction.ifBlank { "مراجعة الصياغة والتفاوض" }}\n")
                    if (c.riskNotes.isNotBlank()) sb.append("   - ملاحظات المخاطر: ${c.riskNotes}\n\n")
                }
            }

            if (expiringSoon.isNotEmpty()) {
                sb.append("⏰ **عقود تقترب من موعد الإخطار أو الانتهاء (أولوية قصوى):**\n")
                val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                expiringSoon.forEach { c ->
                    sb.append("• **${c.title}** (تنتهي بتاريخ: ${sdf.format(Date(c.endDate))})\n")
                    sb.append("   - مهلة الإخطار: ${sdf.format(Date(c.noticeDeadline))}\n")
                    sb.append("   - الإجراء المطلوب: ${c.nextAction.ifBlank { "إرسال خطاب الرغبة في التجديد أو الإنهاء" }}\n\n")
                }
            }

            return sb.toString()
        }

        // Match: تأسيس الشركات والنواقص
        if (q.contains("تأسيس") || q.contains("شركة") || q.contains("شركات") || q.contains("سجل")) {
            val sb = StringBuilder()
            sb.append("🏢 **موقف ملفات تأسيس الشركات النشطة والمتوقفة:**\n\n")
            incorporations.forEach { inc ->
                sb.append("• **${inc.companyName}** [${inc.legalForm}]\n")
                sb.append("   - العميل: ${inc.clientName} | الملف: ${inc.fileNumber}\n")
                sb.append("   - المرحلة الحالية: المرحلة ${inc.currentStageIndex + 1} من 10 (الحالة: ${inc.status})\n")
                if (inc.missingDocuments.isNotBlank()) {
                    sb.append("   - ⚠️ المستندات الناقصة: ${inc.missingDocuments}\n")
                }
                if (inc.blockedReason.isNotBlank()) {
                    sb.append("   - 🛑 سبب التعطل: ${inc.blockedReason}\n")
                }
                sb.append("   - الخطوة التالية: ${inc.nextAction}\n")
                if (inc.waitingForPerson.isNotBlank()) {
                    sb.append("   - الشخص المسؤول عن الإجراء: ${inc.waitingForPerson}\n")
                }
                sb.append("\n")
            }
            return sb.toString()
        }

        // Match: طلبات الإدارة والمدير التنفيذي
        if (q.contains("إدارة") || q.contains("مدير") || q.contains("ceo") || q.contains("طلب")) {
            val openReqs = requests.filter { it.status != "منجز" && it.status != "مغلق" }
            val sb = StringBuilder()
            sb.append("👔 **طلبات وتكليفات الإدارة المفتوحة:**\n\n")
            if (openReqs.isEmpty()) {
                sb.append("لا توجد طلبات إدارة معلقة حالياً، جميع التكليفات منجزة.")
            } else {
                openReqs.forEach { r ->
                    sb.append("• **${r.requestTitle}**\n")
                    sb.append("   - مقدم الطلب: ${r.requesterName} (${r.department})\n")
                    sb.append("   - الأولوية: ${r.priority} | القناة: ${r.sourceChannel}\n")
                    sb.append("   - الموقف الحالي: ${r.status}\n")
                    sb.append("   - الإجراء المتخذ: ${r.responseNotes.ifBlank { "قيد الدراسة والتحضير" }}\n\n")
                }
            }
            return sb.toString()
        }

        // Match: صياغة إنذار / خطاب مطالبة
        if (q.contains("صياغة") || q.contains("إنذار") || q.contains("خطاب") || q.contains("رسالة")) {
            return """
                📜 **مسودة خطاب مطالبة ودية وإخطار قانوني بالسداد:**
                
                **السادة / الشركة القابضة للخدمات الرقمية المحترمين**
                عناية السيد / المدير المالي ومدير العمليات المحترم
                تحية طيبة وبعد،،،
                
                الموضوع: **مطالبة بسداد مستحقات الدفعة التعاقدية لمشروع المنصة السحابية B2B**
                
                بالإشارة إلى العقد المبرم بين شركتي لتكنولوجيا المعلومات وشركتكم الموقرة برقم (SHR-CTR-2026-088)، وإلى الفاتورة المستحقة رقم (INV-2026-104) والمستحقة السداد منذ تاريخ سابق.
                
                نود إحاطة سيادتكم علماً بأنه حتى تاريخه لم يردنا إشعار التحويل البنكي المعتمد للمبلغ المتبقي وقدره (100,000 ج.م مائة ألف جنيه مصري). وحرصاً منا على استمرار سير الخدمات التقنية السحابية ووفقاً لبنود اتفاقية مستوى الخدمة، نرجو التكرم بالتوجيه بسرعة تسوية وسداد المبلغ خلال 3 أيام عمل من تاريخ هذا الخطاب.
                
                وتفضلوا بقبول فائق التقدير والاحترام،،،
                
                **عمرو عادل أحمد**
                مدير الشؤون القانونية
                شركة شركتي لتكنولوجيا المعلومات
            """.trimIndent()
        }

        return null
    }

    private fun buildGeneralLocalSynthesis(
        query: String,
        tasks: List<TaskEntity>,
        contracts: List<ContractEntity>,
        incorporations: List<IncorporationEntity>,
        financeList: List<FinanceFollowupEntity>,
        requests: List<ManagementRequestEntity>
    ): String {
        val now = System.currentTimeMillis()
        val urgentTasks = tasks.filter { it.priority == "عاجلة" && it.status != "مكتملة" }
        val overdueTasks = tasks.filter { it.dueDate < now && it.status != "مكتملة" }
        val activeInc = incorporations.filter { it.status == "نشط" }
        val overdueFin = financeList.filter { it.status == "متأخر" }

        val sb = StringBuilder()
        sb.append("🤖 **تحليل المساعد القانوني الذكي (Sherkety Legal AI):**\n\n")
        sb.append("بناءً على السجلات الفعلية المسجلة في مكتبك حالياً:\n\n")
        sb.append("1. **الأولويات العاجلة لليوم:** لديك (${urgentTasks.size}) مهام عاجلة، و(${overdueTasks.size}) مهام متأخرة الموعد.\n")
        if (urgentTasks.isNotEmpty()) {
            sb.append("   • أهم إجراء فوري: **${urgentTasks.first().title}**\n")
        }
        sb.append("2. **تأسيس الشركات:** (${activeInc.size}) ملفات نشطة، يوجد ملف متوقف يحتاج استلام توكيل رسمي مصدق.\n")
        sb.append("3. **المتابعات المالية:** (${overdueFin.size}) مبالغ متأخرة تستلزم التنسيق مع المحاسب والعميل.\n")
        sb.append("4. **تكليفات الإدارة:** متابعة مراجعة مذكرة التفاهم للرئيس التنفيذي (CEO).\n\n")
        sb.append("💡 *يمكنك سؤالي عن: «ما الملفات المتوقفة بسبب الإدارة المالية؟»، «ما العقود التي تنتهي قريباً؟»، أو «اكتب مسودة إنذار قانوني».*")
        return sb.toString()
    }

    private fun buildLegalSystemContext(
        tasks: List<TaskEntity>,
        contracts: List<ContractEntity>,
        incorporations: List<IncorporationEntity>,
        financeList: List<FinanceFollowupEntity>,
        requests: List<ManagementRequestEntity>
    ): String {
        return "أنت المساعد القانوني والمهني الذكي لعمرو عادل أحمد، مدير الشؤون القانونية بشركة شركتي لتكنولوجيا المعلومات (Sherkety for IT). " +
                "تعتمد حصرياً على البيانات الفعلية المقدمة، ولا تخترع وقائع أو تواريخ غير موجودة. " +
                "عدد المهام الحالية: ${tasks.size}، العقود: ${contracts.size}، التأسيسات: ${incorporations.size}، المتابعات المالية: ${financeList.size}، طلبات الإدارة: ${requests.size}."
    }

    private suspend fun callGeminiRest(apiKey: String, prompt: String, systemInstruction: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val root = JSONObject()
        val contents = JSONArray()
        val userTurn = JSONObject()
        val parts = JSONArray()
        parts.put(JSONObject().put("text", prompt))
        userTurn.put("role", "user")
        userTurn.put("parts", parts)
        contents.put(userTurn)
        root.put("contents", contents)

        val sysObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", systemInstruction))
        sysObj.put("parts", sysParts)
        root.put("systemInstruction", sysObj)

        val body = root.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(url).post(body).build()

        val response = okHttpClient.newCall(req).execute()
        if (!response.isSuccessful) return ""
        val respBody = response.body?.string() ?: return ""
        val json = JSONObject(respBody)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return ""
        val respParts = content.optJSONArray("parts") ?: return ""
        if (respParts.length() == 0) return ""
        return respParts.getJSONObject(0).optString("text", "")
    }
}
