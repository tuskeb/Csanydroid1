package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;
import hu.csany_zeg.one.csanydroid1.core.Player;

public class HeroSelectorActivityFragment extends Fragment {

	Battle mTheBattle = null; // TODO

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hero_selector, container, false);

		final GridView gridView = (GridView) view.findViewById(R.id.grid_layout);

		final EditText nameEditText = (EditText) view.findViewById(R.id.battleNameEditText);
		nameEditText.setText(Battle.getNextName(null));
		nameEditText.selectAll();

		view.findViewById(R.id.heroSelectOK).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTheBattle == null) {
					// TODO szétszedni egy másik gombra
					mTheBattle = new Battle(nameEditText.getText().toString());
					nameEditText.setText(mTheBattle.getName());
					nameEditText.setEnabled(false);

					mTheBattle.addPlayer(Player.CURRENT, true);

					//Toast.makeText(getActivity(), "Battle successfully created. Waiting for other players...", Toast.LENGTH_SHORT).show();

					//return;
                    // ennek csak a bluetooth-os résznél lett volna jelentősége
				}

				ArrayList<String> names = new ArrayList<String>();
				for (int i = 0; i < gridView.getChildCount(); i++) {
					CheckBox cb = (CheckBox) ((gridView.getChildAt(i)).findViewById(R.id.grid_HeroSelectHeroName));
					if (cb.isChecked()) {
						names.add(cb.getText().toString());
					}
				}

				if (names.size() < 2) {
					Toast.makeText(getActivity(), "You need minimum two heroes.", Toast.LENGTH_LONG).show();
				} else {
					try {
						for (String name : names) {
							mTheBattle.addHero(Hero.findHero(name));
						}

						mTheBattle.setPlayerReady(Player.CURRENT);
						getActivity().finish();
					} catch (Exception ignore) { }
				}

			}
		});

		final ArrayList<Hero> mHeroList = Hero.getFreeHeroes();

		gridView.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				return mHeroList.size();
			}

			@Override
			public Object getItem(int position) {
				return mHeroList.get(position);
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			public View getView(int position, View convertView, ViewGroup parent) {

				LayoutInflater inflater = (LayoutInflater) getContext()
						                                           .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View gridView = (convertView == null ? inflater.inflate(R.layout.hero_select_view, null) : convertView);
				final Hero hero = mHeroList.get(position);

				((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName))
						.setText(hero.getName());

				((ImageView) gridView.findViewById(R.id.grid_HeroSelectHeroImage))
						.setImageResource(hero.getHeroImageID());

				((TextView) gridView.findViewById(R.id.grid_HeroSelectHealthText))
						.setText(String.valueOf((int) hero.getHealthPoint()));
				//((ImageView) gridView.findViewById(R.id.grid_HeroSelectHealthImage)).setImageResource(heroList.get(position).getHealthImageID());

				((TextView) gridView.findViewById(R.id.grid_HeroSelectCharmText))
						.setText(String.valueOf((int) hero.getCharm()));
				// ((ImageView) gridView.findViewById(R.id.grid_HeroSelectCharmImage)).setImageResource(heroList.get(position).getCharmImageID());

				((TextView) gridView.findViewById(R.id.grid_HeroSelectOffensiveText))
						.setText(String.valueOf((int) hero.getBaseOffensivePoint()));
				//((ImageView) gridView.findViewById(R.id.grid_HeroSelectOffensiveImage)).setImageResource(heroList.get(position).getOffensiveImageID());

				((TextView) gridView.findViewById(R.id.grid_HeroSelectDefensiveText))
						.setText(String.valueOf((int) hero.getBaseDefensivePoint()));
				//((ImageView) gridView.findViewById(R.id.grid_HeroSelectDefensiveImage)).setImageResource(heroList.get(position).getDefensiveImageID());

               /* if (mHeroList.get(position).getBattle() != null)
                {
                    ((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName)).setEnabled(false);
                    gridView.setAlpha(.2f);
                }
                else
                {
	                */
				((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						((RelativeLayout) buttonView.getParent()).setAlpha(isChecked ? 1 : .2f);
					}
				});

				((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName)).setChecked(true);

				gridView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox checkBox = (CheckBox) v.findViewById(R.id.grid_HeroSelectHeroName);
						checkBox.setChecked(!checkBox.isChecked());
					}
				});
				return gridView;
			}

		});

/*
		gridView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				view.setSelected(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				if (v.isSelected()) {
					v.setAlpha(1f);
					v.setBackgroundColor(Color.GREEN);
					v.setSelected(false);
				} else {
					v.setAlpha(0.4f);
					v.setBackgroundColor(Color.RED);
					v.setSelected(true);
				}
			}
		});

*/

		return view;

	}

}
