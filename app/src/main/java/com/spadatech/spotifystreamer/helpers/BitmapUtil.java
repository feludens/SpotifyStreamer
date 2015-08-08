package com.spadatech.spotifystreamer.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.spadatech.spotifystreamer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by pereirf on 7/14/15.
 */
public enum BitmapUtil {
    INSTANCE;

    //Scale size
    final int REQUIRED_SIZE = 50;
    Context context;

    public Bitmap decodeUrl(String url) {
        try {
            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, options);

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    options.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options newOptions = new BitmapFactory.Options();
            newOptions.inSampleSize = scale;

            return BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, newOptions);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getPlaceholder() {
        // Decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), R.drawable.album_art_missing, options);

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                options.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inSampleSize = scale;

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.album_art_missing, newOptions);

    }

    public void setContext(Context context) {
        this.context = context;
    }
}