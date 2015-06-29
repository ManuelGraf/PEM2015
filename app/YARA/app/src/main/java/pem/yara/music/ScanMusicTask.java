package pem.yara.music;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
                    Log.v("Music scan", "Found song: " + yaraSong);
                } while (musicCursor.moveToNext());
            }
        } finally {
            if (musicCursor != null) {
                musicCursor.close();
            }
        }

        EchoNestClient echoNestClient = new EchoNestClient(context);

        // 2. get all songs from db
        List<YaraSong> allSongs = echoNestClient.getAllSongs();

        // 3. fetch meta data from EchoNest
        for (YaraSong song : songs) {
            if (allSongs.contains(song)) {
                allSongs.remove(song);
                continue;
            }
           echoNestClient.getSongInfo(song);
        }

        // 4. clean db: the remaining songs in this list are not on the device anymore
        echoNestClient.removeSongsFromDB(allSongs);

        Log.i("Music scan", "Done!");

        return songs;
    }
}
