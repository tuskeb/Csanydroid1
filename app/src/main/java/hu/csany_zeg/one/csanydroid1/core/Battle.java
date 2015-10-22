package hu.csany_zeg.one.csanydroid1.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import hu.csany_zeg.one.csanydroid1.App;
import hu.csany_zeg.one.csanydroid1.R;

public class Battle {

    public static final byte
            STATE_AUTO = -2,
            STATE_WAIT_FOR_START = -1,
            STATE_DRAW_START_PLAYER = 0,
            STATE_ATTACKER_CHANGE = 1,
            STATE_DEFENDER_CHANGE = 2,
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

    // nem final, mivel egy komolyabb verzióban lehetőség lett volna ezeket is állítani. majd legközelebb!

    /**
     * Az aktuális támadó és védekező játékos.
     */
    public Hero mAttacker;


    public Hero getDefender() {
        return mDefender;
    }

    public Hero getAttacker() {
        return mAttacker;
    }

    public Hero mDefender;
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

    private byte mState;

    public void setNextState(byte nextState) {
        mNextState = nextState;
    }

    private byte mNextState = STATE_AUTO;

    public void setListener(StateChangeListeners listener) {
        mListener = listener;
    }

    private StateChangeListeners mListener = new StateChangeListeners() {
    };
    private Parcel mParcel = Parcel.obtain();

    /**
     * Létrehoz egy új csatát
     */
    public Battle(String name) throws RuntimeException {
        mOwner = Player.CURRENT;
        mName = getNextName(name);
        Log.v(TAG, "battle created: " + mName);

        mHandler = new Handler(Looper.getMainLooper());

        sBattles.add(this);
    }

    public Battle(Player owner, String name) throws RuntimeException {
        if (findBattle(name) != null) throw new RuntimeException("Name must be unique.");

        mOwner = owner;
        mName = name;

        mHandler = new Handler(Looper.getMainLooper());

        sBattles.add(this);

        mOwner.send(new Player.Message(Player.ACTION_GET_BATTLE, this) {
	        @Override
	        public void extra(Parcel m) {
	        }

        });
    }

