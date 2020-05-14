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

//    private static final RestRepository ourInstance = new RestRepository();
//
//    public static RestRepository getInstance() {
//        return ourInstance;
//    }

    public RestRepository(Application application) {
        init();
    }

    public MutableLiveData<List<Member>> getTopMembers() {
        MutableLiveData<List<Member>> newsData = new MutableLiveData<>();

        Call<List<Member>> call = restApi.getMemberTop20();
        call.enqueue(new Callback<List<Member>>() {
            @Override
            public void onResponse(Call<List<Member>> members, Response<List<Member>> response) {
                if (response.isSuccessful()) {
                    newsData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Member>> call, Throwable e) {
                Log.e(TAG, "loadMembersInBackground onFailure: " + e.getLocalizedMessage());
            }
        });

        return newsData;
    }

    public MutableLiveData<List<UnpublishedCheat>> getMyUnpublishedCheats(Member member) {
        MutableLiveData<List<UnpublishedCheat>> unpublishedCheatsLiveData = new MutableLiveData<>();
        try {
            String password_md5 = AeSimpleMD5.MD5(member.getPassword());

            Log.d(TAG, "XXXXX getMyUnpublishedCheats MID: " + member.getMid());
            Log.d(TAG, "XXXXX getMyUnpublishedCheats MD5: " + password_md5);

            // TODO FIXME konvertieren zu UnpublishedCheat schlägt noch fehl....
            // TODO FIXME konvertieren zu UnpublishedCheat schlägt noch fehl....
            // TODO FIXME konvertieren zu UnpublishedCheat schlägt noch fehl....
            // TODO FIXME konvertieren zu UnpublishedCheat schlägt noch fehl....
            // TODO FIXME konvertieren zu UnpublishedCheat schlägt noch fehl....
            // TODO FIXME konvertieren zu UnpublishedCheat schlägt noch fehl....

            Call<List<UnpublishedCheat>> call = restApi.getMyUnpublishedCheats(member.getMid(), password_md5);
            call.enqueue(new Callback<List<UnpublishedCheat>>() {
                @Override
                public void onResponse(Call<List<UnpublishedCheat>> unpublishedCheats, Response<List<UnpublishedCheat>> response) {
                    Log.d(TAG, "XXXXX onResponse: ");
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

    private void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Konstanten.BASE_URL_REST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restApi = retrofit.create(RestApi.class);
    }
}
