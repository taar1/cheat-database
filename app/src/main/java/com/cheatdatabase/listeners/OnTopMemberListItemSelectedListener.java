package com.cheatdatabase.listeners;

import com.cheatdatabase.businessobjects.Member;

public interface OnTopMemberListItemSelectedListener {
    void onMemberClicked(Member member);
    void onWebsiteClicked(Member member);
}
