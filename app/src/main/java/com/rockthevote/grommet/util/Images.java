package com.rockthevote.grommet.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class Images {

    /**
     * transforms the aspect ratio by adding whitespace around the image
     *
     * @param image
     * @param x
     * @param y
     * @return
     */
    public static Bitmap transformAspectRatio(Bitmap image, int x, int y) {

        if (x == y) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        float imageRatio = (float) width / height;
        float aspectRatio = (float) x / y;

        int newHeight;
        int newWidth;

        if (imageRatio > aspectRatio) {
            // add whitespace to image height
            newHeight = (width * y) / x;
            newWidth = width;

        } else {
            // add whitespace to image width
            newHeight = height;
            newWidth = (x * height) / y;
        }

        Bitmap whiteBgBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(whiteBgBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(image, 0, 0, null);
        return whiteBgBitmap;

    }

    public static Bitmap aspectSafeScale(Bitmap image, int maxWidth, int maxHeight) {

        int width = image.getWidth();
        int height = image.getHeight();

        if (width == maxWidth && height == maxHeight) {
            return image;
        }

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int) (height / ratio);

        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int) (width / ratio);

        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
