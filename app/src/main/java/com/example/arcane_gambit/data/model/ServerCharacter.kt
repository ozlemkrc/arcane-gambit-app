package com.example.arcane_gambit.data.model

import com.google.gson.annotations.SerializedName

data class ServerCharacter(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("characterName")
    val characterName: String,
    val avatar: String,
    @SerializedName("class")
    val characterClass: String, // archer, mage, warrior
    val luck: Int,
    val attack: Int,
    @SerializedName("defense")
    val defense: Int,
    val vitality: Int,
    val attackType: String,
    val attackDamage: Int
)

data class CreateCharacterRequest(
    val characterName: String,
    val avatar: String,
    @SerializedName("class")
    val characterClass: String,
    val luck: Int,
    val attack: Int,
    val defense: Int,
    val vitality: Int,
    val attackType: String,
    val attackDamage: Int
)

// Extension function to convert from local Character to ServerCharacter
fun com.example.arcane_gambit.ui.screens.Character.toServerCharacter(): CreateCharacterRequest {
    return CreateCharacterRequest(
        characterName = this.name,
        avatar = this.avatar,
        characterClass = this.classType.lowercase(),
        luck = this.luck,
        attack = this.attack,
        defense = this.defence,
        vitality = this.vitality,
        attackType = getAttackTypeForClass(this.classType),
        attackDamage = calculateAttackDamage(this.attack)
    )
}

// Extension function to convert from ServerCharacter to local Character
fun ServerCharacter.toLocalCharacter(): com.example.arcane_gambit.ui.screens.Character {
    return com.example.arcane_gambit.ui.screens.Character(
        id = this.id ?: "",
        name = this.characterName,
        classType = this.characterClass.replaceFirstChar { it.uppercase() },
        avatar = this.avatar,
        luck = this.luck,
        attack = this.attack,
        defence = this.defense,
        vitality = this.vitality
    )
}

private fun getAttackTypeForClass(classType: String): String {
    return when (classType.lowercase()) {
        "warrior" -> "melee"
        "mage" -> "magic"
        "archer" -> "ranged"
        else -> "melee"
    }
}

private fun calculateAttackDamage(attack: Int): Int {
    // Simple calculation based on attack stat
    return attack + (attack / 2)
}
