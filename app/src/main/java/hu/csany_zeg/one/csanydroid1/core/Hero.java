package hu.csany_zeg.one.csanydroid1.core;

import android.database.DataSetObservable;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

import hu.csany_zeg.one.csanydroid1.App;
import hu.csany_zeg.one.csanydroid1.R;

public class Hero extends DataSetObservable implements Cloneable {

	private static final String TAG = "hero";

	public static ArrayList<Hero> sHeroRepository = new ArrayList<Hero>();
private static DataSetObservable mGlobalObservable = new DataSetObservable();
	public static DataSetObservable getGlobalObservable() {
		return mGlobalObservable;
	}
	public static short MIN_HEALTH = 10, MAX_HEALTH = 500;
	public static float MIN_CHARM = 0.0f, MAX_CHARM = 20.0f;
	public static float MIN_OFFENSIVE_POINT = 1.0f, MAX_OFFENSIVE_POINT = 10.0f;
	public static float MIN_DEFENSIVE_POINT = 1.0f, MAX_DEFENSIVE_POINT = 10.0f;
	private final Player mOwner;

	private Battle mBattle = null;
	private HeroParams mParams = null;

	/**
	 * A hős neve.
	 */
	protected final String mName;

	/**
	 * A hős élete.
	 */
	protected float mHealthPoint;

	/**
	 * Felhasználható varázsereje.
	 */
	protected float mCharm;

	/**
	 * Alap varázsereje.
	 */
	protected float mBaseCharm;

	/**
	 * Támadási értéke.
	 */
	protected float mOffensivePoint;

	/**
	 * Védekezési értéke.
	 */
	protected float mDefensivePoint;
	private float mDrunkCharm;

	/**
	 * Létrehoz egy helyi hőst.
	 */
	public Hero(String name) throws RuntimeException {
		this(Player.CURRENT, name != null ? name : getNextName());

		// TODO review
		mHealthPoint = (short) (Math.random() * (MAX_HEALTH - MIN_HEALTH + 1) + MIN_HEALTH);
		mBaseCharm = (float) Math.random() * (MAX_CHARM + Float.MIN_VALUE);
		mOffensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;
		mDefensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;

		sHeroRepository.add(this);

		Log.v(TAG, "created");
		super.notifyChanged();

	}

	public Hero(Player owner, String name) throws RuntimeException {
		if(findHeroByName(name) != null) throw new RuntimeException("Name must be unique.");
		mOwner = owner;
		mName = name;

		super.notifyChanged();

	}

	public boolean setBattle(Battle battle) throws Battle.InvalidPlayerException {

		if(isBattle()) {
			// csatázik már
			return false;
		}

		// csata ellenőrzése

		if(battle.addHero(this)) {
			mBattle = battle;
		}

		return true;

	}

	public void removeBattle() throws Exception {
		if(isBattle()) return;

		if(mBattle.getState() == 1) {
			// befejeződött
			mBattle = null;
		} else {
			throw new Exception();
		}

	}

	public boolean isBattle() {
		return mBattle != null;
	}

	public Battle getBattle() {
		return mBattle;
	}

	public Player getOwner() {
		return mOwner;
	}

	protected Hero clone() {
		try {
			return (Hero) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public float getDrunkCharm() {
		return mDrunkCharm;
	}

	public boolean isAlive() { return this.mHealthPoint > 0; }

	/**
	 * @param r A megivás valószínűsége.
	 */
	public float drinkCharm(double r) {
		if (Math.random() < r) {
			mDrunkCharm = Math.min(mCharm, mBattle.MAX_USABLE_CHARM); // "maximális varázsereje"??
			mCharm -= mDrunkCharm;

			this.mTotalDrunkCharm += mDrunkCharm;

		} else { // ne használjon ilyen szereket
			mDrunkCharm = 0;
		}

		super.notifyChanged();
		return mDrunkCharm;
	}

	public String getName() {
		return mName;
	}

	public float getHealthPoint() {
		return mHealthPoint;
	}

	public boolean receiveDamage(float lostLife) {
		if (lostLife <= 0) return false;

		this.mHealthPoint -= lostLife;

		if (!this.isAlive()) {
			super.notifyChanged();
		}
		return true;

	}

	public float getCharm() { return mBaseCharm; }

	public float getBaseOffensivePoint() {
		return mOffensivePoint;
	}

	public float getBaseDefensivePoint() { return mDefensivePoint; }

	public static Hero getHero(int i) {
		return sHeroRepository.get(i);
	}

	public int getRepositoryIndex() {
		return sHeroRepository.indexOf(this);
	}

	// TODO: másik név
	public static int countHeroes() {
		return sHeroRepository.size();
	}

	public boolean canModify() {
		return mBattle == null;
	}

public void remove() throws Exception {
	if(!canModify()) throw new Exception();

	Hero.sHeroRepository.remove(this);
}


	/**
	 * A hős összes védekezésének mértéke.
	 */
	float mTotalOffensivePoints;

	/**
	 * A hős összes támadásának mértéke.
	 */
	float mTotalDefensivePoints;

	/**
	 * A hős összes varázsereje.
	 */
	short mTotalDrunkCharm;

	/**
	 * A hős által legyőzött ellenfelek.
	 */
	short mTotalKills = 0;

	/**
	 * A hős halálainak száma.
	 */
	short mTotalDeathes = 0;

	/**
	 * A hős összes támadása.
	 */
	short mTotalAttacks;

	/**
	 * A hős összes védekezése.
	 */
	short mTotalDefensives;

	/**
	 * A hős összes küzdelme.
	 */
	short mTotalBattles;

	boolean mIsFavourite;

	private static String getNextName() {
		final String baseName = App.getContext().getString(R.string.default_hero_name);
		String name;

		for (long n = 0; findHeroByName((name = baseName + " " + NumberFormat.getInstance().format(++n))) != null; );

		return name;

	}

	public static Hero findHeroByName(String name) {

		for (Hero hero : sHeroRepository) {
			if (hero.getName().compareToIgnoreCase(name) == 0) {
				return hero;
			}
		}

		return null;

	}

	public boolean IsFavourite() {
		return mIsFavourite;
	}

	public void setFavourite(boolean isFavourite) {
		mIsFavourite = isFavourite;
	}

	public void setBaseCharm(float baseCharm) throws Exception {
		if(mBattle != null)
			throw new Exception();

		mBaseCharm = baseCharm;
	}

	public boolean isValidName(String text) {
		text = text.trim();

		if (text.compareTo("") == 0) return false;

		for (Hero hero : sHeroRepository) {
			if (hero != this && hero.getName().compareToIgnoreCase(text) == 0) {
				return false;
			}
		}


		return true;
	}

	public void setOffensivePoint(float offensivePoint) {
		mOffensivePoint = offensivePoint;
	}

	public void setDefensivePoint(float defensivePoint) {
		mDefensivePoint = defensivePoint;
	}

	@Override
	public String toString() {
		return String.format("%s [ nm=%s, hp=%f, ch=%f, op=%f, dp=%f ]", getClass().getName(), mName, mHealthPoint, mCharm, mOffensivePoint, mDefensivePoint);
	}

	public class HeroParams {
		public float mHealthPoint;
		public float mOffensivePoint;
		public float mDefensivePoint;
		public float mCharm;

	}


}
