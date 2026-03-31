package com.matrix.multigpt.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.matrix.multigpt.data.database.entity.Message
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileExportHelper {

    // 1. Download Feature: Saari chat ko text file mein save karne ke liye
    fun downloadChatHistory(context: Context, chatTitle: String, messages: List<Message>) {
        try {
            val stringBuilder = StringBuilder()
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            stringBuilder.append("=== Project MultiGPT Chat Export ===\n")
            stringBuilder.append("Chat: $chatTitle\nDate: $timeStamp\n")
            stringBuilder.append("====================================\n\n")

            messages.forEach { msg ->
                val sender = if (msg.platformType == null) "USER" else "AI (${msg.platformType})"
                stringBuilder.append("[$sender]: ${msg.content}\n\n")
            }

            val fileName = "MultiGPT_Export_${System.currentTimeMillis()}.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { it.write(stringBuilder.toString().toByteArray()) }
            Toast.makeText(context, "Chat saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Full AI Access: Isme AI ke paas har chat aur har message ka access hoga
    fun getFullAIContextFromAllChats(allMessagesFromAllChats: List<Message>): String {
        if (allMessagesFromAllChats.isEmpty()) return "No previous history available."
        
        val contextBuilder = StringBuilder()
        contextBuilder.append("IMPORTANT: Below is the complete history of all your conversations. Use this to analyze patterns or answer based on past interactions.\n\n")
        
        allMessagesFromAllChats.forEach { msg ->
            val role = if (msg.platformType == null) "User" else "Assistant (${msg.platformType})"
            // Har message ko context mein add karna
            contextBuilder.append("[$role]: ${msg.content}\n")
        }
        
        contextBuilder.append("\nEnd of history. Now respond to the latest message using this data.")
        return contextBuilder.toString()
    }

    // 3. Auto Reply Logic
    fun getAutoReply(userMessage: String): String? {
        val input = userMessage.lowercase()
        return when {
            input.contains("hello") || input.contains("hi") -> "Auto-Reply: Namaste! Main MultiGPT hoon. Main aapki har purani chat ko yaad rakh sakta hoon."
            input.contains("download chat") -> "Auto-Reply: Chat download karne ke liye upar diye gaye 'Download' icon par click karein."
            else -> null
        }
    }
}
