package com.spadatech.spotifystreamer.helpers;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.spadatech.spotifystreamer.activities.SpotyMain;

import java.io.InputStream;
import java.net.URL;

public class CoverImageLoader extends AsyncTask<String, String, Bitmap> {
    ProgressDialog pDialog;
    Bitmap coverImage;
    SpotyMain mainActivity;

    public void setMainActivity(SpotyMain activity){
        this.mainActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(mainActivity);
        pDialog.setMessage("Loading Image ....");
        pDialog.show();

    }
    protected Bitmap doInBackground(String... args) {
        try {
            coverImage = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return coverImage;
    }

    protected void onPostExecute(Bitmap image) {
        if(image != null){
            pDialog.dismiss();
            //mainActivity.tempCoverImage = coverImage;
        }else{
            pDialog.dismiss();
        }
    }
}