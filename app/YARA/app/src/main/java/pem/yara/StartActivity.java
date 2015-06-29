package pem.yara;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.astuetz.PagerSlidingTabStrip;

import pem.yara.LocationService.LocalBinder;
import pem.yara.adapters.HomeScreenPageAdapter;
import pem.yara.fragments.SongListFragment;


public class StartActivity extends ActionBarActivity implements SongListFragment.OnSongListInteractionListener {

    LocationService mService;
    Intent locationIntent;


    private HomeScreenPageAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private int mActiveTab = 0;



    private Button btnShowStats;
    private Button btnStartRun;
    private Button btnNewRun;
    private Button btnShowSongs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        // Navigation buttons
        /*btnStartRun = (Button)findViewById(R.id.btnStartRunning);
        btnStartRun.setOnClickListener(startRunListener);
        btnNewRun = (Button)findViewById(R.id.btnRegisterTrack);
        btnNewRun.setOnClickListener(newTrackListener);
        btnShowStats = (Button)findViewById(R.id.btnShowStatistics);
        btnShowStats.setOnClickListener(showStatisticsListener);
        btnShowSongs = (Button)findViewById(R.id.btnShowSongList);
        btnShowSongs.setOnClickListener(showSonglistListener);
        btnFinishRun = (Button)findViewById(R.id.btnFinishRun);*/




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
    protected void onStart(){
        super.onStart();

        ScanMusicTask scanMusicTask = new ScanMusicTask();
        scanMusicTask.execute(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    protected void onStop() {
        super.onStop();


    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocationService, cast the IBinder and get LocationService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            Log.d("onServiceConnected", "after binder.getService()");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("onServiceDisconnected", "onServiceDisconnected");
        }
    };

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
    public void onSongListInteraction(Uri uri) {

    }

    @Override
    public void onImportMusicInteraction() {
      importMusic();
    }

    public void importMusic(){
        // TODO Martin: import music
    }
    public void newTrack(){
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }



}


