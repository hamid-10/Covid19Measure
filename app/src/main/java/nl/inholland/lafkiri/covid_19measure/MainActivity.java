package nl.inholland.lafkiri.covid_19measure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener{

    private DrawerLayout drawer;
    //  the View Model
    private CountryStatisticsViewModel countryStatisticsViewModel;
    private CountryStatisticsAdapter adapter;
    RecyclerView recyclerView;
    List<CountryStatistics> countryStatistics;
    Context context;
    SwipeRefreshLayout swipeRefreshLayout;

//  Google maps stuff
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private List<CountryStatistics> allStats = new ArrayList<>();
    private List<CountryStatistics> currentStats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      loadLocale to get last language, so that even if the app is stopped it will get same language next time it's opened
        loadLocale();
        setContentView(R.layout.activity_main);

        context = this.getApplication();

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.my_drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = findViewById(R.id.stats_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//      set to true if the recyclerview will have fixed size, this will increase efficiency
        recyclerView.setHasFixedSize(true);
        adapter = new CountryStatisticsAdapter(this);

        countryStatisticsViewModel = ViewModelProviders.of(this).get(CountryStatisticsViewModel.class);
        countryStatisticsViewModel.getAllCountryStatistics().observe(this, new Observer<List<CountryStatistics>>() {
            @Override
            public void onChanged(@Nullable List<CountryStatistics> countryStatistics) {
//              update Recycler View
                Log.d(TAG, "onChanged: Called");
//                this will be set once the statistics option is clicked from the Navigation Drawer instead of here
//                adapter.setCountryStatistics(countryStatistics);
                allStats = countryStatistics;

//              Using second list here to remove items from one list, whereas the default items are still on the other list
//              Implemented this way to make it easier when restoring the default items to adapter again
                currentStats.addAll(countryStatistics);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                Toast.makeText(context, getResources().getString(R.string.stat_remove) + "\n" + adapter.getCountryStatistics(viewHolder.getAdapterPosition()).getCountryState(), Toast.LENGTH_SHORT).show();
                allStats.remove(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresher);
        swipeRefreshLayout.setOnRefreshListener(this);

        View detailsFrame = findViewById(R.id.fragment_details_container);
        boolean tabletScreen = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        if (tabletScreen) {

            countryStatistics = new ArrayList<CountryStatistics>();
//          countryStatistics.addAll(countryStatisticsViewModel.getAllCountryStatistics());
            adapter.setOnItemClickListener(new CountryStatisticsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, View v) {

                    CountryStatistics item = adapter.getCountryStatistics(position);
                    String name = item.getCountryState();
                    String totalCases = item.getTotalCases();
                    String recovered = item.getRecovered();
                    String deaths = item.getDeaths();
                    String activeCases = item.getActiveCases();
                    String source = item.getSource();

//                  Highlighting the selected Item
//                    v.setBackgroundColor(Color.parseColor("#000000"));

//                 Starting Fragment programmatically stuff
                // Create new fragment and transaction
                Fragment newFragment = new StatsDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("totalCases", totalCases);
                bundle.putString("recovered", recovered);
                bundle.putString("deaths", deaths);
                bundle.putString("activeCases", activeCases);
                bundle.putString("source", source);
                newFragment.setArguments(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // Replace the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.fragment_details_container, newFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                    Toast.makeText(getApplicationContext(),name + " is selected", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context, StatsDetails.class);
//                context.startActivity(intent);
                }
            });

        } else {
            countryStatistics = new ArrayList<CountryStatistics>();
//        countryStatistics.addAll(countryStatisticsViewModel.getAllCountryStatistics());
            adapter.setOnItemClickListener(new CountryStatisticsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, View v) {

                    CountryStatistics item = adapter.getCountryStatistics(position);
                    String name = item.getCountryState();
                    String totalCases = item.getTotalCases();
                    String recovered = item.getRecovered();
                    String deaths = item.getDeaths();
                    String activeCases = item.getActiveCases();
                    String source = item.getSource();
                    Toast.makeText(getApplicationContext(),name + " is selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, StatsDetails.class);
                intent.putExtra("name", name);
                intent.putExtra("totalCases", totalCases);
                intent.putExtra("recovered", recovered);
                intent.putExtra("deaths", deaths);
                intent.putExtra("activeCases", activeCases);
                intent.putExtra("source", source);
                MainActivity.this.startActivity(intent);
                }
            });
        }
    }

    /**implementation when the fragment is swiped for refresh
     * At the moment it will update the db by looking at the coronascrapper app*/
    @Override
    public void onRefresh() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.db_update), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: Updating the db Asynk task");
                new UpdateDbAsyncTask(countryStatisticsViewModel).execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    //  menuItem is the menuItem selected from the menu. Here we implement what happens when any item is clicked.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_stats:
//              Implemeted this way, so that it will be Showing the recycler view only when clicked on the Statistics menu Item
//              Could also be implemented differently by displaying the recycler view directly when the activity is created:
//              This could be achieved by setting the adapter in the OnCreate() Method
                adapter.setCountryStatistics(allStats);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "onNavigationItemSelected: Statistics clicked adapter size: " + adapter.getItemCount());
                break;
            case R.id.nav_map:
                Toast.makeText(this, getResources().getString(R.string.map_open), Toast.LENGTH_SHORT).show();
                if(isServicesOK()) {
                    Intent intent = new Intent(this, MapActivity.class);
//                  show all countries stats on the Map, even if some were deleted from the recycler view
//                  This is preferred for now
//                  However, if we want to show only stats items (countries/states) that are left on the recycler
//                  we simply change "currentStats" to "allStats" in this next line
                    intent.putExtra("statsList", (Serializable) currentStats);
                    MainActivity.this.startActivity(intent);
                }
                break;
            case R.id.nav_reset:
                Toast.makeText(context, getResources().getString(R.string.stats_list_restore), Toast.LENGTH_SHORT).show();
                allStats.clear();
                allStats.addAll(currentStats);
                adapter.setCountryStatistics(allStats);
                Log.d(TAG, "onNavigationItemSelected: Reset clicked adapter size: " + adapter.getItemCount() + ", " + currentStats.size() + ". " + allStats.size());
