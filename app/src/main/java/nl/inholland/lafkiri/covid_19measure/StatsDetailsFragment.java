package nl.inholland.lafkiri.covid_19measure;

import android.net.http.SslCertificate;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.xml.namespace.QName;

import static android.support.constraint.Constraints.TAG;

public class StatsDetailsFragment extends Fragment {

    private TextView stateName;
    private TextView totalCases;
    private TextView recovered;
    private TextView deaths;
    private TextView activeCases;
    private TextView source;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_item_details, container, false);

        stateName = view.findViewById(R.id.country_name);
        totalCases = view.findViewById(R.id.total_cases);
        recovered = view.findViewById(R.id.recovered);
        deaths = view.findViewById(R.id.deaths);
        activeCases = view.findViewById(R.id.active_cases);
        source = view.findViewById(R.id.source);


        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String totalCases = bundle.getString("totalCases");
            String recovered = bundle.getString("recovered");
            String deaths = bundle.getString("deaths");
            String activeCases = bundle.getString("activeCases");
            String source = bundle.getString("source");


            Log.d(TAG, "onCreateView: Item selected" + name + totalCases + recovered);
            this.stateName.setText(name);
            this.totalCases.setText(totalCases);
            this.recovered.setText(recovered);
            this.deaths.setText(deaths);
            this.activeCases.setText(activeCases);
            this.source.setText(source);
        } else {
            Log.d(TAG, "onCreateView: no Item selected");
        }



        return view;
    }
}
