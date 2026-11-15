package com.example.data.curriculum

import com.example.data.model.*

/**
 * Curated NCTB-aligned StudyQuest original academic catalog for Classes 6-10 & SSC
 * (Bangla Version & English Version), with future-ready HSC extension slot.
 * Every question includes verified original educational explanations and attribution.
 */
object BangladeshCurriculumCatalog {

    val classes: List<ClassLevel> = listOf(
        ClassLevel("class_6", "Class 6", "ষষ্ঠ শ্রেণি", supportsGroups = false),
        ClassLevel("class_7", "Class 7", "সপ্তম শ্রেণি", supportsGroups = false),
        ClassLevel("class_8", "Class 8", "অষ্টম শ্রেণি", supportsGroups = false),
        ClassLevel("class_9", "Class 9", "নবম শ্রেণি", supportsGroups = true),
        ClassLevel("class_10", "Class 10", "দশম শ্রেণি", supportsGroups = true),
        ClassLevel("ssc", "SSC Exam Prep", "এসএসসি প্রস্তুতি", supportsGroups = true),
        ClassLevel("hsc_ready", "HSC (Coming Soon)", "এইচএসসি (শীঘ্রই আসছে)", supportsGroups = true, futureReadyTag = "future_2027")
    )

    val subjects: List<AcademicSubject> = listOf(
        AcademicSubject("math", listOf("class_6", "class_7", "class_8", "class_9", "class_10", "ssc"), listOf("general", "science", "business", "humanities"), "Mathematics", "গণিত", "MATH", 0xFF1B5E3A),
        AcademicSubject("english", listOf("class_6", "class_7", "class_8", "class_9", "class_10", "ssc"), listOf("general", "science", "business", "humanities"), "English", "ইংরেজি", "ENG", 0xFF2E8B57),
        AcademicSubject("bangla", listOf("class_6", "class_7", "class_8", "class_9", "class_10", "ssc"), listOf("general", "science", "business", "humanities"), "Bangla", "বাংলা", "BAN", 0xFFD97706),
        AcademicSubject("gen_science", listOf("class_6", "class_7", "class_8"), listOf("general"), "General Science", "সাধারণ বিজ্ঞান", "SCI", 0xFF0D9488),
        AcademicSubject("physics", listOf("class_9", "class_10", "ssc"), listOf("science"), "Physics", "পদার্থবিজ্ঞান", "PHY", 0xFF0F766E),
        AcademicSubject("chemistry", listOf("class_9", "class_10", "ssc"), listOf("science"), "Chemistry", "রসায়ন", "CHEM", 0xFFB45309),
        AcademicSubject("biology", listOf("class_9", "class_10", "ssc"), listOf("science"), "Biology", "জীববিজ্ঞান", "BIO", 0xFF15803D),
        AcademicSubject("ict", listOf("class_6", "class_7", "class_8", "class_9", "class_10", "ssc"), listOf("general", "science", "business", "humanities"), "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "ICT", 0xFF1D4ED8),
        AcademicSubject("social_studies", listOf("class_6", "class_7", "class_8", "class_9", "class_10", "ssc"), listOf("general", "business", "humanities"), "Bangladesh & Global Studies", "বাংলাদেশ ও বিশ্বপরিচয়", "BGS", 0xFFE66A2C),
        AcademicSubject("gk", listOf("class_6", "class_7", "class_8", "class_9", "class_10", "ssc"), listOf("general", "science", "business", "humanities"), "General Knowledge", "সাধারণ জ্ঞান", "GK", 0xFF7C3AED)
    )

    fun subjectsForClass(classId: String): List<AcademicSubject> =
        subjects.filter { classId in it.classIds }

    fun subjectsForClassAndGroup(classId: String, groupId: String): List<AcademicSubject> =
        subjects.filter {
            classId in it.classIds &&
                (groupId == "general" || groupId in it.groupIds || "general" in it.groupIds)
        }

    val chapters: List<AcademicChapter> = listOf(
        AcademicChapter("ch_math_sets", "math", "class_10", 1, "Sets and Functions", "সেট ও ফাংশন", "Roster & set-builder notation, Venn diagrams, domain & range.", "তালিকা ও গঠন পদ্ধতি, ভেনচিত্র, ডোমেন ও রেঞ্জ।"),
        AcademicChapter("ch_math_trig", "math", "class_10", 2, "Trigonometric Ratios", "ত্রিকোণমিতিক অনুপাত", "Right-triangle ratios, standard angles, and identities.", "সমকোণী ত্রিভুজের অনুপাত, আদর্শ কোণ ও অভেদাবলি।"),
        AcademicChapter("ch_math_c6_num", "math", "class_6", 1, "Whole Numbers & Fractions", "স্বাভাবিক সংখ্যা ও ভগ্নাংশ", "Place value, divisibility, LCM, HCF, and fraction operations.", "স্থানীয় মান, বিভাজ্যতা, ল.সা.গু ও গ.সা.গু।"),
        AcademicChapter("ch_math_c8_alg", "math", "class_8", 1, "Algebraic Formulae", "বীজগণিতীয় সূত্রাবলি", "Square & cube identities, factorization, and applications.", "বর্গ ও ঘনের সূত্র, উৎপাদকে বিশ্লেষণ ও প্রয়োগ।"),
        AcademicChapter("ch_phy_motion", "physics", "class_10", 1, "Motion & Equations", "গতি ও গতির সমীকরণ", "Scalar vs vector, velocity, acceleration, and s = ut + ½at².", "স্কেলার ও ভেক্টর, বেগ, ত্বরণ ও গতির সমীকরণ।"),
        AcademicChapter("ch_chem_atom", "chemistry", "class_10", 1, "Structure of Matter", "পদার্থের গঠন", "Atomic number, mass number, isotopes, and electron shells.", "পারমাণবিক সংখ্যা, ভর সংখ্যা, আইসোটোপ ও ইলেকট্রন বিন্যাস।"),
        AcademicChapter("ch_bio_cell", "biology", "class_10", 1, "Cell & Tissue Biology", "কোষ ও টিস্যু", "Plant vs animal organelles, mitochondria, and mitosis.", "উদ্ভিদ ও প্রাণী কোষ, মাইটোকন্ড্রিয়া ও মাইটোসিস।"),
        AcademicChapter("ch_sci_c7_heat", "gen_science", "class_7", 1, "Heat & Temperature", "তাপ ও তাপমাত্রা", "Celsius-Fahrenheit conversion, conduction, convection, radiation.", "সেলসিয়াস-ফারেনহাইট রূপান্তর ও তাপ সঞ্চালন।"),
        AcademicChapter("ch_eng_grammar", "english", "class_10", 1, "Tense, Voice & Narration", "কাল, বাচ্য ও উক্তি", "Active-passive transformation and direct-indirect speech.", "একটিভ-প্যাসিভ ভয়েস এবং ডিরেক্ট-ইনডিরেক্ট ন্যারেশন।"),
        AcademicChapter("ch_ban_byakoron", "bangla", "class_10", 1, "সমাস, উপসর্গ ও ধ্বনিতত্ত্ব", "সমাস, উপসর্গ ও ধ্বনিতত্ত্ব", "বাংলা ব্যাকরণের মূল ধ্বনি, সন্ধি, উপসর্গ ও সমাস নির্ণয়।", "বাংলা ব্যাকরণের মূল ধ্বনি, সন্ধি, উপসর্গ ও সমাস নির্ণয়।"),
        AcademicChapter("ch_ict_web", "ict", "class_10", 1, "Digital Bangladesh & HTML", "ডিজিটাল বাংলাদেশ ও এইচটিএমএল", "E-governance, binary logic, and semantic HTML tags.", "ই-গভর্ন্যান্স, বাইনারি সংখ্যা এবং এইচটিএমএল ট্যাগ।"),
        AcademicChapter("ch_bgs_lib", "social_studies", "class_10", 1, "Language Movement & 1971", "ভাষা আন্দোলন ও মুক্তিযুদ্ধ", "1952 Language Movement, Six-Point Demand, and Liberation War.", "১৯৫২-র ভাষা আন্দোলন, ছয় দফা ও ১৯৭১-এর মুক্তিযুদ্ধ।"),
        AcademicChapter("ch_gk_bd", "gk", "class_10", 1, "Bangladesh Geography & Rivers", "বাংলাদেশের ভৌগোলিক পরিচিতি ও নদ-নদী", "Major rivers, divisions, Padma Bridge, and national heritage.", "প্রধান নদ-নদী, বিভাগ, পদ্মা সেতু ও জাতীয় ঐতিহ্য।")
    )

    val lessons: List<StudyLesson> = listOf(
        StudyLesson(
            id = "lesson_trig_basics",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_trig",
            topicId = "topic_trig_ratios",
            titleEn = "Right-Triangle Trigonometric Ratios",
            titleBn = "সমকোণী ত্রিভুজের ত্রিকোণমিতিক অনুপাত",
            objectivesEn = listOf(
                "Identify opposite, adjacent, and hypotenuse relative to angle θ.",
                "Recall sin θ, cos θ, tan θ and their reciprocals.",
                "Apply sin²θ + cos²θ = 1 to solve SSC board-style problems."
            ),
            objectivesBn = listOf(
                "সূক্ষ্মকোণ θ-এর সাপেক্ষে লম্ব, ভূমি ও অতিভুজ চিহ্নিত করতে পারবে।",
                "sin θ, cos θ, tan θ এবং এদের বিপরীত অনুপাত মনে রাখতে পারবে।",
                "sin²θ + cos²θ = 1 ব্যবহার করে এসএসসি মানের সমস্যা সমাধান করতে পারবে।"
            ),
            explanationEn = "In a right-angled triangle ABC with ∠B = 90° and ∠C = θ, the side opposite to θ is Perpendicular (P), the side adjacent to θ is Base (B), and the longest side opposite 90° is Hypotenuse (H). Trigonometric ratios relate angle θ to ratios of these sides regardless of triangle size.",
            explanationBn = "একটি সমকোণী ত্রিভুজ ABC-তে ∠B = ৯০° এবং ∠C = θ হলে, θ কোণের বিপরীত বাহু হলো লম্ব (P), সংলগ্ন বাহু হলো ভূমি (B) এবং ৯০° কোণের বিপরীত বৃহত্তম বাহু হলো অতিভুজ (H)। ত্রিভুজটি ছোট বা বড় হলেও নির্দিষ্ট কোণ θ-এর জন্য বাহুগুলোর অনুপাত সর্বদা ধ্রুব থাকে।",
            definitionsEn = listOf(
                "sin θ" to "Opposite / Hypotenuse (P / H)",
                "cos θ" to "Adjacent / Hypotenuse (B / H)",
                "tan θ" to "Opposite / Adjacent (P / B)"
            ),
            definitionsBn = listOf(
                "sin θ" to "লম্ব / অতিভুজ (P / H)",
                "cos θ" to "ভূমি / অতিভুজ (B / H)",
                "tan θ" to "লম্ব / ভূমি (P / B)"
            ),
            formulas = listOf(
                "sin²θ + cos²θ = 1",
                "1 + tan²θ = sec²θ",
                "1 + cot²θ = cosec²θ",
                "sin 30° = 1/2,  cos 60° = 1/2,  tan 45° = 1"
            ),
            diagramAscii = """
                   A
                   |\
                   | \  Hypotenuse (H / অতিভুজ)
      Opposite (P) |  \
          (লম্ব)   |   \ θ
                   B----C
                    Base (B / ভূমি)
            """.trimIndent(),
            examplesEn = listOf("If sin θ = 3/5, then cos θ = √(1 - 9/25) = 4/5 and tan θ = 3/4."),
            examplesBn = listOf("যদি sin θ = ৩/৫ হয়, তবে cos θ = √(১ - ৯/২৫) = ৪/৫ এবং tan θ = ৩/৪।"),
            keyPointsEn = listOf("Remember: sin θ never exceeds 1 for acute angles.", "tan θ = sin θ / cos θ."),
            keyPointsBn = listOf("মনে রাখো: সূক্ষ্মকোণের জন্য sin θ-এর মান কখনো ১-এর বেশি হয় না।", "tan θ = sin θ / cos θ।"),
            relatedTopicIds = listOf("ch_math_sets")
        ),
        StudyLesson(
            id = "lesson_sets_basics",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_sets",
            topicId = "topic_sets_ops",
            titleEn = "Sets, Subsets & Power Sets",
            titleBn = "সেট, উপসেট ও শক্তি সেট",
            objectivesEn = listOf("Distinguish roster and set-builder forms.", "Compute subsets and power set cardinality 2ⁿ."),
            objectivesBn = listOf("তালিকা ও সেট গঠন পদ্ধতি আলাদা করতে পারবে।", "উপসেট ও শক্তি সেটের উপাদান সংখ্যা ২ⁿ নির্ণয় করতে পারবে।"),
            explanationEn = "A set is a well-defined collection of distinct objects. If set A has n elements, its power set P(A) contains exactly 2ⁿ subsets, including the empty set ∅ and A itself.",
            explanationBn = "বাস্তব বা চিন্তাজগতের সুসংজ্ঞায়িত বস্তুর সমাবেশকে সেট বলে। কোনো সেট A-এর উপাদান সংখ্যা n হলে, এর শক্তি সেট P(A)-এর উপাদান সংখ্যা হবে ঠিক ২ⁿ, যার মধ্যে ফাঁকা সেট ∅ এবং সেট A নিজেও অন্তর্ভুক্ত।",
            definitionsEn = listOf("Power Set P(A)" to "The set of all subsets of A.", "Disjoint Sets" to "Two sets with A ∩ B = ∅."),
            definitionsBn = listOf("শক্তি সেট P(A)" to "কোনো সেটের সকল উপসেট নিয়ে গঠিত সেট।", "নিশ্ছেদ সেট" to "দুটি সেটের সাধারণ উপাদান না থাকলে (A ∩ B = ∅)।"),
            formulas = listOf("|P(A)| = 2ⁿ", "n(A ∪ B) = n(A) + n(B) - n(A ∩ B)"),
            diagramAscii = """
                ┌─────────────────────────┐
                │   A (1, 2) ∩ (2, 3) B   │
                │    ( 1  ( 2 )  3 )      │
                └─────────────────────────┘
            """.trimIndent(),
            examplesEn = listOf("For A = {a, b, c}, n = 3 so |P(A)| = 2³ = 8 subsets."),
            examplesBn = listOf("A = {a, b, c} হলে n = ৩, তাই |P(A)| = ২³ = ৮টি উপসেট।"),
            keyPointsEn = listOf("Proper subsets count = 2ⁿ - 1."),
            keyPointsBn = listOf("প্রকৃত উপসেটের সংখ্যা = ২ⁿ - ১।"),
            relatedTopicIds = listOf("ch_math_trig")
        ),
        StudyLesson(
            id = "lesson_phy_motion",
            classId = "class_10",
            subjectId = "physics",
            chapterId = "ch_phy_motion",
            topicId = "topic_kinematics",
            titleEn = "Equations of Uniform Acceleration",
            titleBn = "সুষম ত্বরণে গতির সমীকরণ",
            objectivesEn = listOf("Differentiate displacement and distance.", "Use v = u + at and s = ut + ½at² accurately."),
            objectivesBn = listOf("সরণ ও দূরত্বের পার্থক্য বুঝতে পারবে।", "v = u + at এবং s = ut + ½at² সঠিকভাবে প্রয়োগ করতে পারবে।"),
            explanationEn = "When an object starts with initial velocity u and moves with constant acceleration a for time t, its final velocity v and displacement s follow four foundational kinematic equations.",
            explanationBn = "কোনো বস্তু u আদিবেগে যাত্রা শুরু করে t সময় ধরে a সুষম ত্বরণে চললে শেষবেগ v এবং সরণ s গতির চারটি মৌলিক সমীকরণ মেনে চলে।",
            definitionsEn = listOf("Acceleration (a)" to "Rate of change of velocity: (v - u) / t (unit: m/s²)."),
            definitionsBn = listOf("ত্বরণ (a)" to "সময়ের সাথে বেগ পরিবর্তনের হার: (v - u) / t (একক: m/s²)।"),
            formulas = listOf("v = u + at", "s = ut + ½at²", "v² = u² + 2as", "s = ((u + v)/2) · t"),
            diagramAscii = "u ──(a, t)──▶ v   |   s = area under v-t graph",
            examplesEn = listOf("Starting from rest (u=0) with a = 2 m/s² for 5 s: v = 10 m/s, s = 25 m."),
            examplesBn = listOf("স্থির অবস্থান (u=0) থেকে a = 2 m/s² ত্বরণে ৫ সেকেন্ডে বেগ v = 10 m/s এবং সরণ s = 25 m।"),
            keyPointsEn = listOf("Always convert km/h to m/s by dividing by 3.6."),
            keyPointsBn = listOf("সবসময় km/h কে ৩.৬ দিয়ে ভাগ করে m/s-এ রূপান্তর করো।"),
            relatedTopicIds = listOf("ch_math_trig")
        ),
        StudyLesson(
            id = "lesson_chem_atom",
            classId = "class_10",
            subjectId = "chemistry",
            chapterId = "ch_chem_atom",
            topicId = "topic_isotopes",
            titleEn = "Atomic Number, Mass & Isotopes",
            titleBn = "পারমাণবিক সংখ্যা, ভর ও আইসোটোপ",
            objectivesEn = listOf("Compute protons, neutrons, and electrons.", "Identify isotopes of Carbon and Hydrogen."),
            objectivesBn = listOf("প্রোটন, নিউট্রন ও ইলেকট্রন সংখ্যা বের করতে পারবে।", "কার্বন ও হাইড্রোজেনের আইসোটোপ চিনতে পারবে।"),
            explanationEn = "Atomic number Z equals the number of protons. Mass number A equals protons + neutrons (Z + N). Atoms of the same element with identical Z but different N are isotopes.",
            explanationBn = "মৌলের পরমাণুর নিউক্লিয়াসে অবস্থিত প্রোটন সংখ্যাই পারমাণবিক সংখ্যা (Z)। প্রোটন ও নিউট্রন সংখ্যার যোগফল ভর সংখ্যা (A)। একই প্রোটন সংখ্যা কিন্তু ভিন্ন নিউট্রন সংখ্যাবিশিষ্ট পরমাণুকে আইসোটোপ বলে।",
            definitionsEn = listOf("Isotope" to "Same Z, different neutron number N."),
            definitionsBn = listOf("আইসোটোপ" to "সমান প্রোটন সংখ্যা কিন্তু ভিন্ন ভর ও নিউট্রন সংখ্যাবিশিষ্ট পরমাণু।"),
            formulas = listOf("A = Z + N", "Max electrons in shell n = 2n²"),
            diagramAscii = "  [e⁻] ── Shell K(2), L(8), M(18) ── ( Z p⁺, N n⁰ )",
            examplesEn = listOf("Carbon-14 has Z = 6 protons and N = 14 - 6 = 8 neutrons."),
            examplesBn = listOf("কার্বন-১৪ এর প্রোটন সংখ্যা Z = ৬ এবং নিউট্রন সংখ্যা N = ১৪ - ৬ = ৮।"),
            keyPointsEn = listOf("In a neutral atom, electrons equal protons."),
            keyPointsBn = listOf("নিরপেক্ষ পরমাণুতে ইলেকট্রন ও প্রোটন সংখ্যা সমান।"),
            relatedTopicIds = listOf("ch_bio_cell")
        ),
        StudyLesson(
            id = "lesson_bio_cell",
            classId = "class_10",
            subjectId = "biology",
            chapterId = "ch_bio_cell",
            topicId = "topic_organelles",
            titleEn = "Mitochondria, Plastids & Cell Organelles",
            titleBn = "মাইটোকন্ড্রিয়া, প্লাস্টিড ও কোষীয় অঙ্গাণু",
            objectivesEn = listOf("Compare plant and animal cells.", "Explain why mitochondria is the powerhouse of the cell."),
            objectivesBn = listOf("উদ্ভিদ ও প্রাণী কোষের পার্থক্য ব্যাখ্যা করতে পারবে।", "মাইটোকন্ড্রিয়াকে কেন কোষের পাওয়ার হাউস বলা হয় তা বুঝবে।"),
            explanationEn = "Eukaryotic cells contain membrane-bound organelles. Mitochondria generate ATP through aerobic respiration on cristae folds, while plastids (chloroplasts) occur in plant cells for photosynthesis.",
            explanationBn = "প্রকৃত কোষে ঝিল্লিযুক্ত অঙ্গাণু থাকে। মাইটোকন্ড্রিয়া সবাত শ্বসনের মাধ্যমে ATP শক্তি উৎপন্ন করে বলে একে কোষের পাওয়ার হাউস বলা হয়। অন্যদিকে প্লাস্টিড কেবল উদ্ভিদ কোষে সালোকসংশ্লেষণের জন্য পাওয়া যায়।",
            definitionsEn = listOf("Cristae" to "Inner membrane folds of mitochondria where ATP synthesis occurs."),
            definitionsBn = listOf("ক্রিস্টি" to "মাইটোকন্ড্রিয়ার ভেতরের ভাঁজ হওয়া পর্দা যেখানে শক্তি উৎপন্ন হয়।"),
            formulas = listOf("6CO₂ + 6H₂O + Light ─▶ C₆H₁₂O₆ + 6O₂"),
            diagramAscii = "Plant Cell = Cell Wall + Chloroplast + Large Vacuole + Mitochondria",
            examplesEn = listOf("Mature mammalian RBCs lack nuclei, whereas plant sieve cells have cellulose walls."),
            examplesBn = listOf("উদ্ভিদ কোষে সেলুলোজ নির্মিত কোষপ্রাচীর থাকে যা প্রাণী কোষে অনুপস্থিত।"),
            keyPointsEn = listOf("Centrosome is typically in animal cells; plastids in plant cells."),
            keyPointsBn = listOf("প্লাস্টিড উদ্ভিদ কোষের অনন্য বৈশিষ্ট্য।"),
            relatedTopicIds = listOf("ch_chem_atom")
        ),
        StudyLesson(
            id = "lesson_ict_html",
            classId = "class_10",
            subjectId = "ict",
            chapterId = "ch_ict_web",
            topicId = "topic_html_tags",
            titleEn = "Web Design Basics & HTML Tags",
            titleBn = "ওয়েব ডিজাইন ও HTML ট্যাগ",
            objectivesEn = listOf("Understand paired vs empty HTML tags.", "Build headings, links, and tables cleanly."),
            objectivesBn = listOf("কন্টেইনার ও এম্পটি HTML ট্যাগের পার্থক্য জানবে।", "লিংক ও টেবিল ট্যাগ সঠিকভাবে লিখতে পারবে।"),
            explanationEn = "HTML uses angle-bracket tags to structure web pages. Container tags like <p>...</p> wrap content, whereas empty tags like <br> or <img> need no closing tag.",
            explanationBn = "ওয়েবপেইজের কাঠামো তৈরিতে HTML ট্যাগ ব্যবহৃত হয়। কন্টেইনার ট্যাগের শুরু ও শেষ থাকে (যেমন <p>...</p>), কিন্তু এম্পটি ট্যাগের (<br>, <img>) কোনো ক্লোজিং ট্যাগ থাকে না।",
            definitionsEn = listOf("Hyperlink <a>" to "Uses href attribute to connect web pages."),
            definitionsBn = listOf("হাইপারলিংক <a>" to "href অ্যাট্রিবিউটের মাধ্যমে এক পেইজের সাথে অন্য পেইজ যুক্ত করে।"),
            formulas = listOf("<a href=\"https://studyquest.bd\">Visit</a>", "<tr><td>Row</td></tr>"),
            diagramAscii = "<html> ──▶ <head><title>...</title></head> + <body>...</body>",
            examplesEn = listOf("<h1> is the largest heading tag; <h6> is the smallest."),
            examplesBn = listOf("<h1> হলো সবচেয়ে বড় হেডিং ট্যাগ এবং <h6> সবচেয়ে ছোট।"),
            keyPointsEn = listOf("<table> uses <tr> for rows and <td> / <th> for cells."),
            keyPointsBn = listOf("টেবিলের সারির জন্য <tr> এবং সেলের জন্য <td> ব্যবহৃত হয়।"),
            relatedTopicIds = listOf("ch_eng_grammar")
        ),
        StudyLesson(
            id = "lesson_bgs_1971",
            classId = "class_10",
            subjectId = "social_studies",
            chapterId = "ch_bgs_lib",
            topicId = "topic_liberation",
            titleEn = "1952 Language Movement to 1971 Independence",
            titleBn = "১৯৫২-র ভাষা আন্দোলন থেকে ১৯৭১-এর স্বাধীনতা",
            objectivesEn = listOf("Trace the key milestones of Bengali nationalism.", "Identify dates of 21 February, 7 March, 26 March, and 16 December."),
            objectivesBn = listOf("বাঙালি জাতীয়তাবাদের প্রধান মাইলফলকসমূহ জানতে পারবে।", "২১শে ফেব্রুয়ারি, ৭ই মার্চ, ২৬শে মার্চ ও ১৬ই ডিসেম্বরের তাৎপর্য জানবে।"),
            explanationEn = "The 1952 Language Movement (21 February / ৮ ফাল্গুন ১৩৫৮) sparked cultural and political self-determination, leading through the 1966 Six-Point Movement and the 1971 Liberation War to an independent Bangladesh on 16 December 1971.",
            explanationBn = "১৯৫২ সালের ২১শে ফেব্রুয়ারি (৮ই ফাল্গুন ১৩৫৮) রাষ্ট্রভাষা বাংলার দাবিতে যে আন্দোলন শুরু হয়, তা ১৯৬৬-র ছয় দফা ও ১৯৭১-এর মহান মুক্তিযুদ্ধের মাধ্যমে ১৬ই ডিসেম্বর বিজয়ের পূর্ণতা লাভ করে।",
            definitionsEn = listOf("International Mother Language Day" to "21 February, recognized by UNESCO in 1999."),
            definitionsBn = listOf("আন্তর্জাতিক মাতৃভাষা দিবস" to "২১শে ফেব্রুয়ারি, ইউনেস্কো কর্তৃক ১৯৯৯ সালে স্বীকৃত।"),
            formulas = listOf("1952 (Language) ─▶ 1966 (6-Point) ─▶ 1969 (Mass Uprising) ─▶ 1971 (Victory)"),
            diagramAscii = "1952 ─▶ 1954 ─▶ 1966 ─▶ 1969 ─▶ 26 Mar 1971 ─▶ 16 Dec 1971",
            examplesEn = listOf("Mujibnagar Government took oath on 17 April 1971 at Baidyanathtala (Meherpur)."),
            examplesBn = listOf("১৭ই এপ্রিল ১৯৭১ মেহেরপুরের বৈদ্যনাথতলায় মুজিবনগর সরকার শপথ গ্রহণ করে।"),
            keyPointsEn = listOf("Bangladesh was divided into 11 sectors during the Liberation War."),
            keyPointsBn = listOf("মুক্তিযুদ্ধে বাংলাদেশকে ১১টি সেক্টরে ভাগ করা হয়েছিল।"),
            relatedTopicIds = listOf("ch_gk_bd")
        ),
        StudyLesson(
            id = "lesson_c6_fractions",
            classId = "class_6",
            subjectId = "math",
            chapterId = "ch_math_c6_num",
            topicId = "topic_fractions",
            titleEn = "HCF, LCM & Fraction Comparison",
            titleBn = "গ.সা.গু, ল.সা.গু ও ভগ্নাংশের তুলনা",
            objectivesEn = listOf("Find HCF and LCM of two numbers.", "Compare unlike fractions with common denominators."),
            objectivesBn = listOf("দুটি সংখ্যার গ.সা.গু ও ল.সা.গু নির্ণয় করতে পারবে।", "সমহর বিশিষ্ট করে ভগ্নাংশ তুলনা করতে পারবে।"),
            explanationEn = "For any two positive integers a and b, their product equals HCF(a, b) × LCM(a, b). To add or compare fractions with different denominators, first find the LCM of the denominators.",
            explanationBn = "দুটি সংখ্যার গুণফল = সংখ্যাদ্বয়ের গ.সা.গু × ল.সা.গু। ভিন্ন হরের ভগ্নাংশ যোগ বা তুলনা করতে প্রথমে হরগুলোর ল.সা.গু বের করে সমহর করতে হয়।",
            definitionsEn = listOf("Proper Fraction" to "Numerator < Denominator (e.g., 3/5)."),
            definitionsBn = listOf("প্রকৃত ভগ্নাংশ" to "লব < হর (যেমন ৩/৫)।"),
            formulas = listOf("a × b = HCF(a,b) × LCM(a,b)"),
            diagramAscii = "3/4 vs 2/3 ─▶ LCM(4,3)=12 ─▶ 9/12 > 8/12",
            examplesEn = listOf("HCF(12, 18) = 6 and LCM(12, 18) = 36; 6 × 36 = 216 = 12 × 18."),
            examplesBn = listOf("১২ ও ১৮-এর গ.সা.গু ৬ এবং ল.সা.গু ৩৬; ৬ × ৩৬ = ২১৬ = ১২ × ১৮।"),
            keyPointsEn = listOf("Prime factorization makes LCM fast and error-free."),
            keyPointsBn = listOf("মৌলিক উৎপাদকের সাহায্যে ল.সা.গু নির্ণয় সহজ হয়।"),
            relatedTopicIds = listOf("ch_math_sets")
        )
    )

    val questions: List<QuestionItem> = listOf(
        QuestionItem(
            id = "q_trig_1",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_trig",
            topicId = "topic_trig_ratios",
            board = "Practice Bank",
            examYear = 2025,
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "In a right triangle, if sin θ = 1/2 for an acute angle θ, what is the value of θ?",
            promptBn = "একটি সমকোণী ত্রিভুজে সূক্ষ্মকোণ θ-এর জন্য sin θ = ১/২ হলে, θ-এর মান কত?",
            optionsEn = listOf("30°", "45°", "60°", "90°"),
            optionsBn = listOf("৩০°", "৪৫°", "৬০°", "৯০°"),
            correctOptionIndices = listOf(0),
            explanationEn = "From standard trigonometric ratios, sin 30° = 1/2.",
            explanationBn = "ত্রিকোণমিতিক আদর্শ মান অনুযায়ী sin ৩০° = ১/২।"
        ),
        QuestionItem(
            id = "q_trig_2",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_trig",
            topicId = "topic_trig_ratios",
            type = QuestionType.NUMERICAL,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "Evaluate: 4 sin²30° + cos²60° + tan²45° (enter exact decimal or fraction like 2.25).",
            promptBn = "মান নির্ণয় করো: 4 sin²30° + cos²60° + tan²45° (দশমিক সংখ্যায় লেখো, যেমন 2.25)।",
            acceptableTextAnswers = listOf("2.25", "9/4", "২.২৫"),
            explanationEn = "4(1/2)² + (1/2)² + 1² = 4(1/4) + 1/4 + 1 = 1 + 0.25 + 1 = 2.25.",
            explanationBn = "4(1/2)² + (1/2)² + 1² = 1 + 0.25 + 1 = 2.25 (বা ২.২৫)।"
        ),
        QuestionItem(
            id = "q_sets_1",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_sets",
            topicId = "topic_sets_ops",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "If set A = {2, 4, 6, 8}, how many elements are in its power set P(A)?",
            promptBn = "A = {2, 4, 6, 8} হলে, এর শক্তি সেট P(A)-এর উপাদান সংখ্যা কত?",
            optionsEn = listOf("4", "8", "15", "16"),
            optionsBn = listOf("৪", "৮", "১৫", "১৬"),
            correctOptionIndices = listOf(3),
            explanationEn = "Since n(A) = 4, |P(A)| = 2⁴ = 16.",
            explanationBn = "যেহেতু উপাদান সংখ্যা ৪, তাই |P(A)| = ২⁴ = ১৬।"
        ),
        QuestionItem(
            id = "q_sets_multi",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_sets",
            topicId = "topic_sets_ops",
            type = QuestionType.MULTI_MCQ,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "Select ALL true statements for sets A = {1, 2, 3} and B = {3, 4}:",
            promptBn = "A = {1, 2, 3} এবং B = {3, 4} সেটের জন্য সঠিক উক্তিগুলো চিহ্নিত করো:",
            optionsEn = listOf("A ∩ B = {3}", "A ∪ B has 5 elements", "∅ is a subset of A", "4 ∈ A"),
            optionsBn = listOf("A ∩ B = {3}", "A ∪ B-তে ৫টি উপাদান আছে", "∅ হলো A-এর উপসেট", "4 ∈ A"),
            correctOptionIndices = listOf(0, 2),
            explanationEn = "A ∪ B = {1, 2, 3, 4} has 4 elements (not 5), and 4 ∉ A. Both A ∩ B = {3} and ∅ ⊆ A are true.",
            explanationBn = "A ∪ B = {1, 2, 3, 4} যাতে ৪টি উপাদান আছে। কিন্তু A ∩ B = {3} এবং ∅ ⊆ A সঠিক।"
        ),
        QuestionItem(
            id = "q_phy_1",
            classId = "class_10",
            subjectId = "physics",
            chapterId = "ch_phy_motion",
            topicId = "topic_kinematics",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "A car starts from rest with uniform acceleration 3 m/s². How far does it travel in 4 seconds?",
            promptBn = "একটি গাড়ি স্থির অবস্থা থেকে 3 m/s² সুষম ত্বরণে যাত্রা করলে ৪ সেকেন্ডে কত দূরত্ব অতিক্রম করবে?",
            optionsEn = listOf("12 m", "24 m", "36 m", "48 m"),
            optionsBn = listOf("১২ মি.", "২৪ মি.", "৩৬ মি.", "৪৮ মি."),
            correctOptionIndices = listOf(1),
            explanationEn = "Using s = ut + ½at² with u = 0: s = 0 + ½ × 3 × 4² = 24 m.",
            explanationBn = "s = ut + ½at² অনুযায়ী u = 0 হলে s = ½ × ৩ × ৪² = ২৪ মিটার।"
        ),
        QuestionItem(
            id = "q_phy_tf",
            classId = "class_10",
            subjectId = "physics",
            chapterId = "ch_phy_motion",
            topicId = "topic_kinematics",
            type = QuestionType.TRUE_FALSE,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Displacement can be zero even when total distance travelled is positive.",
            promptBn = "মোট অতিক্রান্ত দূরত্ব ধনাত্মক হলেও বস্তুর সরণ শূন্য হতে পারে।",
            optionsEn = listOf("True", "False"),
            optionsBn = listOf("সত্য", "মিথ্যা"),
            correctOptionIndices = listOf(0),
            explanationEn = "True! If an object returns to its starting point, net displacement is zero while distance is positive.",
            explanationBn = "সত্য! বস্তু ঘুরে আবার আদি অবস্থানে ফিরে এলে সরণ শূন্য হয়, কিন্তু দূরত্ব শূন্য হয় না।"
        ),
        QuestionItem(
            id = "q_chem_1",
            classId = "class_10",
            subjectId = "chemistry",
            chapterId = "ch_chem_atom",
            topicId = "topic_isotopes",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "How many neutrons are in an atom of Sodium-23 (²³₁₁Na)?",
            promptBn = "সোডিয়াম-২৩ (²³₁₁Na) পরমাণুতে কয়টি নিউট্রন আছে?",
            optionsEn = listOf("11", "12", "23", "34"),
            optionsBn = listOf("১১", "১২", "২৩", "৩৪"),
            correctOptionIndices = listOf(1),
            explanationEn = "Neutrons N = A - Z = 23 - 11 = 12.",
            explanationBn = "নিউট্রন সংখ্যা N = A - Z = ২৩ - ১১ = ১২।"
        ),
        QuestionItem(
            id = "q_bio_fill",
            classId = "class_10",
            subjectId = "biology",
            chapterId = "ch_bio_cell",
            topicId = "topic_organelles",
            type = QuestionType.FILL_BLANK,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "The organelle known as the powerhouse of the cell for ATP synthesis is ____.",
            promptBn = "কোষের শক্তি উৎপাদন কেন্দ্র বা পাওয়ার হাউস বলা হয় ____-কে।",
            acceptableTextAnswers = listOf("mitochondria", "mitochondrion", "মাইটোকন্ড্রিয়া", "মাইটোকন্ড্রিয়া"),
            explanationEn = "Mitochondria produce ATP through cellular respiration.",
            explanationBn = "মাইটোকন্ড্রিয়া সবাত শ্বসনের মাধ্যমে ATP শক্তি তৈরি করে।"
        ),
        QuestionItem(
            id = "q_ict_match",
            classId = "class_10",
            subjectId = "ict",
            chapterId = "ch_ict_web",
            topicId = "topic_html_tags",
            type = QuestionType.MATCHING,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Match the HTML tag pair: Which tag creates a line break without a closing tag?",
            promptBn = "HTML ট্যাগ মিল করো: কোন ট্যাগটি ক্লোজিং ছাড়াই লাইন ব্রেক তৈরি করে?",
            optionsEn = listOf("<br> -> Line break", "<a> -> Table row", "<td> -> Hyperlink", "<title> -> Image"),
            optionsBn = listOf("<br> -> লাইন ব্রেক", "<a> -> টেবিল সারি", "<td> -> হাইপারলিংক", "<title> -> ছবি"),
            correctOptionIndices = listOf(0),
            explanationEn = "<br> is an empty tag for line breaks; <a> creates links and <tr>/<td> create table cells.",
            explanationBn = "<br> একটি এম্পটি ট্যাগ যা লাইন ব্রেক তৈরি করে।"
        ),
        QuestionItem(
            id = "q_eng_1",
            classId = "class_10",
            subjectId = "english",
            chapterId = "ch_eng_grammar",
            topicId = "topic_voice",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "Choose the passive form of: 'Rafi reads a storybook every night.'",
            promptBn = "'Rafi reads a storybook every night.' বাক্যটির সঠিক Passive form কোনটি?",
            optionsEn = listOf(
                "A storybook is read by Rafi every night.",
                "A storybook was read by Rafi every night.",
                "A storybook is reading by Rafi every night.",
                "A storybook has been read by Rafi every night."
            ),
            optionsBn = listOf(
                "A storybook is read by Rafi every night.",
                "A storybook was read by Rafi every night.",
                "A storybook is reading by Rafi every night.",
                "A storybook has been read by Rafi every night."
            ),
            correctOptionIndices = listOf(0),
            explanationEn = "Present simple active ('reads') converts to 'is/are + past participle (read)'.",
            explanationBn = "Present indefinite tense-এর passive গঠনে 'is/are + V3' ব্যবহৃত হয়।"
        ),
        QuestionItem(
            id = "q_ban_short",
            classId = "class_10",
            subjectId = "bangla",
            chapterId = "ch_ban_byakoron",
            topicId = "topic_somas",
            type = QuestionType.SHORT_ANSWER,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "What is the Bangla term for the compound word 'সিংহাসন' (সিংহ চিহ্নিত আসন)? Write the Samas name.",
            promptBn = "'সিংহাসন' (সিংহ চিহ্নিত আসন) কোন সমাসের উদাহরণ? সংক্ষেপে উত্তর লেখো।",
            acceptableTextAnswers = listOf("কর্মধারয়", "মধ্যপদলোপী কর্মধারয়", "karmadharaya"),
            explanationEn = "Since the middle word 'চিহ্নিত' is omitted, it is মধ্যপদলোপী কর্মধারয় সমাস.",
            explanationBn = "ব্যাসবাক্যের মধ্যপদ 'চিহ্নিত' লোপ পাওয়ায় এটি মধ্যপদলোপী কর্মধারয় সমাস।"
        ),
        QuestionItem(
            id = "q_bgs_1",
            classId = "class_10",
            subjectId = "social_studies",
            chapterId = "ch_bgs_lib",
            topicId = "topic_liberation",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "On which date did UNESCO declare 21 February as International Mother Language Day?",
            promptBn = "ইউনেস্কো কত সালে ২১শে ফেব্রুয়ারিকে আন্তর্জাতিক মাতৃভাষা দিবস হিসেবে স্বীকৃতি দেয়?",
            optionsEn = listOf("1971", "1996", "1999", "2001"),
            optionsBn = listOf("১৯৭১", "১৯৯৬", "১৯৯৯", "২০০১"),
            correctOptionIndices = listOf(2),
            explanationEn = "UNESCO adopted the resolution on 17 November 1999.",
            explanationBn = "১৯৯৯ সালের ১৭ই নভেম্বর ইউনেস্কো ২১শে ফেব্রুয়ারিকে আন্তর্জাতিক মাতৃভাষা দিবস ঘোষণা করে।"
        ),
        QuestionItem(
            id = "q_gk_1",
            classId = "class_10",
            subjectId = "gk",
            chapterId = "ch_gk_bd",
            topicId = "topic_rivers",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "What is the total length of the main Padma Multipurpose Bridge?",
            promptBn = "মূল পদ্মা বহুমুখী সেতুর দৈর্ঘ্য কত?",
            optionsEn = listOf("4.80 km", "6.15 km", "7.45 km", "9.20 km"),
            optionsBn = listOf("৪.৮০ কি.মি.", "৬.১৫ কি.মি.", "৭.৪৫ কি.মি.", "৯.২০ কি.মি."),
            correctOptionIndices = listOf(1),
            explanationEn = "The main Padma Bridge spans 6.15 kilometers across the Padma River.",
            explanationBn = "মূল পদ্মা সেতুর দৈর্ঘ্য ৬.১৫ কিলোমিটার।"
        ),
        QuestionItem(
            id = "q_cq_math_1",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_trig",
            topicId = "topic_trig_ratios",
            type = QuestionType.CREATIVE_CQ,
            difficulty = QuestionDifficulty.HARD,
            marks = 10,
            promptEn = "Creative Question (CQ) — Trigonometry & Angle of Elevation",
            promptBn = "সৃজনশীল প্রশ্ন (CQ) — ত্রিকোণমিতি ও উন্নতি কোণ",
            cqStemEn = "A vertical tree stands on flat ground. From a point 24 meters away from its foot, the angle of elevation to the top of the tree is 30°.",
            cqStemBn = "সমতল ভূমির ওপর একটি গাছ সোজাভাবে দাঁড়িয়ে আছে। গাছের পাদদেশ থেকে ২৪ মিটার দূরে ভূমির একটি বিন্দুতে গাছের শীর্ষের উন্নতি কোণ ৩০°।",
            cqSubQuestionsEn = listOf(
                "(a) Draw a labeled right-triangle diagram representing the stem. [2 marks]",
                "(b) Calculate the height of the tree. [4 marks]",
                "(c) If the tree later breaks at a height h and touches the ground at 45°, find h. [4 marks]"
            ),
            cqSubQuestionsBn = listOf(
                "(ক) উদ্দীপকের আলোকে চিহ্নিত সমকোণী ত্রিভুজটি অঙ্কন করো। [২ নম্বর]",
                "(খ) গাছটির উচ্চতা নির্ণয় করো। [৪ নম্বর]",
                "(গ) গাছটি যদি h উচ্চতায় ভেঙে ভূমির সাথে ৪৫° কোণ উৎপন্ন করে, তবে h-এর মান নির্ণয় করো। [৪ নম্বর]"
            ),
            cqRubricEn = "Model Rubric (Self-Assessment Only): (a) Right triangle with base = 24m, ∠ = 30° [2]. (b) tan 30° = h/24 => h = 24/√3 = 8√3 ≈ 13.86 m [4]. (c) Set sin 45° = h / (13.86 - h) and solve h ≈ 5.74 m [4].",
            cqRubricBn = "নমুনা উত্তর ও মূল্যায়ন নির্দেশিকা (স্ব-মূল্যায়ন): (ক) ২৪ মি. ভূমি ও ৩০° উন্নতি কোণসহ চিত্র [২]। (খ) tan ৩০° = উচ্চতা / ২৪ => উচ্চতা = ২৪/√৩ = ৮√৩ ≈ ১৩.৮৬ মি. [৪]। (গ) sin ৪৫° = h / (১৩.৮৬ - h) সমাধান করে h ≈ ৫.৭৪ মি. [৪]।",
            explanationEn = "CQ self-assessment scores are personal practice estimates and are never recorded as official board examiner marks.",
            explanationBn = "সৃজনশীল স্ব-মূল্যায়ন অনুশীলনের জন্য প্রদত্ত; এটি কোনো সরকারি পরীক্ষকের নম্বর নয়।"
        ),
        QuestionItem(
            id = "q_flash_1",
            classId = "class_10",
            subjectId = "physics",
            chapterId = "ch_phy_motion",
            topicId = "topic_kinematics",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "What is the SI unit of Acceleration and its dimensional formula?",
            promptBn = "ত্বরণের এসআই (SI) একক এবং মাত্রা সমীকরণ কী?",
            explanationEn = "Unit: m/s² (ms⁻²). Dimension: [LT⁻²].",
            explanationBn = "একক: m/s² (ms⁻²)। মাত্রা: [LT⁻²]।"
        ),
        QuestionItem(
            id = "q_c6_1",
            classId = "class_6",
            subjectId = "math",
            chapterId = "ch_math_c6_num",
            topicId = "topic_fractions",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "If the HCF of two numbers is 6 and their LCM is 36, what is their product?",
            promptBn = "দুটি সংখ্যার গ.সা.গু ৬ এবং ল.সা.গু ৩৬ হলে, সংখ্যা দুটির গুণফল কত?",
            optionsEn = listOf("42", "180", "216", "252"),
            optionsBn = listOf("৪২", "১৮০", "২১৬", "২৫২"),
            correctOptionIndices = listOf(2),
            explanationEn = "Product = HCF × LCM = 6 × 36 = 216.",
            explanationBn = "সংখ্যাদ্বয়ের গুণফল = গ.সা.গু × ল.সা.গু = ৬ × ৩৬ = ২১৬।"
        ),
        QuestionItem(
            id = "q_c7_heat_1",
            classId = "class_7",
            subjectId = "gen_science",
            chapterId = "ch_sci_c7_heat",
            topicId = "topic_heat",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Normal human body temperature is 98.4°F. What is it approximately in Celsius?",
            promptBn = "মানবদেহের স্বাভাবিক তাপমাত্রা ৯৮.৪° ফারেনহাইট হলে সেলসিয়াসে তা প্রায় কত?",
            optionsEn = listOf("35°C", "36.9°C", "38.5°C", "40°C"),
            optionsBn = listOf("৩৫° সে.", "৩৬.৯° সে.", "৩৮.৫° সে.", "৪০° সে."),
            correctOptionIndices = listOf(1),
            explanationEn = "C/5 = (F - 32)/9 => C = 5 × (98.4 - 32) / 9 ≈ 36.9°C.",
            explanationBn = "C/5 = (F - 32)/9 সূত্রে বসিয়ে পাওয়া যায় প্রায় ৩৬.৯° সেলসিয়াস।"
        ),
        QuestionItem(
            id = "q_c8_alg_1",
            classId = "class_8",
            subjectId = "math",
            chapterId = "ch_math_c8_alg",
            topicId = "topic_identities",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "If a + b = 7 and ab = 10, what is the value of a² + b²?",
            promptBn = "যদি a + b = 7 এবং ab = 10 হয়, তবে a² + b²-এর মান কত?",
            optionsEn = listOf("19", "29", "39", "49"),
            optionsBn = listOf("১৯", "২৯", "৩৯", "৪৯"),
            correctOptionIndices = listOf(1),
            explanationEn = "a² + b² = (a + b)² - 2ab = 49 - 20 = 29.",
            explanationBn = "a² + b² = (a + b)² - 2ab = ৪৯ - ২০ = ২৯।"
        ),
        QuestionItem(
            id = "q_flash_math_trig",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_trig",
            topicId = "topic_trig_ratios",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Pythagorean Trigonometric Identities (3 core equations)",
            promptBn = "ত্রিকোণমিতির তিনটি মৌলিক অভেদাবলি কী কী?",
            explanationEn = "1) sin²θ + cos²θ = 1\n2) sec²θ - tan²θ = 1\n3) cosec²θ - cot²θ = 1",
            explanationBn = "১) sin²θ + cos²θ = 1\n২) sec²θ - tan²θ = 1\n৩) cosec²θ - cot²θ = 1"
        ),
        QuestionItem(
            id = "q_flash_math_sets",
            classId = "class_10",
            subjectId = "math",
            chapterId = "ch_math_sets",
            topicId = "topic_sets_ops",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "De Morgan's Laws for Sets A and B",
            promptBn = "সেট A ও B-এর জন্য ডি মর্গানের সূত্র দুটি কী?",
            explanationEn = "(A ∪ B)' = A' ∩ B'  and  (A ∩ B)' = A' ∪ B'",
            explanationBn = "(A ∪ B)' = A' ∩ B'  এবং  (A ∩ B)' = A' ∪ B'"
        ),
        QuestionItem(
            id = "q_flash_math_alg",
            classId = "class_8",
            subjectId = "math",
            chapterId = "ch_math_c8_alg",
            topicId = "topic_identities",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Algebraic Cube Formulae: (a + b)³ and a³ - b³",
            promptBn = "বীজগণিতীয় ঘনের সূত্র: (a + b)³ এবং a³ - b³",
            explanationEn = "(a + b)³ = a³ + 3a²b + 3ab² + b³\na³ - b³ = (a - b)(a² + ab + b²)",
            explanationBn = "(a + b)³ = a³ + 3a²b + 3ab² + b³\na³ - b³ = (a - b)(a² + ab + b²)"
        ),
        QuestionItem(
            id = "q_flash_phy_motion",
            classId = "class_10",
            subjectId = "physics",
            chapterId = "ch_phy_motion",
            topicId = "topic_kinematics",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "Four Equations of Uniform Acceleration",
            promptBn = "সুষম ত্বরণে গতির চারটি সমীকরণ কী কী?",
            explanationEn = "1) v = u + at\n2) s = ((u + v)/2) · t\n3) s = ut + ½at²\n4) v² = u² + 2as",
            explanationBn = "১) v = u + at\n২) s = ((u + v)/2) · t\n৩) s = ut + ½at²\n৪) v² = u² + 2as"
        ),
        QuestionItem(
            id = "q_flash_chem_iso",
            classId = "class_10",
            subjectId = "chemistry",
            chapterId = "ch_chem_atom",
            topicId = "topic_isotopes",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "What are the 3 isotopes of Hydrogen and their neutron counts?",
            promptBn = "হাইড্রোজেনের ৩টি আইসোটোপ ও তাদের নিউট্রন সংখ্যা কত?",
            explanationEn = "Protium (¹H): 0 neutrons\nDeuterium (²H): 1 neutron\nTritium (³H): 2 neutrons",
            explanationBn = "প্রোটিয়াম (¹H): ০টি নিউট্রন\nডিউটেরিয়াম (²H): ১টি নিউট্রন\nট্রিটিয়াম (³H): ২টি নিউট্রন"
        ),
        QuestionItem(
            id = "q_flash_chem_shell",
            classId = "class_10",
            subjectId = "chemistry",
            chapterId = "ch_chem_atom",
            topicId = "topic_isotopes",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Maximum Electron Capacity in Shell n (Bohr-Bury Rule)",
            promptBn = "বোর-বারি নীতি অনুযায়ী n-তম শক্তিস্তরে সর্বোচ্চ ইলেকট্রন সংখ্যা কত?",
            explanationEn = "Max electrons = 2n² (K=2, L=8, M=18, N=32).",
            explanationBn = "সর্বোচ্চ ইলেকট্রন = 2n² (K=২, L=৮, M=১৮, N=৩২)।"
        ),
        QuestionItem(
            id = "q_flash_bio_cell",
            classId = "class_10",
            subjectId = "biology",
            chapterId = "ch_bio_cell",
            topicId = "topic_organelles",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "3 Key Differences Between Plant and Animal Cells",
            promptBn = "উদ্ভিদ ও প্রাণী কোষের ৩টি প্রধান পার্থক্য কী?",
            explanationEn = "Plant cells have cellulose cell walls, plastids (chloroplasts), and large central vacuoles; animal cells generally have centrosomes and lack cell walls.",
            explanationBn = "উদ্ভিদ কোষে সেলুলোজ কোষপ্রাচীর, প্লাস্টিড ও বড় কোষগহ্বর থাকে; প্রাণী কোষে কোষপ্রাচীর ও প্লাস্টিড থাকে না কিন্তু সেন্ট্রোসোম থাকে।"
        ),
        QuestionItem(
            id = "q_flash_eng_voice",
            classId = "class_10",
            subjectId = "english",
            chapterId = "ch_eng_grammar",
            topicId = "topic_voice",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Passive Voice Rule for Present Continuous Tense",
            promptBn = "Present Continuous Tense-কে Passive Voice করার নিয়ম কী?",
            explanationEn = "Object becomes Subject + am/is/are + being + V3 (past participle) + by + Agent.",
            explanationBn = "Object-কে Subject + am/is/are + being + V3 + by + Subject-এর Object রূপ।"
        ),
        QuestionItem(
            id = "q_flash_ban_somas",
            classId = "class_10",
            subjectId = "bangla",
            chapterId = "ch_ban_byakoron",
            topicId = "topic_somas",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.MEDIUM,
            promptEn = "বাংলা সমাস প্রধানত কয় প্রকার ও কী কী?",
            promptBn = "বাংলা সমাস প্রধানত কয় প্রকার ও কী কী?",
            explanationEn = "প্রধানত ৬ প্রকার: দ্বন্দ্ব, কর্মধারয়, তৎপুরুষ, বহুব্রীহি, দ্বিগু ও অব্যয়ীভাব।",
            explanationBn = "প্রধানত ৬ প্রকার: দ্বন্দ্ব, কর্মধারয়, তৎপুরুষ, বহুব্রীহি, দ্বিগু ও অব্যয়ীভাব।"
        ),
        QuestionItem(
            id = "q_flash_ict_html",
            classId = "class_10",
            subjectId = "ict",
            chapterId = "ch_ict_web",
            topicId = "topic_html_tags",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Ordered List vs Unordered List Tags in HTML",
            promptBn = "HTML-এ Ordered ও Unordered List তৈরি করতে কোন ট্যাগ লাগে?",
            explanationEn = "<ol>...</ol> creates numbered lists; <ul>...</ul> creates bulleted lists; <li> defines each list item.",
            explanationBn = "<ol> সংখ্যাযুক্ত তালিকা ও <ul> বুলেট তালিকা তৈরি করে; প্রতিটি আইটেমের জন্য <li> ব্যবহৃত হয়।"
        ),
        QuestionItem(
            id = "q_flash_sci_heat",
            classId = "class_7",
            subjectId = "gen_science",
            chapterId = "ch_sci_c7_heat",
            topicId = "topic_heat",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Celsius, Fahrenheit & Kelvin Conversion Formula",
            promptBn = "সেলসিয়াস, ফারেনহাইট ও কেলভিন স্কেলের সম্পর্কের সূত্র কী?",
            explanationEn = "C / 5 = (F - 32) / 9 = (K - 273) / 5",
            explanationBn = "C / 5 = (F - 32) / 9 = (K - 273) / 5"
        ),
        QuestionItem(
            id = "q_flash_bgs_lib",
            classId = "class_10",
            subjectId = "social_studies",
            chapterId = "ch_bgs_lib",
            topicId = "topic_liberation",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "Key Milestones of Bangladesh's Liberation: 1966, 1969, 17 Apr 1971",
            promptBn = "মুক্তিযুদ্ধের গুরুত্বপূর্ণ তারিখ: ১৯৬৬, ১৯৬৯, ১৭ এপ্রিল ১৯৭১",
            explanationEn = "1966: Six-Point Demand • 1969: Mass Uprising • 17 Apr 1971: Mujibnagar Government Oath.",
            explanationBn = "১৯৬৬: ছয় দফা • ১৯৬৯: গণ-অভ্যুত্থান • ১৭ এপ্রিল ১৯৭১: মুজিবনগর সরকারের শপথ গ্রহণ।"
        ),
        QuestionItem(
            id = "q_flash_gk_bd",
            classId = "class_10",
            subjectId = "gk",
            chapterId = "ch_gk_bd",
            topicId = "topic_rivers",
            type = QuestionType.FLASHCARD,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "How many administrative divisions and districts are in Bangladesh?",
            promptBn = "বাংলাদেশে কয়টি প্রশাসনিক বিভাগ ও কয়টি জেলা রয়েছে?",
            explanationEn = "8 Administrative Divisions and 64 Districts.",
            explanationBn = "৮টি প্রশাসনিক বিভাগ এবং ৬৪টি জেলা।"
        )
    )

    val examTemplates: List<ExamTemplate> = listOf(
        ExamTemplate(
            id = "exam_ssc_math_sprint",
            titleEn = "SSC Mathematics Chapter Sprint",
            titleBn = "এসএসসি গণিত অধ্যায়ভিত্তিক পরীক্ষা",
            classId = "class_10",
            subjectIds = listOf("math"),
            durationSeconds = 300,
            totalMarks = 5,
            negativeMarkingPerWrong = 0.25,
            descriptionEn = "5-minute timed MCQ & numerical check with 0.25 negative marking per wrong MCQ.",
            descriptionBn = "৫ মিনিটের সময়নির্ভর পরীক্ষা; প্রতিটি ভুল উত্তরে ০.২৫ নেগেটিভ মার্কিং রয়েছে।"
        ),
        ExamTemplate(
            id = "exam_ssc_science_combo",
            titleEn = "SSC Science Multi-Subject Mock",
            titleBn = "এসএসসি বিজ্ঞান সম্মিলিত মডেল টেস্ট",
            classId = "class_10",
            subjectIds = listOf("math", "physics", "chemistry", "biology", "ict"),
            durationSeconds = 600,
            totalMarks = 10,
            negativeMarkingPerWrong = 0.0,
            descriptionEn = "Cross-subject SSC Science readiness mock with deterministic scoring and weak-topic breakdown.",
            descriptionBn = "গণিত, পদার্থ, রসায়ন, জীববিজ্ঞান ও আইসিটি সমন্বিত পূর্ণাঙ্গ প্রস্তুতি মূল্যায়ন।"
        ),
        ExamTemplate(
            id = "exam_bd_humanities_gk",
            titleEn = "Bangla, English & BGS Mock Test",
            titleBn = "বাংলা, ইংরেজি ও বাংলাদেশ বিশ্বপরিচয় টেস্ট",
            classId = "class_10",
            subjectIds = listOf("bangla", "english", "social_studies", "gk"),
            durationSeconds = 420,
            totalMarks = 6,
            negativeMarkingPerWrong = 0.0,
            descriptionEn = "Focused language and heritage assessment for all SSC groups.",
            descriptionBn = "সকল বিভাগের শিক্ষার্থীদের জন্য ভাষা ও বাংলাদেশ পরিচিতি মূল্যায়ন।"
        )
    )

    val questsCatalog: List<QuestCatalogItem> = listOf(
        QuestCatalogItem("q_daily_lesson", "daily", "Complete 1 Lesson", "১টি পাঠ সম্পন্ন করো", "Finish any curriculum lesson today.", "আজ যেকোনো একটি পাঠ পড়ে শেষ করো।", 1, 30, 15),
        QuestCatalogItem("q_daily_practice", "daily", "Quick Practice Sprint", "দ্রুত অনুশীলন সেশন", "Complete 1 Quick Practice session.", "১টি কুইক প্র্যাকটিস সেশন সম্পন্ন করো।", 1, 30, 20),
        QuestCatalogItem("q_weekly_exam", "weekly", "Mock Exam Challenger", "মডেল টেস্ট চ্যালেঞ্জার", "Finish 1 timed Mock Exam this week.", "এই সপ্তাহে ১টি সময়নির্ভর মডেল টেস্ট দাও।", 1, 100, 50),
        QuestCatalogItem("q_weekly_social", "weekly", "Scholarly Teamwork", "বন্ধু ও দলগত অধ্যয়ন", "Play 1 multiplayer battle or message a study group.", "১টি মাল্টিপ্লেয়ার ম্যাচ খেলো অথবা স্টাডি গ্রুপে যুক্ত হও।", 1, 100, 45)
    )

    val achievementsCatalog: List<AchievementCatalogItem> = listOf(
        AchievementCatalogItem("ach_first_lesson", "Learning", "First Lesson", "প্রথম পাঠ", "Completed your very first StudyQuest lesson.", "স্টাডিকোয়েস্টে প্রথম পাঠ সম্পন্ন করেছ।", 15, 20),
        AchievementCatalogItem("ach_subject_explorer", "Learning", "Subject Explorer", "বিষয় অভিযাত্রী", "Practiced across 3 or more subjects.", "৩টি বা ততোধিক বিষয়ে অনুশীলন করেছ।", 25, 30),
        AchievementCatalogItem("ach_first_mock", "Academic", "First Mock Exam", "প্রথম মডেল টেস্ট", "Completed a full timed Mock Exam.", "সময়নির্ভর প্রথম মডেল টেস্ট সম্পন্ন করেছ।", 30, 35),
        AchievementCatalogItem("ach_first_friend", "Social", "First Study Buddy", "প্রথম সহপাঠী বন্ধু", "Connected with a verified friend on StudyQuest.", "স্টাডিকোয়েস্টে প্রথম বন্ধু যুক্ত করেছ।", 20, 25),
        AchievementCatalogItem("ach_first_group", "Social", "Study Group Builder", "স্টাডি গ্রুপ নির্মাতা", "Joined or created a private Study Group.", "একটি প্রাইভেট স্টাডি গ্রুপে যুক্ত হয়েছ।", 25, 30),
        AchievementCatalogItem("ach_first_battle", "Competition", "Arena Debut", "প্রতিযোগিতায় অভিষেক", "Completed your first live multiplayer battle.", "প্রথম লাইভ মাল্টিপ্লেয়ার ম্যাচ সম্পন্ন করেছ।", 30, 40)
    )

    val cosmeticsCatalog: List<AvatarCosmetic> = listOf(
        AvatarCosmetic("owl_classic", "Scholar Titu", "বিদ্বান টিটু", "avatar", 0, "Default StudyQuest starter avatar", "ডিফল্ট স্টাডিকোয়েস্ট অ্যাভাটার"),
        AvatarCosmetic("tiger_bengal", "Royal Bengal Cub", "রয়েল বেঙ্গল শাবক", "avatar", 60, "Unlock with 60 Coins", "৬০ কয়েনে আনলক করো"),
        AvatarCosmetic("kingfisher_bd", "River Kingfisher", "মাছরাঙা অভিযাত্রী", "avatar", 90, "Unlock with 90 Coins", "৯০ কয়েনে আনলক করো"),
        AvatarCosmetic("frame_emerald", "Forest Laurel Frame", "সবুজ বিজয় ফ্রেম", "frame", 50, "Unlock with 50 Coins", "৫০ কয়েনে আনলক করো"),
        AvatarCosmetic("frame_mango", "Golden Mango Crest", "সোনালী আমের মুকুট ফ্রেম", "frame", 120, "Unlock with 120 Coins", "১২০ কয়েনে আনলক করো"),
        AvatarCosmetic("title_gonit_bir", "Math Trailblazer", "গণিত পথিকৃৎ", "title", 80, "Cosmetic profile title", "প্রোফাইল উপাধি")
    )
}
