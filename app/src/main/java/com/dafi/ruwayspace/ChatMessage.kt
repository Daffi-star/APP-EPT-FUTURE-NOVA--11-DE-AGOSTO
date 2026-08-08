package com.dafi.ruwayspace

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean // true si es tuyo, false si es del bot
)