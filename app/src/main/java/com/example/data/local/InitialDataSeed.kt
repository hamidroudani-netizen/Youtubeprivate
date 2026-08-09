package com.example.data.local

object InitialDataSeed {
    fun getInitialVideos(): List<VideoEntity> = listOf(
        VideoEntity(
            youtubeId = "60aO8-dM378",
            title = "Petit Ours Brun fait du vélo",
            thumbnailUrl = "https://img.youtube.com/vi/60aO8-dM378/hqdefault.jpg",
            category = "Dessins animés",
            favorite = true,
            duration = "3:00",
            ageMin = 3,
            ageMax = 6,
            displayOrder = 1,
            description = "Un épisode rigolo de Petit Ours Brun qui apprend à faire du vélo."
        ),
        VideoEntity(
            youtubeId = "0_6J_yG8X-M",
            title = "Une Souris Verte - Comptines pour enfants",
            thumbnailUrl = "https://img.youtube.com/vi/0_6J_yG8X-M/hqdefault.jpg",
            category = "Comptines",
            favorite = true,
            duration = "2:15",
            ageMin = 2,
            ageMax = 6,
            displayOrder = 2,
            description = "La célèbre comptine animée pour chanter ensemble."
        ),
        VideoEntity(
            youtubeId = "y3OqjJv13s0",
            title = "Trotro est un grand",
            thumbnailUrl = "https://img.youtube.com/vi/y3OqjJv13s0/hqdefault.jpg",
            category = "Dessins animés",
            favorite = false,
            duration = "3:30",
            ageMin = 3,
            ageMax = 7,
            displayOrder = 3,
            description = "Trotro l'âne rigolo veut montrer qu'il est devenu un grand !"
        ),
        VideoEntity(
            youtubeId = "M-O1C1K21q4",
            title = "Les Chiffres et les Nombres - Éducatif",
            thumbnailUrl = "https://img.youtube.com/vi/M-O1C1K21q4/hqdefault.jpg",
            category = "Éducatif",
            favorite = false,
            duration = "4:00",
            ageMin = 3,
            ageMax = 8,
            displayOrder = 4,
            description = "Apprendre à compter de 1 à 10 en s'amusant avec de jolies couleurs."
        ),
        VideoEntity(
            youtubeId = "qR_a_p0m-f8",
            title = "Peppa Pig - Le Saut dans les flaques",
            thumbnailUrl = "https://img.youtube.com/vi/qR_a_p0m-f8/hqdefault.jpg",
            category = "Dessins animés",
            favorite = true,
            duration = "5:00",
            ageMin = 3,
            ageMax = 7,
            displayOrder = 5,
            description = "Peppa et George adorent sauter dans la boue !"
        ),
        VideoEntity(
            youtubeId = "J_E9121Xz0I",
            title = "Les Couleurs en Musique",
            thumbnailUrl = "https://img.youtube.com/vi/J_E9121Xz0I/hqdefault.jpg",
            category = "Éducatif",
            favorite = false,
            duration = "3:10",
            ageMin = 3,
            ageMax = 6,
            displayOrder = 6,
            description = "Découvrir le rouge, le bleu, le jaune et le vert en chanson."
        )
    )
}
