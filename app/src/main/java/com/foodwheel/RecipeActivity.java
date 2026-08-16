package com.foodwheel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class RecipeActivity extends Activity {
    private ImageView bgImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (android.os.Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(true);

        final int dish = getIntent().getIntExtra("dish", 0);
        FrameLayout root = new FrameLayout(this);

        bgImage = new ImageView(this);
        bgImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(bgImage, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        View scrim = new View(this);
        scrim.setBackgroundResource(R.drawable.bg_recipe_scrim);
        root.addView(scrim, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(30));
        scroll.addView(content);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button back = new Button(this);
        back.setText("‹ 返回");
        back.setTextSize(16);
        back.setTextColor(Color.WHITE);
        back.setBackgroundResource(R.drawable.bg_button_ghost);
        header.addView(back, new LinearLayout.LayoutParams(dp(96), dp(48)));

        TextView spacer = new TextView(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        header.addView(spacer);

        Button bg = new Button(this);
        bg.setText("🎨 换背景");
        bg.setTextSize(15);
        bg.setTextColor(Color.parseColor("#5A2E1B"));
        bg.setBackgroundResource(R.drawable.bg_button_ghost);
        header.addView(bg, new LinearLayout.LayoutParams(dp(110), dp(44)));
        bg.setOnClickListener(v -> startActivityForResult(new Intent(this, BackgroundActivity.class), 71));
        back.setOnClickListener(v -> finish());

        LinearLayout card = buildRecipeCard(dish);
        LinearLayout.LayoutParams lpCard = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpCard.topMargin = dp(18);
        content.addView(card, lpCard);

        TextView quote = new TextView(this);
        quote.setText("“ " + DailySelector.getDailyQuote() + " ”");
        quote.setTextSize(15);
        quote.setTextColor(Color.parseColor("#5A2E1B"));
        quote.setBackgroundResource(R.drawable.bg_quote_card);
        quote.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams lpQuote = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpQuote.topMargin = dp(18);
        content.addView(quote, lpQuote);

        root.addView(scroll, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);
        applyBackground();
    }

    private LinearLayout buildRecipeCard(int dish) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(24), dp(20), dp(22));
        card.setBackgroundResource(R.drawable.bg_card);

        TextView emoji = new TextView(this);
        emoji.setTextSize(58);
        emoji.setGravity(Gravity.CENTER);
        emoji.setText(FoodData.EMOJI[dish]);
        card.addView(emoji);

        TextView name = new TextView(this);
        name.setTextSize(30);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        name.setTextColor(Color.parseColor("#3A2417"));
        name.setText(FoodData.NAMES[dish]);
        card.addView(name);

        TextView desc = new TextView(this);
        desc.setTextSize(16);
        desc.setGravity(Gravity.CENTER);
        desc.setTextColor(Color.parseColor("#6A4A3A"));
        desc.setText(FoodData.DESC[dish]);
        LinearLayout.LayoutParams lpDesc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpDesc.topMargin = dp(10);
        card.addView(desc, lpDesc);

        card.addView(sectionTitle("🛒 食材"));
        for (String ing : FoodData.INGR[dish]) card.addView(ingredientRow(ing));
        card.addView(sectionTitle("🍳 做法"));
        String[] steps = FoodData.STEPS[dish];
        for (int i = 0; i < steps.length; i++) card.addView(stepRow(i + 1, steps[i]));
        card.addView(sectionTitle("✨ 小贴士"));
        for (String tip : FoodData.TIPS[dish]) card.addView(stepRow(0, tip));
        return card;
    }

    private TextView sectionTitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(20);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(Color.parseColor("#C0392B"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(20);
        t.setLayoutParams(lp);
        return t;
    }

    private TextView ingredientRow(String ing) {
        TextView t = new TextView(this);
        t.setText("• " + ing);
        t.setTextSize(16);
        t.setTextColor(Color.parseColor("#4A3225"));
        t.setPadding(dp(2), dp(5), 0, dp(5));
        return t;
    }

    private TextView stepRow(int index, String step) {
        TextView t = new TextView(this);
        t.setText(index > 0 ? (index + ". " + step) : ("⭐ " + step));
        t.setTextSize(16);
        t.setTextColor(Color.parseColor("#4A3225"));
        t.setLineSpacing(0f, 1.2f);
        t.setPadding(dp(2), dp(5), 0, dp(5));
        return t;
    }

    private void applyBackground() {
        String type = AppPrefs.bgType(this);
        String asset = AppPrefs.bgAsset(this);
        String photo = AppPrefs.bgPhoto(this);
        BackgroundUtil.setListViewBackground(bgImage, type, asset, photo, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 71 && resultCode == RESULT_OK) applyBackground();
    }

    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density * v); }
}

