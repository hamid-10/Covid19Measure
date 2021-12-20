package nl.inholland.lafkiri.covid_19measure;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static android.support.constraint.Constraints.TAG;

//here we add all our entities "tables"
//whenever changes have been made to the database architecture the version number must be increased
@Database(entities = {CountryStatistics.class}, version = 1)
public abstract class CountryStatisticsDatabase extends RoomDatabase {

    private static CountryStatisticsDatabase instance;

    private static Context activity;

    public abstract CountryStatisticsDao countryStatisticsDao();


//  creating the database instance
    public static synchronized CountryStatisticsDatabase getInstance(Context context){

        activity = context.getApplicationContext();

        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    CountryStatisticsDatabase.class, "country_statistics_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallack)
                    .build();
        }
        return instance;
    }
// This will be done when creating the dbs
    private static RoomDatabase.Callback roomCallack = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private CountryStatisticsDao countryStatisticsDao;

        private PopulateDbAsyncTask (CountryStatisticsDatabase db) {
            countryStatisticsDao = db.countryStatisticsDao();
        }

//      This is to populate the db manually for test purposes...
        @Override
        protected Void doInBackground(Void... voids) {
//            Testing with hardware coded data...
//            countryStatisticsDao.insert(new CountryStatistics("Netherlands", 1000, 500, 50));
//            countryStatisticsDao.insert(new CountryStatistics("France", 2000, 600, 60));
//            countryStatisticsDao.insert(new CountryStatistics("Germany", 3000, 700, 70));

            fillWithData(activity);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

//  Fill the database with the fetched data...
    private static void fillWithData (Context context) {
        CountryStatisticsDao countryStatisticsDao = getInstance(context).countryStatisticsDao();

        JSONArray stats = loadJSONArray(context);

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

                countryStatisticsDao.insert(new CountryStatistics(countryState, totalCases, recovered, deaths, activeCases, latitude, longitude, sourceName));


            }

        } catch (JSONException e) {

        }
    }

    /**
     * Calling the coronadatascrapper app to get json data which is updated on a daily bases on this web application
     * For Testing purposes, we use a github repo which hosts a sample json file which sample data
     * This is because the json data set provided by coronadatascrapper is big and it takes quite some time to load and parse
     * So for testing the Github repo is used, but for real use, the github repo will be replaced by the real data provided from
     * coronadatascrapper which is commented out in the code currently
     * @param context
     * @return
     */
//  Load the JSON data from the api
    private static JSONArray loadJSONArray (Context context) {
//        StringBuilder builder = new StringBuilder();
        String data = "";

        try {
//          used this repo for sample data only because there is too much data on the coronadatascrapper app and it takes a lot of time to process it...
//          so we use the github repo for testing the functionality
//          URL url = new URL("https://coronadatascraper.com/data.json");
            URL url = new URL("https://raw.githubusercontent.com/hamid-10/covid-19-data/master/data.json");
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
            Log.d(TAG, "loadJSONArray: URL Error");
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            Log.d(TAG, "loadJSONArray: Json Error");
            e.printStackTrace();
        }
        return null;
    }

}
