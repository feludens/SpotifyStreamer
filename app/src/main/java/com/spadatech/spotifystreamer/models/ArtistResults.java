package com.spadatech.spotifystreamer.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Felipe on 7/12/2015.
 */
public class ArtistResults implements Parcelable {

    private String name;
    private Bitmap albumCover;
    private String artistId;
    private String coverImg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getAlbumCover() {
        return albumCover;
    }

    public void setAlbumCover(Bitmap albumCover) {
        this.albumCover = albumCover;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
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
        dest.writeParcelable(this.albumCover, 0);
        dest.writeString(this.artistId);
    }

    public ArtistResults() {
    }

    protected ArtistResults(Parcel in) {
        this.name = in.readString();
        this.albumCover = in.readParcelable(Bitmap.class.getClassLoader());
        this.artistId = in.readString();
    }

    public static final Creator<ArtistResults> CREATOR = new Creator<ArtistResults>() {
        public ArtistResults createFromParcel(Parcel source) {
            return new ArtistResults(source);
        }

        public ArtistResults[] newArray(int size) {
            return new ArtistResults[size];
        }
    };
}
