package com.example.hh.nrfuart;

/**
 * Created by Angel on 2017/6/30.
 */

public class Card {
    private String id;
    private int type;
    public Card(String id, int type)
    {
        this.id = id;
        this.type = type;
    }

    public String getId()
    {
        return id;
    }

    public int getType()
    {
        return type;
    }
}
