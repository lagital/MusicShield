package musicshield.agita.team.com.musicshield;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.AppBarLayout;
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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;

import java.util.ArrayList;

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
    private AppBarLayout.LayoutParams mToolbarForContactsParms;
    private AppBarLayout.LayoutParams mToolbarParms;
    private AppCompatCheckBox mToolbarCheckbox;
    private FragmentMain mFragmentMain;
    private FragmentMissedCalls mFragmentMissedCalls;
    private FragmentContacts mFragmentContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

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
        mToolbarCheckbox.setHighlightColor(ContextCompat.getColor(this, R.color.activity_background_color));
        mToolbarCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFragmentContacts.selectAllItems(isChecked);
            }
        });

        mMenu.getItem(1).setActionView(mToolbarCheckbox);

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
                                .setIcon(R.drawable.ic_delete_sweep_black_36dp)
                                .show();
                    }
                    return true;
                case R.id.action_save_conacts:
                    Log.d(TAG, "onOptionsItemSelected: Save Filter for Contacts");
                    mFragmentContacts.saveCheckedContacts();
                    mFragmentMain.sendMessageToService(ControlService.MSG_UPDATE_WHITE_LIST);
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
                mMenu.setGroupVisible(R.id.contacts_group, false);
                mMenu.setGroupVisible(R.id.settings_group, true);
                mToolbarCheckbox.setVisibility(View.GONE);
                mToolbar.setLayoutParams(mToolbarParms);
                break;
            case 1:
                mMenu.setGroupVisible(R.id.settings_group, false);
                mMenu.setGroupVisible(R.id.contacts_group, false);
                mMenu.setGroupVisible(R.id.missed_calls_group, true);
                mToolbarCheckbox.setVisibility(View.GONE);
                mToolbar.setLayoutParams(mToolbarParms);
                break;
            case 2:
                mMenu.setGroupVisible(R.id.settings_group, false);
                mMenu.setGroupVisible(R.id.missed_calls_group, false);
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
            SpannableStringBuilder sb = new SpannableStringBuilder(" " + tabName[position]); // space added before text for convenience

            Drawable drawable = ContextCompat.getDrawable(mContext, tabImageResId[position]);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }

        private String[] tabName = {
                getResources().getString(R.string.tab_title_home),
                getResources().getString(R.string.tab_title_missed_calls),
                getResources().getString(R.string.tab_title_contacts)
        };

        private int[] tabImageResId = {
                R.drawable.ic_home_black_24dp,
                R.drawable.ic_call_missed_black_24dp,
                R.drawable.ic_contacts_black_24dp
        };
    }
}