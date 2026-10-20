package com.example.data.local

object InitialDataSeed {
    val FAKE_YOUTUBE_IDS = setOf(
        "60aO8-dM378",
        "0_6J_yG8X-M",
        "y3OqjJv13s0",
        "M-O1C1K21q4",
        "qR_a_p0m-f8",
        "J_E9121Xz0I"
    )

    fun getInitialVideos(): List<VideoEntity> = listOf(
        VideoEntity(
            youtubeId = "9iW5fBH6PRQ",
            title = "Dune Awakening [tuto] : Astuces à connaitre indiquées par le HUD",
            thumbnailUrl = "https://i.ytimg.com/vi/9iW5fBH6PRQ/hqdefault.jpg",
            category = "Éducatif",
            favorite = true,
            duration = "10:00",
            ageMin = 3,
            ageMax = 12,
            displayOrder = 1,
            description = "Dune Awakening [tuto] : Astuces à connaitre indiquées par le HUD"
        )
    )
}

