package hu.csany_zeg.one.csanydroid1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
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

    public class heroSelectViewAdapter extends BaseAdapter {
        private Context context;
        private final ArrayList<Hero> heroList;

        public heroSelectViewAdapter(Context context, ArrayList<Hero> heroList) {
            this.context = context;
            this.heroList = heroList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView=null;

            if (convertView == null) {

                gridView = new View(context);

                // get layout from mobile.xml
                gridView = inflater.inflate(R.layout.hero_select_view, null);


                ((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName)).setText(heroList.get(position).getName());
                ((ImageView) gridView.findViewById(R.id.grid_HeroSelectHeroImage)).setImageResource(heroList.get(position).getHeroImageID());

                ((TextView) gridView.findViewById(R.id.grid_HeroSelectHealthText)).setText(String.valueOf((int) heroList.get(position).getHealthPoint()));
                ((ImageView) gridView.findViewById(R.id.grid_HeroSelectHealthImage)).setImageResource(heroList.get(position).getHealthImageID());

                ((TextView) gridView.findViewById(R.id.grid_HeroSelectCharmText)).setText(String.valueOf((int) heroList.get(position).getCharm()));
                ((ImageView) gridView.findViewById(R.id.grid_HeroSelectCharmImage)).setImageResource(heroList.get(position).getCharmImageID());

                ((TextView) gridView.findViewById(R.id.grid_HeroSelectOffensiveText)).setText(String.valueOf((int) heroList.get(position).getBaseOffensivePoint()));
                ((ImageView) gridView.findViewById(R.id.grid_HeroSelectOffensiveImage)).setImageResource(heroList.get(position).getOffensiveImageID());

                ((TextView) gridView.findViewById(R.id.grid_HeroSelectDefensiveText)).setText(String.valueOf((int) heroList.get(position).getBaseDefensivePoint()));
                ((ImageView) gridView.findViewById(R.id.grid_HeroSelectDefensiveImage)).setImageResource(heroList.get(position).getDefensiveImageID());

                if (heroList.get(position).isBattle())
                {
                    ((CheckBox) gridView.findViewById(R.id.grid_HeroSelectHeroName)).setEnabled(false);
                    gridView.setAlpha(0.2f);
                }
                else
                {

                }
            } else {
                gridView = (View) convertView;
            }

            return gridView;
        }

        @Override
        public int getCount() {
            return heroList.size();
        }

        @Override
        public Object getItem(int position) {
            return heroList.get(position);
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

        final Button create = (Button) view.findViewById(R.id.heroSelectOK);
        final EditText name = (EditText) view.findViewById(R.id.battleNameEditText);
        final Activity activity = this.getActivity();
        String defaultBattleName="New Battle";
        String defaultBattleName2=defaultBattleName;
        int bn=2;
        while (Battle.findBattle(defaultBattleName2)!=null)
        {
            defaultBattleName2=defaultBattleName+" " + bn;
            bn++;
        }
        name.setText(defaultBattleName2);
        name.selectAll();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(activity, "Hero name is empty.", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<String> names = new ArrayList<String>();
                    for (int i = 0; i < gridView.getChildCount(); i++) {
                        CheckBox cb = (CheckBox) ((gridView.getChildAt(i)).findViewById(R.id.grid_HeroSelectHeroName));
                        if (cb.isChecked()) {
                            names.add(cb.getText().toString());
                        }
                    }
                    if (names.size() < 2) {
                        Toast.makeText(activity, "You need minimum two heroes.", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Battle battle = new Battle(name.getText().toString());
                            battle.addPlayer(Player.CURRENT, true);
                            for (String s : names) {
                                try {
                                    Hero.findHeroByName(s).setBattle(battle);
                                } catch (Battle.InvalidPlayerException e) {
                                    e.printStackTrace();
                                }
                            }
                            //battle.setPlayerReady(Player.CURRENT);
                            activity.finish();
                        } catch (Exception e) {
                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        gridView.setAdapter(new heroSelectViewAdapter(this.getActivity(), Hero.sHeroRepository));


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
/*
                if (v.isSelected()) {
                    v.setAlpha(1f);
                    v.setBackgroundColor(Color.GREEN);
                    v.setSelected(false);
                } else {
                    v.setAlpha(0.4f);
                    v.setBackgroundColor(Color.RED);
                    v.setSelected(true);
                }*/
            }
        });

        return view;

    }
}
