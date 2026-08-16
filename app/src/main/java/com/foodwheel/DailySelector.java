package com.foodwheel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public final class DailySelector {
    private static final int SLOTS = 16;
    private static int[] dailyOrder = null;
    private static int daySeed = -1;

    private DailySelector() {}

    private static int seedFor(Date d) {
        int y = d.getYear() + 1900;
        int m = d.getMonth() + 1;
        int day = d.getDate();
        return y * 10000 + m * 100 + day;
    }

    private static void ensure(Date d) {
        int seed = seedFor(d);
        if (dailyOrder != null && seed == daySeed) return;
        daySeed = seed;
        Random rnd = new Random(seed);
        int[] perm = new int[FoodData.NAMES.length];
        for (int i = 0; i < perm.length; i++) perm[i] = i;
        for (int i = perm.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = perm[i]; perm[i] = perm[j]; perm[j] = t;
        }
        dailyOrder = new int[SLOTS];
        for (int i = 0; i < SLOTS; i++) dailyOrder[i] = perm[i % perm.length];
    }

    public static int[] getDailyOrder() {
        ensure(new Date());
        return dailyOrder;
    }

    public static int getDishIndex(int slot) {
        int[] order = getDailyOrder();
        return order[Math.floorMod(slot, SLOTS)];
    }

    public static String getDailyQuote() {
        Date d = new Date();
        ensure(d);
        int q = Math.floorMod(seedFor(d) + dailyOrder[0], FoodData.QUOTES.length);
        return FoodData.QUOTES[q];
    }

    public static String getDateLabel() {
        return new SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(new Date());
    }
}
