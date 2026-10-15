package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seedInitialDataIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val existingTasks = db.taskDao().getAllTasksSnapshot()
        if (existingTasks.isNotEmpty()) return@withContext

        val now = System.currentTimeMillis()
        val day = 86400000L

        // 1. Contacts
        val c1 = ContactEntity(
            id = "contact_1",
            name = "الشركة القابضة للخدمات الرقمية",
            entityType = "عميل رئيسي",
            contactPerson = "م. خالد الأنصاري",
            phone = "01001234567",
            email = "khaled@digitalholdings.com",
            address = "القرية الذكية، مبنى B12، الجيزة",
            notes = "عميل استراتيجي لشركتي لتكنولوجيا المعلومات - مشاريع بنية تحتية سحابية"
        )
        val c2 = ContactEntity(
            id = "contact_2",
            name = "مؤسسة الدفع الذكي للتكنولوجيا المالية",
            entityType = "عميل",
            contactPerson = "أ. سارة عبد الرحمن",
            phone = "01129876543",
            email = "sara@smartpay.fin",
            address = "ميدان التحرير، القاهرة",
            notes = "جاري تأسيس شركة جديدة وتوقيع عقد رخصة برمجيات الدفع"
        )
        val c3 = ContactEntity(
            id = "contact_3",
            name = "شركة كلاود إكس للبنية التحتية",
            entityType = "مورد رئيسي",
            contactPerson = "م. طارق المهدي",
            phone = "01234567890",
            email = "tareq@cloudx.io",
            address = "المنطقة التكنولوجية بالمعادي",
            notes = "مورد السيرفرات المخصصة والاستضافة المؤمنة"
        )
        val c4 = ContactEntity(
            id = "contact_4",
            name = "الهيئة العامة للاستثمار والمناطق الحرة (GAFI)",
            entityType = "جهة حكومية",
            contactPerson = "قطاع خدمات المستثمرين",
            phone = "16035",
            email = "info@gafi.gov.eg",
            address = "صلاح سالم، مدينة نصر، القاهرة",
            notes = "متابعة ملفات تأسيس الشركات ومحاضر الجمعيات العمومية"
        )

        db.contactDao().insert(c1)
        db.contactDao().insert(c2)
        db.contactDao().insert(c3)
        db.contactDao().insert(c4)

        // 2. Incorporations
        val inc1 = IncorporationEntity(
            id = "inc_1",
            fileNumber = "INC-2026-001",
            clientId = c2.id,
            clientName = c2.name,
            companyName = "شركة فينتك باي للتكنولوجيا المالية (ش.ذ.م.م)",
            alternativeNames = "فينتك إيجيبت / باي لينك للبرمجيات",
            legalForm = "شركة ذات مسؤولية محدودة (ش.ذ.م.م)",
            activityDescription = "تطوير برمجيات المدفوعات والأنظمة المالية الرقمية وتطبيقات الهواتف",
            jurisdiction = "مركز خدمات المستثمرين - هيئة الاستثمار بالقاهرة",
            currentStageIndex = 5, // المرحلة 6: تقديم الملف ومتابعة الهيئة
            status = "نشط",
            receivedAt = now - day * 10,
            startedAt = now - day * 8,
            targetCompletionDate = now + day * 5,
            professionalFees = 30000.0,
            governmentFeesEstimated = 12000.0,
            governmentFeesActual = 11850.0,
            amountCollected = 25000.0,
            missingDocuments = "شهادة عدم التباس الاسم التجاري النهائية",
            nextAction = "متابعة موظف هيئة الاستثمار واستلام إذن توريد الرسوم الحكومية",
            waitingForPerson = "الأستاذ أحمد فؤاد (مندوب التأسيس)",
            notes = "تم اعتماد عقد التأسيس المبدئي وتوكيلات الشركاء موثقة بالشهر العقاري"
        )

        val inc2 = IncorporationEntity(
            id = "inc_2",
            fileNumber = "INC-2026-002",
            clientId = c1.id,
            clientName = c1.name,
            companyName = "شركة سحابة المستقبل للأنظمة الرقمية",
            alternativeNames = "كلاود سيستمز مصر / نكست ليفل كلاود",
            legalForm = "شركة مساهمة مصرية (ش.م.م)",
            activityDescription = "مراكز البيانات السحابية وخدمات إدارة تكنولوجيا المعلومات",
            jurisdiction = "هيئة الاستثمار - المنطقة الحرة بالعامرية",
            currentStageIndex = 2, // مراجعة الأسماء والنشاط
            status = "متوقف",
            receivedAt = now - day * 5,
            startedAt = now - day * 4,
            targetCompletionDate = now + day * 20,
            professionalFees = 55000.0,
            governmentFeesEstimated = 25000.0,
            governmentFeesActual = 0.0,
            amountCollected = 20000.0,
            missingDocuments = "أصل توكيل الشريك الإماراتي المصدق من الخارجية المصرية",
            nextAction = "التواصل مع ممثل الشريك الأجنبي لاستلام التوكيل المصدق",
            blockedReason = "الملف متوقف لعدم وصول التوكيل المصدق من قنصلية دبي والخارجية المصرية",
            waitingForPerson = "م. خالد الأنصاري (الطرف المقابل)",
            notes = "تم إعداد مسودة عقد التأسيس والنظام الأساسي وبانتظار التوكيل"
        )

        db.incorporationDao().insert(inc1)
        db.incorporationDao().insert(inc2)

        // Seed Parties for inc1
        db.incorporationDao().insertParty(
            IncorporationPartyEntity(
                incorporationId = inc1.id,
                partyName = "أ. سارة عبد الرحمن خليل",
                partyRole = "شريك ومدير عام له حق التوقيع منفردًا",
                ownershipPercentage = 60.0,
                managerAuthorityNotes = "تمثيل الشركة أمام البنوك والجهات الحكومية والتعاقد",
                documentStatus = "مكتمل التوكيل والبطاقة"
            )
        )
        db.incorporationDao().insertParty(
            IncorporationPartyEntity(
                incorporationId = inc1.id,
                partyName = "م. مصطفى إبراهيم حسن",
                partyRole = "شريك",
                ownershipPercentage = 40.0,
                managerAuthorityNotes = "صلاحيات فنية فقط",
                documentStatus = "مكتمل التوكيل والبطاقة"
            )
        )

        // Seed 10 Standard Stages Checklist for inc1
        val stagesNames = listOf(
            "استلام الطلب والبيانات الأولية",
            "مراجعة النشاط وتحديد الشكل القانوني المناسب",
            "حجز ومراجعة أسماء الشركات المقترحة",
            "استلام المستندات والتوكيلات الرسمية",
            "إعداد وتوثيق عقود ومستندات التأسيس",
            "تقديم الملف ومتابعة الإجراءات بهيئة الاستثمار",
            "استخراج السجل التجاري وشهادة التأسيس",
            "فتح الملف الضريبي واستخراج البطاقة الضريبية",
            "متطلبات الفاتورة الإلكترونية والتأمينات",
            "مراجعة اكتمال الملف وتسليم المستندات للعميل"
        )

        val checklistItems = stagesNames.mapIndexed { idx, stageName ->
            IncorporationChecklistEntity(
                incorporationId = inc1.id,
                stageIndex = idx,
                stageTitle = stageName,
                itemName = "إجراءات ومتطلبات: $stageName",
                isRequired = true,
                isDone = idx < 5,
                notes = if (idx == 5) "جاري سداد الرسوم ومطابقة الغرفة التجارية" else ""
            )
        }
        db.incorporationDao().insertChecklistItems(checklistItems)

        // 3. Contracts
        val ctr1 = ContractEntity(
            id = "ctr_1",
            contractNumber = "SHR-CTR-2026-088",
            title = "عقد تطوير المنصة السحابية B2B وإدارة البنية التحتية",
            contractType = "عقد تطوير برمجيات وحوسبة سحابية",
            counterpartyId = c1.id,
            counterpartyName = c1.name,
            requestingDepartment = "الإدارة التنفيذية (CEO)",
            contractValue = 450000.0,
            currency = "EGP",
            reviewStatus = "قيد المراجعة",
            negotiationStatus = "جاري التفاوض",
            approvalStatus = "بانتظار الاعتماد",
            signatureStatus = "غير موقع",
            executionStatus = "ساري",
            startDate = now,
            endDate = now + day * 365,
            renewalDate = now + day * 335,
            noticeDeadline = now + day * 300,
            nextAction = "مراجعة بند حدود المسؤولية SLA وغرامات التأخير وإرسال الملاحظات للعميل",
            riskNotes = "الطرف المقابل طلب شرط جزائي 20%، والحد القانوني المقترح لدينا 5%",
            notes = "أهم عقد للشركة هذا الربع، متابعة مستمرة مع الرئيس التنفيذي",
            isFinalVersion = false
        )

        val ctr2 = ContractEntity(
            id = "ctr_2",
            contractNumber = "SHR-CTR-2026-092",
            title = "اتفاقية سرية وحماية بيانات عدم إفصاح (NDA)",
            contractType = "اتفاقية سرية وعدم إفصاح (NDA)",
            counterpartyId = c3.id,
            counterpartyName = c3.name,
            requestingDepartment = "إدارة أمن المعلومات والتقنية",
            contractValue = 0.0,
            currency = "EGP",
            reviewStatus = "تمت المراجعة",
            negotiationStatus = "متفق عليه",
            approvalStatus = "معتمد داخلياً",
            signatureStatus = "بانتظار التوقيع",
            executionStatus = "ساري",
            startDate = now - day * 15,
            endDate = now + day * 730,
            renewalDate = now + day * 700,
            noticeDeadline = now + day * 670,
            nextAction = "توقيع النسخة النهائية من الرئيس التنفيذي وإرسالها لشركة كلاود إكس",
            riskNotes = "تتضمن حماية الشيفرة المصدرية وخوارزميات الذكاء الاصطناعي",
            notes = "النسخة معتمدة ومطابقة للمعايير القانونية الصارمة للشركة",
            isFinalVersion = true
        )

        val ctr3 = ContractEntity(
            id = "ctr_3",
            contractNumber = "SHR-CTR-2025-041",
            title = "عقد تقديم خدمات الصيانة والدعم الفني السنوي لشبكات الفروع",
            contractType = "عقد تقديم خدمات تقنية وصيانة",
            counterpartyId = c1.id,
            counterpartyName = c1.name,
            requestingDepartment = "إدارة العمليات والتشغيل",
            contractValue = 180000.0,
            currency = "EGP",
            reviewStatus = "تمت المراجعة",
            negotiationStatus = "متفق عليه",
            approvalStatus = "معتمد داخلياً",
            signatureStatus = "موقع رسمياً",
            executionStatus = "ساري",
            startDate = now - day * 340,
            endDate = now + day * 25, // ينتهي قريباً!
            renewalDate = now + day * 10,
            noticeDeadline = now + day * 5, // موعد الإخطار عاجل جداً!
            nextAction = "إرسال إخطار الرغبة في التجديد السنوي مع تعديل القيمة 15% للتضخم",
            riskNotes = "إذا لم نرسل إخطار التجديد قبل 5 أيام سيتجدد بنفس القيمة القديمة تلقائياً",
            notes = "يتطلب اعتماد مالي عاجل لقيمة التجديد المقترحة",
            isFinalVersion = true
        )

        db.contractDao().insert(ctr1)
        db.contractDao().insert(ctr2)
        db.contractDao().insert(ctr3)

        // Obligations for ctr1
        db.contractDao().insertObligation(
            ContractObligationEntity(
                contractId = ctr1.id,
                description = "تسليم بيئة الاختبار المعزولة ومواصفات واجهات برمجة التطبيقات API",
                responsibleParty = "شركتي لتكنولوجيا المعلومات",
                dueDate = now + day * 30,
                status = "قيد التنفيذ"
            )
        )
        db.contractDao().insertObligation(
            ContractObligationEntity(
                contractId = ctr1.id,
                description = "سداد الدفعة المقدمة (30%) عند توقيع العقد",
                responsibleParty = "الشركة القابضة للخدمات الرقمية",
                dueDate = now + day * 7,
                status = "قيد التنفيذ"
            )
        )

        // 4. Tasks
        val t1 = TaskEntity(
            id = "task_1",
            title = "مراجعة بند حدود المسؤولية والشروط الجزائية في عقد المنصة السحابية",
            description = "دراسة التعديلات المقترحة من الإدارة القانونية للشركة القابضة وصياغة بديل متوازن يحمي شركتي لتكنولوجيا المعلومات",
            category = "عقود",
            priority = "عاجلة",
            status = "قيد التنفيذ",
            sourceType = "عقد",
            sourceId = ctr1.id,
            contactId = c1.id,
            isSelfAssigned = true,
            assignedTo = "عمرو عادل",
            requestedBy = "الرئيس التنفيذي (CEO)",
            startDate = now,
            dueDate = now + 86400000L * 1, // اليوم/غداً
            nextAction = "كتابة المذكرة القانونية وإرسالها للرئيس التنفيذي للاعتماد",
            notes = "أولوية قصوى اليوم - الاجتماع مع العميل غداً ظهراً"
        )

        val t2 = TaskEntity(
            id = "task_2",
            title = "متابعة استلام توكيل الشريك الأجنبي لشركة سحابة المستقبل",
            description = "التواصل مع مكتب المحاماة في دبي للتأكد من تصديق التوكيل بالقنصلية وتسليمه لشركة الشحن",
            category = "تأسيس",
            priority = "عالية",
            status = "بانتظار طرف آخر",
            sourceType = "تأسيس",
            sourceId = inc2.id,
            contactId = c1.id,
            isSelfAssigned = true,
            assignedTo = "عمرو عادل",
            requestedBy = "م. خالد الأنصاري",
            startDate = now - day * 2,
            dueDate = now - day * 1, // متأخرة!
            nextAction = "إرسال رسالة واتساب عاجلة لمستشار الشريك الإماراتي",
            waitingFor = "مستشار الشريك الإماراتي (أ. راشد الكعبي)",
            blockedReason = "تأخر وصول بوليصة الشحن الخاصة بالتوكيل",
            notes = "الملف متوقف تماماً عن خطوة هيئة الاستثمار حتى استلام هذا المستند"
        )

        val t3 = TaskEntity(
            id = "task_3",
            title = "إخطار تجديد عقد الدعم الفني السنوي مع تعديل القيمة التعاقدية",
            description = "صياغة الخطاب الرسمي للشركة القابضة لإخطارهم بعدم الرغبة في التجديد التلقائي بالقيمة القديمة، وعرض القيمة المحدثة",
            category = "عقود",
            priority = "عاجلة",
            status = "لم تبدأ",
            sourceType = "عقد",
            sourceId = ctr3.id,
            contactId = c1.id,
            isSelfAssigned = true,
            assignedTo = "عمرو عادل",
            requestedBy = "المدير المالي (CFO)",
            startDate = now,
            dueDate = now + day * 3,
            nextAction = "صياغة الإخطار وعرضه على المدير المالي لاعتماد القيمة الجديدة",
            notes = "المهلة القانونية للإخطار تنتهي بعد 5 أيام"
        )

        val t4 = TaskEntity(
            id = "task_4",
            title = "مطالبة الإدارة المالية ببيان سداد الدفعة الثانية لمشروع البوابة",
            description = "التنسيق مع رئيس الحسابات لمعرفة هل تم استلام إشعار التحويل البنكي من العميل أم لا للبدء في إجراءات الإنذار القانوني",
            category = "متابعة مالية",
            priority = "عالية",
            status = "قيد التنفيذ",
            sourceType = "متابعة مالية",
            sourceId = "fin_1",
            contactId = c1.id,
            isSelfAssigned = true,
            assignedTo = "عمرو عادل",
            requestedBy = "عمرو عادل",
            startDate = now,
            dueDate = now + day * 1,
            nextAction = "مكالمة هاتفية مع رئيس الحسابات (أ. حسام الدين)",
            waitingFor = "إفادة رئيس الحسابات البنكية",
            notes = "المبلغ متأخر 20 يوماً ويؤثر على موعد تسليم المرحلة التالية"
        )

        db.taskDao().insert(t1)
        db.taskDao().insert(t2)
        db.taskDao().insert(t3)
        db.taskDao().insert(t4)

        // Seed subtasks
        db.taskDao().insertSubTask(SubTaskEntity(taskId = t1.id, title = "مقارنة صياغة SLA بمتوسط السوق التقني", isCompleted = true))
        db.taskDao().insertSubTask(SubTaskEntity(taskId = t1.id, title = "صياغة سقف إجمالي للغرامات 5% كحد أقصى", isCompleted = false))
        db.taskDao().insertSubTask(SubTaskEntity(taskId = t1.id, title = "عرض المقترح على م. أحمد (الرئيس التنفيذي)", isCompleted = false))

        // 5. Management Requests
        val req1 = ManagementRequestEntity(
            id = "req_1",
            requestNumber = "CEO-REQ-2026-014",
            requesterName = "الرئيس التنفيذي (CEO - م. أحمد عبد العزيز)",
            department = "الإدارة التنفيذية",
            requestTitle = "مراجعة مذكرة التفاهم (MOU) للشراكة الإقليمية مع شركة التقنية السعودية",
            description = "مطلوب إفادة قانونية عاجلة حول مدى إلزامية الشروط المكتوبة في مذكرة التفاهم، وحماية حقوق الملكية الفكرية لبراءات اختراع البرمجيات الخاصة بنا قبل اجتماع مجلس الإدارة يوم الخميس",
            receivedAt = now - day * 1,
            sourceChannel = "شفهي (خلال اجتماع الإدارة)",
            priority = "عاجلة",
            dueDate = now + day * 1,
            status = "قيد الإجراء",
            clarityLevel = "واضح ومكتمل",
            relatedContractId = null,
            responseNotes = "تم استلام مسودة مذكرة التفاهم وجاري إعداد تقرير النقاط الجوهرية ومخاطر الملكية الفكرية",
            waitingForDepartment = "لا يوجد - الإجراء لدى الشؤون القانونية"
        )

        val req2 = ManagementRequestEntity(
            id = "req_2",
            requestNumber = "CFO-REQ-2026-009",
            requesterName = "المدير المالي (CFO - أ. محمود كمال)",
            department = "الإدارة المالية والمحاسبية",
            requestTitle = "الرأي القانوني في تطبيق ضريبة الخصم والإضافة على عقود ترخيص البرمجيات الأجنبية",
            description = "وصلتنا فاتورة سنوية من مزود السحابة الأجنبي بقيمة 40,000 دولار، ونحتاج لتحديد هل يتوجب علينا قانوناً استقطاع ضريبة من المنبع وفقاً لاتفاقية منع الازدواج الضريبي؟",
            receivedAt = now - day * 3,
            sourceChannel = "بريد إلكتروني رسمي",
            priority = "عالية",
            dueDate = now + day * 2,
            status = "بانتظار رد",
            clarityLevel = "يحتاج مستندات إضافية",
            responseNotes = "طلبنا من الإدارة المالية إرسال شهادة الإقامة الضريبية للمورد الأجنبي وبانتظار استلامها",
            waitingForDepartment = "الإدارة المالية (بانتظار شهادة الإقامة الضريبية)"
        )

        db.managementRequestDao().insert(req1)
        db.managementRequestDao().insert(req2)

        // 6. Finance Followups
        val fin1 = FinanceFollowupEntity(
            id = "fin_1",
            referenceNumber = "FIN-2026-033",
            counterpartyId = c1.id,
            counterpartyName = c1.name,
            contractId = ctr1.id,
            transactionType = "دفعة تعاقدية مستحقة",
            invoiceReference = "INV-2026-104",
            amountDue = 150000.0,
            amountPaid = 50000.0,
            currency = "EGP",
            dueDate = now - day * 12, // متأخرة!
            status = "متأخر",
            financeConfirmationStatus = "بانتظار التأكيد",
            disputeNotes = "العميل يدعي أنه أرسل أمر دفع بنكي، والإدارة المالية لم تتأكد من دخول المبلغ في الحساب",
            responsibleFollowupPerson = "عمرو عادل بالتنسيق مع الحسابات",
            nextAction = "طلب صورة الشيك أو إشعار التحويل المعتمد من المدير المالي للعميل",
            notes = "المتبقي 100,000 ج.م يحتاج لمتابعة مشددة قبل الإيقاف المؤقت للخدمة"
        )

        val fin2 = FinanceFollowupEntity(
            id = "fin_2",
            referenceNumber = "FIN-2026-042",
            counterpartyId = c2.id,
            counterpartyName = c2.name,
            contractId = "",
            transactionType = "أتعاب ورسوم تأسيس شركة",
            invoiceReference = "REC-INC-001",
            amountDue = 30000.0,
            amountPaid = 25000.0,
            currency = "EGP",
            dueDate = now + day * 5,
            status = "مستحق قريباً",
            financeConfirmationStatus = "مؤكد رسمياً",
            disputeNotes = "",
            responsibleFollowupPerson = "عمرو عادل",
            nextAction = "تحصيل الدفعة الأخيرة 5000 ج.م فور استلام السجل التجاري والبطاقة الضريبية",
            notes = "العميل متعاون ومستعد للسداد عند تسليم الملف"
        )

        val fin3 = FinanceFollowupEntity(
            id = "fin_3",
            referenceNumber = "FIN-2026-019",
            counterpartyId = c3.id,
            counterpartyName = c3.name,
            contractId = ctr2.id,
            transactionType = "فاتورة مورد مستحقة السداد",
            invoiceReference = "CX-INV-8910",
            amountDue = 45000.0,
            amountPaid = 0.0,
            currency = "EGP",
            dueDate = now + day * 10,
            status = "مستحق",
            financeConfirmationStatus = "بانتظار التأكيد",
            disputeNotes = "",
            responsibleFollowupPerson = "الإدارة المالية",
            nextAction = "مراجعة بنود مطابقة الفاتورة مع العقد قبل التوقيع على إذن الصرف",
            notes = "تأكد من عدم وجود رسوم صيانة إضافية غير منصوص عليها بالعقد"
        )

        db.financeDao().insert(fin1)
        db.financeDao().insert(fin2)
        db.financeDao().insert(fin3)

        // 7. Opportunities
        val opp1 = OpportunityEntity(
            id = "opp_1",
            opportunityNumber = "OPP-2026-017",
            contactId = c1.id,
            clientName = "شركة النيل للوجستيات والتوزيع",
            companyName = "مجموعة النيل القابضة",
            title = "بناء نظام تتبع الأسطول الذكي وإدارة عقود الشحن",
            serviceType = "تطوير أنظمة متخصصة وتطبيقات إنترنت الأشياء",
            source = "توصية من رئيس مجلس الإدارة",
            estimatedValue = 680000.0,
            currency = "EGP",
            stage = "مراجعة قانونية وتجارية",
            firstContactAt = now - day * 14,
            nextFollowupAt = now + day * 2,
            proposalStatus = "تم الإرسال",
            legalReviewStatus = "قيد المراجعة",
            expectedCloseDate = now + day * 20,
            nextAction = "إعداد مسودة العقد النهائي وبنود الضمان الفني والصيانة لمدة عام",
            notes = "الفرصة ذات أهمية استراتيجية وستفتح باب لقطاع النقل واللوجستيات"
        )

        val opp2 = OpportunityEntity(
            id = "opp_2",
            opportunityNumber = "OPP-2026-021",
            contactId = c2.id,
            clientName = "أكاديمية المعرفة للتعليم الرقمي",
            companyName = "شركة نوليدج تك للتدريب",
            title = "ترخيص منصة الامتحانات الإلكترونية السحابية LMS",
            serviceType = "ترخيص برمجيات كخدمة SaaS",
            source = "الموقع الإلكتروني لشركتي",
            estimatedValue = 195000.0,
            currency = "EGP",
            stage = "تفاوض",
            firstContactAt = now - day * 7,
            nextFollowupAt = now + day * 4,
            proposalStatus = "معتمد",
            legalReviewStatus = "معتمد",
            expectedCloseDate = now + day * 15,
            nextAction = "إرسال مسودة اتفاقية الاشتراك السنوي وبنود حماية الملكية الفكرية",
            notes = "تم الاتفاق على الشروط الفنية، والتفاوض الآن على طريقة السداد ربع السنوي"
        )

        db.opportunityDao().insert(opp1)
        db.opportunityDao().insert(opp2)

        // 8. Meetings
        val m1 = MeetingEntity(
            id = "meet_1",
            title = "اجتماع مفاوضات العقد السحابي مع المستشار القانوني للشركة القابضة",
            meetingDate = now - day * 1,
            participants = "عمرو عادل (مدير الشؤون القانونية)، م. أحمد (CEO)، د. شريف (المستشار القانوني للعميل)",
            summary = "تمت مناقشة البنود الخلافية في العقد. وافق العميل على خفض الشرط الجزائي إلى 7.5% بشرط التزام شركتي بوقت استجابة للأعطال الحرجة أقل من ساعتين.",
            decisions = "1. تعديل المادة 8 الخاصة بمسؤولية مزود الخدمة.\n2. إدراج ملحق اتفاقية مستوى الخدمة SLA التفصيلي.\n3. رفع المسودة المعدلة للتوقيع يوم الأحد القادم.",
            nextAction = "إرسال النسخة المنقحة بصيغة Track Changes للطرف الآخر",
            relatedEntityId = ctr1.id,
            relatedEntityType = "عقد"
        )
        db.meetingDao().insert(m1)

        // 9. Documents
        val d1 = DocumentEntity(
            id = "doc_1",
            fileName = "مسودة_عقد_المنصة_السحابية_v2.2_معتمدة.pdf",
            fileType = "PDF",
            fileSizeKb = 840,
            documentCategory = "عقد",
            relatedEntityType = "عقد",
            relatedEntityId = ctr1.id,
            relatedEntityTitle = ctr1.title,
            versionNumber = "v2.2",
            isFinalVersion = false,
            notes = "النسخة متضمنة تعديلات شروط المسؤولية والسرية"
        )
        val d2 = DocumentEntity(
            id = "doc_2",
            fileName = "شهادة_عدم_التباس_فينتك_باي.pdf",
            fileType = "PDF",
            fileSizeKb = 320,
            documentCategory = "تأسيس",
            relatedEntityType = "تأسيس",
            relatedEntityId = inc1.id,
            relatedEntityTitle = inc1.companyName,
            versionNumber = "v1.0",
            isFinalVersion = true,
            notes = "مستخرجة من الهيئة العامة للاستثمار وسارية لمدة شهرين"
        )
        val d3 = DocumentEntity(
            id = "doc_3",
            fileName = "اتفاقية_سرية_المعلومات_NDA_موقعة.pdf",
            fileType = "PDF",
            fileSizeKb = 460,
            documentCategory = "اتفاقية سرية",
            relatedEntityType = "عقد",
            relatedEntityId = ctr2.id,
            relatedEntityTitle = ctr2.title,
            versionNumber = "v1.0",
            isFinalVersion = true,
            notes = "نسخة نهائية ومصدقة"
        )
        db.documentDao().insert(d1)
        db.documentDao().insert(d2)
        db.documentDao().insert(d3)

        // 10. Activity Logs
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "عقد",
                entityId = ctr1.id,
                actionType = "تعديل",
                summary = "تحديث حالة التفاوض لعقد المنصة السحابية وإضافة التزامات SLA"
            )
        )
        db.activityLogDao().insert(
            ActivityLogEntity(
                entityType = "تأسيس",
                entityId = inc1.id,
                actionType = "إكمال مرحلة",
                summary = "اكتمال توثيق عقود التأسيس والانتقال لمرحلة تقديم الملف بهيئة الاستثمار"
            )
        )
    }
}
