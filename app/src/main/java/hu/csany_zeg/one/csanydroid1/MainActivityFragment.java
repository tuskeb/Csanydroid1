package hu.csany_zeg.one.csanydroid1;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;

public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v, cv;



        v = inflater.inflate(R.layout.fragment_main, container, false);

       // cv = new HeroView(inflater.getContext(), Battle.sBattles.get(0).mHeros.get(0));
	   // ((LinearLayout)v.findViewById(R.id.linearLayout)).addView(cv);

	    //cv = new HeroView(inflater.getContext(), null);
        //((LinearLayout)v.findViewById(R.id.linearLayout)).addView(cv);
        //((RelativeLayout)v.findViewById(R.id.relativeLayout)).addView(new HeroView(inflater.getContext(), Hero.sHeroes.get(1)));



        return v;
    }

}
