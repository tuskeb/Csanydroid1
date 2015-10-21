package hu.csany_zeg.one.csanydroid1.core;

import android.database.DataSetObservable;
import android.os.Parcel;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import hu.csany_zeg.one.csanydroid1.App;
import hu.csany_zeg.one.csanydroid1.R;

public class Hero extends DataSetObservable implements Cloneable {

	private static final String TAG = "hero";

	public static ArrayList<Hero> sHeroRepository;

	public static short MIN_HEALTH = 10, MAX_HEALTH = 500;
	private static int drawableHeroes[] =
			{
					R.drawable.ch1_basic_stay,
					R.drawable.ch2_basic_stay,
					R.drawable.ch3_basic_stay,
					R.drawable.ch4_basic_stay
			};


	private static int drawableHeroesRanged[][] =
			{
				{
					R.drawable.ch1_basic_ranged,
							R.drawable.ch2_basic_ranged,
							R.drawable.ch3_basic_ranged,
							R.drawable.ch4_basic_ranged
				},
					{
							R.drawable.ch1_ranged1,
							R.drawable.ch2_ranged1,
							R.drawable.ch3_ranged1,
							R.drawable.ch4_ranged1
					},
					{
							R.drawable.ch1_ranged2,
							R.drawable.ch2_ranged2,
							R.drawable.ch3_ranged2,
							R.drawable.ch4_ranged2
					}
			};
	private static int drawableHeroesNonRanged[][] =
			{
					{
							R.drawable.ch1_basic_non_ranged,
							R.drawable.ch2_basic_non_ranged,
							R.drawable.ch3_basic_non_ranged,
							R.drawable.ch4_basic_non_ranged

					},
					{
							R.drawable.ch1_non_ranged1,
							R.drawable.ch2_non_ranged1,
							R.drawable.ch3_non_ranged1,
							R.drawable.ch4_non_ranged1
					},
					{
							R.drawable.ch1_non_ranged2,
							R.drawable.ch2_non_ranged2,
							R.drawable.ch3_non_ranged2,
							R.drawable.ch4_non_ranged2
					}
			};

	private static int drawableOffensive[] =
			{
					R.drawable.weapon_icon_dagger,
					R.drawable.weapon_icon_sword,
					R.drawable.weapon_icon_pistol,
					R.drawable.weapon_icon_laser
			};

	private static int drawableOffensiveToHero[][] =
			{
					{
							R.drawable.weapon_dagger_basic,
							R.drawable.weapon_sword_basic,
							R.drawable.weapon_pistol_basic,
							R.drawable.weapon_laser_basic
					},
					{
							R.drawable.weapon_dagger1,
							R.drawable.weapon_sword1,
							R.drawable.weapon_pistol1,
							R.drawable.weapon_laser1
					},
					{
							R.drawable.weapon_dagger2,
							R.drawable.weapon_sword2,
							R.drawable.weapon_pistol2,
							R.drawable.weapon_laser2
					}
			};

	private static int drawableDefensiveToHero[]=
			{
					R.drawable.shield1,
					R.drawable.shield2,
					R.drawable.shield3,
					R.drawable.shield4
			};

	private static int drawableCharmToHero[]=
			{
					R.drawable.magic4,
					R.drawable.magic3,
					R.drawable.magic2,
					R.drawable.magic1,
			};

	private static int drawableHealthToHero[]=
			{
					R.drawable.heart4,
					R.drawable.heart3,
					R.drawable.heart2,
					R.drawable.heart1,
			};

	private static int drawableDefensive[] =
			{
					R.drawable.shield1_icon,
					R.drawable.shield2_icon,
					R.drawable.shield3_icon,
					R.drawable.shield4_icon
			};
	private static int drawableCharms[] =
			{
					R.drawable.magic4_icon,
					R.drawable.magic3_icon,
					R.drawable.magic2_icon,
					R.drawable.magic1_icon
			};
	private static int drawableHealths[] =
			{
					R.drawable.heart4_icon,
					R.drawable.heart3_icon,
					R.drawable.heart2_icon,
					R.drawable.heart1_icon
			};

