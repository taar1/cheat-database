package com.cheatdatabase.data;

import com.cheatdatabase.helpers.Konstanten;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(Konstanten.BASE_URL_REST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

}
