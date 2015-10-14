package hu.csany_zeg.one.csanydroid1;

import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import hu.csany_zeg.one.csanydroid1.core.Hero;

/**
 * A fragment representing a single Hero detail screen.
 * This fragment is either contained in a {@link HeroListActivity}
 * in two-pane mode (on tablets) or a {@link HeroDetailActivity}
 * on handsets.
 */
public class HeroDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	
	/**
	 * The dummy content this fragment is presenting.
	 */
	private Hero mHero;

	private TextView offensiveTextViewNum;
	private TextView defensiveTextViewNum;
	private TextView charmTextViewNum;

	private ImageView charmImageView;
	private ImageView offensiveImageView;
	private ImageView defensiveImageView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public HeroDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {

			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			String itemId = getArguments().getString(ARG_ITEM_ID);
			mHero = itemId != null ? Hero.findHeroByName(itemId) : null;
		} else Log.v("mama", "karcsi");

	}

	private void loadCharmImageView(int imageID)
	{
		switch (imageID) {
			case 0:
				charmImageView.setImageDrawable(getResources().getDrawable(R.drawable.magic1));
				break;
			case 1:
				charmImageView.setImageDrawable(getResources().getDrawable(R.drawable.magic2));
				break;
			case 2:
				charmImageView.setImageDrawable(getResources().getDrawable(R.drawable.magic3));
				break;
			case 3:
				charmImageView.setImageDrawable(getResources().getDrawable(R.drawable.magic4));
				break;
			case 4:
				charmImageView.setImageDrawable(getResources().getDrawable(R.drawable.magic5));
				break;
			default:
				//charmImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_3));
				break;
		}

	}

	private void loadOffensiveImageView(int imageID)
	{
		switch (imageID) {
			case 0:
				offensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_1s));
				break;
			case 1:
				offensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_2s));
				break;
			case 2:
				offensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_3));
				break;
			case 3:
				offensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_4s));
				break;
			case 4:
				offensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_5s));
				break;
			default:
				//offensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_3));
				break;
		}

	}

	private void loadDefensiveImageView(int imageID)
	{
		switch (imageID) {
			case 0:
				defensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.shield1));
				break;
			case 1:
				defensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.shield2));
				break;
			case 2:
				defensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.shield3));
				break;
			case 3:
				defensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.shield4));
				break;
			case 4:
				defensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.shield5));
				break;
			default:
				//defensiveImageView.setImageDrawable(getResources().getDrawable(R.drawable.weapon_3));
				break;
		}

	}


	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View rootView;

		if (mHero == null) {
			rootView = inflater.inflate(R.layout.fragment_hero_overalldetail, container, false);
			((TextView) rootView.findViewById(R.id.number_of_heros)).setText(Hero.countHeroes() + "");
		} else {
			rootView = inflater.inflate(R.layout.fragment_hero_detail, container, false);

			//final TextView textViewCharmNum;


			charmTextViewNum = (TextView) rootView.findViewById(R.id.charmTextViewNum);
			defensiveTextViewNum = (TextView) rootView.findViewById(R.id.defensiveTextViewNum);
			offensiveTextViewNum = (TextView) rootView.findViewById(R.id.offensiveTextViewNum);


			charmImageView = (ImageView) rootView.findViewById(R.id.charmImageView);
			offensiveImageView = (ImageView) rootView.findViewById(R.id.offensiveImageView);
			defensiveImageView = (ImageView) rootView.findViewById(R.id.defensiveImageView);



			EditText editText;

			editText = (EditText) rootView.findViewById(R.id.hero_name);
			editText.setText(mHero.getName());
			/*
			editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					boolean isValid = mHero.isValidName(v.getText().toString());

					if (isValid) {
						mHero.setName(v.getText().toString());
						return false;
					} else {
						AlertDialog alert = new AlertDialog.Builder(getActivity())
								                    .setTitle("Invalid Name")
								                    .setMessage("This is an invalid name!")
								                    .setCancelable(true)
								                    .create();
						alert.show();

						return true;
					}
				}
			});
*/

			SeekBar seekBar;



//Ez a offensivebar porgramozása
			seekBar = (SeekBar) rootView.findViewById(R.id.offensiveBar);
			seekBar.setMax((int) (Hero.MAX_OFFENSIVE_POINT - Hero.MIN_OFFENSIVE_POINT));
			offensiveTextViewNum.setText(String.valueOf(Math.round(mHero.getBaseOffensivePoint())));
			seekBar.setProgress(Math.round(Math.round(mHero.getBaseOffensivePoint() - Hero.MIN_OFFENSIVE_POINT)));
			seekBar.setEnabled(mHero.canModify());
			loadOffensiveImageView(mHero.getOffensiveImageID());
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					int lastBaseOffensiveImageID = mHero.getCharmImageID();
					mHero.setOffensivePoint((float) progress + Hero.MIN_OFFENSIVE_POINT);
					offensiveTextViewNum.setText(String.valueOf(progress + Hero.MIN_OFFENSIVE_POINT));

					try{

						mHero.setOffensivePoint((float) progress+Hero.MIN_OFFENSIVE_POINT);
					}
					catch (Exception e){ }
					if (lastBaseOffensiveImageID != mHero.getOffensiveImageID()) {
						loadOffensiveImageView(mHero.getOffensiveImageID());
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});



