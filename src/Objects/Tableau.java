package Objects;

public class Tableau {
    private int id;
    private String name;
    private int date;
    private String auteur;
    private String description;
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
