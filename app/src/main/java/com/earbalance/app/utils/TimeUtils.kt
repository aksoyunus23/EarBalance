package com.earbalance.app.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> String.format("%dsa %02ddak", hours, minutes)
            minutes > 0 -> String.format("%ddak %02dsn", minutes, secs)
            else -> String.format("%dsn", secs)
        }
    }

    fun formatDurationShort(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> String.format("%ds %02ddak", hours, minutes)
            else -> String.format("%ddak", minutes)
        }
    }

    fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun formatDateDisplay(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString
            val displaySdf = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
            displaySdf.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateShort(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString
            val displaySdf = SimpleDateFormat("d MMM", Locale("tr"))
            displaySdf.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun calculateBalance(leftSeconds: Long, rightSeconds: Long, bothSeconds: Long): Triple<Float, Float, String> {
        val totalTracked = leftSeconds + rightSeconds + (bothSeconds * 2)
        if (totalTracked == 0L) return Triple(50f, 50f, "Veri Yok")

        val effectiveLeft = leftSeconds + bothSeconds
        val effectiveRight = rightSeconds + bothSeconds
        val total = effectiveLeft + effectiveRight

        val leftPct = if (total > 0) (effectiveLeft.toFloat() / total * 100) else 50f
        val rightPct = 100f - leftPct

        val balanceText = when {
            Math.abs(leftPct - rightPct) < 5 -> "Mükemmel Denge! 🎯"
            Math.abs(leftPct - rightPct) < 15 -> "İyi Denge 👍"
            Math.abs(leftPct - rightPct) < 30 -> "Dengesiz ⚠️"
            else -> "Çok Dengesiz ❌"
        }

        return Triple(leftPct, rightPct, balanceText)
    }
}
