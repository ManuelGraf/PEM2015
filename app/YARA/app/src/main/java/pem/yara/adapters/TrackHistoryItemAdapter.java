package pem.yara.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pem.yara.R;
import pem.yara.entity.YaraTrack;

/**
 * Created by yummie on 29.06.2015.
 */
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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.fragment_track_item, parent, false);
        TextView trackName = (TextView) rowView.findViewById(R.id.track_item_name);
        TextView trackDistance = (TextView) rowView.findViewById(R.id.track_item_distance);
        TextView trackCreated = (TextView) rowView.findViewById(R.id.track_item_created);

        trackName.setText("Track name: "+values.get(position).getTitle());
        trackDistance.setText("Track size: "+values.get(position).getLength());
        trackCreated.setText("Recorded: " + values.get(position).getDate_created());

        return rowView;
    }

    @Override
    public YaraTrack getItem(int position){
        return values.get(position);
    }
}