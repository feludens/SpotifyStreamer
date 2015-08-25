package com.spadatech.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.fragments.AlbumsListFragment;
import com.spadatech.spotifystreamer.fragments.TopTracksFragment;
import com.spadatech.spotifystreamer.models.Album;
import com.spadatech.spotifystreamer.utils.Constants;

public class AlbumsListActivity extends AppCompatActivity implements AlbumsListFragment.OnItemSelectedListener{

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);

        mTwoPane = getResources().getBoolean(R.bool.dual_pane);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.ARTIST_SEARCH_QUERY).toUpperCase());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_albums_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAlbumSelected(Intent intent) {
        if(mTwoPane){
            Album album = intent.getParcelableExtra(Constants.ALBUM);

            TopTracksFragment topTracksFragment =
                    TopTracksFragment.newInstance(album);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, topTracksFragment).commit();
        }else{
            startActivity(intent);
        }
    }
}
