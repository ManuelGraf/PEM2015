package pem.yara.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import pem.yara.fragments.HomeScreenFragment;
import pem.yara.fragments.SongListFragment;

/**
 * Created by yummie on 21.06.2015.
 */
public class HomeScreenPageAdapter extends FragmentStatePagerAdapter {
    public HomeScreenPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        switch(i){
            case 0:
                return new HomeScreenFragment();
            case 1:
                return SongListFragment.newInstance("bla","blub");
            default:
                return new HomeScreenFragment();
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return (position == 0) ? "TRACKS" : "SONGS";
    }
}
