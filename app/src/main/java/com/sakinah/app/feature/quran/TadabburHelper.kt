package com.sakinah.app.feature.quran

import com.sakinah.app.feature.quran.model.AyahItem

data class TadabburContent(
    val reflections: List<String>,      // الوقفات التدبرية
    val actionPoints: List<String>,     // العمل بالآيات
    val wordMeanings: List<Pair<String, String>>, // معاني الكلمات
    val tafsirMuyassar: String,         // التفسير الميسر
    val tafsirJalalayn: String          // تفسير الجلالين
)

object TadabburHelper {

    fun getTadabburForAyah(ayah: AyahItem): TadabburContent {
        val muyassar = ayah.tafsir ?: "التفسير الميسر غير متاح حالياً."
        val jalalayn = ayah.jalalaynTafsir ?: "تفسير الجلالين: راجع معاني الكلمات والتفسير الميسر."

        val reflections = mutableListOf<String>()
        val actions = mutableListOf<String>()
        val words = mutableListOf<Pair<String, String>>()

        val text = ayah.text

        // استخراج معاني الكلمات من تفسير الجلالين والتفسير الميسر بذكاء
        if (jalalayn.isNotEmpty() && !jalalayn.startsWith("تفسير الجلالين:")) {
            // تفسير الجلالين يفسر الكلمات مباشرة بين قوسين أو شرحاً مختصراً
            val parts = jalalayn.split("؛", "،", ".")
            for (p in parts.take(3)) {
                val clean = p.trim()
                if (clean.length in 8..80) {
                    words.add("بيان المعنى" to clean)
                }
            }
        }

        // وقفات تدبرية أصيلة مستنبطة من سياق الآية
        when {
            text.contains("ٱلرَّحْمَٰنِ") || text.contains("ٱلرَّحِيمِ") || text.contains("رَّحْمَةَ") -> {
                reflections.add("رحمة الله وسعت كل شيء؛ فاستشعر سعة رحمته مهما عظمت ذنوبك أو ضاقت بك السبل.")
                reflections.add("تقديم صفة الرحمة في الخطاب القرآني يملأ قلب المؤمن أملاً وسكينة وثقة بمولاه.")
                actions.add("ارحم من في الأرض يرحمك من في السماء؛ ابدأ بإحسان ولين كلام مع أهلك اليوم.")
                actions.add("أكثر من قول: (يا حي يا قيوم برحمتك أستغيث).")
                if (words.isEmpty()) {
                    words.add("الرَّحْمَٰنُ" to "ذو الرحمة الشاملة لجميع الخلائق في الدنيا.")
                    words.add("الرَّحِيمُ" to "ذو الرحمة الخاصة بالمؤمنين هداية وتوفيقاً.")
                }
            }
            text.contains("ٱلصَّلَوٰةِ") || text.contains("ٱرْكَعُواْ") || text.contains("ٱسْجُدُواْ") -> {
                reflections.add("الصلاة هي معراج الروح وأعظم صلة بين العبد وربه، وفي السجود أقرب ما يكون العبد من الله.")
                reflections.add("من حافظ على صلاته في وقتها بخشوع كفاه الله ما أهمه في دنياه وآخرته.")
                actions.add("أحسن الوضوء وتفرغ للصلاة القادمة قبل أذانها بدقائق واستحضر الوقوف بين يدي الله.")
                actions.add("أطل سجودك في نافلة اليوم واسكب شكواك ودعاءك لمولاك.")
                if (words.isEmpty()) {
                    words.add("الصَّلَاة" to "الصلة والعبادة المفتتحة بالتكبير والمختتمة بالتسليم.")
                    words.add("الْخُشُوع" to "سكون الجوارح وحضور القلب وخضوعه لله تعالى.")
                }
            }
            text.contains("ٱلصَّبْرِ") || text.contains("ٱصْبِرُواْ") -> {
                reflections.add("الصبر ضياء يضيء عتمة الابتلاء، والجزاء بغير حساب: ﴿إِنَّمَا يُوَفَّى الصَّابِرُونَ أَجْرَهُم بِغَيْرِ حِسَابٍ﴾.")
                reflections.add("الصبر ليس مجرد احتمال، بل هو حبس النفس عن الجزع والرضا بقضاء الله الحكيم.")
                actions.add("احتسب أي مشقة أو تعب أو أذى يمر بك اليوم واقلبه ذكراً واسترجاعاً: (إنا لله وإنا إليه راجعون).")
                actions.add("اصبر عن معصية تهفو إليها نفسك ابتغاء مرضاة الله ورجاء جنته.")
                if (words.isEmpty()) {
                    words.add("الصَّبْر" to "حبس النفس عن الجزع واللسان عن التشكي والجوارح عن المعصية.")
                    words.add("الْمُصَابَرَة" to "مغالبة المشاق وملازمة الصبر والثبات عليه.")
                }
            }
            text.contains("تَوَكَّلْ") || text.contains("ٱلْمُتَوَكِّلِينَ") -> {
                reflections.add("التوكل جمع بين السعي بالأسباب المشروعة وتفويض النتائج إلى مسبب الأسباب وحده.")
                reflections.add("﴿وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ﴾ أي كافيه كل ما يهمه ويريبه.")
                actions.add("إذا أهمك أمر، اعقلها وتوكل، وردد بيقين: (حسبي الله ونعم الوكيل).")
                actions.add("سلّم أمر رزقك ومستقبلك لله واقطع التعلق بالمخلوقين.")
                if (words.isEmpty()) {
                    words.add("التَّوَكُّل" to "صدق اعتماد القلب على الله في جلب المنافع ودفع المضار مع الأخذ بالأسباب.")
                    words.add("حَسْبُهُ" to "كافيه وناصره وواقيه من كل مكروه.")
                }
            }
            text.contains("ٱسْتَغْفِرُواْ") || text.contains("غَفُورٌ") || text.contains("يَغْفِرْ") -> {
                reflections.add("الاستغفار يفتح الأقفال ويشرح البال ويكثر الأموال والعيال وينزل الغيث المدرار.")
                reflections.add("باب التوبة مفتوح لا يغلق حتى تطلع الشمس من مغربها، فبادر بالرجوع إلى مولاك.")
                actions.add("اجعل لسانك رطباً بالاستغفار اليوم مائة مرة: (أستغفر الله وأتوب إليه).")
                actions.add("تب من ذنب خفي بينك وبين الله واستبدله بصدقة أو صلاة ركعتين.")
                if (words.isEmpty()) {
                    words.add("الِاسْتِغْفَار" to "طلب المغفرة وستر الذنب والتجاوز عنه من الله تعالى.")
                }
            }
            else -> {
                // استنباط لطيفة مستندة للتفسير الميسر
                val cleanMuyassar = muyassar.take(120).trim()
                reflections.add("تأمل قوله تعالى في سورة ${ayah.surahName}: يُراد بهذه الآية تذكير العبد بأن: $cleanMuyassar.")
                reflections.add("القرآن الكريم رسائل ربانية حية؛ فاجعل هذه الآية منهجاً يومياً يقود خطاك في معاملاتك.")
                actions.add("ردد هذه الآية الكريمة بخشوع وتدبر ثلاث مرات اليوم واستحضر عظمتها.")
                actions.add("اعمل بما دلت عليه الآية في يومك؛ فإن علم القرآن يثبت بالعمل به.")
                if (words.isEmpty()) {
                    words.add("مفردات الآية" to cleanMuyassar)
                }
            }
        }

        return TadabburContent(
            reflections = reflections,
            actionPoints = actions,
            wordMeanings = words,
            tafsirMuyassar = muyassar,
            tafsirJalalayn = jalalayn
        )
    }
}
