package com.toxoidandroid.restservice.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.toxoidandroid.restservice.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebServiceUtils {

    private static final String TAG = WebServiceUtils.class.getSimpleName();

    public enum METHOD {
        POST, GET, DELETE
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, boolean hasAuth) {
        return requestJSONObject(serviceUrl, method, headerValues, null, null, hasAuth);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues urlValues, ContentValues bodyValues) {
        return requestJSONObject(serviceUrl, method, null, urlValues, bodyValues, false);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, ContentValues urlValues,
                                               ContentValues bodyValues, boolean hasAuth) {

        HttpURLConnection urlConnection = null;
        try {
            if (urlValues != null) {
                serviceUrl = addParametersToUrl(serviceUrl, urlValues);
            }
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            if (hasAuth) {
                addBasicAuth(urlConnection);
            }
            if (headerValues != null) {
                Uri.Builder builder = new Uri.Builder();
                for (String key: headerValues.keySet()) {
                    builder.appendQueryParameter(key, headerValues.getAsString(key));
                }
                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
            }
            if (bodyValues != null) {
                JSONObject jsonObject = new JSONObject();
                for (String key: bodyValues.keySet()) {
                    jsonObject.put(key, bodyValues.getAsString(key));
                }
                String str = jsonObject.toString();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept","application/json");
                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                osw.write(str);
                osw.flush();
                osw.close();
            }

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
                Log.d(TAG, "Unauthorized Access");
            else if (statusCode != HttpURLConnection.HTTP_OK)
                Log.d(TAG, "URL Response Error");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(in));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d(TAG, ":(");
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return null;
    }

    private static String addParametersToUrl(String url, ContentValues urlValues) {
        StringBuffer stringBuffer = new StringBuffer(url);
        stringBuffer.append("?");
        for (String key: urlValues.keySet()) {
            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append(urlValues.getAsString(key));
            stringBuffer.append("&");
        }
        stringBuffer.replace(stringBuffer.length() - 1, stringBuffer.length() - 1, "");
        return stringBuffer.toString();
    }

    private static void addBasicAuth(HttpURLConnection urlConnection) {
        final String basicAuth = "Basic " + Base64.encodeToString((Constants.APP_KEY +
            ":" + Constants.APP_SECRET).getBytes(), Base64.NO_WRAP);
        urlConnection.setRequestProperty(Constants.AUTHORIZATION, basicAuth);
    }

    private static String convertInputStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseText;
        try {
            while ((responseText = bufferedReader.readLine()) != null){
                stringBuilder.append(responseText);
            }
        } catch (IOException e) {
            Log.d(TAG, "IOException in convertInputStreamToString");
        }
        return stringBuilder.toString();
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return  connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
