package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.entity.IngredientEntity
import com.example.recipegenerator.data.repository.IngredientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IngredientViewModel(
    private val repository: IngredientRepository,
    val userId: String
) : ViewModel() {

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    // Tracks ingredients currently being deleted — prevents Firestore
    // snapshot listener from re-adding them before the cloud delete completes
    private val _pendingDeletes = mutableSetOf<String>()

    val ingredients: StateFlow<List<IngredientEntity>> = repository.allIngredients
        .map { list -> list.filter { it.userId == userId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchFromFirestore()
    }

    // ─── Firestore Sync ───────────────────────────────────────────────────────

    private fun fetchFromFirestore() {
        if (userId.isBlank()) return

        try {
            db.collection("users").document(userId)
                .collection("ingredients")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    val cloudIngredients = snapshot?.documents?.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        IngredientEntity(
                            userId = userId,
                            name = name,
                            expirationDate = doc.getString("expirationDate") ?: "",
                            category = doc.getString("category") ?: "Uncategorized",
                            quantity = doc.getDouble("quantity") ?: 1.0,
                            unit = doc.getString("unit") ?: "pcs"
                        )
                    } ?: emptyList()

                    viewModelScope.launch {
                        syncLocalWithCloud(cloudIngredients)
                    }
                }
        } catch (e: Exception) {
            // Firestore unavailable — app runs on Room only
        }
    }

    private suspend fun syncLocalWithCloud(cloudIngredients: List<IngredientEntity>) {
        if (userId.isBlank()) return

        try {
            val localIngredients = repository.allIngredients.first()
                .filter { it.userId == userId }

            // Add cloud items that don't exist locally
            // Skip anything currently being deleted — prevents re-add race condition
            cloudIngredients.forEach { cloudItem ->
                if (_pendingDeletes.contains(cloudItem.name)) return@forEach

                val existsLocally = localIngredients.any { local ->
                    local.name.equals(cloudItem.name, ignoreCase = true)
                }
                if (!existsLocally) repository.addIngredient(cloudItem)
            }

            // Remove local items that no longer exist in cloud
            // Skip pending deletes — they are being handled separately
            localIngredients.forEach { localItem ->
                if (_pendingDeletes.contains(localItem.name)) return@forEach

                val existsInCloud = cloudIngredients.any { cloud ->
                    cloud.name.equals(localItem.name, ignoreCase = true)
                }
                if (!existsInCloud) repository.delete(localItem)
            }
        } catch (e: Exception) {
            // Sync failed silently
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    fun insert(newIngredient: IngredientEntity) {
        viewModelScope.launch {
            repository.addIngredient(newIngredient)

            if (userId.isNotBlank()) {
                try {
                    db.collection("users").document(userId)
                        .collection("ingredients")
                        .add(
                            mapOf(
                                "name" to newIngredient.name,
                                "category" to newIngredient.category,
                                "quantity" to newIngredient.quantity,
                                "unit" to newIngredient.unit,
                                "expirationDate" to newIngredient.expirationDate
                            )
                        ).await()
                } catch (e: Exception) { }
            }
        }
    }

    fun update(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.update(ingredient)

            // Also update in Firestore by finding the matching doc
            if (userId.isNotBlank()) {
                try {
                    val snapshot = db.collection("users")
                        .document(userId)
                        .collection("ingredients")
                        .whereEqualTo("name", ingredient.name)
                        .get()
                        .await()

                    snapshot.documents.firstOrNull()?.reference?.update(
                        mapOf(
                            "category" to ingredient.category,
                            "quantity" to ingredient.quantity,
                            "unit" to ingredient.unit,
                            "expirationDate" to ingredient.expirationDate
                        )
                    )?.await()
                } catch (e: Exception) { }
            }
        }
    }

    fun delete(ingredient: IngredientEntity) {
        // Mark as pending BEFORE coroutine launches so the snapshot
        // listener can't restore it during the async gap
        _pendingDeletes.add(ingredient.name)

        viewModelScope.launch {
            try {
                // 1. Delete from Room immediately
                repository.delete(ingredient)

                // 2. Delete from Firestore so snapshot doesn't restore it
                if (userId.isNotBlank()) {
                    try {
                        val snapshot = db.collection("users")
                            .document(userId)
                            .collection("ingredients")
                            .whereEqualTo("name", ingredient.name)
                            .get()
                            .await()

                        snapshot.documents.forEach { doc ->
                            doc.reference.delete().await()
                        }
                    } catch (e: Exception) {
                        // Firestore delete failed — Room delete still succeeded
                        // Item will be re-synced on next app launch
                    }
                }
            } finally {
                // Always clear the pending delete flag when done
                _pendingDeletes.remove(ingredient.name)
            }
        }
    }
}

class IngredientViewModelFactory(
    private val repository: IngredientRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}