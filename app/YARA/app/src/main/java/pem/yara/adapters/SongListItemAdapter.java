package pem.yara.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pem.yara.R;
import pem.yara.entity.YaraSong;

/**
 * Created by yummie on 29.06.2015.

 */
public class SongListItemAdapter  extends ArrayAdapter<YaraSong> {

    private final Context context;
    private final List<YaraSong> values;

    public SongListItemAdapter(Context context, List<YaraSong> values){
        super(context, R.layout.fragment_song_item, values);
        this.context = context;
        this.values = values;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.fragment_song_item, parent, false);

        TextView songId = (TextView) rowView.findViewById(R.id.songItemId);
        TextView songName = (TextView) rowView.findViewById(R.id.songItemName);
        TextView songBPM = (TextView) rowView.findViewById(R.id.songItemBpm);
        TextView songPlayCount = (TextView) rowView.findViewById(R.id.songItemPlaycount);

        songId.setText(""+values.get(position).getId());
        songName.setText(values.get(position).getArtist()+" - "+values.get(position).getTitle());
        songBPM.setText("BPM: "+values.get(position).getTempo());
        songPlayCount.setText("playcount: "+ values.get(position).getPlayCount());

        return rowView;
    }
}
