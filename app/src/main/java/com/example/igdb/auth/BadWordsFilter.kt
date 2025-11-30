package com.example.igdb.auth

class BadWordsFilter {
    private val badWords = setOf(
        "fuck", "fucking", "fucker", "fuckoff", "motherfucker",
        "shit", "bullshit", "shithead",
        "bitch", "bitches", "sonofabitch",
        "ass", "asshole", "dumbass", "jackass",
        "bastard",
        "cunt",
        "dick", "dickhead",
        "pussy",
        "whore", "slut",
        "moron", "idiot", "stupid",
        "crap",
        "jerk",
        "loser",
        "retard",
        "fag", "faggot",
        "hoe",

        "fck", "fcuk", "fuk",
        "sht", "sh1t",
        "biatch",
        "b!tch", "b1tch",
        "a$$", "a55",
        "d1ck",
        "c0ck",

        "عرص",
        "كسم",
        "كسمها",
        "كسمك",
        "كسها",
        "خول",
        "متناك",
        "زبي",
        "زب",
        "منيك",
        "قحب",
        "قحبة",
        "شرموط",
        "شرموطة",
        "وسخ",
        "حيوان",
        "غبي",
        "مغفل",
        "سيس",
        "متخلف",
        "كلب",
        "يا كلب",
        "ابن الوسخة",
        "ابن المتناك",
        "ابن الشرموطة",
        "منيكك",
        "طيز",
        "متناكين",
        "ولاد الكلب",
        "وسخة",
        "وسخين",
        "زبير",
        "معفن",
        "خرة",
        "زبالة",
        "خخخخخخ",

        "يا ابن المرة",
        "يا ابن الكلب",
        "يا ابن الشرموطة",
        "متناك",
        "منتاك",
        "عرص ابن عرص",
        "كسمكوا",
        "كسختك",
        "يا معفن",
        "يا وسخ",
        "قرف",
        "حمار",
        "حمار انت",
        "طياز",
        "طيظ",
        "طياظ",

        "غبي", "هبيل", "تافه", "مسطول"
        ,"بتطس","بتتس","مدعر","مومس",
        "أهطل","اهطل","اهبل","أهبل",


        "3rs", "ars",
        "kosomak", "kos omak",
        "kosomko",
        "manyak", "manyak",
        "sharmouta", "sharmoota",
        "kleb", "kalb",
        "7omar", "homar",
        "ghabi", "ghaby",
        "m5n2", "mkhanza",
        "kos", "ks",


        "kosk", "kosmk", "ksmk",
        "nk", "mnek",
        "tiz", "tyz",
        "zpy","zpi"
    )


    fun isReviewInappropriate(text: String): Boolean {
        val lowercasedText = text.lowercase()
        return badWords.any { badWord -> lowercasedText.contains(badWord) }
    }
}