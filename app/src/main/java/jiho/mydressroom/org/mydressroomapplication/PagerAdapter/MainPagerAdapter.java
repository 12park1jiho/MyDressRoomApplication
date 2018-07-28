package jiho.mydressroom.org.mydressroomapplication.PagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import jiho.mydressroom.org.mydressroomapplication.Fragment.MyFragment;
import jiho.mydressroom.org.mydressroomapplication.Fragment.OtherFragment;
import jiho.mydressroom.org.mydressroomapplication.Fragment.SearchFragment;

public class MainPagerAdapter extends FragmentStatePagerAdapter {
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new SearchFragment();
            case 1:
                return new MyFragment();
            case 2:
                return new OtherFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 3;
    }
}

