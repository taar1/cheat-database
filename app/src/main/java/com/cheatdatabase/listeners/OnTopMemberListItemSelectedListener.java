package com.cheatdatabase.listeners;

import com.cheatdatabase.model.Member;

public interface OnTopMemberListItemSelectedListener {
    void onMemberClicked(Member member);
    void onWebsiteClicked(Member member);
}
