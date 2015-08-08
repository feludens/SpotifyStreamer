package com.spadatech.spotifystreamer.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Felipe on 7/13/2015.
 */
public class MyTracks implements Parcelable {

    private String name;
    private Bitmap coverImage;
    private String album;
    private String coverImg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Bitmap coverImage) {
        this.coverImage = coverImage;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeParcelable(this.coverImage, 0);
        dest.writeString(this.album);
    }

    public MyTracks() {
    }

    protected MyTracks(Parcel in) {
        this.name = in.readString();
        this.coverImage = in.readParcelable(Bitmap.class.getClassLoader());
        this.album = in.readString();
    }

    public static final Creator<MyTracks> CREATOR = new Creator<MyTracks>() {
        public MyTracks createFromParcel(Parcel source) {
            return new MyTracks(source);
        }

        public MyTracks[] newArray(int size) {
            return new MyTracks[size];
        }
    };
}
