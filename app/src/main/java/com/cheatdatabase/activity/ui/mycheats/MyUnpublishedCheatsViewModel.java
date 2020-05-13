package com.cheatdatabase.activity.ui.mycheats;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.UnpublishedCheat;
import com.cheatdatabase.data.repository.RestRepository;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class MyUnpublishedCheatsViewModel extends AndroidViewModel {
    private static final String TAG = "MyUnpublishedCheatsViewModel";

    private Retrofit retrofit;
    private RestApi restApi;
    private Call<List<Member>> allMembers;
    private final Member member;
    private RestRepository restRepository;
    private MutableLiveData<List<UnpublishedCheat>> unpublishedCheatsList;
    private SharedPreferences settings;

    public MyUnpublishedCheatsViewModel(@NonNull Application application) {
        super(application);

        settings = application.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
//        init();
//        getAllMembers();
    }

//    public void  getAllMembers() {
//        Call<List<Member>> call = restApi.getMemberTop20();
//        call.enqueue(new Callback<List<Member>>() {
//            @Override
//            public void onResponse(Call<List<Member>> members, Response<List<Member>> response) {
//                memberList = response.body();
//            }
//
//            @Override
//            public void onFailure(Call<List<Member>> call, Throwable e) {
//                Log.e(TAG, "loadMembersInBackground onFailure: " + e.getLocalizedMessage());
//            }
//        });
//    }

    public void init() {
        if (unpublishedCheatsList != null) {
            return;
        }

        restRepository = new RestRepository(getApplication());
        unpublishedCheatsList = restRepository.getMyUnpublishedCheats(member);

//        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
//
//        retrofit = new Retrofit.Builder()
//                .client(okHttpClient)
//                .baseUrl(Konstanten.BASE_URL_REST)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        restApi = retrofit.create(RestApi.class);
    }

    public LiveData<List<UnpublishedCheat>> getTopMembersRepository() {
        return unpublishedCheatsList;
    }

}
