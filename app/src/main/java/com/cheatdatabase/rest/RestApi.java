package com.cheatdatabase.rest;

import com.cheatdatabase.model.Cheat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestApi {

    @POST("getCheatsByGameId.php")
    Call<List<Cheat>> getCheatsByGameId(@Body int gameId);

}
