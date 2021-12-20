package nl.inholland.lafkiri.covid_19measure;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CountryStatisticsAdapter extends RecyclerView.Adapter<CountryStatisticsAdapter.CountryStatisticsHolder> implements Filterable {
    private List<CountryStatistics> stats = new ArrayList<>();
    private List<CountryStatistics> statsFull = new ArrayList<>();
    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }
    private Context context;

    public CountryStatisticsAdapter(Context context) {
        this.context = context;
    }

    //  CountryStatisticsViewHolder**
    @NonNull
    @Override
    public CountryStatisticsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.country_statistics_item, viewGroup, false);
        return new CountryStatisticsHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryStatisticsHolder countryStatisticsHolder, int i) {
//      Here we pass data/value to the View Holder
        final CountryStatistics currentCountryStatistics = stats.get(i);

        countryStatisticsHolder.textViewName.setText(currentCountryStatistics.getCountryState());
        countryStatisticsHolder.textViewInfected.setText(String.valueOf(currentCountryStatistics.getTotalCases()));
        countryStatisticsHolder.textViewDeceased.setText(String.valueOf(currentCountryStatistics.getDeaths()));
//        if (countryStatisticsHolder.relativeLayout != null) {
//        countryStatisticsHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, StatsDetails.class);
//                intent.putExtra("countryState", currentCountryStatistics.getCountryState());
//                intent.putExtra("totalCases", currentCountryStatistics.getTotalCases());
//                intent.putExtra("recovered", currentCountryStatistics.getRecovered());
//                intent.putExtra("deaths", currentCountryStatistics.getDeaths());
//                intent.putExtra("activeCases", currentCountryStatistics.getActiveCases());
//                intent.putExtra("source", currentCountryStatistics.getSource());
//                intent.putExtra("latitude", currentCountryStatistics.getLatitude());
//                intent.putExtra("longitude", currentCountryStatistics.getLongitude());
//                context.startActivity(intent);

        //                countryStatisticsHolder.itemView.setBackgroundColor(Color.parseColor("#000000"));
//
//            }
//        });
//    }
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    public void setCountryStatistics (List<CountryStatistics> stats) {
        this.stats = stats;
        this.statsFull = stats;
        notifyDataSetChanged();
    }

    public CountryStatistics getCountryStatistics (int position) {
        return stats.get(position);
    }




    class CountryStatisticsHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewInfected;
        private TextView textViewDeceased;
        private RelativeLayout relativeLayout;

        public CountryStatisticsHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.country_name);
            textViewInfected = itemView.findViewById(R.id.country_infected);
            textViewDeceased = itemView.findViewById(R.id.country_deceased);
            relativeLayout = itemView.findViewById(R.id.list_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position, v);
                        }
                    }

                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return statsFilter;
    }

    private Filter statsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CountryStatistics> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(statsFull);
            } else {
//              get search string, convert to lowercase and get rid of empty spaces at beginning and end of input string with trim()
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (CountryStatistics item : statsFull) {
                    if (item.getCountryState().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
//            stats.clear();
//            stats.addAll((List) results.values);
            stats = (ArrayList) results.values;
            notifyDataSetChanged();

        }
    };
}
