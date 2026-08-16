package com.foodwheel;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class BackgroundUtil {
    private BackgroundUtil() {}

    public static BitmapFactory.Options boundsOptions() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        return o;
    }

    public static Bitmap loadScaled(InputStream in, int screenW, int screenH) {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] tmp = new byte[16384];
            int n;
            while ((n = in.read(tmp)) != -1) bos.write(tmp, 0, n);
            byte[] bytes = bos.toByteArray();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            int dw = bounds.outWidth;
            int dh = bounds.outHeight;
            int sample = 1;
            while (dw / sample > screenW * 2 || dh / sample > screenH * 2) sample *= 2;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = sample;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap loadAssetBitmapScaled(Context ctx, String asset, int maxW, int maxH) {
        try {
            InputStream in = ctx.getAssets().open("wallpapers/" + asset);
            byte[] bytes = readAll(in);
            if (in != null) in.close();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            int sample = 1;
            while (bounds.outWidth / sample > maxW || bounds.outHeight / sample > maxH) sample *= 2;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = sample;
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] readAll(InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] tmp = new byte[16384];
        int n;
        while ((n = in.read(tmp)) != -1) bos.write(tmp, 0, n);
        return bos.toByteArray();
    }

    public static Bitmap centerCropBitmap(Bitmap src, int targetW, int targetH) {
        if (src == null) return null;
        float sx = targetW / (float) src.getWidth();
        float sy = targetH / (float) src.getHeight();
        float scale = Math.max(sx, sy);
        int outW = Math.round(src.getWidth() * scale);
        int outH = Math.round(src.getHeight() * scale);
        Matrix m = new Matrix();
        m.postScale(scale, scale);
        m.postTranslate((targetW - outW) / 2f, (targetH - outH) / 2f);
        Bitmap result = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c = new android.graphics.Canvas(result);
        c.drawColor(0xFF201A17);
        c.drawBitmap(src, m, null);
        if (result != src && !src.isRecycled()) src.recycle();
        return result;
    }

    public static void setListViewBackground(View root, String type, String asset, String photoPath, Context ctx) {
        if (root == null || ctx == null) return;
        Bitmap bmp = null;
        boolean photoMode = "photo".equals(type) && photoPath != null && !photoPath.isEmpty();
        if (photoMode) {
            File f = new File(photoPath);
            if (f.exists()) bmp = loadBitmapFile(f, ctx);
            if (bmp == null && android.os.Build.VERSION.SDK_INT >= 28) {
                try {
                    android.graphics.ImageDecoder.Source src = android.graphics.ImageDecoder.createSource(f);
                    bmp = android.graphics.ImageDecoder.decodeBitmap(src);
                } catch (Exception ignored) {}
            }
        }
        if (bmp == null) {
            if (photoMode) {
                android.widget.Toast.makeText(ctx, "照片背景加载失败，请换一张试试", android.widget.Toast.LENGTH_SHORT).show();
                bmp = loadAssetBitmap(ctx, currentAsset(ctx));
            } else {
                bmp = loadAssetBitmap(ctx, asset);
            }
        }
        if (bmp == null && !asset.isEmpty()) {
            bmp = loadAssetBitmap(ctx, currentAsset(ctx));
        }
        if (bmp != null) {
            if (root instanceof android.widget.ImageView) {
                ((android.widget.ImageView) root).setImageBitmap(bmp);
                root.invalidate();
            } else {
                root.setBackground(new android.graphics.drawable.BitmapDrawable(ctx.getResources(), bmp));
            }
        }
    }

    public static Bitmap loadBitmapFile(File f, Context ctx) {
        try (InputStream in = new java.io.FileInputStream(f)) {
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            return loadScaled(in, dm.widthPixels, dm.heightPixels);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap loadAssetBitmap(Context ctx, String asset) {
        try {
            InputStream in = ctx.getAssets().open("wallpapers/" + asset);
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            Bitmap bmp = loadScaled(in, dm.widthPixels, dm.heightPixels);
            in.close();
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    public static String copyUserPhoto(Context ctx, InputStream in) {
        if (in == null) return null;
        try {
            File dir = new File(ctx.getFilesDir(), "wallpapers");
            if (!dir.exists()) dir.mkdirs();
            File target = new File(dir, "user_photo.jpg");
            File tmp = new File(dir, "user_" + System.currentTimeMillis() + ".jpg");
            try (OutputStream os = new FileOutputStream(tmp)) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) != -1) os.write(buf, 0, n);
            }
            if (target.exists() && !target.delete()) target.deleteOnExit();
            boolean ok = tmp.renameTo(target);
            if (!ok) {
                try (InputStream is = new java.io.FileInputStream(tmp);
                     OutputStream os = new FileOutputStream(target)) {
                    byte[] buf = new byte[16384];
                    int n;
                    while ((n = is.read(buf)) != -1) os.write(buf, 0, n);
                }
                tmp.delete();
                ok = target.exists() && target.length() > 0;
            }
            if (!ok) return null;
            return target.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] listWallpapers(Context ctx) {
        try {
            AssetManager am = ctx.getAssets();
            String[] list = am.list("wallpapers");
            java.util.ArrayList<String> out = new java.util.ArrayList<>();
            for (String s : list) if (s.toLowerCase(java.util.Locale.US).endsWith(".jpg")) out.add(s);
            return out.toArray(new String[0]);
        } catch (Exception e) {
            return new String[]{"wallpaper_01.jpg"};
        }
    }

    public static String currentAsset(Context ctx) {
        String a = AppPrefs.bgAsset(ctx);
        if (a.isEmpty()) a = "wallpaper_01.jpg";
        return a;
    }
}

