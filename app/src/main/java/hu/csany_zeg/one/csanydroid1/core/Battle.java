package hu.csany_zeg.one.csanydroid1.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// TODO http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html

public class Battle implements Parcelable {
	public static ArrayList<Battle> sBattles = new ArrayList<Battle>();

	ArrayBlockingQueue<Integer> eventQueue = new ArrayBlockingQueue<Integer>(10);

	public ArrayList<Hero> mHeros = new ArrayList<Hero>();

	protected Battle(Parcel in) {
		mHeros = in.createTypedArrayList(Hero.CREATOR);
		mName = in.readString();
		mAttacker = in.readParcelable(Hero.class.getClassLoader());
		mDefender = in.readParcelable(Hero.class.getClassLoader());
		OFF_RAND_MIN = in.readFloat();
		OFF_RAND_MAX = in.readFloat();
		OFF_CHARM_FACTOR = in.readFloat();
		DEF_RAND_MIN = in.readFloat();
		DEF_RAND_MAX = in.readFloat();
		DEF_CHARM_FACTOR = in.readFloat();
		MAX_USABLE_CHARM = in.readFloat();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mHeros);
		dest.writeString(mName);
		dest.writeParcelable(mAttacker, flags);
		dest.writeParcelable(mDefender, flags);
		dest.writeFloat(OFF_RAND_MIN);
		dest.writeFloat(OFF_RAND_MAX);
		dest.writeFloat(OFF_CHARM_FACTOR);
		dest.writeFloat(DEF_RAND_MIN);
		dest.writeFloat(DEF_RAND_MAX);
		dest.writeFloat(DEF_CHARM_FACTOR);
		dest.writeFloat(MAX_USABLE_CHARM);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<Battle> CREATOR = new Creator<Battle>() {
		@Override
		public Battle createFromParcel(Parcel in) {
			return new Battle(in);
		}

		@Override
		public Battle[] newArray(int size) {
			return new Battle[size];
		}
	};

	public static int countBattles() {
		return sBattles.size();
	}

	public String getName() {
		return mName;
	}

	/**
	 * A csata neve
	 */
	public final String mName;
	public Hero mAttacker, mDefender;
	public Opponent mOpponent = null;

	public static final int HERO_STATE_WAIT_NEXT_HERO = 1;

	public float OFF_RAND_MIN = .7f, OFF_RAND_MAX = 1.15f, OFF_CHARM_FACTOR = 1f / 5f;
	public float DEF_RAND_MIN = .5f, DEF_RAND_MAX = 1.3f, DEF_CHARM_FACTOR = 1f / 2.5f;
	public float MAX_USABLE_CHARM = 5f;

	public void setAttacker() {
		//if(mPreviousAction != ACTION_ROUND_STARTED) throw new Exception();

		final Hero old = mAttacker;
		mAttacker = mHeros.get((mAttacker != null ? (mHeros.indexOf(mAttacker) + 1) % mHeros.size() : (int)(Math.random() * mHeros.size())));
		mPreviousAction = ACTION_OFFENSIVE_HERO_SELECTED;

	}

	public Hero getADefender() {
		Hero defHero;
		while (mAttacker == (defHero = mHeros.get((int) (Math.random() * mHeros.size()))) || mDefender == defHero);
		return defHero;
	}

	public void dispose() {
		sBattles.remove(this);
	}

	private static String mPreviousAction;

	public static String ACTION_CHARM_DRANK = "CharmUsed";
	public static String ACTION_ROUND_STARTED = "RoundStarted";
	public static String ACTION_OFFENSIVE_HERO_SELECTED = "OffensiveHeroSelected";
	public static String ACTION_DEFENSIVE_HERO_SELECTED = "DefensiveHeroSelected";

	public void setDefender(Hero defHero) throws InvalidParameterException {
		assert mAttacker != defHero; // TODO throw exception?
		mDefender = defHero;
	}

	public void duel() {

		final float offensivePoint = mAttacker.getBaseOffensivePoint() * ((OFF_RAND_MIN + (float) Math.random() * (OFF_RAND_MAX - OFF_RAND_MIN)) + (mAttacker.getDrunkCharm() * OFF_CHARM_FACTOR));
		final float defensivePoint = mDefender.getBaseDefensivePoint() * ((DEF_RAND_MIN + (float) Math.random() * (DEF_RAND_MAX - DEF_RAND_MIN)) + (mDefender.getDrunkCharm() * DEF_CHARM_FACTOR));

		if (mDefender.receiveDamage(offensivePoint - defensivePoint)) { // vesztett életet?

			if (!mDefender.isAlive()) { // most ölte meg

                // gratulálunk a győztesnek
				if(mAttacker instanceof LocalHero) {
					++((LocalHero)mAttacker).mTotalKills;

					mAttacker.notifyChanged();
				}

			}

		}

	}

	public void beginMassacre() {

		if(this.mHeros.size() < 2) {
			throw new InvalidParameterException();
		}

		// reset variables
		mPreviousAction = ACTION_ROUND_STARTED;
		mAttacker = null;
		mDefender = null;

		setAttacker();


	}

	/**
	 * Létrehoz egy új csatát
	 */
	public Battle(String name, Opponent opponent) {
		mName = name != null ? name : "Névtelen csataaa";
		mOpponent = opponent;

		sBattles.add(this);
	}

	public void addHero(Hero h) {

		// this.mCharm = this.mBaseCharm;

		if(h instanceof LocalHero) {

		}

	}

	@Override
	public String toString() {
		return getName();
	}
}
