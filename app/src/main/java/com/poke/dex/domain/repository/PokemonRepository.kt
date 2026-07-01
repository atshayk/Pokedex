package com.poke.dex.domain.repository

import com.poke.dex.data.model.PokemonEntity
import com.poke.dex.data.model.PokemonResponse
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun getPokemonList(): PokemonResponse
    suspend fun getCachedPokemon() : Flow<List<PokemonEntity>>
    suspend fun refreshPokemonList()
}