package com.cheatdatabase.model;

import javax.inject.Inject;

public class WelcomeMessage {

    private String created;
    private int id;
    private String title;
    private String title_de;
    private String welcomeMessage;
    private String welcomeMessage_de;

    @Inject
    public WelcomeMessage() {

    }

    public String getCreated() {
        return created;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTitle_de() {
        return title_de;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getWelcomeMessage_de() {
        return welcomeMessage_de;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle_de(String title_de) {
        this.title_de = title_de;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public void setWelcomeMessage_de(String welcomeMessage_de) {
        this.welcomeMessage_de = welcomeMessage_de;
    }

}
