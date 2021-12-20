package nl.inholland.lafkiri.covid_19measure;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class StatsDetails extends FragmentActivity {


    private static final String TAG ="StatsDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_item_details);

        Bundle bundle =  this.getIntent().getExtras();
        String name = bundle.get("name").toString();
        String totalCases = bundle.get("totalCases").toString();
        String recovered = bundle.get("recovered").toString();
        String deaths = bundle.get("deaths").toString();
        String activeCases = bundle.get("activeCases").toString();
        String source = bundle.get("source").toString();

        Bundle newBundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("totalCases", totalCases);
        bundle.putString("recovered", recovered);
        bundle.putString("deaths", deaths);
        bundle.putString("activeCases", activeCases);
        bundle.putString("source", source);
        StatsDetailsFragment statsDetailsFragment = new StatsDetailsFragment();
        statsDetailsFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_details_container, statsDetailsFragment);
        transaction.commit();
    }
}
