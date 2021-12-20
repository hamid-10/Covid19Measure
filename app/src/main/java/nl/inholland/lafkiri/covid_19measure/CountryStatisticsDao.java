package nl.inholland.lafkiri.covid_19measure;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface CountryStatisticsDao {

    @Insert
    void insert(CountryStatistics stats);

    @Update
    void update (CountryStatistics stats);

    @Delete
    void delete(CountryStatistics stats);

    @Query("DELETE FROM stats_table")
    void deleteAllStats();

//  we add LiveData so that each time the data is updated whenever there is any changed in stats_table
    @Query("SELECT * FROM stats_table")
    LiveData<List<CountryStatistics>> getAllStats();

}
