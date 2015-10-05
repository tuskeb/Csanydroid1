package hu.csany_zeg.one.csanydroid1.core;

import android.database.DataSetObservable;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.animation.Animation;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Hero extends DataSetObservable implements Cloneable, Parcelable {

	public static final Parcelable.Creator<Hero> CREATOR
			= new Parcelable.Creator<Hero>() {

		public Hero createFromParcel(Parcel in) {
			return new Hero(in);
		}

		public Hero[] newArray(int size) {
			return new Hero[size];
		}
	};

	public static Hero sAttackerHero = null, sDefensiveHero = null;
	public static short MIN_HEALTH = 10, MAX_HEALTH = 500;
	public static float MIN_CHARM = 0.0f, MAX_CHARM = 20.0f;
	public static float MIN_OFFENSIVE_POINT = 1.0f, MAX_OFFENSIVE_POINT = 10.0f;
	public static float MIN_DEFENSIVE_POINT = 1.0f, MAX_DEFENSIVE_POINT = 10.0f;

	/**
	 * A hős neve.
	 */
	protected String mName;

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
	 * Létrehoz egy távoli hőst.
	 */
	protected Hero(Parcel in) {
		mName = in.readString();
		mHealthPoint = in.readFloat();
		mBaseCharm = in.readFloat();
		mOffensivePoint = in.readFloat();
		mDefensivePoint = in.readFloat();
	}

	/**
	 * Létrehoz egy helyi hőst.
	 */
	public Hero() {

		mName = Math.random() > .5 ? "Szilvás gombóc" : "Burgonya";

		// és feltölti véletlen értékekkel
		// TODO review
		mHealthPoint = (short) (Math.random() * (MAX_HEALTH - MIN_HEALTH + 1) + MIN_HEALTH);
		mBaseCharm = (float) Math.random() * (MAX_CHARM + Float.MIN_VALUE);
		mOffensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;
		mDefensivePoint = (float) Math.random() * (MAX_OFFENSIVE_POINT - MIN_OFFENSIVE_POINT + Float.MIN_VALUE) + MIN_OFFENSIVE_POINT;

	}

	public void addToBattle(Battle battle) {
		if (battle.mHeros.indexOf(this) > 0) return;
		battle.mHeros.add(this);

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
	private void drinkCharm(double r, Battle battle) {
		if (Math.random() < r) {
			this.mDrunkCharm = Math.min(mCharm, battle.MAX_USABLE_CHARM); // "maximális varázsereje"??
			mCharm -= this.mDrunkCharm;

			if (this instanceof LocalHero) {
				((LocalHero) this).mTotalDrunkCharm += this.mDrunkCharm;
			}

		} else { // ne használjon ilyen szereket
			this.mDrunkCharm = 0;
		}

		super.notifyChanged();

	}

	public int getBattleCount() {
		int r = 0;
		for (Battle battle : Battle.sBattles) {
			if (battle.mHeros.contains(this)) ++r;
		}

		return r;
	}

	private boolean shouldDrinkCharm() {
		return true;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
		super.notifyChanged();
	}

	public float getHealthPoint() {
		return mHealthPoint;
	}

	public boolean receiveDamage(float lostLife) {
		if (lostLife <= 0) return false;

		this.mHealthPoint -= lostLife;

		if (!this.isAlive()) {
			if (this instanceof LocalHero) {
				super.notifyChanged();
			}
		}
		return true;

	}

	public float getCharm() { return mBaseCharm; }

	public float getBaseOffensivePoint() {
		return mOffensivePoint;
	}

	public float getBaseDefensivePoint() { return mDefensivePoint; }

	@Override
	public String toString() {
		return mName;
		//return String.format("%s [ nm=%s, hp=%f, ch=%f, op=%f, dp=%f ]", getClass().getName(), mName, mHealthPoint, mCharm, mOffensivePoint, mDefensivePoint);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getName());
		dest.writeFloat(getHealthPoint());
		dest.writeFloat(getCharm());
		dest.writeFloat(getBaseDefensivePoint());
		dest.writeFloat(getBaseOffensivePoint());
	}


	public interface HeroListener {

		void onAttackerHeroChanged(Hero oldHero);

		void onDefensiveHeroChanged(Hero oldHero);

		void onCharmChanged(float oldCharm);

		void onHealthPointChanged(float oldHealthPoint);

	}

	public class HeroParametersUnmodifiable extends Throwable {

	}

}
