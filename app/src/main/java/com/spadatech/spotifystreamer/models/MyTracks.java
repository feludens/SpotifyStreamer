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

    public MyTracks() {
    }

    ////////////////
    //  Getters  //
    ///////////////

    public String getName() { return name; }

    public String getAlbum() { return album; }

    public String getCoverImg() {
        return coverImg;
    }

    ////////////////
    //  Setters  //
    ///////////////

    public void setName(String name) { this.name = name; }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    //////////////////////////
    //  Parcelable Methods  //
    //////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(coverImage, flags);
        dest.writeString(album);
        dest.writeString(coverImg);
    }

    protected MyTracks(Parcel in) {
        name = in.readString();
        coverImage = in.readParcelable(Bitmap.class.getClassLoader());
        album = in.readString();
        coverImg = in.readString();
    }

    public static final Creator<MyTracks> CREATOR = new Creator<MyTracks>() {
        @Override
        public MyTracks createFromParcel(Parcel in) {
            return new MyTracks(in);
        }

        @Override
        public MyTracks[] newArray(int size) {
            return new MyTracks[size];
        }
    };
}
