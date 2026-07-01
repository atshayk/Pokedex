package com.poke.dex.data.api

import com.poke.dex.data.model.PokemonResponse
import retrofit2.http.GET

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(): PokemonResponse // In the url, there are no queries. So i have removed it.
}