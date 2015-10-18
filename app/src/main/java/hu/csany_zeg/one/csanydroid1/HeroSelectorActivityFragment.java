package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;

/**
 * A placeholder fragment containing a simple view.
 */
public class HeroSelectorActivityFragment extends Fragment {

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private final ArrayList<Hero> heroList;

        public ImageAdapter(Context context, ArrayList<Hero> heroList) {
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

                // set value into textview
                TextView textView = (TextView) gridView.findViewById(R.id.grid_item_label);
                textView.setText(heroList.get(position).getName());

                // set image based on selected text
                ImageView imageView = (ImageView) gridView
                        .findViewById(R.id.grid_item_image);
                imageView.setImageResource(heroList.get(position).getHeroImageID());

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
            return null;
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

        GridView gridView = (GridView)view.findViewById(R.id.grid_layout);

        gridView.setAdapter(new ImageAdapter(this.getActivity(), Hero.sHeroRepository));

        return view;

    }
}
