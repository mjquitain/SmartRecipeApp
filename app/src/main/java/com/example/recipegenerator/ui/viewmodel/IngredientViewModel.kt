package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.entity.IngredientEntity
import com.example.recipegenerator.data.repository.IngredientRepository
import com.example.recipegenerator.model.Ingredient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientViewModel(private val repository: IngredientRepository, val userId: String) : ViewModel() {
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    // Exposes ingredient list as StateFlow
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

    private fun fetchFromFirestore() {
        db.collection("users").document(userId).collection("ingredients")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val cloudIngredients = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: ""
                    val expDate = doc.getString("expirationDate") ?: "N/A"

                    IngredientEntity(
                        userId = userId,
                        name = name,
                        expirationDate = expDate,
                        category = doc.getString("category") ?: "Uncategorized",
                        quantity = doc.getDouble("quantity") ?: 1.0,
                        unit = doc.getString("unit") ?: "pcs"
                    )
                } ?: emptyList()

                viewModelScope.launch {
                    syncLocalWithCloud(cloudIngredients)
                }
            }
    }

    private suspend fun syncLocalWithCloud(cloudIngredients: List<IngredientEntity>) {
        val localIngredients = repository.allIngredients.first().filter { it.userId == userId }

        cloudIngredients.forEach { cloudItem ->
            val existsLocally = localIngredients.any { local ->
                local.name.equals(cloudItem.name, ignoreCase = true) &&
                        local.expirationDate == cloudItem.expirationDate
            }

            if (!existsLocally) {
                repository.addIngredient(cloudItem)
            }
        }

        localIngredients.forEach { localItem ->
            val existsInCloud = cloudIngredients.any { cloud ->
                cloud.name.equals(localItem.name, ignoreCase = true) &&
                        cloud.expirationDate == localItem.expirationDate
            }

            if (!existsInCloud) {
                repository.delete(localItem)
            }
        }
    }

    fun insert(newIngredient: IngredientEntity) {
        viewModelScope.launch {
            repository.addIngredient(newIngredient)
        }
    }

    fun update(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.update(ingredient)
        }
    }

    fun delete(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.delete(ingredient)
        }
    }
}

// Needed for ViewModel constructor parameter (repository)
class IngredientViewModelFactory(private val repository: IngredientRepository, private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}