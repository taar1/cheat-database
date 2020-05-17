package com.cheatdatabase.activity.ui.mycheats;

import androidx.lifecycle.MutableLiveData;

import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.UnpublishedCheat;

import java.util.List;

public interface MyUnpublishedCheatsRepository {

    MutableLiveData<List<UnpublishedCheat>> getMyUnpublishedCheats(Member member);

    String deleteUnpublishedCheat(UnpublishedCheat cheat);

}
