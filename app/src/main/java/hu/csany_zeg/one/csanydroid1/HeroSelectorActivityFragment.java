package hu.csany_zeg.one.csanydroid1;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;

/**
 * A placeholder fragment containing a simple view.
 */
public class HeroSelectorActivityFragment extends Fragment {

    public HeroSelectorActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hero_selector, container, false);

        GridView gridView = (GridView)view.findViewById(R.id.grid_layout);

        gridView.setAdapter(new ArrayAdapter<Hero>(
                getContext(),
                R.layout.fragment_hero_icon,
                Hero.sHeroRepository) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.fragment_hero_icon, null);
                }

                ((TextView) convertView.findViewById(R.id.hero_name)).setText(getItem(position).getName());
                //((TextView)convertView.findViewById(android.R.id.text2)).setText(getItem(position).getRound());

                return convertView;
            }

        });

        for(int i = Hero.countHeroes(); i> 0;) {
            Hero hero = Hero.getHero(--i);
            View heroIconView = inflater.inflate(R.layout.fragment_hero_icon, container, false);

            heroIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("mama", "kacsa");
                }
            });

            TextView textView = (TextView)heroIconView.findViewById(R.id.hero_name);
            textView.setText(hero.getName());

           // HeroIconFragment heroIconFragment = HeroIconFragment.newInstance(hero);
            gridView.addView(heroIconView, 0);
        }
        return view;

        // ((GridLayout)this.findViewById(R.id.gridLayout)).getchild
    }
}
