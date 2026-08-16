package com.foodwheel;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class BackgroundActivity extends Activity {
    private static final int PICK_PHOTO = 88;
    private Handler handler = new Handler();
    private GridView grid;
    private String[] wallpapers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (android.os.Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(18));
        root.setBackgroundColor(Color.parseColor("#FFF8EF"));

        TextView title = new TextView(this);
        title.setText("🎨 设置背景");
        title.setTextSize(28);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#3A2417"));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("选择一款内置美食壁纸，或用自己的照片做背景");
        sub.setTextSize(14);
        sub.setTextColor(Color.parseColor("#8A6A56"));
        LinearLayout.LayoutParams lpSub = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpSub.topMargin = dp(6);
        root.addView(sub, lpSub);

        Button photo = new Button(this);
        photo.setText("📷 使用我的照片");
        photo.setTextSize(17);
        photo.setTextColor(Color.WHITE);
        photo.setBackgroundResource(R.drawable.bg_button_primary);
        LinearLayout.LayoutParams lpPhoto = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        lpPhoto.topMargin = dp(18);
        root.addView(photo, lpPhoto);
        photo.setOnClickListener(v -> pickPhoto());

        grid = new GridView(this);
        grid.setNumColumns(2);
        grid.setVerticalSpacing(dp(12));
        grid.setHorizontalSpacing(dp(12));
        grid.setPadding(0, dp(14), 0, dp(10));
        LinearLayout.LayoutParams lpGrid = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(grid, lpGrid);

        wallpapers = BackgroundUtil.listWallpapers(this);
        grid.setAdapter(new WallpaperAdapter());
        setContentView(root);
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        try {
            startActivityForResult(intent, PICK_PHOTO);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开相册", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            handlePhoto(data.getData());
        }
    }

    private void handlePhoto(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            String path = BackgroundUtil.copyUserPhoto(this, in);
            if (in != null) in.close();
            if (path == null) {
                Toast.makeText(this, "照片读取失败，请换一张试试", Toast.LENGTH_SHORT).show();
                return;
            }
            AppPrefs.applyPhoto(this, path);
            Toast.makeText(this, "已应用你的照片", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "照片读取失败，请换一张试试", Toast.LENGTH_SHORT).show();
        }
    }

    private class WallpaperAdapter extends android.widget.BaseAdapter {
        @Override public int getCount() { return wallpapers.length; }
        @Override public Object getItem(int position) { return wallpapers[position]; }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String name = wallpapers[position];
            final ImageView iv;
            if (convertView instanceof ImageView) {
                iv = (ImageView) convertView;
            } else {
                iv = new ImageView(BackgroundActivity.this);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setAdjustViewBounds(false);
                iv.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));
                iv.setPadding(dp(2), dp(2), dp(2), dp(2));
                iv.setBackgroundResource(R.drawable.bg_card);
            }
            String current = AppPrefs.bgAsset(BackgroundActivity.this);
            if (name.equals(current) && "asset".equals(AppPrefs.bgType(BackgroundActivity.this))) {
                iv.setForeground(new android.graphics.drawable.ColorDrawable(Color.parseColor("#66E0452F")));
            } else {
                iv.setForeground(null);
            }
            final int p = position;
            handler.postDelayed(() -> {
                Bitmap bmp = BackgroundUtil.loadAssetBitmapScaled(BackgroundActivity.this, name, 240, 360);
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                    iv.setTag(name);
                }
            }, p * 90L);
            iv.setOnClickListener(v -> {
                AppPrefs.applyWallpaper(BackgroundActivity.this, name);
                Toast.makeText(BackgroundActivity.this, "背景已更换", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
            return iv;
        }
    }

    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density * v); }
}

