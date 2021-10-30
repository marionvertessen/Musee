package Agents;

import Objects.Tableau;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Musee extends Agent {
    private int taille_x = 10;
    private int taille_y = 10;
    List<Tableau> liste_tot_tableaux = new ArrayList<Tableau>();
    List<AgentController> listeGuide = new ArrayList<>();
    List<JPanel> listeGrille = new ArrayList<>();
    List<List<JLabel>> liste_Couleur_Grille = new ArrayList<>();
    ImageIcon cercle_vert = new ImageIcon(new ImageIcon("cercle-vert-fond-transparent.png").getImage().getScaledInstance(25,25 , Image.SCALE_SMOOTH));
    ImageIcon cercle_rouge = new ImageIcon(new ImageIcon("cercle-rouge-fond-transparent.png").getImage().getScaledInstance(25,25 , Image.SCALE_SMOOTH));
    protected void setup() {

        System.out.println("Le musée "+ getAID().getName() + " est ok !!");
        liste_tot_tableaux.add(new Tableau(1, "Saint Augustin et son disciple Alypius reçoivent la visite de Ponticianus", 1413, "NICCOLÒ DI PIETRO", 2,2));
        liste_tot_tableaux.add(new Tableau(2, "La Lignée de sainte Anne", 1500, "ATELIER DE GÉRARD DAVID",3,3));
        liste_tot_tableaux.add(new Tableau(3, "Vierge à l'Enfant entourée d'anges",  1509, "QUENTIN METSYS",4,4));
        AgentController ag = createGuide();
        listeGuide.add(ag);
        Make_interface();
        doWait(20000);
        addBehaviour(new ProgramNewVisit());
    }

    private AgentController createGuide () {
        try {
            AgentController ag = this.getContainerController().createNewAgent(
                    "Marion",
                    "Agents.Guide",
                    new Object[]{});
            ag.start();
            return ag;
        } catch (StaleProxyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void Make_interface () {
        JFrame t = new JFrame();
        JPanel pan = new JPanel (new GridLayout(taille_x,taille_y));
        Border blackline = BorderFactory.createLineBorder(Color.black,1);
        for(int i = 0; i<taille_x*taille_y;i++){
            JPanel ptest = new JPanel(new GridLayout(2,1));
            JLabel toptest = new JLabel(), downtest = new JLabel();
            List<JLabel> casetest = new ArrayList<>();
            ptest.setBorder(blackline);
            ptest.add(toptest);
            ptest.add(downtest);
            pan.add(ptest);
            listeGrille.add(ptest);
            liste_Couleur_Grille.add(casetest);
            liste_Couleur_Grille.get(i).add(toptest);
            liste_Couleur_Grille.get(i).add(downtest);
        }

        pan.setBorder(blackline);
        t.add(pan);
        t.setVisible(true);
        t.setSize(900,900);
        for (int i=0; i<liste_tot_tableaux.size(); i++) {
            listeGrille.get(liste_tot_tableaux.get(i).posY*taille_y + liste_tot_tableaux.get(i).posX).setBackground(Color.blue);
            liste_Couleur_Grille.get(liste_tot_tableaux.get(i).posY*taille_y + liste_tot_tableaux.get(i).posX).get(0).setIcon(cercle_rouge);
            liste_Couleur_Grille.get(liste_tot_tableaux.get(i).posY*taille_y + liste_tot_tableaux.get(i).posX).get(1).setIcon(cercle_vert);
            liste_Couleur_Grille.get(liste_tot_tableaux.get(i).posY*taille_y + liste_tot_tableaux.get(i).posX).get(1).setText("5");
        }

    }

    public class ProgramNewVisit extends Behaviour {
        private MessageTemplate mt;
        private int step =0;
        private boolean message_ok = false;
        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                    AID aid = null;
                    try {
                        aid = new AID(listeGuide.get(0).getName(), true);
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                    msg.addReceiver(aid);
                    msg.setConversationId("ask_visite");
                    msg.setContent(String.valueOf(4));
                    send(msg);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("ask_visite"),
                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
                    step = 1;
                }
                case 1 -> {
                    //Recevoir le message du guide
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        message_ok = true;
                    }
                }
            }
            }

        @Override
        public boolean done() {
             if (step==1 && message_ok) {
                 System.out.println("La visite peut avoir lieu !");
                 return true;
            }
             else {
                 return false;
             }
        }
    }


    protected void takeDown () {
        System.out.println("Guide "+getAID().getName()+" terminating.");
    }
}
