package pem.yara.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import pem.yara.adapters.SongListItemAdapter;
import pem.yara.db.SongDbHelper;
import pem.yara.entity.YaraSong;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link pem.yara.fragments.SongItemFragment.onTrackItemInteractionListener}
 * interface.
 */
public class SongItemFragment extends ListFragment {
    
    SongDbHelper dbHelper;
    SQLiteDatabase db;
    ArrayList<YaraSong> values;
    private onTrackItemInteractionListener mListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new SongDbHelper(getActivity().getApplicationContext());
        db = dbHelper.getReadableDatabase();
        String[] projection = {
                SongDbHelper.SongDbItem._ID,
                SongDbHelper.SongDbItem.COLUMN_NAME_ARTIST,
                SongDbHelper.SongDbItem.COLUMN_NAME_TITLE,
                SongDbHelper.SongDbItem.COLUMN_NAME_URI,
                SongDbHelper.SongDbItem.COLUMN_NAME_BPM,
                SongDbHelper.SongDbItem.COLUMN_NAME_PLAYCOUNT,
                SongDbHelper.SongDbItem.COLUMN_NAME_BLOCKED,
                SongDbHelper.SongDbItem.COLUMN_NAME_SCORE
        };

        String sortOrder = SongDbHelper.SongDbItem.COLUMN_NAME_ARTIST + " DESC";
        
        Cursor c = db.query(
                SongDbHelper.SongDbItem.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        c.moveToFirst();

        values = new ArrayList<YaraSong>();
        int offset = 0;

        while(offset < c.getCount()){
            values.add(new YaraSong(
                    c.getInt(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem._ID)),
                    c.getString(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_TITLE)),
                    c.getString(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_ARTIST)),
                    c.getString(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_URI)),
                    c.getDouble(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_BPM)),
                    c.getDouble(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_SCORE)),
                    c.getInt(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_BLOCKED)),
                    c.getInt(c.getColumnIndexOrThrow(SongDbHelper.SongDbItem.COLUMN_NAME_PLAYCOUNT))
                    ));
            c.moveToNext();
            offset++;
        }
        setListAdapter(new SongListItemAdapter(getActivity().getApplicationContext(), values));

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
        public void onTrackItemClick(YaraSong id);
    }

}
