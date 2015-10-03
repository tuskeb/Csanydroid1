package hu.csany_zeg.one.csanydroid1.core;

import android.database.DataSetObservable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import hu.csany_zeg.one.csanydroid1.MainActivity;

public class Hero extends DataSetObservable implements Cloneable {

	private String mName;

	private short mHealthPoint;

	private float mCharm;

	private float mOffensivePoint;

	private float mDefensivePoint;


	// TODO? statisztika az összes támadásról és védekezésről
	private short killedOpponents = 0;

	public Hero() {

		// választ egy véletlen nevet
		final String NAMES[] = new String[]{"Lekvároscsusza", "Lekvárosbukta", "Túrosgombóc", "Barack", "Szilva", "Krokodil", "Pilóta keksz"};
		mName = NAMES[(int) (NAMES.length * Math.random())];

		// és feltölti véletlen értékekkel
		mHealthPoint = (short) (Math.random() * 491f + 10);
		mCharm = (float) (Math.random() * 20.0);
		mOffensivePoint = (float) (Math.random() * 10f) + 1;
		mDefensivePoint = (float) (Math.random() * 10f) + 1;

		sHeroes.add(this);
	}

	public void dispose() {
		sHeroes.remove(this);

	}

	protected Hero clone() {
		try {
			return (Hero) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public float finalizeOffensivePoint() {
		final float RAND_MIN = .7f, RAND_MAX = 1.15f;
		return mOffensivePoint * ((RAND_MIN + (float) Math.random() * (RAND_MAX - RAND_MIN)) + (useCharm() / 5));
	}

	private float finalizeDefensivePoint() {
		final float RAND_MIN = .5f, RAND_MAX = 1.3f;
		return mDefensivePoint * ((RAND_MIN + (float) Math.random() * (RAND_MAX - RAND_MIN)) + (useCharm() / 2.5f));
	}

	private float useCharm() {
		if (mCharm == 0) return 0f; // elfogyott a varázsereje

		final float usedCharm = Math.min(mCharm, 5f);

		mCharm -= usedCharm;
		return usedCharm;

	}


	public static ArrayList<Hero> sHeroes = new ArrayList<Hero>();
	public static final Handler sEventHandler = new Handler(Looper.getMainLooper());
	public static HeroListener mListener = null;
	public static Hero sAttackerHero = null, sDefensiveHero = null;

	public static Thread sBattleThread = new Thread() {

		@Override
		public void run() {
			final Hero old = sAttackerHero;

			sAttackerHero = sHeroes.get((sAttackerHero != null ? sHeroes.indexOf(sAttackerHero) + 1 : 0) % sHeroes.size());

			if (mListener != null) mListener.onAttackerHeroChanged(old);
		}

	};

	private final Runnable sAttackerHeroChange = new Runnable() {
		@Override
		public void run() {
			final Hero old = sAttackerHero;

			sAttackerHero = sHeroes.get((sAttackerHero != null ? sHeroes.indexOf(sAttackerHero) + 1 : 0) % sHeroes.size());

			if (mListener != null) mListener.onAttackerHeroChanged(old);
		}
	};

	private final Runnable sDefensiveHeroChange = new Runnable() {
		@Override
		public void run() {
			final Hero old = sAttackerHero;

			while (sAttackerHero == (sDefensiveHero = sHeroes.get((int) (Math.random() * sHeroes.size()))))
				;

			if (mListener != null) mListener.onDefensiveHeroChanged(old);

		}
	};

	private final Runnable sCharmChange = new Runnable() {
		@Override
		public void run() {
			final float old = mCharm;

			while (sAttackerHero == (sDefensiveHero = sHeroes.get((int) (Math.random() * sHeroes.size())))) {}

			if (mListener != null) mListener.onCharmChanged(1f);


		}
	};

	public void receiveDemage(float lostLife) {
		assert lostLife > 0;

		sDefensiveHero.mHealthPoint -= lostLife;
		if (sDefensiveHero.mHealthPoint <= 0) {
			sDefensiveHero.mHealthPoint = 0;

			++sAttackerHero.killedOpponents;
			// a játékos halott :(

		}

	}

	public static void duel(Hero oppHero) {
		final float offensivePoint;
		offensivePoint = sAttackerHero.finalizeOffensivePoint();
		final float defensivePoint = sDefensiveHero.finalizeDefensivePoint();

		if (offensivePoint > defensivePoint) {
			// a védekező játékos életet veszít
			sDefensiveHero.receiveDemage(offensivePoint - defensivePoint);
		}
	}

	// TODO implementálni kell
	public Hero(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
		super.notifyChanged();
	}

	public short getHealthPoint() {
		return mHealthPoint;
	}

	public void setHealthPoint(short healthPoint) {
		mHealthPoint = healthPoint;
		super.notifyChanged();
	}

	public float getCharm() {
		return mCharm;
	}

	public void setCharm(float charm) {
		mCharm = charm;
		super.notifyChanged();
	}

	public float getOffensivePoint() {
		return mOffensivePoint;
	}

	public void setOffensivePoint(float offensivePoint) {
		mOffensivePoint = offensivePoint;
		super.notifyChanged();
	}

	public float getDefensivePoint() {
		return mDefensivePoint;
	}

	public void setDefensivePoint(float defensivePoint) {
		mDefensivePoint = defensivePoint;
		super.notifyChanged();
	}

	public short getKilledOpponents() {
		return killedOpponents;
	}

	public void setKilledOpponents(short killedOpponents) {
		this.killedOpponents = killedOpponents;
		super.notifyChanged();
	}

	@Override
	public String toString() {
		return String.format("%s [ nm=%s, hp=%d, ch=%f, op=%f, dp=%f ]", getClass().getName(), mName, mHealthPoint, mCharm, mOffensivePoint, mDefensivePoint);
	}

	public interface HeroListener {

		void onAttackerHeroChanged(Hero oldHero);

		void onDefensiveHeroChanged(Hero oldHero);

		void onCharmChanged(float oldCharm);

		void onHealthPointChanged(float oldHealthPoint);

	}

}
