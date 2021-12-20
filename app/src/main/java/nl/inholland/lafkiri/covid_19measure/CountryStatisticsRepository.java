package nl.inholland.lafkiri.covid_19measure;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class CountryStatisticsRepository {

    private CountryStatisticsDao countryStatisticsDao;
    private LiveData<List<CountryStatistics>> allCountryStatistics;

    public CountryStatisticsRepository (Application application) {
        CountryStatisticsDatabase database = CountryStatisticsDatabase.getInstance(application);
        countryStatisticsDao = database.countryStatisticsDao();
        allCountryStatistics = countryStatisticsDao.getAllStats();
    }

    public void insert(CountryStatistics countryStatistics) {
        new InsertCountryStatisticsAsyncTask(countryStatisticsDao).execute(countryStatistics);
    }

    public void update(CountryStatistics countryStatistics) {
        new UpdateCountryStatisticsAsyncTask(countryStatisticsDao).execute(countryStatistics);
    }

    public void delete(CountryStatistics countryStatistics) {
        new DeleteCountryStatisticsAsyncTask(countryStatisticsDao).execute(countryStatistics);
    }

    public void deleteAllCountryStatistics() {
        new DeleteAllCountryStatisticsAsyncTask(countryStatisticsDao).execute();
    }

    public LiveData<List<CountryStatistics>> getAllCountryStatistics() {
        return allCountryStatistics;
    }

//  Insertion AsynkTask
    private static class InsertCountryStatisticsAsyncTask extends AsyncTask<CountryStatistics, Void, Void> {
        private CountryStatisticsDao countryStatisticsDao;

        private InsertCountryStatisticsAsyncTask(CountryStatisticsDao countryStatisticsDao) {
            this.countryStatisticsDao = countryStatisticsDao;
        }

    @Override
    protected Void doInBackground(CountryStatistics... countryStatistics) {
            countryStatisticsDao.insert(countryStatistics[0]);
            return null;
    }
}

    private static class UpdateCountryStatisticsAsyncTask extends AsyncTask<CountryStatistics, Void, Void> {
        private CountryStatisticsDao countryStatisticsDao;

        private UpdateCountryStatisticsAsyncTask(CountryStatisticsDao countryStatisticsDao) {
            this.countryStatisticsDao = countryStatisticsDao;
        }

        @Override
        protected Void doInBackground(CountryStatistics... countryStatistics) {
            countryStatisticsDao.update(countryStatistics[0]);
            return null;
        }
    }

    private static class DeleteCountryStatisticsAsyncTask extends AsyncTask<CountryStatistics, Void, Void> {
        private CountryStatisticsDao countryStatisticsDao;

        private DeleteCountryStatisticsAsyncTask(CountryStatisticsDao countryStatisticsDao) {
            this.countryStatisticsDao = countryStatisticsDao;
        }

        @Override
        protected Void doInBackground(CountryStatistics... countryStatistics) {
            countryStatisticsDao.delete(countryStatistics[0]);
            return null;
        }
    }

    private static class DeleteAllCountryStatisticsAsyncTask extends AsyncTask<Void, Void, Void> {
        private CountryStatisticsDao countryStatisticsDao;

        private DeleteAllCountryStatisticsAsyncTask(CountryStatisticsDao countryStatisticsDao) {
            this.countryStatisticsDao = countryStatisticsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            countryStatisticsDao.deleteAllStats();
            return null;
        }
    }

}
