package com.example.Zan.nrfuart;

import java.util.Random;

/**
 * Created by Administrator_nodgd on 2017/09/17.
 */

public class IdentifierManager {

    private static int g = 5;
    private static int p = 1000000009;
    private static int x = 0;

    synchronized public static int getNewIdentifier() {
        if (x == 0) {
            x = new Random().nextInt(p) + 1;
        } else {
            x = (int) ((long) x * g % p);
        }
        return x;
    }
}
