package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import hu.csany_zeg.one.csanydroid1.core.Battle;

public class BattleActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Battle.sBattles.size() == 0) {
            finish();
        }

        setContentView(R.layout.activity_battle);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, BattleFragment.newInstance(position))
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            //actionBar.setSubtitle("Pirates");
            actionBar.setTitle(mTitle);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.battle, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class BattleFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_BATTLE_NUMBER = "battle_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static BattleFragment newInstance(int sectionNumber) {
            BattleFragment fragment = new BattleFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_BATTLE_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        Battle mBattle;

        public BattleFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mBattle = Battle.sBattles.get(getArguments().getInt(ARG_BATTLE_NUMBER));

        }

        Battle.OnStateChange mStateChangeListeners = new Battle.OnStateChange() {
            @Override
            public void onChange(final Battle battle, final Object param) {
                battle.mHandler.postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        battle.nextState();
                    }
                }, 1000);

            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment_battle, container, false);
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.hero_linear_layout);
            HeroView heroView;

            heroView = new HeroView(getContext(), mBattle.getHeroes(true).get(0), true);
            linearLayout.addView(heroView);

            heroView = new HeroView(getContext(), mBattle.getHeroes(false).get(1), false);
            linearLayout.addView(heroView);

            return rootView;
        }

        public void selectItem(int position) {
            final Battle battle = Battle.sBattles.get(position);
            final BattleActivity activity = (BattleActivity) getActivity();

            activity.mTitle = battle.getName();

        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            selectItem(getArguments().getInt(ARG_BATTLE_NUMBER));


        }

        @Override
        public void onDetach() {
            super.onDetach();
        }
    }

}
