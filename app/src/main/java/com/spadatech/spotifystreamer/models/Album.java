package com.spadatech.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Felipe on 7/12/2015.
 */
public class Album implements Parcelable {

    private String name;
    private String artistId;
    private String coverImg;

    public Album() {
    }

    ////////////////
    //  Getters  //
    ///////////////


    public String getName() { return name; }

    public String getArtistId() { return artistId; }

    public String getCoverImg() { return coverImg; }

    ////////////////
    //  Setters  //
    ///////////////

    public void setName(String name) { this.name = name; }

    public void setArtistId(String artistId) { this.artistId = artistId; }

    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    //////////////////////////
    //  Parcelable Methods  //
    //////////////////////////



    protected Album(Parcel in) {
        name = in.readString();
        artistId = in.readString();
        coverImg = in.readString();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artistId);
        dest.writeString(coverImg);
    }

}
