package com.example.newsfeed;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final String LOG_TAG = Utils.class.getName();

    private Utils() {
    }

    public static List<NewsItem> getNewsData(String requestUrl) {
        //Create URL object
        URL url = createUrl(requestUrl);

        //Perform HTTP request
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making HTTP request", e);
        }

        //Extract NewsItems
        return extractNewsItemsFromJson(jsonResponse);
    }

    private static URL createUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building URL", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        //If URL is null, return here and don't proceed
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(1000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            //If response code is 200 proceed
            if(httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + httpURLConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving news results", e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
        }
        return stringBuilder.toString();
    }

    private static List<NewsItem> extractNewsItemsFromJson(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        //Create empty list to add NewsItems to
        List<NewsItem> newsItems = new ArrayList<>();

        //JSON parsing - all within try-catch because may throw exception
        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse).optJSONObject("response");
            if (baseJsonResponse != null) {
                JSONArray newsItemArray = baseJsonResponse.optJSONArray("results");
                if (newsItemArray != null) {
                    for (int i = 0; i < newsItemArray.length(); i++) {
                        JSONObject newsItem = newsItemArray.optJSONObject(i);
                        JSONObject fields = newsItem.getJSONObject("fields");
                        JSONArray tags = fields.optJSONArray("tags");
                        String tagString = "";
                        StringBuilder concatTags = new StringBuilder();
                        if (tags != null) {
                            for (int j = 0; j < tags.length(); j++) {
                                JSONObject tag = tags.getJSONObject(j);
                                concatTags.append(tag.getString("webTitle"));
                                if (j + 1 < tags.length()) {
                                    concatTags.append(",");
                                }
                            }
                            tagString = concatTags.toString();
                        }
                        Drawable thumbnail = null;
                        String thumbnailUrl = fields.optString("thumbnail");
                        if (!TextUtils.isEmpty(thumbnailUrl)) {
                            try {
                                InputStream inputStream =
                                        (InputStream) new URL(thumbnailUrl).getContent();
                                thumbnail = Drawable.createFromStream(
                                        inputStream, "Guardian");
                            } catch (MalformedURLException e) {
                                Log.e(LOG_TAG, "Problem getting image (Malformed): " + e);
                            } catch (IOException e) {
                                Log.e(LOG_TAG, "Problem getting image (IO): " + e);
                            }
                        }
                        newsItems.add(new NewsItem(
                                newsItem.optString("id"),
                                newsItem.optString("sectionName"),
                                newsItem.optString("webPublicationDate"),
                                newsItem.optString("webTitle"),
                                newsItem.optString("webUrl"),
                                fields.optString("trailText"),
                                fields.optString("byline"),
                                fields.optString("bodyText"),
                                fields.optInt("wordcount"),
                                thumbnail,
                                tagString));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing JSON results");
        }
        return newsItems;
    }
}
