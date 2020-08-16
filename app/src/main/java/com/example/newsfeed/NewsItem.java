package com.example.newsfeed;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsItem {

    private String mId;
    private String mSectionName;
    private String mDate;
    private String mWebTitle;
    private String mWebUrl;
    private String mStandFirst;
    private String mByline;
    private String mBody;
    private int mWordCount;
    private Drawable mThumbnail;
    private String mTags;
    private final String LOG_TAG = NewsItem.class.getName();

    public NewsItem(
            String id,
            String sectionName,
            String date,
            String webTitle,
            String webUrl,
            String standFirst,
            String byline,
            String body,
            int wordCount,
            Drawable thumbnail,
            String tags) {
        mId = id;
        mSectionName = sectionName;
        mWebTitle = webTitle;
        mWebUrl = webUrl;
        mStandFirst = standFirst;
        mByline = byline;
        mBody = body;
        mWordCount = wordCount;
        mThumbnail = thumbnail;
        mTags = tags;

        mDate = "";
        try {
            SimpleDateFormat dateFormatUK = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            SimpleDateFormat dateFormatUS = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date newsDate = dateFormatUK.parse(date.substring(0,10));
            if (newsDate != null) {
                mDate = dateFormatUS.format(newsDate);
            }
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Problem parsing date: " + e);
        }
    }

    public String getId() {
        return mId;
    }

    public String getWebtitle() {
        return mWebTitle;
    }

    public String getByline() {
        return mByline;
    }

    public String getDate() {
        return mDate;
    }

    public String getBody() {
        return mBody;
    }

    public int getWordCount() {
        return mWordCount;
    }

    public String getStandFirst() {
        return mStandFirst;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public Drawable getThumbnail() {
        return mThumbnail;
    }

    public String getTags() {
        return mTags;
    }
}
