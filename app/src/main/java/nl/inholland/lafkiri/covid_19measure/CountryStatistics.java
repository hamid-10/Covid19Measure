package nl.inholland.lafkiri.covid_19measure;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

//This is creating an SQLite table: stats_table
@Entity(tableName = "stats_table")
public class CountryStatistics implements Serializable {

//  these are the table columns
    @PrimaryKey(autoGenerate = true)
    private int id;
//  in case a different name for columns is need: we can use: @ColumnInfo(name = "column_name")
    private String countryState;
    private String totalCases;
    private String recovered;
    private String deaths;
    private String activeCases;
    private double latitude;
    private double longitude;
    private String source;

    public CountryStatistics(String countryState, String totalCases, String recovered, String deaths, String activeCases, double latitude, double longitude, String source) {
        this.countryState = countryState;
        this.totalCases = totalCases;
        this.recovered = recovered;
        this.deaths = deaths;
        this.activeCases = activeCases;
        this.latitude = latitude;
        this.longitude = longitude;
        this.source = source;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getCountryState() {
        return countryState;
    }

    public String getTotalCases() {
        return totalCases;
    }

    public String getRecovered() {
        return recovered;
    }

    public String getDeaths() {
        return deaths;
    }

    public String getActiveCases() {
        return activeCases;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSource() {
        return source;
    }
}
