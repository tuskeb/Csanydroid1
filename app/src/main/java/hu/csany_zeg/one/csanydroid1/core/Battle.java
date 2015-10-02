package hu.csany_zeg.one.csanydroid1.core;

import java.util.ArrayList;

public class Battle {
	ArrayList<Hero> listOfHeroes = new ArrayList();

	//Megadja, hogy hányadik lépésnél áll a csata. A hős indexe, aki támad.
	private int step = 0;


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
