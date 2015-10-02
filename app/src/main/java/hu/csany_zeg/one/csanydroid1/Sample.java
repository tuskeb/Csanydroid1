package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.view.View;

import hu.csany_zeg.one.csanydroid1.core.Hero;

/**
 * Created by tanuló on 2015.10.02..
 */
public class Sample extends View {
    private Hero hero;
    public Sample(Context context, Hero hero) {
        super(context);
        this.hero=hero;
    }

}
