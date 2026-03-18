package com.example.recipegenerator.data.repository

import com.example.recipegenerator.data.dao.NotificationDao
import com.example.recipegenerator.data.dao.IngredientDao
import com.example.recipegenerator.data.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val ingredientDao: IngredientDao
) {
    val notifications: Flow<List<NotificationEntity>> =
        notificationDao.getAllNotifications()

    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    /**
     * Scans all ingredients and creates notifications for those
     * expiring within 7 days. Skips duplicates.
     */
    suspend fun syncExpiringNotifications(userId: String) {
        val ingredients = ingredientDao.getAllIngredients().first()
            .filter { it.userId == userId }

        val today = java.time.LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")

        ingredients.forEach { ingredient ->
            if (ingredient.expirationDate.isBlank()) return@forEach

            val expDate = try {
                java.time.LocalDate.parse(ingredient.expirationDate, formatter)
            } catch (e: Exception) { return@forEach }

            val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, expDate)

            if (daysLeft in 0..7) {

                val existing = notificationDao.getByIngredientName(ingredient.name)
                if (existing != null) return@forEach

                val isCritical = daysLeft <= 3
                val message = when {
                    daysLeft == 0L -> "${ingredient.name} expires TODAY!"
                    daysLeft == 1L -> "${ingredient.name} expires tomorrow"
                    isCritical -> "${ingredient.name} expires in ${daysLeft} days — use it soon"
                    else -> "${ingredient.name} expires in ${daysLeft} days"
                }

                notificationDao.insertNotification(
                    NotificationEntity(
                        ingredientName = ingredient.name,
                        message = message,
                        daysUntilExpiry = daysLeft,
                        isCritical = isCritical
                    )
                )
            }
        }
    }

    suspend fun clearAll() = notificationDao.clearAll()
    suspend fun markAsRead(id: Int) = notificationDao.markAsRead(id)
}