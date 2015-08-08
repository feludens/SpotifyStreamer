package com.spadatech.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Felipe on 7/12/2015.
 */
public class ArtistResults implements Parcelable {

    private String name;
    private String artistId;
    private String coverImg;

    public ArtistResults() {
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



    protected ArtistResults(Parcel in) {
        name = in.readString();
        artistId = in.readString();
        coverImg = in.readString();
    }

    public static final Creator<ArtistResults> CREATOR = new Creator<ArtistResults>() {
        @Override
        public ArtistResults createFromParcel(Parcel in) {
            return new ArtistResults(in);
        }

        @Override
        public ArtistResults[] newArray(int size) {
            return new ArtistResults[size];
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
