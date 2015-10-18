package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class HeroSelectorActivityFragment extends Fragment {

	public class HeroSelectViewAdapter extends BaseAdapter {
		private Context mContext;
		private final ArrayList<Hero> mHeroList;

		public HeroSelectViewAdapter(Context context, ArrayList<Hero> heroList) {
			this.mContext = context;
			this.mHeroList = heroList;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) mContext
					                                           .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View gridView;

			if (convertView == null) {
				//gridView = new View(mContext);
				// get layout from mobile.xml
				gridView = inflater.inflate(R.layout.hero_select_view, null);
			} else {
				gridView = convertView;
			}

			((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName))
					.setText(mHeroList.get(position).getName());

			((ImageView) gridView.findViewById(R.id.grid_HeroSelectHeroImage))
					.setImageResource(mHeroList.get(position).getHeroImageID());

			((TextView) gridView.findViewById(R.id.grid_HeroSelectHealthText))
					.setText(String.valueOf((int) mHeroList.get(position).getHealthPoint()));
			//((ImageView) gridView.findViewById(R.id.grid_HeroSelectHealthImage)).setImageResource(heroList.get(position).getHealthImageID());

			((TextView) gridView.findViewById(R.id.grid_HeroSelectCharmText))
					.setText(String.valueOf((int) mHeroList.get(position).getCharm()));
			// ((ImageView) gridView.findViewById(R.id.grid_HeroSelectCharmImage)).setImageResource(heroList.get(position).getCharmImageID());

			((TextView) gridView.findViewById(R.id.grid_HeroSelectOffensiveText))
					.setText(String.valueOf((int) mHeroList.get(position).getBaseOffensivePoint()));
			//((ImageView) gridView.findViewById(R.id.grid_HeroSelectOffensiveImage)).setImageResource(heroList.get(position).getOffensiveImageID());

			((TextView) gridView.findViewById(R.id.grid_HeroSelectDefensiveText))
					.setText(String.valueOf((int) mHeroList.get(position).getBaseDefensivePoint()));
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

			//}
		//}

		return gridView;
	}

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

}


	public HeroSelectorActivityFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hero_selector, container, false);

		final GridView gridView = (GridView) view.findViewById(R.id.grid_layout);

		//  final Button create = (Button) ;
		final EditText name = (EditText) view.findViewById(R.id.battleNameEditText);
		//final Activity activity = this.getActivity();
		name.setText(Battle.getNextName());
		name.selectAll();

		view.findViewById(R.id.heroSelectOK).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (name.getText().toString().equalsIgnoreCase("")) {
					Toast.makeText(getActivity(), "Hero name is empty.", Toast.LENGTH_SHORT).show();
				} else {
					ArrayList<String> names = new ArrayList<String>();
					for (int i = 0; i < gridView.getChildCount(); i++) {
						CheckBox cb = (CheckBox) ((gridView.getChildAt(i)).findViewById(R.id.grid_HeroSelectHeroName));
						if (cb.isChecked()) {
							names.add(cb.getText().toString());
						}
					}
					if (names.size() < 2) {
						Toast.makeText(getActivity(), "You need minimum two heroes.", Toast.LENGTH_SHORT).show();
					} else {
						try {
							Battle battle = new Battle(name.getText().toString());
							battle.addPlayer(Player.CURRENT, true);
							for (String name : names) {
								battle.addHero(Hero.findHeroByName(name));
							}
							//battle.setPlayerReady(Player.CURRENT);
							getActivity().finish();
						} catch (Exception e) {
							Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				}
			}
		});

		// TODO a létrehozás és, hogy kész a játékos más gombon kell, hogy legyenek.

		gridView.setAdapter(new HeroSelectViewAdapter(getActivity(), Hero.getFreeHeroes()));


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

		return view;

	}
}
