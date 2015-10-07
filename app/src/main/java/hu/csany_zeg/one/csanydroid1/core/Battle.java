package hu.csany_zeg.one.csanydroid1.core;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

// TODO: http://stackoverflow.com/questions/18903735/how-to-change-the-text-color-of-a-listview-item
// TODO http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
/*
#ebd46e
#d3b21f
 */
public class Battle {

	private static final String TAG = "game engine";

	public static final byte
			STATE_DRAW_START_PLAYER = 0,
			STATE_CHOOSE_ATTACKER_HERO = 1,
			STATE_CHOOSE_DEFENDER_HERO = 2,
			STATE_ATTACKER_DRINK_CHARM = 3,
			STATE_DEFENDER_DRINK_CHARM = 4,
			STATE_ATTACK = 5,
			STATE_TURN_FINISHED = 6; // gondolkodási/elemzési idő


	public float
			OFF_RAND_MIN = .7f,
			OFF_RAND_MAX = 1.15f,
			OFF_CHARM_FACTOR = 1f / 5f;
	public float
			DEF_RAND_MIN = .5f,
			DEF_RAND_MAX = 1.3f,
			DEF_CHARM_FACTOR = 1f / 2.5f;
	public float
			MAX_USABLE_CHARM = 5f;

	public static ArrayList<Battle> sBattles = new ArrayList<Battle>();

	/**
	 * A csata neve
	 */
	public final String mName;
	/**
	 * Az aktuális támadó és védekező játékos.
	 */
	public Hero mAttacker, mDefender;
	public byte mLastOAttacker, mLastFAttacker;

	public final Player mOwner, mOpponent;

	ArrayBlockingQueue<Integer> mEventQueue = new ArrayBlockingQueue<Integer>(10);
	private ArrayList<Hero> mOHeroes = new ArrayList<Hero>(); // [O]wner
	private ArrayList<Hero> mFHeroes = new ArrayList<Hero>(); // [F]oreign
	private final UUID mUUID;
	private short mTurn;
	private boolean mStartingPlayer;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	private byte mState;
	private StateChangeListeners mListener = new StateChangeListeners() { };

	/**
	 * Létrehoz egy új csatát
	 */
	public Battle(String name, Player opponent) {
		mName = name != null ? name : "Névtelen csata";
		mTurn = 0;
		mOwner = Player.CURRENT;
		mOpponent = opponent;
		mUUID = UUID.randomUUID();
		sBattles.add(this);
	}

	public static int countBattles() {
		return sBattles.size();
	}

	public String getName() {
		return mName;
	}

	public void giveUp() {

		dispose();
	}

	public void setNextState() {
		if(mState < STATE_TURN_FINISHED) ++mState; // következő állapot
		else mState = STATE_CHOOSE_ATTACKER_HERO; // kezdje előről

		Log.v(TAG, "state changed: " + mState);

		switch (mState) {
			case STATE_CHOOSE_ATTACKER_HERO:    mListener.OnChooseAttackerHero();
				break;
			case STATE_CHOOSE_DEFENDER_HERO:    mListener.OnChooseDefenderHero();
				break;
			case STATE_ATTACKER_DRINK_CHARM:    mListener.OnAttackerDrinkCharm();
				break;
			case STATE_DEFENDER_DRINK_CHARM:    mListener.OnDefenderDrinkCharm();
				break;
			case STATE_ATTACK:                  mListener.OnAttack();
				break;
			case STATE_TURN_FINISHED:           mListener.OnTurnFinished();
				break;

		}
	}

	public Player getCurrentPlayer() {
		return (mTurn & 0x1) > 0 ? mOwner : mOpponent;
	}

	public Hero setAttacker() {
		final Hero old = mAttacker;
		ArrayList<Hero> heroes =  getCurrentPlayer() == Player.CURRENT ? mOHeroes : mFHeroes;
		mAttacker = heroes.get(mAttacker != null ? (heroes.indexOf(mAttacker) + 1) % heroes.size() : (int) (Math.random() * heroes.size()));
		return old;
	}

	public Hero setDefender() {

		Hero defHero;
		ArrayList<Hero> heroes =  getCurrentPlayer() != Player.CURRENT ? mOHeroes : mFHeroes;
		while (mAttacker == (defHero = heroes.get((int) (Math.random() * heroes.size()))) || mDefender == defHero)
			;
		mDefender = defHero;
		return defHero;
	}

	public void dispose() {
		sBattles.remove(this);
	}

	public short getTurn() {
		return mTurn;
	}

	public void duel() {

		final float offensivePoint = mAttacker.getBaseOffensivePoint() * ((OFF_RAND_MIN + (float) Math.random() * (OFF_RAND_MAX - OFF_RAND_MIN)) + (mAttacker.getDrunkCharm() * OFF_CHARM_FACTOR));
		final float defensivePoint = mDefender.getBaseDefensivePoint() * ((DEF_RAND_MIN + (float) Math.random() * (DEF_RAND_MAX - DEF_RAND_MIN)) + (mDefender.getDrunkCharm() * DEF_CHARM_FACTOR));

		if (mDefender.receiveDamage(offensivePoint - defensivePoint)) { // vesztett életet?

			if (!mDefender.isAlive()) { // most ölte meg

				// gratulálunk a győztesnek
				if (mAttacker instanceof LocalHero) {
					++((LocalHero) mAttacker).mTotalKills;

					mAttacker.notifyChanged();
				}

			}

		}

	}

	public void playerReady(Player player) {
		if(mOwner == Player.CURRENT) {
			beginMassacre();
		} else {
			mOwner.send(Player.ACTION_READY_FOR_BATTLE, null);
		}
	}

	private void beginMassacre() {

		if(mOpponent == Player.CURRENT) {
			mFHeroes = mOHeroes;
		}

		Log.v(TAG, mFHeroes.size() + " vs. " + mOHeroes.size());

		mAttacker = null;
		mDefender = null;

		// reset variables
		mState = STATE_DRAW_START_PLAYER;
		mListener.OnDrawStartPlayer();

	}

	public int getState() {
		return mState;
	}
	public boolean isStarted() {return mTurn > 0;}

	public boolean addHero(Hero hero) {
		if (isStarted()) return false;

		(hero.getOwner() == mOwner ? mOHeroes : mFHeroes).add(hero);

		return true;
	}

	public boolean removeHero(Hero hero) {
		if (getState() != 0) { // a csata már elkezdődött
			return false;
		}

		(hero.getOwner() == mOwner ? mOHeroes : mFHeroes).remove(hero);

		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

	public abstract class StateChangeListeners {
		public void OnDrawStartPlayer() {
			mStartingPlayer = (mUUID.getLeastSignificantBits() & 0x1) > 0;
			setNextState();
		}

		public void OnChooseAttackerHero() {
			Hero oldAttacker = setAttacker();
			setNextState();
		}

		public void OnChooseDefenderHero() {
			Hero oldDefender = setDefender();
			setNextState();
		}

		public void OnAttackerDrinkCharm() {
			if(mAttacker.drinkCharm(.5) > 0) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setNextState();
					}
				}, 1000);
			} else {
				setNextState();
			}
		}

		public void OnDefenderDrinkCharm() {
			if(mDefender.drinkCharm(.5) > 0) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setNextState();
					}
				}, 1000);
			} else {
				setNextState();
			}
		}

		public void OnAttack() {

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setNextState();
				}
			}, 1000);

		}

		public void OnTurnFinished() {

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setNextState();
				}
			}, 5000);

		}

	}

}
