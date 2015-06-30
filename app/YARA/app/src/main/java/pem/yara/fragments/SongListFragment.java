package pem.yara.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pem.yara.R;
import pem.yara.adapters.SongListItemAdapter;
import pem.yara.db.SongDbHelper;
import pem.yara.entity.YaraSong;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SongListFragment.OnSongListInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SongListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView txtSongsEmpty;
    private ListView listSongs;
    private Button btnImportMusic;
    private View mRootView;
    SongDbHelper dbHelper;
    SQLiteDatabase db;
    ArrayList<YaraSong> values;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnSongListInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SongListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SongListFragment newInstance(String param1, String param2) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SongListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


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
    }

    @Override
    public void onResume() {
        super.onResume();

        listSongs.setAdapter(new SongListItemAdapter(getActivity().getApplicationContext(), values));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_song_list, container, false);


        btnImportMusic = (Button)rootView.findViewById(R.id.btnImportMusic);
        btnImportMusic.setOnClickListener(importMusicListener);
        txtSongsEmpty = (TextView)rootView.findViewById(R.id.txtSonglistEmpty);
        listSongs = (ListView)rootView.findViewById(R.id.songItems);

        listSongs.setAdapter(new SongListItemAdapter(getActivity().getApplicationContext(), values));
        // Songlist Click listener
        listSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                Log.d("listitemclick","clicked song " + values.get(position).getTitle());
            }
        });

        if(values.size() > 1){
            txtSongsEmpty.setVisibility(View.GONE);
            btnImportMusic.setVisibility(View.GONE);
        }else{
            txtSongsEmpty.setVisibility(View.VISIBLE);
            btnImportMusic.setVisibility(View.VISIBLE);
        }

        mRootView = rootView;
        Bundle args = getArguments();

        return rootView;

    }
    View.OnClickListener importMusicListener = new View.OnClickListener() {
        public void onClick(View v) {
          mListener.onImportMusicInteraction();
        }
    };



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSongListInteractionListener) activity;
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
    public interface OnSongListInteractionListener {
        // TODO: Update argument type and name
        public void onSongListInteraction(YaraSong s);
        public void onImportMusicInteraction();
    }

}
