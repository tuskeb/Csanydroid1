package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import hu.csany_zeg.one.csanydroid1.core.Battle;

public class BattleActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	Battle mBattle = null;
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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
		HeroView
				mHeroViewA,
				mHeroViewB;
		Battle.OnStateChange mStateChangeListeners = new Battle.OnStateChange() {
			@Override
			public void onChange(final Battle battle, final Object param) {
				switch (battle.getState()) {
					case Battle.STATE_ATTACKER_CHANGE: {
						mHeroViewA.setHero(battle.getAttacker());
					}
					break;
					case Battle.STATE_DEFENDER_CHANGE: {
						mHeroViewB.setHero(battle.getDefender());
					}
					break;
					case Battle.STATE_ATTACK: {
						mHeroViewB.onLifeLost((float) param);

					}
					break;
					case Battle.STATE_BEFORE_FINISH: {
						// reménykedek benne, hogy múködik. tudom, nem valami bíztató...
						//getActivity().finish();
						Log.v("battle", "receive finish signal");
						Toast.makeText(getActivity(), "Köszönjük a figyelmet! A csata véget ért.", Toast.LENGTH_SHORT).show();
						mHeroViewB.setState(HeroView.STATE_SHOW_TROPHY);

					}
					break;
				}
			}
		};
		Battle mBattle;

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static BattleFragment newInstance(int battleNumber) {
			BattleFragment fragment = new BattleFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_BATTLE_NUMBER, battleNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {

			mBattle = Battle.get(getArguments().getInt(ARG_BATTLE_NUMBER));
			mBattle.setOnStateChangeListener(mStateChangeListeners);

			super.onCreate(savedInstanceState);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_battle, container, false);
			LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.hero_linear_layout);

			mHeroViewA = new HeroView(getContext());
			mHeroViewB = new HeroView(getContext());

			mHeroViewA.setHero(mBattle.getAttacker());
			mHeroViewB.setHero(mBattle.getDefender());

			((FrameLayout) linearLayout.findViewById(R.id.hero_a_container)).addView(mHeroViewA);
			((FrameLayout) linearLayout.findViewById(R.id.hero_b_container)).addView(mHeroViewB);

			return rootView;
		}

		public void selectItem(int position) {

			final Battle battle = Battle.get(position);
			final BattleActivity activity = (BattleActivity) getActivity();

			activity.mTitle = battle.getName();


		}

		@Override
		public void onAttach(Context context) {
			super.onAttach(context);

			selectItem(getArguments().getInt(ARG_BATTLE_NUMBER));

		}

		@Override
		public void onDestroy() {

			if (mBattle != null) {
				mBattle.setOnStateChangeListener(null);
			}

			super.onDestroy();
		}

	}

}
