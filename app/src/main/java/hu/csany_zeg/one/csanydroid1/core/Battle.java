package hu.csany_zeg.one.csanydroid1.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import hu.csany_zeg.one.csanydroid1.App;
import hu.csany_zeg.one.csanydroid1.R;

// TODO: http://stackoverflow.com/questions/18903735/how-to-change-the-text-color-of-a-listview-item
// TODO http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
/* #ebd46e, #d3b21f */

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
	private static final String TAG = "battle";
	public static ArrayList<Battle> sBattles = new ArrayList<Battle>();

	/**
	 * A csata neve
	 */
	public final String mName;
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
	private Player mOwner;

	private ArrayList<Player> mPlayersA = new ArrayList<Player>(), mPlayersB = new ArrayList<Player>();
	private ArrayList<Player> mReadyPlayers = new ArrayList<Player>();
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
	public Battle(String name) throws RuntimeException {
		this(Player.CURRENT, name != null ? name : getNextName());
		mTurn = 0;
	}

	private static String getNextName() {
		final String baseName = App.getContext().getString(R.string.unnamed_battle); // TODO default_battle_prefix
		String name;

		for (long n = 0; findBattle((name = baseName + " " + NumberFormat.getInstance().format(++n))) != null; );

		return name;
	}

	public Battle(Player owner, String name) throws RuntimeException {
		synchronized (sBattles) {
			if(findBattle(name) != null) throw new RuntimeException("Name must be unique.");

			mOwner = owner;
			mName = name;

			sBattles.add(this);

			mOwner.send(new Player.Message(this) {

				@Override
				public void obtain(Parcel parcel) {
					parcel.writeByte(Player.ACTION_GET_BATTLE);
				}

			});

		}

	}

	public static ArrayList<Battle> getOwnBattles() {
		ArrayList<Battle> ownBattles = new ArrayList<Battle>();
		for (Battle battle : sBattles) {
			if (battle.getOwner() == Player.CURRENT) {
				ownBattles.add(battle);
			}

		}

		return ownBattles;
	}

	public static int countBattles() {
		return sBattles.size();
	}

	public static Battle findBattle(String name) {
		for (Battle battle : sBattles) {
			if (battle.mName.compareTo(name) == 0) return battle;
		}
		return null;
	}

	public Player getOwner() {
		return mOwner;
	}

	public String getName() {
		return mName;
	}

	public void giveUp() {
		mOwner.send(new Player.Message(this) {

			@Override
			public void obtain(Parcel parcel) {
				parcel.writeByte(Player.ACTION_GIVE_UP);
			}
		});

		// dispose();
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

	public ArrayList<Hero> getHeroes(boolean playerA) {
		return playerA ? mHeroesA : mHeroesB;
	}

	public Hero nextAttacker() {
		final Hero oldHero = mAttacker;

		final ArrayList<Hero> heroes = getHeroes(getCurrentPlayerAsBoolean());

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
		final ArrayList<Hero> heroes = getHeroes(!getCurrentPlayerAsBoolean());
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

					// ++((LocalHero) mAttacker).mTotalKills;

					mAttacker.notifyChanged();

			}

		}

	}

	public Player findPlayerByUUID(UUID uuid) {
		for (Player player : mReadyPlayers) {
			if (player.getUUID() == uuid) {
				return player;
			}
		}

		return null;

	}

	public void processMessage(byte action, Parcel data, Player player) {
		switch (action) {
			case Player.ACTION_READY_FOR_BATTLE: {
				if (mReadyPlayers.contains(player)) break;
				mReadyPlayers.add(player);

				tryStartMassacre();
			}
			break;
			case Player.ACTION_GIVE_UP: {
				try {
					removePlayerHeroes(player);
				} catch (InvalidPlayerException e) { }
			}
			break;

		}

	}



	private void tryStartMassacre() {
		// mindegyik játékos készen áll?
		if(mPlayersA.size() + mPlayersB.size() > mReadyPlayers.size()) return;


		// nincs ellenfél
		if (mPlayersB.size() == 0) {
			mPlayersB = mPlayersA;
			mHeroesB = mHeroesA;
		}

		// csata kezdése
		Log.v(TAG, mHeroesA.size() + " vs. " + mHeroesB.size());

		mAttacker = null;
		mDefender = null;

		// reset variables
		mState = STATE_DRAW_START_PLAYER;
		mListener.OnDrawStartPlayer();

	}

	private void broadcastMessage(Parcel data) {
		for (Player player : mReadyPlayers) {
			if (player == Player.CURRENT) continue;
			player.send(new Player.Message(this) {

				@Override
				public void obtain(Parcel parcel) {

				}
			});
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public abstract class StateChangeListeners {
		public void OnDrawStartPlayer() {
			mStartingPlayer = Math.random() > Math.random();
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

	public void finish() {
// TODO

		sBattles.remove(this);

	}


	private Parcel mParcel = Parcel.obtain();
	private void broadcast(Player.Message message) {
		synchronized (mParcel) {
			mParcel.recycle();
			message.obtain(mParcel);

			for (Player player : mReadyPlayers)
				player.send(mParcel);

		}
	}

	public void setPlayerReady(Player player) {
		broadcast(new Player.Message(this) {
			@Override
			public void obtain(Parcel parcel) {
				parcel.writeByte(Player.ACTION_READY_FOR_BATTLE);
			}
		});
	}

	private void updateHero(final Hero hero) {
		final Player owner = hero.getOwner();
		for(Player player : mReadyPlayers) {
			if(owner != player) {
				player.send(new Player.Message(this) {
					@Override
					public void obtain(Parcel parcel) {
						parcel.writeByte(Player.ACTION_GET_HERO);


					}
				});
			}
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

	private void removePlayerHeroes(Player player) throws InvalidPlayerException {
		ArrayList<Hero> heroes = getHeroes(getPlayerRole(player));
		for (int i = heroes.size(); i > 0; ) {
			Hero hero = heroes.get(--i);
			if (hero.getOwner() == player) {
				heroes.remove(i);
			}
		}
	}

	public boolean addHero(Hero hero) throws InvalidPlayerException {
		if (isStarted()) return false;

		getHeroes(getPlayerRole(hero.getOwner())).add(hero);

		return true;
	}

	public boolean removeHero(Hero hero) {
		if (getState() != 0) { // a csata már elkezdődött
			return false;
		}

		try {
			getHeroes(getPlayerRole(hero.getOwner())).remove(hero);

		} catch (InvalidPlayerException e) { }

		return true;
	}


	public class InvalidPlayerException extends Throwable {}


}
