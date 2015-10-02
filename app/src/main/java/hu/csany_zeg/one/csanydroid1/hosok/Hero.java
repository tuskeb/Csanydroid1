package hu.csany_zeg.one.csanydroid1.hosok;

public class Hero {

	private String name;

	private int healthPoint;

	private float charm;

	private float offensivePoint;

	private float defensivePoint;


	public Hero() {
		this.name = "János";
		this.healthPoint = (int) (Math.random() * 491f + 10f);
		this.charm = (float) (Math.random() * 20.0);
		this.offensivePoint = (float) (Math.random() * 10.0) + 1f;
		this.defensivePoint = (float) (Math.random() * 10.0) + 1f;
	}

	//Önmagát klónozza egy új objektum példányba. Figyelni kell, amikor új változókat viszünk be az osztályba.
	//Le kell klónozni.
	public Hero clone() {
		Hero newHero = new Hero();
		newHero.setName(getName());
		//......................
		return newHero;
	}

	public float finalizeOffensivePoint() {
		return this.offensivePoint * ((.7f + (float) (Math.random() * (1.15 - .7))) + (this.useCharm() / 5));
	}

	private float finalizeDefensivePoint() {
		return this.defensivePoint * ((.5f + (float) (Math.random() * (1.3f - .5f))) + (this.useCharm() / 2.5f));
	}

	private float useCharm() {
		if(this.charm == 0) return 0f;

		final float usedCharm = Math.min(this.charm, 5f);
		this.charm -= usedCharm;
		return usedCharm;

	}


	private void damaged(float lostHelthPoint) {

		if((this.healthPoint -= lostHelthPoint) < 0) {
			// a játékos halott



		}

	}


	public void duel(Hero offensive) {
		//float a = offensive.finalizeOffensivePoint();
		//float b = defensive.;
		damaged(offensive.finalizeOffensivePoint());
	}

	/*Implementálni kell*/
	public Hero(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHealthPoint(int healthPoint) {
		this.healthPoint = healthPoint;
	}

	public void setCharm(float charm) {	this.charm = charm;	}

	public void setOffensivePoint(float offensivePoint) {
		this.offensivePoint = offensivePoint;
	}

	public void setDefensivePoint(float defensivePoint) {
		this.defensivePoint = defensivePoint;
	}

	@Override
	public String toString() {
		return String.format("%s - %d", this.name, this.healthPoint);
	}

	public String getName() {
		return this.name;
	}

	public int getHealthPoint() {
		return this.healthPoint;
	}

	public float getCharm() {
		return this.charm;
	}

	public float getOffensivePoint() {
		return this.offensivePoint;
	}

	public float getDefensivePoint() {
		return this.defensivePoint;
	}
}
