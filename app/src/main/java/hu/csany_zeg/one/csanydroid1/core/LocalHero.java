package hu.csany_zeg.one.csanydroid1.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class LocalHero extends Hero {

	public static ArrayList<LocalHero> sHeros = new ArrayList<LocalHero>();

	public static final Parcelable.Creator<LocalHero> CREATOR
			= new Parcelable.Creator<LocalHero>() {

		public LocalHero createFromParcel(Parcel in) {
			return new LocalHero(in);
		}

		public LocalHero[] newArray(int size) {
			return new LocalHero[size];
		}
	};
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

	boolean isFavourite;


	private LocalHero(Parcel in) {
		super(in);
		Log.v("mama", "locapar");
	}

	public LocalHero() {
		super();

		sHeros.add(this);
	}

	@Override
	public void setName(String name) {

		for(Hero h : sHeros) {
			if(h.getName().compareTo(name) == 0) {
				throw new InvalidParameterException("Same named heroes.");
			}
		}

		super.setName(name);
	}
}
