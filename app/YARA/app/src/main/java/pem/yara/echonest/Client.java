package pem.yara.echonest;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import pem.yara.db.SongDbHelper;
import pem.yara.entity.YaraSong;

public class Client {
    
    private Application context;

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private SQLiteDatabase db;
    
    public Client(Application context) {
        this.context = context;
        SongDbHelper songDbHelper = new SongDbHelper(context);
        db = songDbHelper.getWritableDatabase();
    }

    public void getSongInfo(final YaraSong song) {
        YaraSong yaraSong = findSong(song);
        if (yaraSong != null) {
            Log.i("EchoNestClient", song + " already known");
            return;
        }

        String url = null;
        try {
            url = String.format(Config.SONG_SEARCH_URL, URLEncoder.encode(song.getArtist(), "UTF-8"), URLEncoder.encode(song.getTitle(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.i("EchoNestClient", "Requesting " + url);
        asyncHttpClient.get(context, url, null, null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.e("EchoNestClient", s);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Gson gson = new Gson();
                SongSearchDTO songSearchDTO = gson.fromJson(s, SongSearchDTO.class);
                for (SongSearchDTO.SongsDTO songsDTO : songSearchDTO.getResponse().getSongsDTO()) {
                    Double tempo = songsDTO.getAudioSummary().getTempo();
                    Log.v("EchoNestClient", song.getTitle() + " by " + song.getArtist() + " has " + tempo + " bpm");
                    saveSong(song, tempo);
                }
            }
        });
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private YaraSong findSong(YaraSong song) {
        String[] projection = {"title", "artist", "uri", "bpm", "count", "score", "blocked"};

        Cursor cursor = db.query(
                SongDbHelper.SongDbItem.TABLE_NAME,
                projection,                         // The columns to return
                "uri=?",                            // The columns for the WHERE clause
                new String[]{song.getUri()},        // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                null                                // The sort order
        );

        cursor.moveToFirst();
        YaraSong persistentSong = null;
        if (!cursor.isAfterLast()) {
            persistentSong = new YaraSong(cursor.getString(0), cursor.getString(1), cursor.getString(2), Double.parseDouble(cursor.getString(3)));
        }
        cursor.close();
        return persistentSong;
    }

    private void saveSong(YaraSong song, Double tempo) {
        ContentValues values = new ContentValues();
        values.put("artist", song.getArtist());
        values.put("title", song.getTitle());
        values.put("uri", song.getUri());
        values.put("bpm", tempo);

        db.insert(SongDbHelper.SongDbItem.TABLE_NAME, null, values);
    }
}
