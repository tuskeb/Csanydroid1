package hu.csany_zeg.one.csanydroid1.core;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Battle {
	ArrayList<Hero> listOfHeroes = new ArrayList();
	ArrayBlockingQueue<Integer> eventQueue = new ArrayBlockingQueue<Integer>(10);

	public static Hero attacker = null;

	public static final int STATE_WAIT_NEXT_HERO = 1;

	public static void setAttacker() {

	}

	public static void beginMassacre() {
		attacker = Hero.sHeroes.get(0);

	}

	public void addHero(Hero hero) {
		listOfHeroes.add(hero);
	}

	//Legenerálja, hogy ki, kivel fog harcolni, a hősökön keresztül pedik
	//kiszámolj, hogyan alakulnak a pontjaik.
	public BattleParams nextStep() {

		//A 4 hőst meg kell adni.
		BattleParams bp = new BattleParams();

		//Ez csak minta!!!
		bp.defensiveBefore = listOfHeroes.get(0).clone();

		return bp;
	}


}
