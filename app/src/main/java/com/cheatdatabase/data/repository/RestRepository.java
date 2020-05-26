package com.cheatdatabase.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.UnpublishedCheat;
import com.cheatdatabase.helpers.AeSimpleMD5;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestRepository {
    private static final String TAG = "RestRepository";

    private LiveData<List<Member>> allMembers;
    private Retrofit retrofit;
    private RestApi restApi;

    public RestRepository(Application application) {
        init();
    }

//    public MutableLiveData<List<Member>> getTopMembers() {
//        MutableLiveData<List<Member>> newsData = new MutableLiveData<>();
//
//        Call<List<Member>> call = restApi.getMemberTop20();
//        call.enqueue(new Callback<List<Member>>() {
//            @Override
//            public void onResponse(Call<List<Member>> members, Response<List<Member>> response) {
//                if (response.isSuccessful()) {
//                    newsData.setValue(response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Member>> call, Throwable e) {
//                Log.e(TAG, "loadMembersInBackground onFailure: " + e.getLocalizedMessage());
//            }
//        });
//
//        return newsData;
//    }

    public MutableLiveData<List<UnpublishedCheat>> getMyUnpublishedCheats(Member member) {
        MutableLiveData<List<UnpublishedCheat>> unpublishedCheatsLiveData = new MutableLiveData<>();
        try {
//            String password_md5 = AeSimpleMD5.MD5(member.getPassword());
//            Log.d(TAG, "XXXXX getMyUnpublishedCheats MID: " + member.getMid());
//            Log.d(TAG, "XXXXX getMyUnpublishedCheats MD5: " + password_md5);

            Call<List<UnpublishedCheat>> call = restApi.getMyUnpublishedCheats(member.getMid(), AeSimpleMD5.MD5(member.getPassword()));
            call.enqueue(new Callback<List<UnpublishedCheat>>() {
                @Override
                public void onResponse(Call<List<UnpublishedCheat>> unpublishedCheats, Response<List<UnpublishedCheat>> response) {
                    if (response.isSuccessful()) {
                        unpublishedCheatsLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<UnpublishedCheat>> call, Throwable e) {
                    Log.e(TAG, "XXXXX getMyUnpublishedCheats onFailure: " + e.getLocalizedMessage(), e);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "XXXXX NoSuchAlgorithmException: " + e.getLocalizedMessage());
        }

        return unpublishedCheatsLiveData;
    }

    /**
     * Deletes an unpublished cheat of a member.
     *
     * @param unpublishedCheat
     * @param member
     * @return delete_ok | delete_nok | wrong_pw | member_banned | member_not_exist | no_database_access
     */
    public String deleteUnpublishedCheat(UnpublishedCheat unpublishedCheat, Member member) {
        StringBuilder returnValue = new StringBuilder();
        try {
            Call<JsonObject> call = restApi.deleteUnpublishedCheat(member.getMid(), AeSimpleMD5.MD5(member.getPassword()), unpublishedCheat.getId(), unpublishedCheat.getGame().getGameId(), unpublishedCheat.getTableInfo());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> unpublishedCheats, Response<JsonObject> response) {
                    Log.d(TAG, "XXXXX onResponse: ");
                    if (response.isSuccessful()) {
                        JsonObject responseJsonObject = response.body();

                        returnValue.append(responseJsonObject.get("returnValue").getAsString());
                        Log.d(TAG, "deleteUnpublishedCheat SUCCESS: " + returnValue);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable e) {
                    Log.e(TAG, "XXXXX getMyUnpublishedCheats onFailure: " + e.getLocalizedMessage(), e);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "XXXXX NoSuchAlgorithmException (MD5 Hash of pw error): " + e.getLocalizedMessage());
        }

        return returnValue.toString();
    }

    private void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Konstanten.BASE_URL_REST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        restApi = retrofit.create(RestApi.class);
    }
}
