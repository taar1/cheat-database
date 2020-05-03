package com.cheatdatabase.listeners;

import com.cheatdatabase.data.model.Member;

public interface OnTopMemberListItemSelectedListener {
    void onMemberClicked(Member member);
    void onWebsiteClicked(Member member);
}
