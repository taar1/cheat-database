package com.cheatdatabase.activity.ui.mvvmexample.ui.mvvmtest;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.repository.RestRepository;
import com.cheatdatabase.rest.RestApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class MvvmTestViewModel extends AndroidViewModel {
    private static final String TAG = "MvvmTestViewModel";

    private Retrofit retrofit;
    private RestApi restApi;
    private Call<List<Member>> allMembers;
    private MutableLiveData<List<Member>> memberList;
    private RestRepository restRepository;

    public MvvmTestViewModel(@NonNull Application application) {
        super(application);

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
        if (memberList != null) {
            return;
        }

        restRepository = new RestRepository(getApplication());
        memberList = restRepository.getTopMembers();

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

    public LiveData<List<Member>> getTopMembersRepository() {
        return memberList;
    }

}
