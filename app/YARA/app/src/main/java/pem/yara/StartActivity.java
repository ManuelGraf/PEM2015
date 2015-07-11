package pem.yara;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import pem.yara.adapters.HomeScreenPageAdapter;
import pem.yara.db.RunDbHelper;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraSong;
import pem.yara.fragments.SongListFragment;
import pem.yara.music.ScanMusicTask;

import static android.os.AsyncTask.Status.PENDING;

public class StartActivity extends ActionBarActivity implements SongListFragment.OnSongListInteractionListener {

    private ScanMusicTask scanMusicTask = new ScanMusicTask(this);
    private HomeScreenPageAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private int mActiveTab = 0;
    private TextView txtLoadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        txtLoadingIndicator = (TextView)findViewById(R.id.infoBox);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
    }

    public boolean onPrepareOptionsMenu(final Menu menu) {

        MenuItem btnAddTrack = menu.findItem(R.id.actionAddTrack);
        switch(mActiveTab){
            case 0:
                btnAddTrack.setVisible(true).setEnabled(true);
                break;
            case 1:
                btnAddTrack.setVisible(false).setEnabled(false);
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

    public void setInfo(String msg){
        txtLoadingIndicator.setText(msg);
    }
    public void hideLoadingIndicator(){
        setInfo("");
    }
    public void importMusic(){
        if (scanMusicTask.getStatus() == PENDING) {
            scanMusicTask.execute(getApplication());
        }
    }
    public void newTrack(){
        Intent intent = new Intent(this, RunActivity.class);
        intent.putExtra("TrackID",-1);
        startActivity(intent);
    }

}


