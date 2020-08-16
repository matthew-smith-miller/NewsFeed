package com.example.newsfeed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<NewsItem>> {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search";
    private static final int LOADER_ID = 1;
    private NewsItemAdapter mAdapter;
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshNews();
    }

    public void refreshNews (View view) {
        refreshNews();
    }

    private void refreshNews () {
        //Find the ListView and set empty view
        ListView newsItemListView = (ListView) findViewById(R.id.list);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        newsItemListView.setEmptyView(mEmptyStateTextView);

        //Set emptyText to "", hide refresh, show spiner
        mEmptyStateTextView.setText("");
        findViewById(R.id.refresh_button).setVisibility(View.GONE);
        findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);

        //Set up NewsItem adapter and set to ListView + onItemClickListener to go to website
        mAdapter = new NewsItemAdapter(this, new ArrayList<NewsItem>());
        newsItemListView.setAdapter(mAdapter);
        newsItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsItem newsItem = mAdapter.getItem(position);
                if (newsItem != null) {
                    Uri newsItemUri = Uri.parse(newsItem.getWebUrl());
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsItemUri);
                    startActivity(websiteIntent);
                }
            }
        });

        //Check connectivity before launching Loader; if no connection display no connection msg
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                LoaderManager loaderManager = getSupportLoaderManager();
                loaderManager.initLoader(LOADER_ID, null, this);
            } else {
                findViewById(R.id.loading_indicator).setVisibility(View.GONE);
                mEmptyStateTextView.setText(R.string.empty_no_internet_connection);
                findViewById(R.id.refresh_button).setVisibility(View.VISIBLE);
            }
        }
    }

    @NonNull
    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, @Nullable Bundle args) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String number = sharedPreferences.getString(
                getString(R.string.settings_number_key),
                getString(R.string.settings_number_default)
        );
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter(
                getString(R.string.query_format_key),
                getString(R.string.query_format_value));
        uriBuilder.appendQueryParameter(
                getString(R.string.query_api_key_key),
                getString(R.string.query_api_key_value));
        uriBuilder.appendQueryParameter(
                getString(R.string.query_show_fields_key),
                getString(R.string.query_show_fields_value));
        uriBuilder.appendQueryParameter(
                getString(R.string.query_show_tags_key),
                getString(R.string.query_show_tags_value));
        String sectionFilter = sharedPreferences.getString(
                getString(R.string.settings_section_filter_key),
                getString(R.string.settings_section_filter_default));
        Log.d(LOG_TAG, "sectionFilter: " + sectionFilter);
        if (!TextUtils.isEmpty(sectionFilter) &&
                !sectionFilter.equals(getString(R.string.settings_section_filter_default))) {
            uriBuilder.appendQueryParameter(
                    getString(R.string.query_section_key),
                    sectionFilter);
        }

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<NewsItem>> loader, List<NewsItem> newsItems) {
        findViewById(R.id.loading_indicator).setVisibility(View.GONE);
        mAdapter.clear();
        if (newsItems != null && !newsItems.isEmpty()) {
            mAdapter.addAll(newsItems);
        } else {
            mEmptyStateTextView.setText(R.string.empty_no_content_found);
            findViewById(R.id.refresh_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}