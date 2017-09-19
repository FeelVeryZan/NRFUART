package com.example.Zan.nrfuart;

/**
 * Created by Administrator_nodgd on 2017/09/17.
 */

public class IdentifierManager {

    private static int x = 233;

    synchronized public static int getNewIdentifier() {
        return ++x;
    }
}
