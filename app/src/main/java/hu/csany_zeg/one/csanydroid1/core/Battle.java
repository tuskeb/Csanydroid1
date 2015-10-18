package hu.csany_zeg.one.csanydroid1.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;

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
			STATE_ATTACKER_DRINKS_CHARM = 3,
			STATE_DEFENDER_DRINKS_CHARM = 4,
			STATE_BEFORE_ATTACK = 5,
			STATE_ATTACK = 6,
			STATE_AFTER_ATTACK = 7,
			STATE_TURN_FINISHED = 8,
			STATE_NEXT_PLAYER = 9,
			STATE_BEFORE_FINISH = 10,
			STATE_FINISH = 11;
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
	private Player mOwner;

	private ArrayList<Player> mPlayersA = new ArrayList<Player>(), mPlayersB = new ArrayList<Player>();
	private ArrayList<Player> mReadyPlayers = new ArrayList<Player>();
	private ArrayList<Hero> mHeroesA = new ArrayList<Hero>(), mHeroesB = new ArrayList<Hero>();
	private short mTurn = 0;
	/**
	 * true:    A
	 * false:   B
	 */
	private boolean mStartingTeam;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	private byte mState;

	public void setListener(StateChangeListeners listener) {
		mListener = listener;
	}

	private StateChangeListeners mListener = new StateChangeListeners() {};
	private Parcel mParcel = Parcel.obtain();

	/**
	 * Létrehoz egy új csatát
	 */
	public Battle(String name) throws RuntimeException {
		mOwner = Player.CURRENT;
		mName = getNextName(name);
Log.v(TAG, "battle created: " + mName);
		sBattles.add(this);
	}

	public Battle(Player owner, String name) throws RuntimeException {
		if (findBattle(name) != null) throw new RuntimeException("Name must be unique.");

		mOwner = owner;
		mName = name;

		sBattles.add(this);

		mOwner.send(new Player.Message(Player.ACTION_GET_BATTLE, this) {
			@Override
			public void extra(Parcel m) {
			}

		});
	}

	public static String getNextName(String prefix) {
		if(prefix == null || (prefix = prefix.trim()).length() == 0)
			prefix = App.getContext().getString(R.string.unnamed_battle); // TODO default_battle_prefix
		String name;

		if(findBattle(prefix) == null) return prefix;

		prefix += " ";

		long n = 0;
		while (true) {
			if (findBattle((name = prefix + NumberFormat.getInstance().format(++n))) == null)
				break;
		}

		return name;
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
		mOwner.send(new Player.Message(Player.ACTION_GIVE_UP, this) {
			@Override
			public void extra(Parcel m) { }
		});

		// dispose();
	}

	public void setNextState() {
		if (mState < STATE_NEXT_PLAYER && mState >= STATE_DRAW_START_PLAYER) ++mState; // következő állapot
		else if (mState == STATE_NEXT_PLAYER) mState = STATE_CHOOSE_ATTACKER_HERO; // kezdje előről

		// Log.v(TAG, "state changed: " + mState);

		for (final Method method : mListener.getClass().getMethods()) {
			final BattleState annotation = method.getAnnotation(BattleState.class);
			if (annotation == null) continue;

			if (mState == annotation.value()) {
				try {

					// Log.v(TAG, "invoking method(" + mState + ")");

					method.invoke(mListener);
				} catch (InvocationTargetException e) {
					Log.e(TAG, ">>>>>invoke <<<<<< failed(" + mState + ")");
					e.printStackTrace();
				} catch (Exception e) {
					Log.e(TAG, "invoke failed(" + mState + ")");
					e.printStackTrace();
				}
				return;
			}
		}

		assert false;

	}

	public boolean getCurrentPlayerAsBoolean() {
		return (mTurn % 2 == 0) == mStartingTeam;
	}

	public ArrayList<Hero> getHeroes(boolean team) {
		return team ? mHeroesA : mHeroesB;
	}

	public Hero nextAttacker() {
		final Hero oldHero = mAttacker;

		final ArrayList<Hero> heroes = getHeroes(getCurrentPlayerAsBoolean());

		byte lastAttacker = getCurrentPlayerAsBoolean() ? mLastAttackerA : mLastAttackerB;

		while (true) {
			lastAttacker = (byte) ((lastAttacker + 1) % heroes.size());
			mAttacker = heroes.get(lastAttacker);
			if (mAttacker.isAlive()) break;
		}

		if (getCurrentPlayerAsBoolean())
			mLastAttackerA = lastAttacker;
		else
			mLastAttackerB = lastAttacker;

		return oldHero;
	}

	public Hero nextDefender() {
		final Hero oldHero = mDefender;

		final ArrayList<Hero> heroes = getHeroes(!getCurrentPlayerAsBoolean());
/*
		boolean b = false;
		for (int i = heroes.size(); i > 0;) {
			if(heroes.get(--i).isAlive()) {
				b = true;
				break;
			}
		}*/

		//if(b) {
		Hero newDefender;
		while (true) {
			if (mAttacker != (newDefender = heroes.get((int) (Math.random() * heroes.size()))) && newDefender.isAlive())
				break;
		}

		mDefender = newDefender;
		//} else {
		//	mDefender = null;
		//}

		return oldHero;
	}

	public void dispose() {
		sBattles.remove(this);
	}

	public short getTurn() {
		return mTurn;
	}

	protected void duel() {

		final float offensivePoint = mAttacker.getBaseOffensivePoint() * ((OFF_RAND_MIN + (float) Math.random() * (OFF_RAND_MAX - OFF_RAND_MIN)) + (mAttacker.getDrunkCharm() * OFF_CHARM_FACTOR));
		final float defensivePoint = mDefender.getBaseDefensivePoint() * ((DEF_RAND_MIN + (float) Math.random() * (DEF_RAND_MAX - DEF_RAND_MIN)) + (mDefender.getDrunkCharm() * DEF_CHARM_FACTOR));
		// Log.v(TAG, offensivePoint + " vs " + defensivePoint);

		mAttacker.updateStatistics(Hero.STATISTICS_OFFENSIVE_POINT, offensivePoint);
		mDefender.updateStatistics(Hero.STATISTICS_DEFENSIVE_POINT, defensivePoint);

		mAttacker.updateStatistics(Hero.STATISTICS_ATTACKS, (int)1);
		mDefender.updateStatistics(Hero.STATISTICS_DEFENCES, (int)1);

		if (mDefender.receiveDamage(offensivePoint - defensivePoint)) { // vesztett életet?
			Log.v(TAG, "defender received damage");

			if (!mDefender.isAlive()) { // most ölte meg

				Log.v(TAG, "  ...and died from his wounds");

				int c = 0;
				for(Hero hero : getHeroes(getHeroTeam(mDefender))) {
					if(hero.isAlive()) {
						++c;
					}
				}

				if((c == 0) || (!isMultiPlayer() && c == 1)) {
					mState = STATE_BEFORE_FINISH;
					Log.v(TAG, "  ...and unfortunately its team is died too :(");
				}


				mAttacker.updateStatistics(Hero.STATISTICS_KILLS, (int)1);
				mDefender.updateStatistics(Hero.STATISTICS_DEATHS, (int)1);

				mAttacker.notifyChanged();

			}

		}

	}

	public void processMessage(byte action, Parcel m, Player player) {
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
			case Player.ACTION_ADD_HERO_TO_BATTLE: {
				String heroName = m.readString();
				Hero hero = (player == Player.CURRENT ? Hero.findHeroByName(heroName) : new Hero(player, heroName));
				if (hero == null) break;
				// IMPORTANT! only one instance is allowed

				try {
					if (!hero.setBattle(this)) break;
					getHeroes(getPlayerTeam(hero.getOwner())).add(hero);
				} catch (InvalidPlayerException ignored) { }
			}
			break;
			default:
				Log.e(TAG, "unimplemented operation (" + action + ")");
				break;
		}

	}

	public boolean isMultiPlayer() {
		return mHeroesA != mHeroesB;
	}

	private void tryStartMassacre() {
		// mindegyik játékos készen áll?
		if (mPlayersA.size() + mPlayersB.size() > mReadyPlayers.size()) return;

		final ArrayList<Hero> heroes = new ArrayList<>();
		heroes.addAll(mHeroesA);
		heroes.addAll(mHeroesB);

		for(Hero hero : heroes) {
			//++hero.mTotalBattles;
			hero.updateStatistics("battles", (short)1);
		}



		// nincs ellenfél
		if (mPlayersB.size() == 0) {
			// single player
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

	/*
	private void broadcastMessage(Parcel data) {
		for (Player player : mReadyPlayers) {
			if (player == Player.CURRENT) continue;
			player.send(new Player.Message(this) {

				@Override
				public void extra(Parcel m) { }
			});
		}
	}
*/

	@Override
	public String toString() {
		return getName();
	}

	public void finish() {
// TODO

		sBattles.remove(this);

	}

	public void setPlayerReady(Player player) {
		mOwner.send(new Player.Message(Player.ACTION_READY_FOR_BATTLE, this) {
			@Override
			public void extra(Parcel m) { }
		});
	}

	private void updateHero(final Hero hero) {
		final Player owner = hero.getOwner();
		for (Player player : mReadyPlayers) {
			if (owner != player) {
				player.send(new Player.Message(Player.ACTION_GET_HERO, this) {
					@Override
					public void extra(Parcel m) { }
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

	private boolean getPlayerTeam(Player player) throws InvalidPlayerException {
		if (mPlayersA.contains(player)) return true;
		else if (mPlayersB.contains(player)) return false;
		else throw new InvalidPlayerException();
	}

	private boolean getHeroTeam(Hero hero) {
		if (mHeroesA.contains(hero)) return true;
		else if (mHeroesB.contains(hero)) return false;
		else throw new NullPointerException();

	}

	public void addPlayer(Player player, boolean isAttacker) {
		getPlayers(isAttacker).add(player);
		++mState;
	}

	private boolean removePlayer(Player player) {
		return mPlayersA.remove(player) || mPlayersB.remove(player);
	}

	private void removePlayerHeroes(Player player) throws InvalidPlayerException {
		ArrayList<Hero> heroes = getHeroes(getPlayerTeam(player));
		for (int i = heroes.size(); i > 0; ) {
			Hero hero = heroes.get(--i);
			if (hero.getOwner() == player) {
				heroes.remove(i);
			}
		}
	}


	public void addHero(final Hero hero) {
		mOwner.send(new Player.Message(Player.ACTION_ADD_HERO_TO_BATTLE, this) {
			@Override
			public void extra(Parcel m) {
				m.writeString(hero.getName());
			}
		});

	}

	public boolean removeHero(Hero hero) {
		if (getState() != 0) { // a csata már elkezdődött
			return false;
		}

		try {
			getHeroes(getHeroTeam(hero)).remove(hero);

		} catch (NullPointerException ignored) { }

		return true;
	}

	public abstract class StateChangeListeners {
		@BattleState(Battle.STATE_DRAW_START_PLAYER)
		public void OnDrawStartPlayer() {
			mStartingTeam = Math.random() - .25 < Math.random(); // +25% chance for team `A`
			Log.v(TAG, "starting team: " + (mStartingTeam ? "A" : "B"));

			setNextState();
		}

		@BattleState(Battle.STATE_CHOOSE_ATTACKER_HERO)
		public void OnChooseAttackerHero() {
			Hero oldAttacker = nextAttacker();
			setNextState();
		}

		@BattleState(Battle.STATE_CHOOSE_DEFENDER_HERO)
		public void OnChooseDefenderHero() {
			Hero oldDefender = nextDefender();
			setNextState();
		}

		@BattleState(Battle.STATE_ATTACKER_DRINKS_CHARM)
		public void OnAttackerDrinksCharm() {
			mAttacker.drinkCharm(.5f);
			setNextState();
			/*
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setNextState();
					}
				}, 1000);
				*/
		}

		@BattleState(Battle.STATE_DEFENDER_DRINKS_CHARM)
		public void OnDefenderDrinksCharm() {
			mDefender.drinkCharm(.5f);
			setNextState();

		}

		@BattleState(Battle.STATE_BEFORE_ATTACK)
		public void OnBeforeAttack() {
			setNextState();
		}

		@BattleState(Battle.STATE_ATTACK)
		public void OnAttack() {
			Battle.this.duel();
			setNextState();
		}


		@BattleState(Battle.STATE_AFTER_ATTACK)
		public void OnAfterAttack() {
			setNextState();
		}

		@BattleState(Battle.STATE_TURN_FINISHED)
		public void OnTurnFinished() {
			setNextState();

		}

		@BattleState(Battle.STATE_NEXT_PLAYER)
		public void OnNextPlayer() {
			// setNextState();

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setNextState();
				}
			}, 0);
		}

		@BattleState(Battle.STATE_BEFORE_FINISH)
		public void OnWin() {
			Log.v(TAG, "yuuppppppiiiiiie!");
			//Log.v(TAG, "k" + mHeroesA.get(0).getStatistics(Hero.STATISTICS_ATTACKS).intValue());

		}


	}

	public class InvalidPlayerException extends Throwable {}


}
