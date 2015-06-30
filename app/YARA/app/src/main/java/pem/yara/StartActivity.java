package pem.yara;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import pem.yara.LocationService.LocalBinder;
import pem.yara.adapters.HomeScreenPageAdapter;
import pem.yara.db.RunDbHelper;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraSong;
import pem.yara.fragments.SongListFragment;
import pem.yara.music.AudioPlayer;
import pem.yara.music.ScanMusicTask;

import static android.os.AsyncTask.Status.PENDING;

public class StartActivity extends ActionBarActivity implements SongListFragment.OnSongListInteractionListener {

    private ScanMusicTask scanMusicTask = new ScanMusicTask();
    private HomeScreenPageAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private int mActiveTab = 0;

    private ServiceConnection serviceConnection = new AudioPlayerServiceConnection();
    private AudioPlayer audioPlayer;
    private Intent audioPlayerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // TODO: Remove reset-Statements when going live
//        new RunDbHelper(getBaseContext()).resetDB();
//        new TrackDbHelper(getBaseContext()).resetDB();

        mPagerAdapter = new HomeScreenPageAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabsHome);
        tabs.setShouldExpand(true);
        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mActiveTab = position;
                        supportInvalidateOptionsMenu();
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        importMusic();

        audioPlayerIntent = new Intent(this, AudioPlayer.class);
        bindService(audioPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        Intent audioPlayerIntent = new Intent(getApplicationContext(), AudioPlayer.class);
        startService(audioPlayerIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        stopService(audioPlayerIntent);
    }

    public boolean onPrepareOptionsMenu(final Menu menu) {

        MenuItem btnScanForMusic = menu.findItem(R.id.actionScanMusicLibrary);
        MenuItem btnAddTrack = menu.findItem(R.id.actionAddTrack);
        switch(mActiveTab){
            case 0:
                btnAddTrack.setVisible(true).setEnabled(true);
                btnScanForMusic.setVisible(false).setEnabled(false);
                break;
            case 1:
                btnAddTrack.setVisible(false).setEnabled(false);
                btnScanForMusic.setVisible(true).setEnabled(true);
                break;
            default:
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.actionAddTrack:
                newTrack();
                return true;
            case R.id.actionScanMusicLibrary:
                importMusic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Interactions for SonglistFragment
    @Override
    public void onSongListInteraction(YaraSong s) {

    }

    @Override
    public void onImportMusicInteraction() {
      importMusic();
    }

    public void importMusic(){
        if (scanMusicTask.getStatus() == PENDING) {
            scanMusicTask.execute(getApplication());
        }
    }
    public void newTrack(){
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }

    private final class AudioPlayerServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            Log.v("StartActivity", "AudioPlayerServiceConnection: Service connected");
            audioPlayer = ((AudioPlayer.AudioPlayerBinder) baBinder).getService();
            startService(audioPlayerIntent);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("StartActivity", "AudioPlayerServiceConnection: Service disconnected");
            audioPlayer = null;
        }
    }

}


