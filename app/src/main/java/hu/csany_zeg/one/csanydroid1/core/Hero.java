package hu.csany_zeg.one.csanydroid1.core;

import android.database.DataSetObservable;
import android.os.Parcel;
import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

import hu.csany_zeg.one.csanydroid1.App;
import hu.csany_zeg.one.csanydroid1.R;

public class Hero extends DataSetObservable implements Cloneable {

	private static final String TAG = "hero";

	public static ArrayList<Hero> sHeroRepository = new ArrayList<Hero>();
	public static short MIN_HEALTH = 10, MAX_HEALTH = 20;
	public static int drawableHeroes[] =
			{
					R.drawable.hero1_blue,
					R.drawable.hero1_green,
					R.drawable.hero1_purple,
					R.drawable.hero1_red
			};
	public static int drawableOffensive[] =
			{
					R.drawable.weapon_1s,
					R.drawable.weapon_2s,
					R.drawable.weapon_3,
					R.drawable.weapon_4s,
					R.drawable.weapon_5s
			};
	public static int drawableDefensive[] =
			{
					R.drawable.shield1,
					R.drawable.shield2,
					R.drawable.shield3,
					R.drawable.shield4,
					R.drawable.shield5
			};
	public static int drawableCharms[] =
			{
					R.drawable.magic1,
					R.drawable.magic2,
					R.drawable.magic3,
					R.drawable.magic4,
					R.drawable.magic5
			};
	public static int drawableHealths[] =
			{
					R.drawable.heart,
					R.drawable.heart_1,
					R.drawable.heart_2,
					R.drawable.heart_3,
					R.drawable.heart_4
			};

	public static float MIN_CHARM = 0.0f, MAX_CHARM = 20.0f;
	public static float MIN_OFFENSIVE_POINT = 1.0f, MAX_OFFENSIVE_POINT = 10.0f;
	public static float MIN_DEFENSIVE_POINT = 1.0f, MAX_DEFENSIVE_POINT = 10.0f;
	private static DataSetObservable mGlobalObservable = new DataSetObservable();
	/**
	 * A hős neve.
	 */
	protected final String mName;
	private final Player mOwner;
	/**
	 * A hős képének az indexe az images tömbben
	 */
	protected int mPicture;
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
	private Battle mBattle = null;
	private HeroParams mParams = null;
	private float mDrunkCharm;

	/**
	 * Létrehoz egy helyi hőst.
	 */
	public Hero(String name) throws RuntimeException {
		if(name == null) name = getNextName();
		else if (findHeroByName(name) != null) throw new RuntimeException("Name must be unique.");

		mOwner = Player.CURRENT;
		mName = name;

		// TODO review
		mHealthPoint = (short) (Math.random() * (MAX_HEALTH - MIN_HEALTH + 1) + MIN_HEALTH);
		mBaseCharm = (float) Math.random() * (MAX_CHARM + Float.MIN_VALUE);
		mOffensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;
		mDefensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;

		sHeroRepository.add(this);

		Log.v(TAG, "created");
		super.notifyChanged();

	}

	public Hero(Player owner, String name) {
		mOwner = owner;
		mName = name;

		mOwner.send(new Player.Message(Player.ACTION_GET_HERO) {
			@Override
			public void extra(Parcel m) {
				m.writeString(mName);
			}
		});

		super.notifyChanged();

	}

	public static DataSetObservable getGlobalObservable() {
		return mGlobalObservable;
	}

	public static Hero getHero(int i) {
		return sHeroRepository.get(i);
	}

	// TODO: másik név
	public static int countHeroes() {
		return sHeroRepository.size();
	}

	private static String getNextName() {
		final String baseName = App.getContext().getString(R.string.default_hero_name);
		String name;

		for (long n = 0; findHeroByName((name = baseName + " " + NumberFormat.getInstance().format(++n))) != null; )
			;

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

	public int getPicture() {
		return mPicture;
	}

	public void setPicture(int Picture) {
		this.mPicture = Picture;
	}

	public boolean setBattle(Battle battle) {

		if (mBattle != null) {
			// csatázik már
			return false;
		}

		mBattle = battle;

		return true;

	}

	public void removeBattle() throws Exception {
		if (mBattle != null) return;

		if (mBattle.getState() == 1) {
			// befejeződött
			mBattle = null;
		} else {
			throw new Exception();
		}

	}

	public int getIndex() {
		if (mOwner != Player.CURRENT)
			return -1;
		return sHeroRepository.indexOf(this);
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

	public int getHeroImageID() {
		return drawableHeroes[mPicture];
	}

	public int getCharmImageID() {
		int x = (int) (((float) drawableCharms.length) * ((getCharm() - MIN_CHARM) / (MAX_CHARM - MIN_CHARM)));
		if (x >= drawableCharms.length) {
			return drawableCharms[drawableCharms.length - 1];
		} else {
			return drawableCharms[x];
		}
	}

	public int getOffensiveImageID() {
		int x = (int) (((float) drawableOffensive.length) * ((getBaseOffensivePoint() - MIN_OFFENSIVE_POINT) / (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT)));
		if (x >= drawableOffensive.length) {
			return drawableOffensive[drawableOffensive.length - 1];
		} else {
			return drawableOffensive[x];
		}
	}

	public int getDefensiveImageID() {
		int x = (int) (((float) drawableDefensive.length) * ((getBaseDefensivePoint() - MIN_DEFENSIVE_POINT) / (MAX_DEFENSIVE_POINT - MIN_DEFENSIVE_POINT)));
		if (x >= drawableDefensive.length) {
			return drawableDefensive[drawableDefensive.length - 1];
		} else {
			return drawableDefensive[x];
		}
	}

	public int getHealthImageID() {
		int x = (int) (((float) drawableHealths.length) * ((getHealthPoint() - MIN_HEALTH) / (MAX_HEALTH - MIN_HEALTH)));
		if (x >= drawableHealths.length) {
			return drawableHealths[drawableHealths.length - 1];
		} else {
			return drawableHealths[x];
		}

	}

	public float getDrunkCharm() {
		return mDrunkCharm;
	}

	public boolean isAlive() { return this.mHealthPoint > 0; }

	private static final Random RANDOM = new Random();

	/**
	 * @param r A megivás valószínűsége.
	 */
	public float drinkCharm(float r) {

		if (RANDOM.nextFloat() < r) {
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

		if((this.mHealthPoint -= lostLife) < 0) this.mHealthPoint = 0;

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

	public int getRepositoryIndex() {
		return sHeroRepository.indexOf(this);
	}

	public boolean canModify() {
		return mBattle == null;
	}

	public void remove() throws Exception {
		if (!canModify()) throw new Exception();

		Hero.sHeroRepository.remove(this);
	}

	public boolean IsFavourite() {
		return mIsFavourite;
	}

	public void setFavourite(boolean isFavourite) {
		mIsFavourite = isFavourite;
	}

	public void setBaseCharm(float baseCharm) throws Exception {
		if (mBattle != null)
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
		return String.format("%s [ name=\"%s\", hept=%f, chrm=%f, oppt=%f, dept=%f ]\n", getClass().getName(), mName, mHealthPoint, mCharm, mOffensivePoint, mDefensivePoint);
	}

	public class HeroParams {
		public float mHealthPoint;
		public float mOffensivePoint;
		public float mDefensivePoint;
		public float mCharm;

	}


}
