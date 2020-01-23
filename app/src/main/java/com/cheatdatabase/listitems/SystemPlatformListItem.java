package com.cheatdatabase.listitems;

import com.cheatdatabase.model.SystemPlatform;

public class SystemPlatformListItem extends ListItem {

    private SystemPlatform systemPlatform;

    public SystemPlatform getSystemPlatform() {
        return systemPlatform;
    }

    public void setSystemPlatform(SystemPlatform systemPlatform) {
        this.systemPlatform = systemPlatform;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_SYSTEM;
    }

    @Override
    public String getTitle() {
        return systemPlatform.getSystemName().substring(0, 1);
    }
}
