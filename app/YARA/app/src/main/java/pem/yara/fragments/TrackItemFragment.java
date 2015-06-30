package pem.yara.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import pem.yara.adapters.TrackHistoryItemAdapter;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraTrack;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 *  * Wird momentan noch nicht benötigt, erst wenn jedes Listitem utnerschiedliche buttons beinhält vll...
 * Activities containing this fragment MUST implement the {@link pem.yara.fragments.TrackItemFragment.onTrackItemInteractionListener}
 * interface.
 */
public class TrackItemFragment extends ListFragment {
    
    TrackDbHelper dbHelper;
    SQLiteDatabase db;
    ArrayList<YaraTrack> values;
    private onTrackItemInteractionListener mListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new TrackDbHelper(getActivity().getApplicationContext());
        values = dbHelper.getTracks(-1);

        setListAdapter(new TrackHistoryItemAdapter(getActivity().getApplicationContext(), values));

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onTrackItemInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onTrackItemClick(values.get(position));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface onTrackItemInteractionListener {
        // TODO: Update argument type and name
        public void onTrackItemClick(YaraTrack id);
    }

}
