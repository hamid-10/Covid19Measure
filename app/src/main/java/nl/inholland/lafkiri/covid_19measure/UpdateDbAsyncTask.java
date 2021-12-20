package nl.inholland.lafkiri.covid_19measure;

import android.content.Context;
import android.os.AsyncTask;
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

import static android.content.ContentValues.TAG;

public class UpdateDbAsyncTask extends AsyncTask<Void, Void, Void> {

    private CountryStatisticsViewModel countryStatisticsViewModel;

    public UpdateDbAsyncTask(CountryStatisticsViewModel countryStatisticsViewModel) {
        this.countryStatisticsViewModel = countryStatisticsViewModel;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: process in background");
        updateData(countryStatisticsViewModel);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Log.d(TAG, "onPostExecute: data has been updated");
    }

    //  Fill the database with the fetched data...
    private static void updateData (CountryStatisticsViewModel countryStatisticsViewModel) { ;
        Log.d(TAG, "fillWithData: getting new data");
        JSONArray stats = loadJSONArray();

        try {
            for (int i = 0; i < stats.length(); i++) {
                JSONObject countryStats = stats.getJSONObject(i);
                String countryState = countryStats.getString("name");
                String totalCases = String.valueOf(countryStats.getInt("cases"));
                String recovered = String.valueOf(countryStats.getInt("recovered"));
                String deaths = String.valueOf(countryStats.getInt("deaths"));
                String activeCases = String.valueOf(countryStats.getInt("active"));
//              get coordinates array, and get latitude and longitude separately
                JSONArray coordinates = countryStats.getJSONArray("coordinates");
                double latitude = coordinates.getDouble(1);
                double longitude = coordinates.getDouble(0);
//                JSONArray sources = countryStats.getJSONArray("sources");
                JSONObject source = countryStats.getJSONArray("sources").getJSONObject(0);
                String sourceName = source.getString("name");
                
                countryStatisticsViewModel.update(new CountryStatistics(countryState, totalCases, recovered, deaths, activeCases, latitude, longitude, sourceName));


            }

        } catch (JSONException e) {

        }
    }

    private static JSONArray loadJSONArray () {
        Log.d(TAG, "loadJSONArray: loading data");
//        StringBuilder builder = new StringBuilder();
        String data = "";

        try {
//          on Swipe up the data is fetched again from the CoronaScrapper app, and the data are updated on db
            URL url = new URL("https://raw.githubusercontent.com/hamid-10/covid-19-data/master/data.json");
//          URL url = new URL("https://coronadatascraper.com/data.json");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while (line != null) {
                line = bufferedReader.readLine();
                data = data + line;
            }

            JSONArray jsonArray = new JSONArray(data);

            return jsonArray;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
