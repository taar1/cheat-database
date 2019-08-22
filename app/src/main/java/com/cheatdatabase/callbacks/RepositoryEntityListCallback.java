package com.cheatdatabase.callbacks;

import java.util.List;

public abstract class RepositoryEntityListCallback<T> {
    public abstract void onSuccess(List<T> entityList);

    public abstract void onFailure(Exception e);
}
