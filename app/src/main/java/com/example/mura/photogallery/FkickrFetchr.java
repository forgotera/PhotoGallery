package com.example.mura.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FkickrFetchr {
    private static final String API_KEY = "4026ac3df7e660af33f802bb39a470a3";
    private static final String TAG = "FlickrFetchr";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSec) throws IOException {
        URL url = new URL(urlSec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() +  ": with "+
                        urlSec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }

        }

        public String getUrlString(String urlSpec) throws IOException {
            return new String(getUrlBytes(urlSpec));
        }

    public List<GalleryItem> downloadGalleryItems(String uri) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(uri);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }
        return items;
    }



    private String buildUri(String method, String query){
        Uri.Builder builder = ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);
                if (method.equals(SEARCH_METHOD)){
                    builder.appendQueryParameter("text",query);
                }
                return builder.build().toString();
    }
    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUri(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUri(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }


    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}



