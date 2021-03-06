package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentHostCallback;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
	
	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
	
	/**
	 * Per the design guidelines, you should show the drawer on launch until the user manually
	 * expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
	
	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private NavigationDrawerCallbacks mCallbacks;
	
	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mDrawerToggle;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private View mFragmentContainerView;
	
	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;
	
	public NavigationDrawerFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Read in the flag indicating whether or not the user has demonstrated awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}

		// Select either the default item (0) or the last selected item.
		selectItem(mCurrentSelectedPosition);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

		mDrawerListView = (ListView) view.findViewById(R.id.battle_list);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position);
			}
		});
/*
		mDrawerListView.setAdapter(new ArrayAdapter<Battle>(
				                                                   getActionBar().getThemedContext(),
				                                                   android.R.layout.simple_list_item_activated_2,
				                                                   android.R.id.text1,
				                                                   Battle.sBattles));
*/

		mDrawerListView.setAdapter(new ArrayAdapter<Battle>(
				                                                   getActionBar().getThemedContext(),
				                                                   android.R.layout.simple_list_item_2,
				                                                   Battle.sBattles) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = View.inflate(getContext(), android.R.layout.simple_list_item_2, null);
				}

				((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position).getName());
				//((TextView)convertView.findViewById(android.R.id.text2)).setText(getItem(position).getRound());

				return convertView;
			}

		});

		mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
/*
		((Button) view.findViewById(R.id.new_battle_button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent myIntent = new Intent(MainActivity.this, BattleActivity.class);
				//MainActivity.this.startActivity(myIntent);
			}
		});
*/
		return view;
	}
	
	public boolean isDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}
	
	/**
	 * Users of this fragment must call this method to set up the navigation drawer interactions.
	 *
	 * @param fragmentId   The android:id of this fragment in its activity's layout.
	 * @param drawerLayout The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;
		
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the proper interactions
		// between the navigation drawer and the action bar app icon.

		mDrawerToggle = new ActionBarDrawerToggle(
				                                         getActivity(),                    /* host Activity */
				                                         mDrawerLayout,                    /* DrawerLayout object */
				                                         //R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
				                                         R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
				                                         R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}
				
				getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}
			
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}
				
				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				}
				
				getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()

			}
		};
		
		// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}
		
		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
	
	public void selectItem(int position) {
		mCurrentSelectedPosition = position;

        // itt tuti elvérzik
        if(position < 0 || position >= Battle.countBattles()) {
           getActivity().finish();
        } else {
            if (mDrawerListView != null) {
                mDrawerListView.setItemChecked(position, true);
            }
            if (mDrawerLayout != null) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
            if (mCallbacks != null) {
                mCallbacks.onNavigationDrawerItemSelected(position);
            }
        }
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mCallbacks = (NavigationDrawerCallbacks) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar. See also
		// showGlobalContextActionBar, which controls the top-left area of the action bar.
		if (mDrawerLayout != null && isDrawerOpen()) {
			//inflater.inflate(R.menu.global, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
			case R.id.give_up:
// http://developer.android.com/guide/topics/ui/dialogs.html

				AlertDialog alert = new AlertDialog.Builder(getActivity())
						                    .setTitle("Confirm")
						                    .setMessage(getString(R.string.battle_give_up_confirmation))
						                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
							                    @Override
							                    public void onClick(DialogInterface dialog, int which) {
								                    Battle.sBattles.get(mCurrentSelectedPosition).giveUp();

								                    if (Battle.countBattles() == mCurrentSelectedPosition) {
									                    selectItem(mCurrentSelectedPosition - 1);
								                    } else {
									                    selectItem(mCurrentSelectedPosition);
								                    }

								                    getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
								                    ((BattleActivity) getActivity()).onNavigationDrawerItemSelected(mCurrentSelectedPosition);

								                    dialog.dismiss();
							                    }

						                    })
						                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
							                    @Override
							                    public void onClick(DialogInterface dialog, int which) {
								                    dialog.dismiss();
							                    }
						                    })
						                    .setCancelable(true)
						                    .create();
				alert.show();

				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Per the navigation drawer design guidelines, updates the action bar to show the global app
	 * 'context', rather than just what's in the current screen.
	 */
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.title_activity_battle);
		//actionBar.setSubtitle(null);
	}
	
	private ActionBar getActionBar() {
		return ((AppCompatActivity) getActivity()).getSupportActionBar();
	}
	
	/**
	 * Callbacks interface that all activities using this fragment must implement.
	 */
	public interface NavigationDrawerCallbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(int position);
	}
}
