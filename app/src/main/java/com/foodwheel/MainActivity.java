package com.foodwheel;

import android.animation.ObjectAnimator;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.content.res.AssetFileDescriptor;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends android.app.Activity {
    private static float savedRotation = 0f;
    private FoodWheelView wheel;
    private TextView resultText;
    private Button detailButton;
    private Button spinButton;
    private TextView quoteView;
    private TextView dateView;
    private int selectedDishIndex = -1;
    private SoundPool soundPool;
    private int chainSoundId;
    private ImageView bgImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        initSoundPool();

        FrameLayout root = new FrameLayout(this);
        ImageView bg = new ImageView(this);
        bgImageView = bg;
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(bg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View scrim = new View(this);
        scrim.setBackgroundResource(R.drawable.bg_scrim);
        root.addView(scrim, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(20), dp(16), dp(28));
        scroll.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView titleDate = new TextView(this);
        titleDate.setText(DailySelector.getDateLabel() + " · 今日菜单已刷新");
        titleDate.setTextColor(Color.parseColor("#FFF3D0"));
        titleDate.setTextSize(15);
        content.addView(titleDate);

        TextView title = new TextView(this);
        title.setText("今天吃什么？");
        title.setTextSize(34);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.WHITE);
        title.setShadowLayer(dp(4), 0, dp(2), Color.parseColor("#66000000"));
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpTitle.topMargin = dp(4);
        content.addView(title, lpTitle);

        quoteView = new TextView(this);
        quoteView.setText("“ " + DailySelector.getDailyQuote() + " ”");
        quoteView.setTextSize(15);
        quoteView.setTextColor(Color.parseColor("#5A2E1B"));
        quoteView.setBackgroundResource(R.drawable.bg_quote_card);
        quoteView.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams lpQuote = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpQuote.topMargin = dp(14);
        content.addView(quoteView, lpQuote);

        dateView = new TextView(this);
        dateView.setText("每天自动刷新菜品，帮今天做个决定");
        dateView.setTextSize(13);
        dateView.setTextColor(Color.parseColor("#F2D8B8"));
        dateView.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpDate.topMargin = dp(8);
        content.addView(dateView, lpDate);

        int[] dims = getDisplaySize(); int wheelSize = Math.max(240, Math.min(dims[0] - dp(24), Math.min(dp(342), dims[1] - dp(280))));
        FrameLayout wheelFrame = new FrameLayout(this);
        LinearLayout.LayoutParams lpWheelFrame = new LinearLayout.LayoutParams(wheelSize, wheelSize);
        lpWheelFrame.topMargin = dp(16);
        lpWheelFrame.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        wheelFrame.setPadding(0, 0, 0, 0);
        content.addView(wheelFrame, lpWheelFrame);

        int inner = (int)(wheelSize - dp(46));
        wheel = new FoodWheelView(this);
        FrameLayout.LayoutParams lpWheel = new FrameLayout.LayoutParams(inner, inner);
        lpWheel.gravity = android.view.Gravity.CENTER;
        wheelFrame.addView(wheel, lpWheel);

        NeedleView needle = new NeedleView(this);
        FrameLayout.LayoutParams lpNeedle = new FrameLayout.LayoutParams(dp(64), dp(64));
        lpNeedle.gravity = android.view.Gravity.RIGHT | android.view.Gravity.CENTER_VERTICAL;
        lpNeedle.rightMargin = dp(20);
        wheelFrame.addView(needle, lpNeedle);

        resultText = new TextView(this);
        resultText.setText("点击下方「转一下」开始选择");
        resultText.setTextSize(20);
        resultText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        resultText.setTextColor(Color.WHITE);
        resultText.setGravity(android.view.Gravity.CENTER);
        resultText.setShadowLayer(dp(3), 0, dp(2), Color.parseColor("#99000000"));
        LinearLayout.LayoutParams lpResult = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpResult.topMargin = dp(6);
        content.addView(resultText, lpResult);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(dp(2), dp(8), dp(2), 0);
        content.addView(buttonRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button bgButton = new Button(this);
        bgButton.setText("🎨 换背景");
        bgButton.setTextSize(15);
        bgButton.setTextColor(Color.parseColor("#5A2E1B"));
        bgButton.setBackgroundResource(R.drawable.bg_button_ghost);
        LinearLayout.LayoutParams lpBg = new LinearLayout.LayoutParams(0, dp(52));
        lpBg.weight = 0.42f;
        lpBg.rightMargin = dp(8);
        buttonRow.addView(bgButton, lpBg);
        bgButton.setOnClickListener(v -> startActivityForResult(new Intent(this, BackgroundActivity.class), 41));

        spinButton = new Button(this);
        spinButton.setText("转一下 🎡");
        spinButton.setTextSize(17);
        spinButton.setTextColor(Color.WHITE);
        spinButton.setBackgroundResource(R.drawable.bg_button_primary);
        LinearLayout.LayoutParams lpSpin = new LinearLayout.LayoutParams(0, dp(56));
        lpSpin.weight = 0.58f;
        buttonRow.addView(spinButton, lpSpin);

        detailButton = new Button(this);
        detailButton.setText("🍳 看看这道菜怎么做");
        detailButton.setTextSize(16);
        detailButton.setTextColor(Color.WHITE);
        detailButton.setBackgroundResource(R.drawable.bg_button_primary);
        detailButton.setEnabled(false);
        detailButton.setAlpha(0.55f);
        LinearLayout.LayoutParams lpDetail = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        lpDetail.topMargin = dp(10);
        content.addView(detailButton, lpDetail);
        detailButton.setOnClickListener(v -> {
            if (selectedDishIndex >= 0) {
                Intent i = new Intent(this, RecipeActivity.class);
                i.putExtra("dish", selectedDishIndex);
                startActivity(i);
            }
        });

        setContentView(root);
        applyBackground(bg);
        wheel.setRotation(savedRotation);
        wheel.setDishes(DailySelector.getDailyOrder());
        setupWheelEvents();

        boolean firstRun = getSharedPreferences("food_wheel_prefs", MODE_PRIVATE).getBoolean("first_run_seen", false);
        if (!firstRun) {
            android.app.AlertDialog d = new android.app.AlertDialog.Builder(this)
                .setTitle("今天吃什么？")
                .setMessage("每天菜单会自动刷新。点「转一下」选定今日菜品，然后点「看看这道菜怎么做」查看食谱；点「换背景」还可以用你自己的照片当背景。")
                .setPositiveButton("知道了", null)
                .create();
            try {
                d.setOnShowListener(dialog -> {
                    d.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#E0452F"));
                });
            } catch (Exception ignored) {}
            d.show();
            getSharedPreferences("food_wheel_prefs", MODE_PRIVATE).edit().putBoolean("first_run_seen", true).apply();
        }
    }

    private void setupWheelEvents() {
        wheel.setOnSpinFinish(new FoodWheelView.OnSpinFinish() {
            @Override public void onSpinStart() {
                spinButton.setEnabled(false);
                spinButton.setAlpha(0.6f);
                resultText.setText("命运正在转盘上奔跑…");
            }
            @Override public void onSpinFinish(int sector) {
                spinButton.setEnabled(true);
                spinButton.setAlpha(1f);
                selectedDishIndex = DailySelector.getDishIndex(sector);
                resultText.setText(FoodData.EMOJI[selectedDishIndex] + " 今天决定：吃 " + FoodData.NAMES[selectedDishIndex] + "！");
                detailButton.setEnabled(true);
                detailButton.setAlpha(1f);
                wheel.animate().scaleX(1.02f).scaleY(1.02f).setDuration(140).withEndAction(() -> wheel.animate().scaleX(1f).scaleY(1f).setDuration(140).start()).start();
            }
        });
        spinButton.setOnClickListener(v -> {
            if (soundPool != null) soundPool.play(chainSoundId, 0.6f, 0.6f, 1, 0, 1.0f);
            int target = (int)(Math.random() * 16);
            wheel.spinTo(target, 7, 4200);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wheel != null) wheel.setDishes(DailySelector.getDailyOrder());
        if (quoteView != null) quoteView.setText("“ " + DailySelector.getDailyQuote() + " ”");
        if (dateView != null) dateView.setText("每天自动刷新菜品，今天要好好吃饭");
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("rotation", wheel != null ? wheel.getRotation() : savedRotation);
        outState.putInt("dish", selectedDishIndex);
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle state) {
        super.onRestoreInstanceState(state);
        savedRotation = state.getFloat("rotation", savedRotation);
        selectedDishIndex = state.getInt("dish", -1);
        if (selectedDishIndex >= 0) {
            resultText.setText(FoodData.EMOJI[selectedDishIndex] + " 今天决定：吃 " + FoodData.NAMES[selectedDishIndex] + "！");
            detailButton.setEnabled(true);
            detailButton.setAlpha(1f);
        }
    }

    @Override
    protected void onDestroy() {
        savedRotation = wheel != null ? wheel.getRotation() : savedRotation;
        super.onDestroy();
    }

    private void applyBackground(ImageView bg) {
        String type = AppPrefs.bgType(this);
        String asset = AppPrefs.bgAsset(this);
        String photo = AppPrefs.bgPhoto(this);
        BackgroundUtil.setListViewBackground(bg, type, asset, photo, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 41 && resultCode == RESULT_OK) {
            // Apply background directly without recreating the activity
            applyBackground(bgImageView);
        }
    }

    private void initSoundPool() {
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attrs)
                .build();
            AssetFileDescriptor afd = getAssets().openFd("chain_sound.wav");
            chainSoundId = soundPool.load(afd, 1);
            afd.close();
        } catch (Exception e) {
            soundPool = null;
        }
    }

    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density * v); }
    private int[] getDisplaySize() {
        android.graphics.Point p = new android.graphics.Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        return new int[]{p.x, p.y};
    }
}



