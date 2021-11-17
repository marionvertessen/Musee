package Objects;

public class Tableau {
    private int id;
    public String name;
    public int date;
    public String auteur;
    public String description;
    public int posX;
    public int posY;


    public Tableau(int i, String nom, int dat, String aut, int posx, int posy) {
        id = i;
        name = nom;
        date = dat;
        auteur = aut;
        description = null;
        posX =posx;
        posY = posy;
    }
}
