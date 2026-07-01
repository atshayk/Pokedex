package com.poke.dex.presentation.pokedex

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poke.dex.data.model.PokemonBrief
import com.poke.dex.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokeDexViewModel @Inject constructor(
    private val repository: PokemonRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading)

    val uiState = _uiState.asStateFlow()

    init { // As opined earlier, this init is needed when we want to load data as soon view model is initiated.
        fetchPokemonList()
    }

    fun fetchPokemonList() {
        viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading
            try {
                val response = repository.getPokemonList()
                Log.i("test api", response.results.toString())
                _uiState.value = PokemonUiState.Success(response.results)
            } catch(e: Exception) {
                Log.e("PokeDexViewModel","Error fetching pokemon",e)
                _uiState.value = PokemonUiState.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }
}