package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChildProfile

@Entity(tableName = "child_profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarEmoji: String = "👦",
    val age: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChildProfile = ChildProfile(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        age = age,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(profile: ChildProfile): ProfileEntity = ProfileEntity(
            id = profile.id,
            name = profile.name,
            avatarEmoji = profile.avatarEmoji,
            age = profile.age,
            createdAt = profile.createdAt
        )
    }
}
