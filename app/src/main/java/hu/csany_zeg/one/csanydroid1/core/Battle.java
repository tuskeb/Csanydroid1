package hu.csany_zeg.one.csanydroid1.core;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Array;
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

	public static final byte
			STATE_DRAW_START_PLAYER = 0,
			STATE_CHOOSE_ATTACKER_HERO = 1,
			STATE_CHOOSE_DEFENDER_HERO = 2,
			STATE_ATTACKER_DRINK_CHARM = 3,
			STATE_DEFENDER_DRINK_CHARM = 4,
			STATE_ATTACK = 5,
			STATE_TURN_FINISHED = 6,
			STATE_NEXT_PLAYER = 7; // gondolkodási/elemzési idő
	private static final String TAG = "game engine";
	public static ArrayList<Battle> sBattles = new ArrayList<Battle>();
	/**
	 * A csata neve
	 */
	public final String mName;
	public final Player mServer;
	private final UUID mUUID;
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
	/**
	 * Az aktuális támadó és védekező játékos.
	 */
	public Hero mAttacker, mDefender;
	public byte mLastAttackerA = -1, mLastAttackerB = -1;
	ArrayBlockingQueue<Integer> mEventQueue = new ArrayBlockingQueue<Integer>(10);
	// TODO átnevezni: attacker & defender heroes
	private ArrayList<Player> mPlayersA, mPlayersB;
	private ArrayList<Player> mReadyPlayers = null;
	private ArrayList<Hero> mHeroesA = new ArrayList<Hero>(), mHeroesB = new ArrayList<Hero>();
	private short mTurn;
	/**
	 * true:    A
	 * false:   B
	 */
	private boolean mStartingPlayer;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	/**
	 * Felkészült játékosok száma, majd a csata állapota
	 */
	private byte mState;
	private StateChangeListeners mListener = new StateChangeListeners() {};

	/**
	 * Létrehoz egy új csatát
	 */
	public Battle(String name, Player opponent) {
		mName = name != null ? name : "Névtelen csata";
		mTurn = 0;
		mServer = Player.CURRENT; // mi leszünk a kiszolgáló
		mReadyPlayers = new ArrayList<Player>();

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
		if (mState < STATE_NEXT_PLAYER) ++mState; // következő állapot
		else mState = STATE_CHOOSE_ATTACKER_HERO; // kezdje előről

		Log.v(TAG, "state changed: " + mState);

		switch (mState) {
			case STATE_CHOOSE_ATTACKER_HERO:
				mListener.OnChooseAttackerHero();
				break;
			case STATE_CHOOSE_DEFENDER_HERO:
				mListener.OnChooseDefenderHero();
				break;
			case STATE_ATTACKER_DRINK_CHARM:
				mListener.OnAttackerDrinkCharm();
				break;
			case STATE_DEFENDER_DRINK_CHARM:
				mListener.OnDefenderDrinkCharm();
				break;
			case STATE_ATTACK:
				mListener.OnAttack();
				break;
			case STATE_TURN_FINISHED:
				mListener.OnTurnFinished();
				break;
			case STATE_NEXT_PLAYER:
				mListener.OnNextPlayer();
				break;

		}
	}

	public boolean getCurrentPlayerAsBoolean() {
		return (mTurn % 2 == 0) == mStartingPlayer;
	}

	public ArrayList<Hero> getPlayerHeroes(boolean playerA) {
		return playerA ? mHeroesA : mHeroesB;
	}

	public Hero nextAttacker() {
		final Hero oldHero = mAttacker;

		final ArrayList<Hero> heroes = getPlayerHeroes(getCurrentPlayerAsBoolean());

		if (getCurrentPlayerAsBoolean()) { // A
			mLastAttackerA = (byte) ((mLastAttackerA + 1) % heroes.size());
			mAttacker = heroes.get(mLastAttackerA);
		} else { // B
			mLastAttackerB = (byte) ((mLastAttackerB + 1) % heroes.size());
			mAttacker = heroes.get(mLastAttackerB);
		}

		return oldHero;
	}

	public Hero nextDefender() {
		final Hero oldHero = mDefender;

		Hero newDefender;
		final ArrayList<Hero> heroes = getPlayerHeroes(!getCurrentPlayerAsBoolean());
		while (mAttacker == (newDefender = heroes.get((int) (Math.random() * heroes.size()))) || mDefender == newDefender)
			;
		mDefender = newDefender;

		return oldHero;
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
					// ++((LocalHero) mAttacker).mTotalKills;

					mAttacker.notifyChanged();
				}

			}

		}

	}

	public Player findPlayerByUUID(UUID uuid) {
		for(Player player : mReadyPlayers) {
			if(player.getUUID() == uuid) {
				return player;
			}
		}

		return null;

	}

	public void processMessage(Parcel data) {
		switch (data.readByte()) {
			case Player.ACTION_READY_FOR_BATTLE: {
				long mostSigBits = data.readLong();
				long leastSigBits = data.readLong();
				Player player = findPlayerByUUID(new UUID(mostSigBits, leastSigBits));

				if (mReadyPlayers.contains(player)) break;
				mReadyPlayers.add(player);
			}
			break;

		}

		broadcastMessage(data);

	}

	private void broadcastMessage(Parcel data) {
		for (Player player : mReadyPlayers) {
			if (player == Player.CURRENT) continue;
			player.send(data);
		}
	}

	public void setPlayerReady(Player player) {

		if (mServer != Player.CURRENT) {
			mServer.send(null); // Player.ACTION_READY_FOR_BATTLE
		} else {
			// csata kezdése
			Log.v(TAG, mHeroesA.size() + " vs. " + mHeroesB.size());

			mAttacker = null;
			mDefender = null;

			// reset variables
			mState = STATE_DRAW_START_PLAYER;
			mListener.OnDrawStartPlayer();

		}

	}

	public int getState() {
		return mState;
	}

	public ArrayList<Player> getPlayers(boolean attackers) {
		return attackers ? mPlayersA : mPlayersB;
	}

	public boolean isStarted() {return mTurn > 0;}

	private boolean getPlayerRole(Player player) throws InvalidPlayerException {
		if (mPlayersA.contains(player)) return true;
		else if (mPlayersB.contains(player)) return false;
		else throw new InvalidPlayerException();
	}

	public void addPlayer(Player player, boolean isAttacker) {
		getPlayers(isAttacker).add(player);
		++mState;
	}

	private boolean removePlayer(Player player) {
		return mPlayersA.remove(player) || mPlayersB.remove(player);
	}

	public boolean addHero(Hero hero) throws InvalidPlayerException {
		if (isStarted()) return false;

		getPlayerHeroes(getPlayerRole(hero.getOwner())).add(hero);

		return true;
	}

	public boolean removeHero(Hero hero) {
		if (getState() != 0) { // a csata már elkezdődött
			return false;
		}

		try {
			getPlayerHeroes(getPlayerRole(hero.getOwner())).remove(hero);

		} catch (InvalidPlayerException e) { }

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
			Hero oldAttacker = nextAttacker();
			setNextState();
		}

		public void OnChooseDefenderHero() {
			Hero oldDefender = nextDefender();
			setNextState();
		}

		public void OnAttackerDrinkCharm() {
			if (mAttacker.drinkCharm(.5) > 0) {
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
			if (mDefender.drinkCharm(.5) > 0) {
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


		public void OnNextPlayer() {

			setNextState();

		}


	}

	public class InvalidPlayerException extends Throwable {}

}