	public static ArrayList<Hero> getFreeHeroes() {
		ArrayList<Hero> heroes = new ArrayList<>();
		for (Hero hero : sHeroRepository) {
			if (hero.getBattle() == null) heroes.add(hero);
		}

		Collections.sort(heroes, new Comparator<Hero>() {
			@Override
			public int compare(Hero lhs, Hero rhs) {
				return lhs.mIsFavourite != rhs.mIsFavourite ? (lhs.mIsFavourite ? 1 : -1) : lhs.getName().compareTo(rhs.getName());
			}
		});

		return heroes;
	}



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

	public static final String
			STATISTICS_OFFENSIVE_POINT = "offensivePoint",
			STATISTICS_DEFENSIVE_POINT = "defensivePoint",
				STATISTICS_DRUNK_CHARM = "drunkCharm",
			STATISTICS_KILLS = "kills",
			STATISTICS_DEATHS = "deaths",
			STATISTICS_ATTACKS = "attacks",
			STATISTICS_DEFENCES = "defences",
			STATISTICS_BATTLES = "battles";

	/**
	 * A hős összes védekezésének mértéke.
	 */
	@HeroStatistics(STATISTICS_OFFENSIVE_POINT)
	private float mTotalOffensivePoint;
	/**
	 * A hős összes támadásának mértéke.
	 */
	@HeroStatistics(STATISTICS_DEFENSIVE_POINT)
	private float mTotalDefensivePoint;
	/**
	 * A hős összes varázsereje.
	 */
	@HeroStatistics(STATISTICS_DRUNK_CHARM)
	private float mTotalDrunkCharm;
	/**
	 * A hős által legyőzött ellenfelek.
	 */
	@HeroStatistics(STATISTICS_KILLS)
	private int mTotalKills = 0;
	/**
	 * A hős halálainak száma.
	 */
	@HeroStatistics(STATISTICS_DEATHS)
	private int mTotalDeaths = 0;
	/**
	 * A hős összes támadása.
	 */
	@HeroStatistics(STATISTICS_ATTACKS)
	private int mTotalAttacks;
	/**
	 * A hős összes védekezése.
	 */
	@HeroStatistics(STATISTICS_DEFENCES)
	private int mTotalDefences;
	/**
	 * A hős összes küzdelme.
	 */
	@HeroStatistics(STATISTICS_BATTLES)
	private int mTotalBattles;

	boolean mIsFavourite;
	private Battle mBattle = null;
	private HeroParams mParams = null;
	private float mDrunkCharm;

	public Number getStatistics(String name) {
		for (final Field field : this.getClass().getDeclaredFields()) {
			final HeroStatistics annotation = field.getAnnotation(HeroStatistics.class);
			if (annotation == null) continue;
			if (annotation.value().compareTo(name) != 0) continue;

			try {
				field.setAccessible(true);
				return (Number) field.get(this);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}

		}

		return null;
	}

	public void updateStatistics(final String name, final Number number) {
		mOwner.send(new Player.Message(Player.ACTION_UPDATE_HERO_STAT, this) {
			@Override
			public void extra(Parcel m) {
				m.writeString(name);
				m.writeValue(number);
			}
		});

		Log.v(TAG, "cannot found");

	}

	/**
	 * Létrehoz egy helyi hőst.
	 */
	public Hero(String name) {
		name = getNextName(name);

		mOwner = Player.CURRENT;
		mName = name;

		// TODO review
		mHealthPoint = (short) (Math.random() * (MAX_HEALTH - MIN_HEALTH + 1) + MIN_HEALTH);
		mBaseCharm = (float) Math.random() * (MAX_CHARM + Float.MIN_VALUE);
		mOffensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;
		mDefensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;
		mPicture = (int) (Math.random() * drawableHeroes.length);

		sHeroRepository.add(this);
		notifyChanged();

	}

	public Hero(Parcel parcel) {
		mOwner = Player.CURRENT;
		// Log.v(TAG, "load");

		// general
		mName = parcel.readString();
		mIsFavourite = parcel.readByte() > 0;
		mHealthPoint = parcel.readFloat();
		mBaseCharm = parcel.readFloat();
		mOffensivePoint = parcel.readFloat();
		mDefensivePoint = parcel.readFloat();
		mPicture = parcel.readInt();

		// statistics
		mTotalOffensivePoint = parcel.readFloat();
		mTotalDefensivePoint = parcel.readFloat();
		mTotalDrunkCharm = parcel.readFloat();
		mTotalKills = parcel.readInt();
		mTotalDeaths = parcel.readInt();
		mTotalAttacks = parcel.readInt();
		mTotalDefences = parcel.readInt();
		mTotalBattles = parcel.readInt();

		sHeroRepository.add(this);
		notifyChanged();

	}

