package com.example.data

object NumberTranslator {

    // Digits mapping
    private val BANGLA_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val HINDI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun toLocalizedDigits(number: Int, language: String): String {
        val numStr = number.toString()
        val digits = when (language.lowercase()) {
            "bangla" -> BANGLA_DIGITS
            "hindi" -> HINDI_DIGITS
            "arabic" -> ARABIC_DIGITS
            else -> return numStr
        }
        val sb = StringBuilder()
        for (char in numStr) {
            if (char in '0'..'9') {
                sb.append(digits[char - '0'])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    // Word mapping helpers
    private val ENGLISH_UNITS = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    private val ENGLISH_TENS = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    private val BANGLA_UNITS = arrayOf(
        "শূণ্য", "এক", "দুই", "তিন", "চার", "পাচ", "ছয়", "সাত", "আট", "নয়", "দশ",
        "এগারো", "বারো", "তেরো", "চোদ্দ", "পনেরো", "ষোল", "সতেরো", "আঠারো", "উনিশ", "বিশ",
        "একুশ", "বাইশ", "তেইশ", "চব্বিশ", "পঁচিশ", "ছাব্বিশ", "সাতাশ", "আটাশ", "ঊনত্রিশ", "ত্রিশ",
        "একত্রিশ", "বত্রিশ", "তেত্রিশ", "চৌত্রিশ", "পঁয়ত্রিশ", "ছত্রিশ", "সাঁইত্রিশ", "আটত্রিশ", "ঊনচল্লিশ", "চল্লিশ",
        "একচল্লিশ", "বিয়াল্লিশ", "তেতাল্লিশ", "চৌয়াল্লিশ", "পঁয়তাল্লিশ", "ছেচল্লিশ", "সাতচল্লিশ", "আটচল্লিশ", "ঊনপঞ্চাশ", "পঞ্চাশ",
        "একান্ন", "বায়ান্ন", "তিপ্পান্ন", "চুয়ান্ন", "পঞ্চান্ন", "ছাপ্পান্ন", "সাতান্ন", "আটান্ন", "ঊনষাট", "ষাট",
        "একষট্টি", "বাষট্টি", "তেষট্টি", "চৌষট্টি", "পঁয়ষট্টি", "ছেষট্টি", "সাতষট্টি", "আটষট্টি", "ঊনসত্তর", "সত্তর",
        "একাত্তর", "বাহাত্তর", "তেহাত্তর", "চুয়াত্তর", "পঁচাত্তর", "ছেয়াত্তর", "সাতাত্তর", "আটাত্তর", "ঊনআশি", "আশি",
        "একাশি", "বিয়াশি", "তিরাশি", "চৌরাশি", "পঁচাশী", "ছিয়াশি", "সাতাশি", "অষ্টআশি", "ঊননব্বই", "নব্বই",
        "একানব্বই", "বিয়ানব্বই", "তিরানব্বই", "চৌরানব্বই", "পঁচানব্বই", "ছিয়ানব্বই", "সাতানব্বই", "আটানব্বই", "নিরানব্বই", "একশত"
    )

    private val HINDI_UNITS = arrayOf(
        "शून्य", "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ", "दस",
        "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस", "बीस",
        "इक्कीस", "बाईस", "तेईस", "चौबीस", "पच्चीस", "छब्बीस", "सत्ताईस", "अठारह", "उनतीस", "तीस",
        "इकतीस", "बत्तीस", "तैंतीस", "चौंतीस", "पैंतीस", "छत्तीस", "सैंतीस", "अड़तीस", "उनतालीस", "चालीस",
        "एकतालीस", "बयालीस", "तैंतालीस", "चवालीस", "पैंतालीस", "छियालीस", "सैंतालीस", "अड़तालीस", "उनचास", "पचास",
        "इक्वन", "बावन", "तिरेपन", "चौवन", "पचपन", "छप्पन", "सतावन", "अठावन", "उनसठ", "साठ",
        "इकसठ", "बासत", "तिरसठ", "चौंसठ", "पैंसठ", "छियासठ", "सरसठ", "अड़सठ", "उनहत्तर", "सत्तर",
        "इहत्तर", "बहत्तर", "तिहत्तर", "चौहत्तर", "पचहत्तर", "छहत्तर", "सतहत्तर", "अठहत्तर", "उनासी", "अस्सी",
        "इक्यासी", "बयासी", "तिरासी", "चौरासी", "पचासी", "छियासी", "सतासी", "अठासी", "नवासी", "नब्बे",
        "इक्यानवे", "बानवे", "तिरानवे", "चौरानवे", "पचानवे", "छियानवे", "सत्तानवे", "अट्ठानवे", "निन्यानवे", "सौ"
    )

    private val ARABIC_UNITS = arrayOf(
        "صفر", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة", "عشرة",
        "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    )
    private val ARABIC_TENS = arrayOf("", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون")
    private val ARABIC_HUNDREDS = arrayOf("", "مائة", "مائتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة")

    fun toWords(number: Int, language: String): String {
        if (number == 0) {
            return when (language.lowercase()) {
                "bangla" -> "শূণ্য"
                "hindi" -> "शून्य"
                "arabic" -> "صفر"
                else -> "Zero"
            }
        }

        return when (language.lowercase()) {
            "bangla" -> translateBangla(number)
            "hindi" -> translateHindi(number)
            "arabic" -> translateArabic(number)
            else -> translateEnglish(number)
        }
    }

    private fun translateEnglish(number: Int): String {
        if (number < 20) return ENGLISH_UNITS[number]
        if (number < 100) {
            val rem = number % 10
            return ENGLISH_TENS[number / 10] + (if (rem > 0) " $rem" else "")
        }
        if (number < 1000) {
            val rem = number % 100
            return ENGLISH_UNITS[number / 100] + " Hundred" + (if (rem > 0) " " + translateEnglish(rem) else "")
        }
        if (number < 1000000) {
            val rem = number % 1000
            return translateEnglish(number / 1000) + " Thousand" + (if (rem > 0) " " + translateEnglish(rem) else "")
        }
        return number.toString()
    }

    private fun translateBangla(number: Int): String {
        if (number <= 100) return BANGLA_UNITS[number]
        if (number < 1000) {
            val hundreds = number / 100
            val rem = number % 100
            val hundredWord = when (hundreds) {
                1 -> "একশো"
                2 -> "দুশো"
                else -> BANGLA_UNITS[hundreds] + "শত"
            }
            return if (rem > 0) "$hundredWord " + BANGLA_UNITS[rem] else hundredWord
        }
        if (number < 100000) {
            val thousands = number / 1000
            val rem = number % 1000
            val thousandWord = BANGLA_UNITS[thousands] + " হাজার"
            return if (rem > 0) "$thousandWord " + translateBangla(rem) else thousandWord
        }
        return number.toString()
    }

    private fun translateHindi(number: Int): String {
        if (number <= 100) return HINDI_UNITS[number]
        if (number < 1000) {
            val hundreds = number / 100
            val rem = number % 100
            val hundredWord = HINDI_UNITS[hundreds] + " सौ"
            return if (rem > 0) "$hundredWord " + HINDI_UNITS[rem] else hundredWord
        }
        if (number < 100000) {
            val thousands = number / 1000
            val rem = number % 1000
            val thousandWord = HINDI_UNITS[thousands] + " हज़ार"
            return if (rem > 0) "$thousandWord " + translateHindi(rem) else thousandWord
        }
        return number.toString()
    }

    private fun translateArabic(number: Int): String {
        if (number < 20) return ARABIC_UNITS[number]
        if (number < 100) {
            val ones = number % 10
            val tens = number / 10
            if (ones == 0) return ARABIC_TENS[tens]
            return ARABIC_UNITS[ones] + " و " + ARABIC_TENS[tens]
        }
        if (number < 1000) {
            val hundreds = number / 100
            val rem = number % 100
            val hundredWord = ARABIC_HUNDREDS[hundreds]
            if (rem == 0) return hundredWord
            return hundredWord + " و " + translateArabic(rem)
        }
        if (number < 1000000) {
            val thousands = number / 1000
            val rem = number % 1000
            val thousandWord = when (thousands) {
                1 -> "ألف"
                2 -> "ألفين"
                in 3..10 -> translateArabic(thousands) + " آلاف"
                else -> translateArabic(thousands) + " ألف"
            }
            if (rem == 0) return thousandWord
            return thousandWord + " و " + translateArabic(rem)
        }
        return number.toString()
    }

    fun getPronunciation(number: Int, language: String): String {
        return when (language.lowercase()) {
            "bangla" -> {
                when (number) {
                    0 -> "Shunno"
                    1 -> "Ek"
                    2 -> "Dui"
                    3 -> "Tin"
                    4 -> "Char"
                    5 -> "Pach"
                    6 -> "Chhoy"
                    7 -> "Shat"
                    8 -> "Aat"
                    9 -> "Noy"
                    10 -> "Dosh"
                    11 -> "Egaro"
                    12 -> "Baro"
                    13 -> "Tero"
                    14 -> "Choddo"
                    15 -> "Ponero"
                    20 -> "Bish"
                    50 -> "Ponchash"
                    100 -> "Ek-shoto"
                    else -> toWords(number, "bangla")
                }
            }
            "hindi" -> {
                when (number) {
                    0 -> "Shunya"
                    1 -> "Ek"
                    2 -> "Do"
                    3 -> "Teen"
                    4 -> "Chaar"
                    5 -> "Paanch"
                    6 -> "Chhah"
                    7 -> "Saat"
                    8 -> "Aath"
                    9 -> "Nau"
                    10 -> "Das"
                    11 -> "Gyaarah"
                    12 -> "Baarah"
                    13 -> "Teraah"
                    14 -> "Chaudah"
                    15 -> "Pandrah"
                    20 -> "Bees"
                    50 -> "Pachaas"
                    100 -> "Ek-sau"
                    else -> toWords(number, "hindi")
                }
            }
            "arabic" -> {
                when (number) {
                    0 -> "Sifr"
                    1 -> "Wahid"
                    2 -> "Ithnan"
                    3 -> "Thalathah"
                    4 -> "Arba'ah"
                    5 -> "Khamsah"
                    6 -> "Sittah"
                    7 -> "Sab'ah"
                    8 -> "Thamaniyah"
                    9 -> "Tis'ah"
                    10 -> "Asharah"
                    11 -> "Ahada Ashar"
                    12 -> "Ithna Ashar"
                    20 -> "Ishrun"
                    50 -> "Khamsun"
                    100 -> "Mi'ah"
                    else -> toWords(number, "arabic")
                }
            }
            else -> toWords(number, "english")
        }
    }

    fun getRomanNumeral(number: Int): String {
        if (number <= 0 || number > 3999) return "N/A"
        val romanSymbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val sb = StringBuilder()
        var remaining = number
        for (i in values.indices) {
            while (remaining >= values[i]) {
                remaining -= values[i]
                sb.append(romanSymbols[i])
            }
        }
        return sb.toString()
    }

    fun getOrdinal(number: Int, language: String): String {
        return when (language.lowercase()) {
            "bangla" -> {
                when (number) {
                    1 -> "১ম (প্রথম)"
                    2 -> "২য় (দ্বিতীয়)"
                    3 -> "৩য় (তৃতীয়)"
                    4 -> "৪র্থ (চতুর্থ)"
                    5 -> "৫ম (পঞ্চম)"
                    6 -> "৬ষ্ঠ (ষষ্ঠ)"
                    7 -> "৭ম (সপ্তম)"
                    8 -> "৮ম (অষ্টম)"
                    9 -> "৯ম (নবম)"
                    10 -> "১০ম (দশম)"
                    else -> "${toLocalizedDigits(number, "bangla")}তম"
                }
            }
            "hindi" -> {
                when (number) {
                    1 -> "पहला (1st)"
                    2 -> "दूसरा (2nd)"
                    3 -> "तीसरा (3rd)"
                    4 -> "चौथा (4th)"
                    5 -> "पाँचवाँ (5th)"
                    else -> "${number}वाँ"
                }
            }
            "arabic" -> {
                when (number) {
                    1 -> "الأول (1st)"
                    2 -> "الثاني (2nd)"
                    3 -> "الثالث (3rd)"
                    4 -> "الرابع (4th)"
                    5 -> "الخامس (5th)"
                    else -> "المركز $number"
                }
            }
            else -> {
                val j = number % 10
                val k = number % 100
                if (j == 1 && k != 11) {
                    return number.toString() + "st"
                }
                if (j == 2 && k != 12) {
                    return number.toString() + "nd"
                }
                if (j == 3 && k != 13) {
                    return number.toString() + "rd"
                }
                return number.toString() + "th"
            }
        }
    }

    fun getLargeNumberTerm(number: Long, language: String): String {
        return when (language.lowercase()) {
            "bangla" -> {
                if (number == 100000L) "১ লাখ (Lakh)"
                else if (number == 10000000L) "১ কোটি (Crore)"
                else if (number == 1000000L) "১০ লাখ (১ মিলিয়ন)"
                else if (number == 1000000000L) "১ বিলিয়ন (১০০ কোটি)"
                else number.toString()
            }
            "hindi" -> {
                if (number == 100000L) "१ लाख (Lakh)"
                else if (number == 10000000L) "१ करोड़ (Crore)"
                else if (number == 1000000L) "१० लाख (१ मिलियन)"
                else if (number == 1000000000L) "१ अरब (१ बिलियन)"
                else number.toString()
            }
            "arabic" -> {
                if (number == 1000000L) "مليون (Million)"
                else if (number == 1000000000L) "مليار (Billion)"
                else number.toString()
            }
            else -> {
                if (number == 100000L) "One Hundred Thousand"
                if (number == 1000000L) "One Million"
                if (number == 1000000000L) "One Billion"
                else number.toString()
            }
        }
    }
}
