package musicshield.agita.team.com.musicshield;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = "ActivityMain";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Menu mMenu;
    private Toolbar mToolbar;
    private FragmentMain mFragmentMain;
    private FragmentMissedCalls mFragmentMissedCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        updateToolbar(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
            switch (item.getItemId()) {
                case R.id.action_settings:
                    Log.d(TAG, "onOptionsItemSelected: Settings");
                    Intent i = new Intent(this, ActivitySettings.class);
                    startActivity(i);
                    return true;
                case R.id.action_refresh_missed_calls:
                    Log.d(TAG, "onOptionsItemSelected: Refresh Calls");
                    if (mFragmentMissedCalls == null) {
                        Log.d(TAG, "onOptionsItemSelected: " + "wrong fragment");
                    } else {
                        mFragmentMissedCalls.refreshCallList();
                    }
                    return true;
                case R.id.action_clear_missed_calls:
                    Log.d(TAG, "onOptionsItemSelected: Clear Calls");
                    if (mFragmentMissedCalls == null) {
                        Log.d(TAG, "onOptionsItemSelected: " + "wrong fragment");
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(getResources().getString(R.string.alert_clear_title))
                                .setMessage(getResources().getString(R.string.alert_clear_question))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mFragmentMissedCalls.clearCallList();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    public void updateToolbar (Integer position){
        if(mMenu == null)
            return;
        switch (position) {
            case 0:
                mMenu.setGroupVisible(R.id.missed_calls_group, false);
                mMenu.setGroupVisible(R.id.settings_group, true);
                break;
            case 1:
                mMenu.setGroupVisible(R.id.settings_group, false);
                mMenu.setGroupVisible(R.id.missed_calls_group, true);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a Fragment (defined as a static inner class below).
            // TODO: new fragment with missed calls
            switch (position) {
                case 0:
                    mFragmentMain = FragmentMain.newInstance(position);
                    return mFragmentMain;
                case 1:
                    mFragmentMissedCalls = FragmentMissedCalls.newInstance(position);
                    return mFragmentMissedCalls;
                default:
                    mFragmentMain = FragmentMain.newInstance(position);
                    return mFragmentMain;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SpannableStringBuilder sb = new SpannableStringBuilder(" " + tabName[position]); // space added before text for convenience

            Drawable drawable = getResources().getDrawable(tabImageResId[position]);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }

        private String[] tabName = {
                getResources().getString(R.string.tab_title_home),
                getResources().getString(R.string.tab_title_missed_calls)
        };

        private int[] tabImageResId = {
                R.drawable.ic_home_black_24dp,
                R.drawable.ic_call_missed_black_24dp
        };
    }
}