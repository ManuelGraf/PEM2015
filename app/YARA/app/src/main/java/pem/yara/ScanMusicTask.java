package pem.yara;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

import pem.yara.echonest.Client;
import pem.yara.entity.YaraSong;

public class ScanMusicTask extends AsyncTask<Application, Void, ArrayList<YaraSong>> {

    @Override
    protected ArrayList<YaraSong> doInBackground(Application... params) {
        Application context = params[0];

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        String where = MediaStore.Audio.Media.IS_MUSIC + " = 1";

        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, where, null, MediaStore.Audio.Media.TITLE);

        // 1. find music on device
        ArrayList<YaraSong> songs = new ArrayList<>();
        try {
            if (musicCursor != null && musicCursor.moveToFirst()) {
                do {
                    YaraSong yaraSong = new YaraSong(musicCursor.getString(0), musicCursor.getString(1), musicCursor.getString(2));
                    songs.add(yaraSong);
                    Log.i("Music scan", "Found song: " + yaraSong);
                } while (musicCursor.moveToNext());
            }
        } finally {
            if (musicCursor != null) {
                musicCursor.close();
            }
        }

        // 2. fetch meta data from EchoNest
        Client echoNestClient = new Client(context);
        for (YaraSong song : songs) {
            echoNestClient.getSongInfo(song);
        }

        return songs;
    }
}
