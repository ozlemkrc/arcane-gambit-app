package com.example.arcane_gambit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcane_gambit.data.model.toLocalCharacter
import com.example.arcane_gambit.data.model.toServerCharacter
import com.example.arcane_gambit.data.repository.CharacterRepository
import com.example.arcane_gambit.ui.screens.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterViewModel : ViewModel() {
    private val characterRepository = CharacterRepository()
    
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadCharacters(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            characterRepository.getAllCharacters(token).fold(
                onSuccess = { serverCharacters ->
                    _characters.value = serverCharacters.map { it.toLocalCharacter() }
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun createCharacter(token: String, character: Character, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            characterRepository.createCharacter(token, character.toServerCharacter()).fold(
                onSuccess = { serverCharacter ->
                    val newCharacter = serverCharacter.toLocalCharacter()
                    _characters.value = _characters.value + newCharacter
                    _isLoading.value = false
                    onSuccess()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun deleteCharacters(token: String, characterIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            var hasError = false
            
            characterIds.forEach { characterId ->
                characterRepository.deleteCharacter(token, characterId).fold(
                    onSuccess = {
                        // Remove from local list
                        _characters.value = _characters.value.filterNot { it.id == characterId }
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        hasError = true
                    }
                )
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