	@Override
	public void notifyChanged() {
		super.notifyChanged();
		mGlobalObservable.notifyChanged();
	}

	public void obtainProperties(Parcel parcel) {
		// Log.v(TAG, "save");

		// general
		parcel.writeString(getName());
		parcel.writeByte((byte) (mIsFavourite ? 1 : 0));
		parcel.writeFloat(mHealthPoint);
		parcel.writeFloat(mBaseCharm);
		parcel.writeFloat(mOffensivePoint);
		parcel.writeFloat(mDefensivePoint);
		parcel.writeInt(mPicture);

		// statistics
		parcel.writeFloat(mTotalOffensivePoint);
		parcel.writeFloat(mTotalDefensivePoint);
		parcel.writeFloat(mTotalDrunkCharm);
		parcel.writeInt(mTotalKills);
		parcel.writeInt(mTotalDeaths);
		parcel.writeInt(mTotalAttacks);
		parcel.writeInt(mTotalDefences);
		parcel.writeInt(mTotalBattles);


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

		notifyChanged();

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


	public static String getNextName(String prefix) {

		if (prefix == null || (prefix = prefix.trim()).length() == 0)
			prefix = App.getContext().getString(R.string.default_hero_name_prefix);
		String name;

		if (findHero(prefix) == null) return prefix;

		prefix += " ";

		long n = 0;
		while (true) {
			if (findHero((name = prefix + NumberFormat.getInstance().format(++n))) == null)
				break;
		}

		return name;

	}

	public static Hero findHero(String name) {
		if (name != null) {
			for (Hero hero : sHeroRepository) {
				if (hero.getName().compareToIgnoreCase(name) == 0) {
					return hero;
				}
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
		if (battle != null) {
			if (mBattle != null) // már csatázik
				return false;

			mParams = new HeroParams();

		} else {
			if (mBattle == null) // amúgy sem csatázott
				return true;

			if (mBattle.getState() != Battle.STATE_FINISH)
				return false;

			assert mParams != null;

			mParams.restore();


			mParams = null;
		}

		mBattle = battle;

		return true;

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
		return drawableCharms[getCharmImageIndex()];
	}

	public int getOffensiveImageID() {
		return drawableOffensive[getOffensiveImageIndex()];
	}

	public int getDefensiveImageID() {
		return drawableDefensive[getDefensiveImageIndex()];
	}

	public int getHealthImageID() {
		return drawableHealths[getHealthImageIndex()];
	}

	public int getHeroImageIndex() {
		return mPicture;
	}

	private int getCharmImageIndex() {
		return Math.min((int)((getCharm() - MIN_CHARM) / ((float)(MAX_CHARM - MIN_CHARM) / (float) drawableCharms.length)), drawableCharms.length - 1);
	}

    private int getOffensiveImageIndex() {
		return Math.min((int)((getBaseOffensivePoint() - MIN_OFFENSIVE_POINT) / ((float)(MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT) / (float) drawableOffensive.length)), drawableOffensive.length - 1);
	}

    private int getDefensiveImageIndex() {
		return Math.min((int) ((getBaseDefensivePoint() - MIN_DEFENSIVE_POINT) / ((float) (MAX_DEFENSIVE_POINT - MIN_DEFENSIVE_POINT) / (float) drawableDefensive.length)), drawableDefensive.length - 1);
	}

    private int getHealthImageIndex() {
		return Math.min((int) ((getHealthPoint() - MIN_HEALTH) / ((float) (MAX_HEALTH - MIN_HEALTH) / (float) drawableHealths.length)), drawableHealths.length - 1);
	}

	public float getDrunkCharm() {
		return mDrunkCharm;
	}

	public boolean isAlive() { return this.mHealthPoint > 0; }

	/**
	 * @param r A megivás valószínűsége.
	 */
	public float drinkCharm(float r) {

		if (Math.random() < r) {
			mDrunkCharm = Math.min(mCharm, mBattle.MAX_USABLE_CHARM); // "maximális varázsereje"??
			mCharm -= mDrunkCharm;

			updateStatistics(STATISTICS_DRUNK_CHARM, mDrunkCharm);

		} else { // ne használjon ilyen szereket
			mDrunkCharm = 0;
		}

		notifyChanged();
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

		if ((this.mHealthPoint -= lostLife) < 0) this.mHealthPoint = 0;

        notifyChanged();

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
		notifyChanged();
	}

	public void setBaseCharm(float baseCharm) throws RuntimeException {
		if (!canModify()) throw new RuntimeException("Value cannot be modified.");

		if(baseCharm < MIN_CHARM) mBaseCharm = MIN_CHARM;
		else if(baseCharm > MAX_CHARM) mBaseCharm = MAX_CHARM;
		else mBaseCharm = baseCharm;

		notifyChanged();
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


    public void setHealthPoint(float healthPoint) throws RuntimeException {
        if (!canModify()) throw new RuntimeException("Value cannot be modified");

        if (healthPoint < MIN_HEALTH) mHealthPoint = MIN_HEALTH;
        else if (healthPoint > MAX_HEALTH) mHealthPoint = MAX_HEALTH;
        else mHealthPoint = healthPoint;

        notifyChanged();
    }

	public void setOffensivePoint(float offensivePoint) throws RuntimeException {
		if (!canModify()) throw new RuntimeException("Value cannot be modified");

		if (offensivePoint < MIN_OFFENSIVE_POINT) mOffensivePoint = MIN_OFFENSIVE_POINT;
		else if (offensivePoint > MAX_OFFENSIVE_POINT) mOffensivePoint = MAX_OFFENSIVE_POINT;
		else mOffensivePoint = offensivePoint;

		notifyChanged();
	}

	public void setDefensivePoint(float defensivePoint) throws RuntimeException {
		if (!canModify()) throw new RuntimeException("Value cannot be modified");

		if (defensivePoint < MIN_DEFENSIVE_POINT) mDefensivePoint = MIN_DEFENSIVE_POINT;
		else if (defensivePoint > MAX_DEFENSIVE_POINT) mDefensivePoint = MAX_DEFENSIVE_POINT;
		else mDefensivePoint = defensivePoint;

		notifyChanged();
	}

	@Override
	public String toString() {
		return String.format("%s [ name=\"%s\", hept=%f, chrm=%f, oppt=%f, dept=%f ]", getClass().getName(), mName, mHealthPoint, mCharm, mOffensivePoint, mDefensivePoint);
	}

	private class HeroParams {
		public float mHealthPoint;
		public float mOffensivePoint;
		public float mDefensivePoint;
		public float mCharm;

		public HeroParams() {
			mHealthPoint = Hero.this.mHealthPoint;
			mOffensivePoint = Hero.this.mOffensivePoint;
			mDefensivePoint = Hero.this.mDefensivePoint;
			mCharm = Hero.this.mCharm;
		}

		public void restore() {
			Hero.this.mHealthPoint = mHealthPoint;
			Hero.this.mOffensivePoint = mOffensivePoint;
			Hero.this.mDefensivePoint = mDefensivePoint;
			Hero.this.mCharm = mCharm;

		}

	}

	/** Index 0,1,2 */
	public ArrayList<Integer> getOffensiveImageArray(int animIndex)
	{
		ArrayList<Integer> a= new ArrayList<>();
		switch (getOffensiveImageIndex()) {
			case 0:
			case 1:
				a.add(drawableHeroesNonRanged[animIndex][getHeroImageIndex()]);
				break;
			case 2:
			case 3:
				a.add(drawableHeroesRanged[animIndex][getHeroImageIndex()]);
				break;
		}
		a.add(drawableOffensiveToHero[animIndex][getOffensiveImageIndex()]);
		//addBasicImages(a);
		return a;
	}


	public ArrayList<Integer> getDefensiveImageArray() {
		ArrayList<Integer> a = new ArrayList<>();
		a.add(drawableOffensiveToHero[0][getOffensiveImageIndex()]);
		a.add(drawableOffensiveToHero[getOffensiveImageIndex()][0]);
		addBasicImages(a);
		return a;
	}

	private void addBasicImages(ArrayList<Integer>  a)
	{

		a.add(drawableDefensiveToHero[getDefensiveImageIndex()]);
		a.add(drawableHealthToHero[getHealthImageIndex()]);
		a.add(drawableCharmToHero[getCharmImageIndex()]);

	}
}
