package com.cesba.pokeapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PokeApiService {

    @GET("pokemon/{pokemon}")
    Call<Pokemon> getPokemon(
            @Path("pokemon") String pokemon
    );

    @GET("pokemon-species/{id}")
    Call<PokemonSpecies> getPokemonSpecies(
            @Path("id") int id
    );

    @GET("move/{name}")
    Call<MoveDetail> getMove(
            @Path("name") String name
    );

    @GET
    Call<EvolutionChainResponse> getEvolutionChain(
            @Url String url
    );

    @GET("pokemon")
    Call<PokemonListResponse> getPokemonList(
            @Query("limit") int limit,
            @Query("offset") int offset
    );
}