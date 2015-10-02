package hu.csany_zeg.one.csanydroid1.hosok;

/**
 * Created by tanuló on 2015.10.02..
 */
public class Hero{
    private String name;
    private int healthPoint;
    private float magic;
    private float offensivePoint;
    private float defensivePoint;

    //Önmagát klónozza egy új objektum példányba. Figyelni kell, amikor új változókat viszünk be az osztályba.
    //Le kell klónozni.
    public Hero clone()
    {
        Hero newHero = new Hero();
        newHero.setName(getName());
        //......................
        return newHero;
    }

    public Hero()
    {
        name = "János";
        healthPoint = (int)(Math.random()*491f+10f);
        magic = (float)(Math.random()*20.0);
        offensivePoint = (float)(Math.random()*10.0)+1f;
        defensivePoint = (float)(Math.random()*10.0)+1f;
    }

    public float finalizeOffensivePoint()
    {
        //2
        //Kivonja az értékekből a támadási ponthoz felhasznált értékeket
        //Eredményként a támadási pontot adja
        //finalizeMagicPoint();
        return 0f;
    }

    private float finalizeDefensivePoint()
    {
        //4
        //finalizeMagicPoint();
        return 0f;
    }

    private float finalizeMagicPoint()
    {
        //3-5
        return 0f;
    }


    private void damaged(float lostPoints)
    {
        //6. pont
    }


    public void duel(Hero offensive)
    {
        //float a = offensive.finalizeOffensivePoint();
        //float b = defensive.;
        damaged(offensive.finalizeOffensivePoint());
    }

    /*Implementálni kell*/
    public Hero(String name)
    {
        this.name=name;
    }







    public void setName(String name) {
        this.name = name;
    }

    public void setHealthPoint(int healthPoint) {
        this.healthPoint = healthPoint;
    }

    public void setMagic(float magic) {
        this.magic = magic;
    }

    public void setOffensivePoint(float offensivePoint) {
        this.offensivePoint = offensivePoint;
    }

    public void setDefensivePoint(float defensivePoint) {
        this.defensivePoint = defensivePoint;
    }

    @Override
    public String toString() {
        return name + " - " + healthPoint;
    }

    public String getName() {
        return name;
    }

    public int getHealthPoint() {
        return healthPoint;
    }

    public float getMagic() {
        return magic;
    }

    public float getOffensivePoint() {
        return offensivePoint;
    }

    public float getDefensivePoint() {
        return defensivePoint;
    }
}
