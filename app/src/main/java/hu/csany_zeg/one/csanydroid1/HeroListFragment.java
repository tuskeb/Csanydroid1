package hu.csany_zeg.one.csanydroid1;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import hu.csany_zeg.one.csanydroid1.core.Hero;

/**
 * A list fragment representing a list of Heros. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link HeroDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class HeroListFragment extends ListFragment {
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};
	ArrayAdapter<Hero> mArrayAdapter;

	DataSetObserver arrayAdapterObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			mArrayAdapter.notifyDataSetChanged();
		}
	};


	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;
	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public HeroListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mArrayAdapter = new ArrayAdapter<Hero>(getActivity(), R.layout.listitem_hero, Hero.sHeroRepository) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				if (convertView == null) {
					convertView = View.inflate(getContext(), R.layout.listitem_hero, null);
				}

				final Hero hero = getItem(position);

				((TextView) convertView.findViewById(R.id.text1)).setText(hero.getName());
				((ImageView) convertView.findViewById(R.id.imageview1)).setImageResource(hero.IsFavourite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

				return convertView;
			}

		};

		Hero.getGlobalObservable().registerObserver(arrayAdapterObserver);
		setListAdapter(mArrayAdapter);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Hero.getGlobalObservable().unregisterObserver(arrayAdapterObserver);

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				    && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		} else {
			setActivatedPosition(ListView.INVALID_POSITION);
		}

	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Activity activity = getActivity();

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		selectItem(position, true);
	}

	public void selectItem(int position, boolean user) {

		mCallbacks.onItemSelected(position >= 0 ? Hero.getHero(position).getName() : null);
		setActivatedPosition(position);

		if (!user) getListView().setSelection(position);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}

	}
	
	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick
				                            ? ListView.CHOICE_MODE_SINGLE
				                            : ListView.CHOICE_MODE_NONE);
	}
	
	private void setActivatedPosition(int position) {

		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}
}