//              this could be optional, let's see...
//                recyclerView.setAdapter(adapter);
                break;
            case R.id.nav_language:
                Toast.makeText(this, getResources().getString(R.string.language_settings), Toast.LENGTH_SHORT).show();
                showChangeLanguageDialog();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangeLanguageDialog() {
//      Array of Languages
//      Arabic is removed from the list because it causes problems with the layout !
        final String[] languagesList = {"English", "Nederlands", "Français", "Deutsche", "Español"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.change_language));
        builder.setSingleChoiceItems(languagesList, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
//                  English
                    setLocale("en");
                    recreate();
                } else if (which == 1) {
//                  Dutch
                    setLocale("nl");
                    recreate();
                } else if (which == 2) {
//                  French
                    setLocale("fr");
                    recreate();
                } else if (which == 3) {
//                  German
                    setLocale("de");
                    recreate();
                } else if (which == 4) {
//                  Spanish
                    setLocale("es");
                    recreate();
                }
//              Dismiss alert dialog when language is selected
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
//      show it
        alertDialog.show();
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//      save data to shared Preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Language", language);
        editor.apply();
    }

//  load language saved in shared preferences
    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Language", "");
        setLocale(language);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else{
            super.onBackPressed();
        }
    }

//  searching from recycler view stuff...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_state, menu);

        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.list_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_stats:
                Log.d(TAG, "onOptionsItemSelected: Deleting all Statistics");
                allStats.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(context, getResources().getString(R.string.all_stats_removed), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Google maps:
     * Checking Google API Availability
     * Verifying Google play services version
     */
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS) {
//          everything is fine, map requests possible now
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
//          this error can be resolved
            Log.d(TAG, "isServicesOK: there is an error, but can be fixed");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Map requests not possible", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
