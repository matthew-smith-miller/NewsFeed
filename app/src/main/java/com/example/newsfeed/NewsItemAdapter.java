package com.example.newsfeed;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsItemAdapter extends ArrayAdapter<NewsItem> {

    public NewsItemAdapter(Context context, ArrayList<NewsItem> newsItems) {
        super(context, 0, newsItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_card, parent, false);
        }

        //Get the NewsItem located in the position
        NewsItem newsItem = getItem(position);
        if (newsItem != null) {
            ((TextView) convertView.findViewById(R.id.web_title)).setText(newsItem.getWebtitle());
            ((TextView) convertView.findViewById(R.id.byline)).setText(newsItem.getByline());
            ((TextView) convertView.findViewById(R.id.section_name)).setText(newsItem.getSectionName());
            ((TextView) convertView.findViewById(
                    R.id.web_publishing_date)).setText(newsItem.getDate());
            ((TextView) convertView.findViewById(R.id.body)).setText(
                    newsItem.getStandFirst().replace("<br>", "\n").
                            replaceAll("\\<.*?\\>",""));
            Drawable thumbnailDrawable = newsItem.getThumbnail();
            if (thumbnailDrawable != null) {
                ((ImageView) convertView.findViewById(R.id.list_image)).setImageDrawable(
                        thumbnailDrawable);
            } else {
                convertView.findViewById(R.id.list_image).setVisibility(View.GONE);
            }

        }

        return convertView;
    }
}
