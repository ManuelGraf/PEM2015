package pem.yara.music;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pem.yara.db.SongDbHelper;
import pem.yara.entity.YaraSong;

import static android.media.MediaPlayer.OnCompletionListener;

public class AudioPlayer extends Service implements OnCompletionListener {

    private Application context;

    private SQLiteDatabase db;
    private SongDbHelper dbHelper;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    private List<YaraSong> playlist = new LinkedList<>();

    private boolean paused = false;

    private int currentSong = 0;

    @Override
    public void onCreate() {
        Log.i("AudioPlayer", "onCreate() called");
        context = getApplication();
        dbHelper = new SongDbHelper(context);
        db = dbHelper.getWritableDatabase();

        adjustPlaylist(105);
    }

    @Override
    public void onDestroy() {
        Log.i("AudioPlayer", "onDestroy() called");
        release();
    }

    public void adjustRate(float rate){
        //Should call Soundpool: public final void setRate (int streamID, float rate)
    }

    public YaraSong getCurrentSong(){
        YaraSong song = null;
        if(playlist.size() > 0){
            song = playlist.get(currentSong);
        }
        return song;
    }

    public void adjustPlaylist(double runningBPM) {
        Log.i("AudioPlayer", "Creating new playlist with " + runningBPM + " BPM");

        // reset playlist
        playlist.clear();
        currentSong = 0;

        // add new songs and shuffle
        List<YaraSong> songsWithinRange = dbHelper.findSongsWithinRange(runningBPM,5);
        playlist.addAll(songsWithinRange);
        Collections.shuffle(playlist);

        // start playback
        play();
    }

//    private List<YaraSong> findSongsWithinRange(double bpm, double upperBound) {
//        String[] projection = {SongDbHelper.SongDbItem._ID,"title", "artist", "uri", "bpm", "count", "score", "blocked"};
//
//        Cursor cursor = db.query(
//                SongDbHelper.SongDbItem.TABLE_NAME,
//                projection,                         // The columns to return
//                "bpm > " + lowerBound + " AND bpm < " + upperBound,
//                null,                               // The values for the WHERE clause
//                null,                               // don't group the rows
//                null,                               // don't filter by row groups
//                null                                // The sort order
//        );
//
//        cursor.moveToFirst();
//        List<YaraSong> songs = new ArrayList<>();
//        while(!cursor.isAfterLast()) {
//            YaraSong song = new YaraSong(cursor.getInt(0),cursor.getString(1), cursor.getString(2), cursor.getString(3), Double.parseDouble(cursor.getString(4)),cursor.getDouble(5),cursor.getInt(6),cursor.getInt(7));
//            songs.add(song);
//            cursor.moveToNext();
//        }
//        cursor.close();
//        return songs;
//    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        release();
        nextTrack();
    }

    private void release() {
        if( mediaPlayer == null) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void nextTrack() {
        currentSong++;
        if (currentSong >= playlist.size()) {
            currentSong = 0;
            Collections.shuffle(playlist);
        }
        play();
    }

    public void skip() {
        stop();
        nextTrack();
    }

    public void play() {
        if (playlist.size() == 0) {
            Log.d("AudioPlayer", "Trying to play empty playlist... returning!");
            return;
        }

        YaraSong yaraSong = playlist.get(currentSong);

        if (mediaPlayer != null && paused) {
            mediaPlayer.start();
            paused = false;
        } else if( mediaPlayer != null ) {
            release();
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, Uri.parse(yaraSong.getUri()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException ioe) {
            Log.e("AudioPlayer", "error playing" + yaraSong);
        }
    }
    // play a specific song from current playlist
    public void play(int pos) {
        if (playlist.size() == 0) {
            Log.d("AudioPlayer", "Trying to play empty playlist... returning!");
            return;
        }
        YaraSong yaraSong;
        try{
            yaraSong = playlist.get(pos);
        }catch(ArrayIndexOutOfBoundsException e ){
            e.printStackTrace();
            return;
        }

        if (mediaPlayer != null && paused) {
            mediaPlayer.start();
            paused = false;
        } else if( mediaPlayer != null ) {
            release();
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, Uri.parse(yaraSong.getUri()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException ioe) {
            Log.e("AudioPlayer", "error playing" + yaraSong);
        }
    }

    public List<YaraSong> getPlayList(){
        return playlist;
    }

    public void stop() {
        release();
    }

    public void pause() {
        if( mediaPlayer != null) {
            mediaPlayer.pause();
            paused = true;
        }
    }

    // TODO do something with it on GUI
    public int elapsed() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    public class AudioPlayerBinder extends Binder {
        public AudioPlayer getService() {
            Log.i("AudioPlayer", "getService() called");
            return AudioPlayer.this;
        }
    }
    private final IBinder audioPlayerBinder = new AudioPlayerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("AudioPlayer", "onBind() called");
        return audioPlayerBinder;
    }
}
