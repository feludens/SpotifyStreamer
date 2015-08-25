package com.spadatech.spotifystreamer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.activities.TopTracksActivity;
import com.spadatech.spotifystreamer.adapters.AlbumsArrayAdapter;
import com.spadatech.spotifystreamer.models.Album;
import com.spadatech.spotifystreamer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;

public class AlbumsListFragment extends Fragment {
    private static final String LOG_TAG = AlbumsListFragment.class.getSimpleName();
    private static final String SELECTED_ITEM = "item_position";

    private SpotifyApi spotifyApi = new SpotifyApi();
    private SpotifyService spotifyService = spotifyApi.getService();

    private String query;
    private ListView albumsList;
    private AlbumsArrayAdapter adapter;
    private ArrayList<Album> mArtistResults;
    private OnItemSelectedListener listener;

    private int mItemPosition = ListView.INVALID_POSITION;
    private boolean isOrientationChange = false;
    private boolean mTwoPane;

    public AlbumsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        query = getActivity().getIntent().getStringExtra(Constants.ARTIST_SEARCH_QUERY);
        mTwoPane = getResources().getBoolean(R.bool.dual_pane);

        if (savedInstanceState == null || !savedInstanceState.containsKey("artistResults")) {
            mArtistResults = new ArrayList<>();
            performSearch(query);
            isOrientationChange = false;
        } else {
            mArtistResults = savedInstanceState.getParcelableArrayList("artistResults");
            isOrientationChange = true;
        }
        adapter = new AlbumsArrayAdapter(getActivity(), mArtistResults);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_albums_list, null);

        albumsList = (ListView) view.findViewById(R.id.lv_albums);
        albumsList.setAdapter(adapter);

        albumsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                albumsList.setItemChecked(i, true);
                mItemPosition = i;

                Album album = adapter.getItem(i);
                updateDetail(album);
            }
        });

        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_ITEM)){
            mItemPosition = savedInstanceState.getInt(SELECTED_ITEM);
            albumsList.setItemChecked(mItemPosition, true);
            albumsList.smoothScrollToPosition(mItemPosition);
        }

        return view;
    }

    private void performSearch(String query) {
        spotifyService.searchArtists(query, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, retrofit.client.Response response) {
                List<Artist> listOfArtists = artistsPager.artists.items;

                if (listOfArtists.size() == 0) {
                    Toast.makeText(getActivity(), "Ops, no albums found. Try again!", Toast.LENGTH_LONG).show();
                }else {

                    for (Artist element : listOfArtists) {
                        Album album = new Album();
                        album.setName(element.name);
                        album.setArtistId(element.id);
                        if (element.images.size() > 0) {
                            String url = element.images.get(0).url;
                            album.setCoverImg(url);
                        }
                        mArtistResults.add(album);
                    }
                    adapter.notifyDataSetChanged();
                    if(mTwoPane && !isOrientationChange){
                        albumsList.setItemChecked(0, true);
                        updateDetail(adapter.getItem(0));
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Ops, no albums found. Try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artistResults", mArtistResults);
        outState.putString(Constants.ARTIST_SEARCH_QUERY, query);
        if(mItemPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_ITEM, mItemPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public interface OnItemSelectedListener {
        public void onAlbumSelected(Intent intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnItemSelectedListener){
            listener = (OnItemSelectedListener) activity;
        }else {
            throw new ClassCastException(activity.toString()
                    + " must implemenet AlbumsListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void updateDetail(Album album) {
        Intent intent = new Intent(getActivity(), TopTracksActivity.class);
        intent.putExtra(Constants.ALBUM, album);
        intent.putExtra("artistId", album.getArtistId());
        intent.putExtra("artistName", query.toUpperCase());

        // inform the Activity about the change
        listener.onAlbumSelected(intent);
    }
}
