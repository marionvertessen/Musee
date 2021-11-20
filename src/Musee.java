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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Musee extends Agent {
    private final int taille_x = 10;
    private final int taille_y = 10;
    List<Tableau> liste_tot_tableaux = new ArrayList<Tableau>();
    List<JPanel> listeGrille = new ArrayList<>();
    List<List<JLabel>> liste_Couleur_Grille = new ArrayList<>();
    ImageIcon cercle_vert = new ImageIcon(new ImageIcon("cercle-vert-fond-transparent.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
    ImageIcon cercle_rouge = new ImageIcon(new ImageIcon("cercle-rouge-fond-transparent.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
    private final ArrayList<Integer> coord_guide_x = new ArrayList<>(Collections.nCopies(20, -1));
    private final ArrayList<Integer> coord_guide_y = new ArrayList<>(Collections.nCopies(20, -1));
    private final ArrayList<Integer> coord_visitor_x = new ArrayList<>(Collections.nCopies(20, -1));
    private final ArrayList<Integer> coord_visitor_y = new ArrayList<>(Collections.nCopies(20, -1));
    JPanel pan = new JPanel(new GridLayout(taille_x, taille_y));
    public final Ontology ontology = Ontology.getInstance();
    private final Codec codec = new SLCodec();
    private DFAgentDescription[] tourists = new DFAgentDescription[0];
    private DFAgentDescription[] guides = new DFAgentDescription[0];

    protected void setup() {

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Museum");
        sd.setName(getLocalName());
        register(sd);

        System.out.println("Le musée " + getAID().getName() + " est ok !!");
        liste_tot_tableaux.add(new Tableau(1, "Saint Augustin et son disciple Alypius reçoivent la visite de Ponticianus", 1413, "NICCOLÒ DI PIETRO", 2, 2));
        liste_tot_tableaux.add(new Tableau(2, "La Lignée de sainte Anne", 1500, "ATELIER DE GÉRARD DAVID", 3, 3));
        liste_tot_tableaux.add(new Tableau(3, "Vierge à l'Enfant entourée d'anges", 1509, "QUENTIN METSYS", 4, 4));
        createAgent("Agents.Guide",0,0, "Marion","French");
        createAgent("Agents.Tourist", 1,1, "Gauthier", "French");
        createAgent("Agents.Tourist", 2,2, "Paul", "French");
        createAgent("Agents.Tourist", 3,3, "Jean", "French");
        doWait(10000);
        try {
            Make_interface();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        tourists = TakeRegisterOf(this, "Visit1");
        guides = TakeRegisterOf(this, "Guide");
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

    DFAgentDescription[] TakeRegisterOf(Agent agent, String name) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(name);
        dfd.addServices(sd);
        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }

    private void createAgent(String classname, int x, int y, String name, String language) {
        try {
            Object[] args = new Object[3];
            args[0] = x;
            args[1] = y;
            args[2] = language;
            AgentController ag = this.getContainerController().createNewAgent(
                    name,
                    classname,
                    (Object[]) args);
            ag.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private AgentController createTourist(String name, int x, int y, String language) {
        try {
            Object[] args = new Object[2];
            args[0] = x;
            args[1] = y;
            args[2] = language;
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
        t.setSize(600, 600);
        for (Tableau liste_tot_tableau : liste_tot_tableaux) {
            listeGrille.get(liste_tot_tableau.posY * taille_y + liste_tot_tableau.posX).setBackground(Color.blue);
        }
    }

    private void Modifier_grille() {
        for (List<JLabel> jLabels : liste_Couleur_Grille) {
            jLabels.get(0).setIcon(null);
            jLabels.get(0).setText(null);
        }
        //System.out.println(coord_guide_x);
        //System.out.println(coord_guide_y);
        for (int j = 0; j < guides.length; j++) {
            if (coord_guide_x.get(j) !=-1) {
                int nb = CountPeople(coord_guide_x.get(j), coord_guide_y.get(j), coord_guide_x, coord_guide_y);
                liste_Couleur_Grille.get(coord_guide_y.get(j) * taille_y + coord_guide_x.get(j)).get(0).setText(String.valueOf(nb));
                liste_Couleur_Grille.get(coord_guide_y.get(j) * taille_y + coord_guide_x.get(j)).get(0).setIcon(cercle_rouge);
                //System.out.println(coord_guide_x.get(j) + " " + coord_guide_y.get(j) + " " + j);
            }
        }
    }
    private void Modifier_grille_T() {
        for (List<JLabel> jLabels : liste_Couleur_Grille) {
            jLabels.get(1).setIcon(null);
            jLabels.get(1).setText(null);
        }
        for (int j=0; j<tourists.length;j++) {
            if (coord_visitor_x.get(j) !=-1) {
                int nb = CountPeople(coord_visitor_x.get(j), coord_visitor_y.get(j), coord_visitor_x, coord_visitor_y);
                System.out.println(coord_visitor_y.get(j) + " * " + taille_y + " + "  +coord_visitor_x.get(j));
                liste_Couleur_Grille.get(coord_visitor_y.get(j) * taille_y + coord_visitor_x.get(j)).get(1).setText(String.valueOf(nb));
                liste_Couleur_Grille.get(coord_visitor_y.get(j) * taille_y + coord_visitor_x.get(j)).get(1).setIcon(cercle_vert);
            }
        }
    }
    private int CountPeople (int x, int y, List<Integer> list_x, List<Integer> list_y) {
        int number = 0;
        for (int i=0; i<list_x.size(); i++) {
            if (list_x.get(i) == x && list_y.get(i) == y){
                number++;
            }
        }
        return number;
    }

    public class HaveCoord extends Behaviour {
        private int step = 0;
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
                            AID sender = msg.getSender();
                            int position = RetrievePosSender(sender);
                            ce = getContentManager().extractContent(msg);
                            if (ce instanceof Pos pos) {
                                coord_visitor_x.set(position, pos.getX());
                                coord_visitor_y.set(position, pos.getY());
                                step = 1;
                            }

                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                    }

                }
                case 1 -> {
                    //System.out.println("JE SUIS DANS LA DENRIERE BOUCLE");
                    Modifier_grille_T();
                    pan.repaint();
                    //liste_AID.clear();
                    //System.out.println(liste_AID + "  " + coord_visitor_x + "   " +coord_visitor_y);
                    step = 0;

                }
            }
        }

        @Override
        public boolean done() {
            return false;
        }


    }
    private int RetrievePosSender (AID sender) {
        int pos = -1;
        for (int i=0; i<tourists.length; i++) {
            if (Objects.equals(sender.getName(), tourists[i].getName().getName())) {
                pos = i;
            }
        }
        return pos;
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
                    for (DFAgentDescription dfAgentDescription : guides) {
                        AID aid = dfAgentDescription.getName();
                        msg.addReceiver(aid);
                        msg.setConversationId("ask_visit");
                        msg.setContent(String.valueOf("Visit1"));
                        send(msg);
                    }
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("ask_visit"),
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

