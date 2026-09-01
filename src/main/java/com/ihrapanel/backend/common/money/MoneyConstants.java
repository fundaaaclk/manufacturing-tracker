package com.ihrapanel.backend.common.money;

import java.math.RoundingMode;

public final class MoneyConstants {

    public static final int MONEY_SCALE = 2;  //12,50  yerine 12,555  tercih edilmez 
    public static final int FX_RATE_SCALE = 6; //döviz için ,... çok önemli

    public static final RoundingMode ROUNDING_MODE =
            RoundingMode.HALF_UP;

    private MoneyConstants() { //bu constructor baska yerde oluşturulmasın cunku aslında bu class  kural 
    }
}