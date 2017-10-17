package com.example.Zan.nrfuart;

/**
 * Created by nodgd on 2017/09/23.
 */

public abstract class BaseCardData {

    private int identifier;
    protected String title;

    public BaseCardData() {
        identifier = IdentifierManager.getNewIdentifier();
        title = "";
    }

    public BaseCardData(BaseCardData cardData) {
        identifier = IdentifierManager.getNewIdentifier();
        title = cardData.title;
    }

    public int getIdentifier() {
        return identifier;
    }

    public abstract String getTitle();

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }
}
