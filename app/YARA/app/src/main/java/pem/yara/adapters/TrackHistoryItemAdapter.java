package pem.yara.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import pem.yara.R;
import pem.yara.StatisticsActivity;
import pem.yara.entity.YaraTrack;

public class TrackHistoryItemAdapter extends ArrayAdapter<YaraTrack> {
    private final Context context;
    private final List<YaraTrack> values;

    public TrackHistoryItemAdapter(Context context, List<YaraTrack> values){
        super(context, R.layout.fragment_track_item, values);
        this.context = context;
        this.values = values;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.fragment_track_item, parent, false);
            TextView trackName = (TextView) rowView.findViewById(R.id.track_item_name);
            TextView trackDistance = (TextView) rowView.findViewById(R.id.track_item_distance);
            TextView trackCreated = (TextView) rowView.findViewById(R.id.track_item_created);

            trackName.setText("Track name: "+values.get(position).getTitle());
            trackDistance.setText("Track length: " + String.format("%.2f", values.get(position).getLength()/1000) + "km");
            trackCreated.setText("Recorded: " + values.get(position).getDate_created().substring(0, 10));
        ListView list = (ListView)parent;

        list.setOnItemClickListener(startRunListener);

        return rowView;
    }

    @Override
    public YaraTrack getItem(int position){
        return values.get(position);
    }

    AdapterView.OnItemClickListener startRunListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            Intent intent = new Intent(getContext(), StatisticsActivity.class);
            intent.putExtra("TrackID", values.get(pos).getId());
            Log.i("History Item", "ID: " + values.get(pos).getId() + " !");
            getContext().startActivity(intent);
        }
    };
}