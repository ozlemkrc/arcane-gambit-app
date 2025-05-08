package com.example.arcane_gambit.ui.screens

data class Character(
    val id: String,
    val name: String,
    val classType: String,
    val avatar: String,
    val luck: Int,
    val attack: Int,
    val defence: Int,
    val vitality: Int,
)