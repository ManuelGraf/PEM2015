package pem.yara.fragments;

import android.content.Intent;
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
import pem.yara.RunActivity;
import pem.yara.adapters.TrackHistoryItemAdapter;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraTrack;


public class HomeScreenFragment extends Fragment implements TrackItemFragment.onTrackItemInteractionListener {
    public static final String ARG_OBJECT = "object";
    private Button btnNewTrack;
    private TextView txtTracksEmpty;
    private ListView trackList;
    private View mRootView;
    private TrackDbHelper mTrackDBHelper;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_home_screen, container, false);


        btnNewTrack = (Button)rootView.findViewById(R.id.btnNewTrack);
        btnNewTrack.setOnClickListener(startRunListener);
        txtTracksEmpty = (TextView)rootView.findViewById(R.id.txtTracklistEmpty);
        trackList = (ListView)rootView.findViewById(R.id.trackItems);

        // Populating Track List:
        mTrackDBHelper = new TrackDbHelper(getActivity().getApplicationContext());
        ArrayList<YaraTrack> values = new ArrayList<YaraTrack>();
        values = mTrackDBHelper.getAllTracks();
        trackList.setAdapter(new TrackHistoryItemAdapter(getActivity(), values));
        Log.d("HomeScreenFragment", "Adater Set");

        mRootView = rootView;
        Bundle args = getArguments();

        return rootView;
    }

    @Override
    public void onTrackItemClick(YaraTrack id) {
        Intent intent = new Intent(getActivity(), RunActivity.class);
        intent.putExtra("track_id",id.getId());
        getActivity().startActivity(intent);
    }

    View.OnClickListener startRunListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), RunActivity.class);
            // Set this flag so finish() (called in RunActivity) will return to StartActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getActivity().startActivity(intent);
        }
    };

}
