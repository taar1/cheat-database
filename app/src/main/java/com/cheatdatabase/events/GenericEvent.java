package com.cheatdatabase.events;

public class GenericEvent {

    public enum Action {
        CLICK_CHEATS_DRAWER
    }

    private final Action action;

    public GenericEvent(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
}
