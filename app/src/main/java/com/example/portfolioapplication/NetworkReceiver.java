package com.example.portfolioapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isInternetAvailable(context)) {
            uploadLocationsToServer(context);
        }
    }

    private void uploadLocationsToServer(Context context) {
        // Fetch location data from the local database
        SQLiteHelper sqliteHelper = new SQLiteHelper(context.getApplicationContext());
        List<Location> locations = sqliteHelper.getAllLocations();

        // Execute the upload task in the background
        new UploadDataTask().execute(locations);
    }

    private boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }


    // AsyncTask to upload data in the background
    private static class UploadDataTask extends AsyncTask<List<Location>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(List<Location>... lists) {
            List<Location> locations = lists[0];
            boolean success = false;

            // Server URL for the mock API
            String serverUrl = "https://webhook.site/db5f594c-b37a-4746-aa83-da975dd39ceb";
            HttpURLConnection connection = null;
            try {
                // Create JSON array to hold location data
                JSONArray locationsArray = new JSONArray();

                // Iterate over the locations and build the JSON objects
                for (Location location : locations) {
                    JSONObject locationObject = new JSONObject();
                    locationObject.put("latitude", location.getLatitude());
                    locationObject.put("longitude", location.getLongitude());
                    locationObject.put("timestamp", location.getTimestamp());
                    locationsArray.put(locationObject);
                }

                // Create the final JSON object
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("locations", locationsArray);

                // Convert the JSON payload to string
                String jsonString = jsonPayload.toString();

                // Send data to the server
                connection = (HttpURLConnection) new URL(serverUrl).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Send the request body
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Log the request
                Log.d("NetworkReceiver", "Request: " + jsonString);

                // Get the response code to check if the upload was successful
                int responseCode = connection.getResponseCode();
                Log.d("NetworkReceiver", "Response Code: " + responseCode);

                // Capture the response
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.d("NetworkReceiver", "Response: " + response.toString());
                    success = true;
                } else {
                    Log.e("NetworkReceiver", "Failed with response code: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("NetworkReceiver", "Error uploading locations", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("NetworkReceiver", "Data uploaded successfully");
            } else {
                Log.d("NetworkReceiver", "Failed to upload data");
            }
        }
    }


}