    public static String getNextName(String prefix) {
        if (prefix == null || (prefix = prefix.trim()).length() == 0)
            prefix = App.getContext().getString(R.string.default_battle_name_prefix); // TODO default_battle_prefix
        String name;

        if (findBattle(prefix) == null) return prefix;

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

    public static Battle get(int i) {
        return sBattles.get(i);
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

    private void nextState() {
        if (mNextState == STATE_AUTO) {
            if (mState < STATE_NEXT_PLAYER && mState >= STATE_DRAW_START_PLAYER)
                ++mState; // következő állapot
            else if (mState == STATE_NEXT_PLAYER)
                mState = STATE_ATTACKER_CHANGE; // kezdje előről

        } else {
            mState = mNextState;
        }

        Log.v(TAG, "state changed: " + mState);

        for (final Method method : mListener.getClass().getMethods()) {
            final BattleState annotation = method.getAnnotation(BattleState.class);
            if (annotation == null) continue;

            if (mState == annotation.value()) {
                mNextState = annotation.next();

                try {
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
Log.v(TAG, "adf");
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

	    Log.v(TAG, "_adf");

        return oldHero;
    }

    public Hero nextDefender() {
        final Hero oldHero = mDefender;

	    Log.v(TAG, "adfaa");
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

	    Log.v(TAG, "__adfaa");

        return oldHero;
    }

    public void dispose() {
        sBattles.remove(this);
        /*if (mHandler != null) {
            mHandler.getLooper().quit();
        }*/

        mDisposed = true;
    }

    public short getTurn() {
        return mTurn;
    }

    protected float duel() {

        final float offensivePoint = mAttacker.getBaseOffensivePoint() * ((OFF_RAND_MIN + (float) Math.random() * (OFF_RAND_MAX - OFF_RAND_MIN)) + (mAttacker.getDrunkCharm() * OFF_CHARM_FACTOR));
        final float defensivePoint = mDefender.getBaseDefensivePoint() * ((DEF_RAND_MIN + (float) Math.random() * (DEF_RAND_MAX - DEF_RAND_MIN)) + (mDefender.getDrunkCharm() * DEF_CHARM_FACTOR));
        // Log.v(TAG, offensivePoint + " vs " + defensivePoint);

        mAttacker.updateStatistics(Hero.STATISTICS_OFFENSIVE_POINT, offensivePoint);
        mDefender.updateStatistics(Hero.STATISTICS_DEFENSIVE_POINT, defensivePoint);

        mAttacker.updateStatistics(Hero.STATISTICS_ATTACKS, (int) 1);
        mDefender.updateStatistics(Hero.STATISTICS_DEFENCES, (int) 1);

        final float r = offensivePoint - defensivePoint;
        if (mDefender.receiveDamage(r)) { // vesztett életet?
            Log.v(TAG, "defender received damage");

            if (!mDefender.isAlive()) { // most ölte meg

                Log.v(TAG, "  ...and died from his wounds");

                int c = 0;
                for (Hero hero : getHeroes(getHeroTeam(mDefender))) {
                    if (hero.isAlive()) {
                        ++c;
                    }
                }
                if ((c == 0) || (!isMultiPlayer() && c == 1)) {
                    mState = STATE_BEFORE_FINISH;
                    Log.v(TAG, "  ...and unfortunately its team is died too :(");
                }


                mAttacker.updateStatistics(Hero.STATISTICS_KILLS, (int) 1);
                mDefender.updateStatistics(Hero.STATISTICS_DEATHS, (int) 1);

                mAttacker.notifyChanged();

            }

        }

        return r;

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
                Hero hero = (player == Player.CURRENT ? Hero.findHero(heroName) : new Hero(player, heroName));
                if (hero == null) break;
                // IMPORTANT! only one instance is allowed

                try {
                    if (!hero.setBattle(this)) break;
                    getHeroes(getPlayerTeam(hero.getOwner())).add(hero);
                } catch (InvalidPlayerException ignored) {
                }
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

	    if (mHeroesA.size() + mHeroesB.size() < 2) return; // TODO broadcast error message

        final ArrayList<Hero> heroes = new ArrayList<>();
        heroes.addAll(mHeroesA);
        heroes.addAll(mHeroesB);

        for (Hero hero : heroes) {
            //++hero.mTotalBattles;
            hero.updateStatistics("battles", 1);
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
        mState = STATE_WAIT_FOR_START;
        nextState();

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
            public void extra(Parcel m) {
            }
        });
    }

    private void updateHero(final Hero hero) {
        final Player owner = hero.getOwner();
        for (Player player : mReadyPlayers) {
            if (owner != player) {
                player.send(new Player.Message(Player.ACTION_GET_HERO, this) {
                    @Override
                    public void extra(Parcel m) {
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

    public boolean isStarted() {
        return mTurn > 0;
    }

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
        mReadyPlayers.remove(player);

        if(mReadyPlayers.size() == 0 && !isMultiPlayer()) {
            dispose();
        }

    }

    boolean mDisposed = false;
    public boolean isDisposed() {
        return mDisposed;
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

        } catch (NullPointerException ignored) {
        }

        return true;
    }

    public void setOnStateChangeListener(OnStateChange onStateChangeListener) {

        mOnStateChangeListener = onStateChangeListener != null ? onStateChangeListener :
                new OnStateChange() {
                    @Override
                    public void onChange(Battle battle, Object param) {
                    }
                };

    }

    OnStateChange mOnStateChangeListener;

    {
        setOnStateChangeListener(null);
    }

    public static abstract class OnStateChange {
        public abstract void onChange(final Battle battle, final Object param);
    }

    private Handler mHandler;

    private void delayNextState(int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextState();
            }
        }, delay);
    }

    public abstract class StateChangeListeners {

        @BattleState(
                value = Battle.STATE_WAIT_FOR_START,
                next = Battle.STATE_DRAW_START_PLAYER
        )
        public void OnWait() {
            delayNextState(500);
            mOnStateChangeListener.onChange(Battle.this, null);
        }

        @BattleState(value = Battle.STATE_DRAW_START_PLAYER)
        public void OnDrawStartPlayer() {
            mStartingTeam = Math.random() - .25 < Math.random(); // +25% chance for team `A`
            Log.v(TAG, "starting team: " + (mStartingTeam ? "A" : "B"));
            delayNextState(2000);
            mOnStateChangeListener.onChange(Battle.this, mStartingTeam);

        }

        @BattleState(Battle.STATE_ATTACKER_CHANGE)
        public void OnChooseAttackerHero() {
            Hero oldAttacker = nextAttacker();
            Log.v(TAG, "SET ATTACKER: " + +getAttacker().getIndex() );
            delayNextState(100);
            mOnStateChangeListener.onChange(Battle.this, oldAttacker);

        }

        @BattleState(Battle.STATE_DEFENDER_CHANGE)
        public void OnChooseDefenderHero() {
            Hero oldDefender = nextDefender();
            Log.v(TAG, "SET DEFENDER: " + getDefender().getIndex());
            delayNextState(100);

            mOnStateChangeListener.onChange(Battle.this, oldDefender);

        }

        @BattleState(Battle.STATE_ATTACKER_DRINKS_CHARM)
        public void OnAttackerDrinksCharm() {
            mAttacker.drinkCharm(.5f);
            delayNextState(400);
            mOnStateChangeListener.onChange(Battle.this, mAttacker);

        }

        @BattleState(Battle.STATE_DEFENDER_DRINKS_CHARM)
        public void OnDefenderDrinksCharm() {
            mDefender.drinkCharm(.5f);
            delayNextState(400);
            mOnStateChangeListener.onChange(Battle.this, mDefender);

        }

        @BattleState(Battle.STATE_BEFORE_ATTACK)
        public void OnBeforeAttack() {
            Log.v(TAG, "BEFORE ATTACK");
            delayNextState(1000);
            mOnStateChangeListener.onChange(Battle.this, null);
        }

        @BattleState(Battle.STATE_ATTACK)
        public void OnAttack() {
            delayNextState(2000);
            final float r = Battle.this.duel();
            mOnStateChangeListener.onChange(Battle.this, r);
        }


        @BattleState(Battle.STATE_AFTER_ATTACK)
        public void OnAfterAttack() {
            delayNextState(100);
            mOnStateChangeListener.onChange(Battle.this, null);
        }

        @BattleState(Battle.STATE_TURN_FINISHED)
        public void OnTurnFinished() {
            delayNextState(100);
            mOnStateChangeListener.onChange(Battle.this, null);
        }

        @BattleState(Battle.STATE_NEXT_PLAYER)
        public void OnNextPlayer() {
            delayNextState(500);
            mOnStateChangeListener.onChange(Battle.this, null);
        }

        @BattleState(
                value = Battle.STATE_BEFORE_FINISH,
                next = Battle.STATE_FINISH
        )
        public void OnWin() {
            Log.v(TAG, "won!");
            delayNextState(1000);
            mOnStateChangeListener.onChange(Battle.this, null);
            //Log.v(TAG, "k" + mHeroesA.get(0).getStatistics(Hero.STATISTICS_ATTACKS).intValue());

        }

        @BattleState(value = Battle.STATE_FINISH)
        public void OnFinish() {
            //dispose();
            Log.v(TAG, "disposed");
        }


    }

    public class InvalidPlayerException extends Throwable {
    }


}
