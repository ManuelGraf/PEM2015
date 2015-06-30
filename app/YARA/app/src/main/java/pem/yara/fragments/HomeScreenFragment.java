package pem.yara.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pem.yara.R;
import pem.yara.adapters.TrackHistoryItemAdapter;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraTrack;


public class HomeScreenFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    private Button btnNewTrack;
    private TextView txtTracksEmpty;
    private ListView trackList;
    private View mRootView;
    private TrackHistoryItemAdapter mTrackAdapter;
    private TrackDbHelper mTrackDBHelper;
    private ArrayList<YaraTrack> values;

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<YaraTrack> values = mTrackDBHelper.getAllTracks();
        Log.d("HomeScreenFragment", "Tracklist contains " + values.size() + " elements.");
        mTrackAdapter = new TrackHistoryItemAdapter(getActivity(), values);
        trackList.setAdapter(mTrackAdapter);
        Log.d("HomeScreenFragment", "Adapter Set");
        trackList.setEmptyView(mRootView.findViewById(R.id.txtTracklistEmpty));
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_home_screen, container, false);


        txtTracksEmpty = (TextView)rootView.findViewById(R.id.txtTracklistEmpty);
        trackList = (ListView)rootView.findViewById(R.id.trackItems);

        // Populating Track List:
        mTrackDBHelper = new TrackDbHelper(getActivity().getApplicationContext());
        values = mTrackDBHelper.getAllTracks();
        Log.d("HomeScreenFragment", "Tracklist contains " + values.size() + " elements.");
        mTrackAdapter = new TrackHistoryItemAdapter(getActivity(), values);
        trackList.setAdapter(mTrackAdapter);


        //Log.d("HomeScreenFragment", "Adapter Set");
        trackList.setEmptyView(rootView.findViewById(R.id.txtTracklistEmpty));


        mRootView = rootView;
        Bundle args = getArguments();

        return rootView;
    }




}
