package nl.inholland.lafkiri.covid_19measure;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class CountryStatisticsViewModel extends AndroidViewModel {
    private CountryStatisticsRepository repository;
//  a List of all country statistics
    LiveData<List<CountryStatistics>> allStats;


    public CountryStatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new CountryStatisticsRepository(application);
        allStats = repository.getAllCountryStatistics();
    }

    public void insert (CountryStatistics statistics) {
        repository.insert(statistics);
    }

    public void update (CountryStatistics statistics) {
        repository.update(statistics);
    }

    public void delete (CountryStatistics statistics) {
        repository.delete(statistics);
    }

    public void deleteAllCountryStatistics () {
        repository.deleteAllCountryStatistics();
    }

    public LiveData<List<CountryStatistics>> getAllCountryStatistics () {
        return allStats;
    }
}
