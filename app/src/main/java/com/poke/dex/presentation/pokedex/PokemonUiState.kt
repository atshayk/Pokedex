package com.poke.dex.presentation.pokedex

import com.poke.dex.data.model.PokemonBrief

sealed interface PokemonUiState {
    object Loading : PokemonUiState
    data class Success(val data: List<PokemonBrief>) : PokemonUiState
    data class Error(val message: String) : PokemonUiState
}