//Ez a defensivebar programozása
			seekBar = (SeekBar) rootView.findViewById(R.id.defensiveBar);
			seekBar.setMax((int) (Hero.MAX_DEFENSIVE_POINT - Hero.MIN_DEFENSIVE_POINT));
			defensiveTextViewNum.setText(String.valueOf(Math.round(mHero.getBaseDefensivePoint())));
			seekBar.setProgress(Math.round(Math.round(mHero.getBaseDefensivePoint() - Hero.MIN_DEFENSIVE_POINT)));
			seekBar.setEnabled(mHero.canModify());
			loadDefensiveImageView(mHero.getDefensiveImageID());
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					int lastBaseDefensiveImageID = mHero.getCharmImageID();
					defensiveTextViewNum=(TextView)rootView.findViewById(R.id.defensiveTextViewNum);
					mHero.setDefensivePoint((float) progress + Hero.MIN_DEFENSIVE_POINT);
					defensiveTextViewNum.setText(String.valueOf(progress + Hero.MIN_DEFENSIVE_POINT));
					try {
						mHero.setDefensivePoint((float) progress + Hero.MIN_DEFENSIVE_POINT);
					}
					catch (Exception e){}
					if (lastBaseDefensiveImageID != mHero.getDefensiveImageID()) {
						loadDefensiveImageView(mHero.getDefensiveImageID());
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { }

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { }
			});




//Ez a charmbar programozása
			seekBar = (SeekBar) rootView.findViewById(R.id.charmBar);
			seekBar.setMax((int) (Hero.MAX_CHARM - Hero.MIN_CHARM));
			charmTextViewNum.setText(String.valueOf(Math.round(mHero.getCharm())));
			loadCharmImageView(mHero.getCharmImageID());
			seekBar.setProgress(Math.round(mHero.getCharm() - Hero.MIN_CHARM));
			seekBar.setEnabled(mHero.canModify());
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					int lastBaseCharmImageID = mHero.getCharmImageID();


					try {
						mHero.setBaseCharm((float) progress + Hero.MIN_CHARM);
					} catch (Exception e) {
					}

					charmTextViewNum.setText(String.valueOf(progress + Hero.MIN_CHARM));

					if (lastBaseCharmImageID != mHero.getCharmImageID()) {
						loadCharmImageView(mHero.getCharmImageID());
					}


				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});





			ImageButton button;
			button = (ImageButton) rootView.findViewById(R.id.remove_hero);
			button.setEnabled(mHero.canModify());
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog alert = new AlertDialog.Builder(getActivity())
							.setTitle("Confirm")
							.setMessage("Do you sure want to remove this hero from your repository?")
							.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

									try {
										final int position = mHero.getRepositoryIndex();

										mHero.remove();

										HeroListFragment heroListFragment = (HeroListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.hero_list);
										((ArrayAdapter) heroListFragment.getListAdapter()).notifyDataSetChanged();

										if (Hero.countHeroes() == position) {
											heroListFragment.selectItem(position - 1, false);
										} else {
											heroListFragment.selectItem(position, false);
										}

									} catch (Exception e) {

									}
									dialog.dismiss();
								}

							})
							.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.create();
					alert.show();

				}
			});


			button = (ImageButton) rootView.findViewById(R.id.add_to_favourites_hero);
			button.setImageResource(mHero.IsFavourite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mHero.setFavourite(!mHero.IsFavourite());
					((ImageButton) v).setImageResource(mHero.IsFavourite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
				}
			});

		}

		return rootView;
	}
}
