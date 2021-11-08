package Agents;

import Objects.Tableau;
import Utils.Ontology;
import Utils.Pos;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.AgentTree;
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
    private final int taille_x = 10;
    private final int taille_y = 10;
    List<Tableau> liste_tot_tableaux = new ArrayList<Tableau>();
    List<AgentController> listeGuide = new ArrayList<>();
    List<AgentController> listeToursit = new ArrayList<>();
    List<JPanel> listeGrille = new ArrayList<>();
    List<List<JLabel>> liste_Couleur_Grille = new ArrayList<>();
    ImageIcon cercle_vert = new ImageIcon(new ImageIcon("cercle-vert-fond-transparent.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
    ImageIcon cercle_rouge = new ImageIcon(new ImageIcon("cercle-rouge-fond-transparent.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
    private final ArrayList<Integer> coord_guide_x = new ArrayList<>();
    private final ArrayList<Integer> coord_guide_y = new ArrayList<>();
    private final ArrayList<Integer> coord_visitor_x = new ArrayList<>();
    private final ArrayList<Integer> coord_visitor_y = new ArrayList<>();
    JPanel pan = new JPanel(new GridLayout(taille_x, taille_y));
    public final Ontology ontology = Ontology.getInstance();
    private final Codec codec = new SLCodec();
    private final int nb_tourist = 3;

    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Musee");
        sd.setName(getLocalName());
        register(sd);

        System.out.println("Le musée " + getAID().getName() + " est ok !!");
        liste_tot_tableaux.add(new Tableau(1, "Saint Augustin et son disciple Alypius reçoivent la visite de Ponticianus", 1413, "NICCOLÒ DI PIETRO", 2, 2));
        liste_tot_tableaux.add(new Tableau(2, "La Lignée de sainte Anne", 1500, "ATELIER DE GÉRARD DAVID", 3, 3));
        liste_tot_tableaux.add(new Tableau(3, "Vierge à l'Enfant entourée d'anges", 1509, "QUENTIN METSYS", 4, 4));
        AgentController ag = createGuide(0,0);
        AgentController ag2 = createTourist("Gauthier", 2,2);
        AgentController ag3 = createTourist("Paul", 5,5);
        AgentController ag4 = createTourist("Jean", 6,6);
        listeGuide.add(ag);
        listeToursit.add(ag2);
        listeToursit.add(ag3);
        listeToursit.add(ag4);
        try {
            Make_interface();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        doWait(20000);
        addBehaviour(new HaveCoord());
        addBehaviour(new HaveCoordTourist());
        addBehaviour(new ProgramNewVisit());

    }

    void register(ServiceDescription sd) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private AgentController createGuide(int x, int y) {
        try {
            Object[] args = new Object[2];
            args[0] = x;
            args[1] = y;
            AgentController ag = this.getContainerController().createNewAgent(
                    "Marion",
                    "Agents.Guide",
                    (Object[]) args);
            ag.start();
            return ag;
        } catch (StaleProxyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private AgentController createTourist(String name, int x, int y) {
        try {
            Object[] args = new Object[2];
            args[0] = x;
            args[1] = y;
            AgentController ag = this.getContainerController().createNewAgent(
                    name,
                    "Agents.Tourist",
                    args);
            ag.start();
            return ag;
        } catch (StaleProxyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void Make_interface() throws StaleProxyException {
        JFrame t = new JFrame();
        Border blackline = BorderFactory.createLineBorder(Color.black, 1);
        for (int i = 0; i < taille_x * taille_y; i++) {
            JPanel ptest = new JPanel(new GridLayout(2, 1));
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
        t.setSize(900, 900);
        for (int i = 0; i < liste_tot_tableaux.size(); i++) {
            listeGrille.get(liste_tot_tableaux.get(i).posY * taille_y + liste_tot_tableaux.get(i).posX).setBackground(Color.blue);
            liste_Couleur_Grille.get(liste_tot_tableaux.get(i).posY * taille_y + liste_tot_tableaux.get(i).posX).get(1).setText("5");
        }
    }

    private void Modifier_grille() {
        for (int k = 0; k < liste_Couleur_Grille.size(); k++) {
            liste_Couleur_Grille.get(k).get(0).setIcon(null);
        }
        System.out.println(coord_guide_x);
        System.out.println(coord_guide_y);
        for (int j = 0; j < listeGuide.size(); j++) {
            liste_Couleur_Grille.get(coord_guide_y.get(j) * taille_y + coord_guide_x.get(j)).get(0).setIcon(cercle_rouge);
            System.out.println(coord_guide_x.get(j) + " " + coord_guide_y.get(j) + " " + j);
        }
    }
    private void Modifier_grille_T() {
        for (int k = 0; k < liste_Couleur_Grille.size(); k++) {
            liste_Couleur_Grille.get(k).get(1).setIcon(null);
        }
        for (int j=0; j<listeToursit.size();j++) {
            liste_Couleur_Grille.get(coord_visitor_y.get(j)*taille_y + coord_visitor_x.get(j)).get(1).setIcon(cercle_vert);
        }
    }

    public class HaveCoord extends Behaviour {
        private int step = 0;
        private boolean guide = false;
        private boolean tourist = false;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.and(
                                    MessageTemplate.MatchOntology(ontology.getName()),
                                    MessageTemplate.MatchConversationId("Guide_pos")));
                    ACLMessage msg = receive(mt);
                    ContentElement ce;
                    if (msg != null) {
                        try {
                            ce = getContentManager().extractContent(msg);
                            if (ce instanceof Pos pos) {
                                coord_guide_x.add(pos.getX());
                                coord_guide_y.add(pos.getY());
                                step=1;
                            }
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                    }
                }
                case 1 -> {
                    Modifier_grille();
                    pan.repaint();
                    coord_guide_x.clear();
                    coord_guide_y.clear();
                    step = 0;
                }
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }

    public class HaveCoordTourist extends Behaviour {
        private int step = 0;
        private int comp = 0;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.and(
                            MessageTemplate.MatchOntology(ontology.getName()),
                            MessageTemplate.MatchConversationId("Tourist_pos")));
                    ACLMessage msg = receive(mt);
                    ContentElement ce;
                    if (msg != null) {
                        try {
                            ce = getContentManager().extractContent(msg);
                            if (ce instanceof Pos pos) {
                                coord_visitor_x.add(pos.getX());
                                coord_visitor_y.add(pos.getY());
                                comp++;
                                System.out.println(coord_visitor_x);
                                System.out.println(coord_visitor_y);
                                //System.out.println("Message de la part : "+ msg);
                            }
                            if (comp == nb_tourist) {
                                step = 1;
                            }
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                    }

                }
                case 1 -> {
                    System.out.println("JE SUIS DANS LA DENRIERE BOUCLE");
                    Modifier_grille_T();
                    pan.repaint();
                    coord_visitor_x.clear();
                    coord_visitor_y.clear();
                    comp =0 ;
                    step = 0;
                }
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }


    public class ProgramNewVisit extends Behaviour {
        private MessageTemplate mt;
        private int step = 0;
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
                    msg.setContent(String.valueOf(3));
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
            if (step == 1 && message_ok) {
                System.out.println("La visite peut avoir lieu !");
                return true;
            } else {
                return false;
            }
        }
    }

    protected void takeDown() {
        System.out.println("Guide " + getAID().getName() + " terminating.");
    }
}

