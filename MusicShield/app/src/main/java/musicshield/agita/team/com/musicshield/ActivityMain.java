package musicshield.agita.team.com.musicshield;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TableLayout;

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
    private TabLayout mTabLayout;
    private Menu mMenu;
    private Toolbar mToolbar;
    private AppBarLayout.LayoutParams mToolbarForContactsParms;
    private AppBarLayout.LayoutParams mToolbarParms;
    private AppCompatCheckBox mToolbarCheckbox;
    private FragmentMain mFragmentMain;
    private FragmentMissedCalls mFragmentMissedCalls;
    private FragmentContacts mFragmentContacts;
    private FloatingActionButton mFAB;
    private int[] tabImageResId = {
            R.drawable.ic_home_black_24dp,

            R.drawable.ic_call_missed_black_24dp,
            R.drawable.ic_contacts_black_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mFAB = (FloatingActionButton) findViewById(R.id.fab);

        mToolbarParms = new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mToolbarParms.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mToolbarForContactsParms = new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mToolbarForContactsParms.setMargins(
                0,
                0,
                getResources().getDimensionPixelSize(R.dimen.toolbar_right_margin),
                0);
        mToolbarForContactsParms.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Give the TabLayout the ViewPager
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(tabImageResId[i]);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position, ActivityMain.this);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        updateUI(0, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);

        mToolbarCheckbox = new AppCompatCheckBox(this, null);
        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(
                android.widget.Toolbar.LayoutParams.MATCH_PARENT,
                android.widget.Toolbar.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, getResources().getDimensionPixelSize(R.dimen.toolbar_right_margin), 10);
        mToolbarCheckbox.setLayoutParams(lp);
        mToolbarCheckbox.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.toolbar_checkbox_statelist)));
        mToolbarCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFragmentContacts.selectAllItems(isChecked);
            }
        });

        mMenu.getItem(1).setActionView(mToolbarCheckbox);
        updateUI(0, this);

        return super.onCreateOptionsMenu(menu);
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
            }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI (Integer position, final Context context){
        if(mMenu == null)
            return;
        switch (position) {
            case 0:
                mFAB.setImageResource(R.drawable.ic_security_white_24dp);
                //mFAB.show();
                mFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFragmentMain.update();
                    }
                });

                mMenu.setGroupVisible(R.id.contacts_group, false);
                mMenu.setGroupVisible(R.id.settings_group, true);
                mToolbarCheckbox.setVisibility(View.GONE);
                mToolbar.setLayoutParams(mToolbarParms);
                break;
            case 1:
                mFAB.setImageResource(R.drawable.ic_delete_sweep_white_24dp);
                mFAB.show();
                mFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "FAB: Clear Calls");
                        if (mFragmentMissedCalls == null) {
                            Log.d(TAG, "FAB: " + "wrong fragment");
                        } else {
                            new AlertDialog.Builder(context)
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
                                    .setIcon(R.drawable.ic_delete_sweep_black_36dp)
                                    .show();
                        }
                    }
                });

                mMenu.setGroupVisible(R.id.settings_group, false);
                mMenu.setGroupVisible(R.id.contacts_group, false);
                mToolbarCheckbox.setVisibility(View.GONE);
                mToolbar.setLayoutParams(mToolbarParms);
                break;
            case 2:
                mFAB.setImageResource(R.drawable.ic_done_white_24dp);
                mFAB.show();
                mFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "FAB: Save Filter for Contacts");
                        mFragmentContacts.saveCheckedContacts();
                        mFragmentMain.sendMessageToService(ControlService.MSG_UPDATE_LISTS);
                    }
                });


                mMenu.setGroupVisible(R.id.settings_group, false);
                mMenu.setGroupVisible(R.id.contacts_group, true);
                mToolbarCheckbox.setVisibility(View.VISIBLE);
                mToolbar.setLayoutParams(mToolbarForContactsParms);
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Context mContext;

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
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
                case 2:
                    mFragmentContacts = FragmentContacts.newInstance(position);
                    return mFragmentContacts;
                default:
                    mFragmentMain = FragmentMain.newInstance(position);
                    return mFragmentMain;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}