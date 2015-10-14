package hu.csany_zeg.one.csanydroid1;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;

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
				.replace(R.id.container, PlaceholderFragment.newInstance(position))
				.commit();
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.setSubtitle("Pirates");
		actionBar.setTitle(mTitle);
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
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "battle_number";
		
		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}
		
		public PlaceholderFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_battle, container, false);
LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.hero_linear_layout);
			HeroView heroView;

			heroView = new HeroView(this.getContext(), Hero.getHero(0), true);
			linearLayout.addView(heroView);

			heroView = new HeroView(this.getContext(), Hero.getHero(1), false);
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

			selectItem(getArguments().getInt(ARG_SECTION_NUMBER));


		}
	}
	
}
