package com.poke.dex.data.repository

import com.poke.dex.data.api.PokeApiService
import com.poke.dex.data.database.PokemonDao
import com.poke.dex.data.model.PokemonEntity
import com.poke.dex.data.model.PokemonResponse
import com.poke.dex.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val apiService: PokeApiService,
    private val pokemonDao: PokemonDao
) : PokemonRepository {

    override suspend fun getCachedPokemon(): Flow<List<PokemonEntity>> {
        return pokemonDao.getAllPokemon()
    }

    override suspend fun refreshPokemonList() {
        try {
            val response = apiService.getPokemonList()
            val entities = response.results.mapIndexed { index, brief ->
                PokemonEntity(id = index + 1, name = brief.name, url = brief.url)
            }
            pokemonDao.insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getPokemonList(): PokemonResponse {
        return apiService.getPokemonList()
    }
}