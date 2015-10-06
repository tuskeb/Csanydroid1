package hu.csany_zeg.one.csanydroid1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import hu.csany_zeg.one.csanydroid1.core.LocalHero;


/**
 * An activity representing a list of Heros. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link HeroDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link HeroListFragment} and the item details
 * (if present) is a {@link HeroDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link HeroListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class HeroListActivity extends FragmentActivity
		implements HeroListFragment.Callbacks {
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	private HeroListFragment mHeroListFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_hero_list);

		mHeroListFragment = (HeroListFragment) getSupportFragmentManager().findFragmentById(R.id.hero_list);

		if ((mTwoPane = (findViewById(R.id.hero_detail_container) != null))) {

			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			mHeroListFragment.setActivateOnItemClick(true);
		}

		findViewById(R.id.add_hero_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//runOnUiThread(new Runnable() {
				//	@Override
				//	public void run() {
				final LocalHero newHero = new LocalHero();
				((ArrayAdapter) mHeroListFragment.getListAdapter()).notifyDataSetChanged();
				mHeroListFragment.selectItem(LocalHero.sHeros.indexOf(newHero), false);

				//	}
				//}
				//);

			}
		});

		// TODO: If exposing deep links into your app, handle intents here.

	}


	/**
	 * Callback method from {@link HeroListFragment.Callbacks}
	 * indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(HeroDetailFragment.ARG_ITEM_ID, id);
			HeroDetailFragment fragment = new HeroDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.hero_detail_container, fragment)
					.commit();
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, HeroDetailActivity.class);
			detailIntent.putExtra(HeroDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
}
