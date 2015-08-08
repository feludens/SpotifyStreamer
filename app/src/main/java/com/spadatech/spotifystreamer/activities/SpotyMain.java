package com.spadatech.spotifystreamer.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.adapters.ArtistArrayAdapter;
import com.spadatech.spotifystreamer.models.ArtistResults;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;

public class SpotyMain extends AppCompatActivity {

    //Layout Elements
    private EditText etSearchArtist;
    private Toolbar toolbar;

    //private Menu menu;
    private ImageButton btnSearch;

    //SPOTIFY related variables
    private SpotifyApi spotifyApi = new SpotifyApi();
    private SpotifyService spotifyService = spotifyApi.getService();

    //Data variables
    ArrayList<ArtistResults> mArtistResults;
    ArrayList<ArtistResults> mTopTracks;
    private String previousSearchQuery;

    //Others
    private ListView albumsList;
    ProgressDialog pDialog;
    private ArtistArrayAdapter adapter;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spoty_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        albumsList = (ListView) findViewById(R.id.lvAlbums);
        etSearchArtist = (EditText) findViewById(R.id.etSearchArtist);

        if(savedInstanceState == null || !savedInstanceState.containsKey("artistResults")) {
            mArtistResults = new ArrayList<ArtistResults>();
        }
        else {
            mArtistResults = savedInstanceState.getParcelableArrayList("artistResults");
            etSearchArtist.setVisibility(View.VISIBLE);
        }

        if(savedInstanceState == null || !savedInstanceState.containsKey("query")) {
            previousSearchQuery = "";
        }
        else {
            previousSearchQuery = savedInstanceState.getString("query");
        }

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        adapter = new ArtistArrayAdapter(getApplicationContext(), mArtistResults);
        albumsList.setAdapter(adapter);

        btnSearch = (ImageButton) findViewById(R.id.btnSearch);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading Image ....");

        etSearchArtist.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    prepareForSearch(btnSearch);
                    return true;
                }
                return false;
            }
        });

        albumsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArtistResults album = adapter.getItem(i);
                Intent intent = new Intent(getApplicationContext(), TopTracksActivity.class);
                intent.putExtra("artistId", album.getArtistId());
                intent.putExtra("artistName", previousSearchQuery.toUpperCase());
                startActivity(intent);
            }
        });
    }

    public void clearSearchBar(View view){
        etSearchArtist.setText("");
    }

    public void prepareForSearch(View view){
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if(!etSearchArtist.getText().toString().equals(previousSearchQuery)){
            mArtistResults.clear();
            adapter.clear();
            //adapter.notifyDataSetChanged();
            previousSearchQuery = etSearchArtist.getText().toString();
            performSearch(previousSearchQuery);
        }
    }

    private void performSearch(String searchString) {
        spotifyService.searchArtists(searchString, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, retrofit.client.Response response) {
                List<Artist> listOfArtists = artistsPager.artists.items;

                if(listOfArtists.size() == 0){
                    Toast.makeText(getApplicationContext(), "Ops, no albums found. Try again!", Toast.LENGTH_LONG).show();
                }

                for (Artist element : listOfArtists) {
                    ArtistResults artistResults = new ArtistResults();
                    artistResults.setName(element.name);
                    artistResults.setArtistId(element.id);
                    if (element.images.size() > 0) {
                        String url = element.images.get(0).url;
                        artistResults.setCoverImg(url);
                    }
                    mArtistResults.add(artistResults);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Ops, no albums found. Try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artistResults", mArtistResults);
        outState.putString("query", previousSearchQuery);
        super.onSaveInstanceState(outState);
    }

}
