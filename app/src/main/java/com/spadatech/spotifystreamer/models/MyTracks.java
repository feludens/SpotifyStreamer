package com.spadatech.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Felipe on 7/13/2015.
 */
public class MyTracks implements Parcelable{

    private String name;
    private String artist;
    private String album;
    private String coverImgs;
    public String preview_url;

    public MyTracks() {
    }

    ////////////////
    //  Getters  //
    ///////////////

    public String getName() {
        return name;
    }

    public String getAlbum() {
        return album;
    }

    public String getCoverImgs() {
        return coverImgs;
    }

    public String getArtist() {
        return artist;
    }

    public String getPreview_url() {
        return preview_url;
    }

    ////////////////
    //  Setters  //
    ///////////////

    public void setName(String name) {
        this.name = name;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setCoverImgs(String coverImgs) {
        this.coverImgs = coverImgs;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    //////////////////////////
    //  Parcelable Methods  //
    //////////////////////////

    protected MyTracks(Parcel in) {
        name = in.readString();
        artist = in.readString();
        album = in.readString();
        coverImgs = in.readString();
        preview_url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(coverImgs);
        dest.writeString(preview_url);
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
