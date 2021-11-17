package Agents;


import Belief.BeliefGuide;
import Objects.Tableau;
import Utils.Ontology;
import Utils.Pos;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;


public class Guide extends Agent {

    private final BeliefGuide belief = new BeliefGuide();
    public final Ontology ontology = Ontology.getInstance();
    private final Codec codec = new SLCodec();
    private int tour = 0;

    //Initialisation of the agent
    protected void setup() {
        //Init
        init();
        //SetUp Ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        //Display beginning
        System.out.println("Hello! Guide " + getAID().getName() + " is ready.");
        System.out.println("Je suis a la position : (" + belief.getPosX() + ";" + belief.getPosY() + ")");

        //Selection desir
        BeKnown(); //Agent want to know the museum and the tourists and be known by them.

        doWait(10000);
        //addBehaviour(new WaitInformationFromMuseum());
        //addBehaviour(new GoAppointment());
        //addBehaviour(new WaitTourist());
        addBehaviour(new PickUpTouristWithPatience());
        addBehaviour(new MakeVisitWithPatience());

    }

    protected void takeDown() {
        System.out.println("Guide " + getAID().getName() + " terminating.");
    }

    private void init (){
        List<Tableau> liste = new ArrayList<>();
        liste.add(new Tableau(1, "Saint Augustin et son disciple Alypius reçoivent la visite de Ponticianus", 1413, "NICCOLÒ DI PIETRO", 2, 2));
        liste.add(new Tableau(2, "La Lignée de sainte Anne", 1500, "ATELIER DE GÉRARD DAVID", 3, 3));
        liste.add(new Tableau(3, "Vierge à l'Enfant entourée d'anges", 1509, "QUENTIN METSYS", 4, 4));
        belief.setTableauList(liste);
        Object[] args = getArguments();
        int arg1 = (int) args[0]; // Position X
        int arg2 = (int) args[1]; // Position Y
        String arg3 = (String) args[2];
        belief.setPosX(arg1);
        belief.setPosY(arg2);
        belief.setLanguage(arg3);
    }


    /****************************************************************
    * DEFINITION OF DESIRES
    * - Be Known
    * - Pick up tourist with patience
    ****************************************************************/

    private void BeKnown () {
        //Add to register
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Guide");
        sd.setName(getLocalName());
        register(sd);

        //Take museum and tourist AID
        belief.setMuseum(TakeRegisterOf(this, "Museum"));
        belief.setTourists(TakeRegisterOf(this, "Visit1"));

    }

    public class PickUpTouristWithPatience extends Behaviour {
        private int step = 0;
        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    addBehaviour(new WaitInformationFromMuseum());
                    step = 1;
                }
                case 1 -> {
                    if (belief.isAssigned()) {
                        addBehaviour(new GoAppointment());
                        step =2;
                    }

                }
                case 2 -> {
                    if (belief.getPosX() == belief.getPosXAppointment() && belief.getPosY() == belief.getPosYAppointment() ){
                        addBehaviour(new WaitTourist());
                        step = 3;
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return (step == 3);
        }
    }

    public class MakeVisitWithPatience extends Behaviour {
        private int step =0;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    if (belief.getStepTotal()==1) {
                        step = 1;
                    }
                }
                case 1 -> {
                    addBehaviour(new GiveTouristNextPosition());
                    step = 2;
                }
                case 2 -> {
                    addBehaviour(new GoAppointment());
                    step = 3;
                }
                case 3 -> {
                    if (belief.getPosX() == belief.getPosXAppointment() && belief.getPosY() == belief.getPosYAppointment()) {
                        step = 4;
                    }
                }
                case 4 -> {
                    addBehaviour(new SendInformations());
                    //belief.setStepVisit(belief.getStepVisit() + 1);
                    step =5;
                }
                case 5 -> {
                    addBehaviour(new AnswerQuestion());
                    doWait(5000);
                    step = 6;
                }
                case 6 -> {
                    if (tour < belief.getTableauList().size()-1) {
                        tour++;
                        step = 1;
                    }
                    else {
                        step = 7;
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return (step==7);
        }
    }

    /****************************************************************
     * DEFINITION OF INTENTIONS
     * - register : register him in the Guide register
     * - TakeRegisterOf : return a register
     * - GoAppointment (Behaviour): the agent go to the appointment destination
     * - WaitInformationFromMuseum (Behaviour): wait for information from the museum about a visit (if isAssigned : REFUSE else ACCEPT)
     * -
     ****************************************************************/
    private void register(ServiceDescription sd) {
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
        return result;
    }

    public class GoAppointment extends Behaviour {
        private int step = 0;

        public void action() {
            switch (step) {
                case 0 -> {
                    if (belief.isAssigned()) {
                        SendPos();
                        step = 1;
                    }
                }
                case 1 -> {
                    if (belief.getPosX() > belief.getPosXAppointment()) {
                        belief.setPosX(belief.getPosX() - 1);
                    } else if (belief.getPosX() < belief.getPosXAppointment()) {
                        belief.setPosX(belief.getPosX() + 1);
                    } else {
                        if (belief.getPosY() > belief.getPosYAppointment()) {
                            belief.setPosY(belief.getPosY() - 1);
                        } else {
                            belief.setPosY(belief.getPosY() + 1);
                        }
                    }
                    SendPos();
                    doWait(1000);
                }
            }
        }
        @Override
        public boolean done() {
            if (belief.getPosY() == belief.getPosYAppointment() && belief.getPosX() == belief.getPosXAppointment() && step == 1) {
                System.out.println("L'agent " + getAID().getName() + " est au point de rendez-vous en : (" + belief.getPosX() + ";" + belief.getPosY() + ")");
                return true;
            } else {
                return false;
            }
        }
    }

    public class WaitInformationFromMuseum extends Behaviour {
        private int step = 0;
        ACLMessage msg;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("ask_visit"),
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                    msg = myAgent.receive(mt);
                    if (msg != null) {
                        if (belief.isAssigned()) {
                            step = 2;
                        } else {
                            step = 1;
                        }
                    }
                }
                case 1 -> {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    reply.setConversationId("ask_visite");
                    reply.setContent("OK");
                    myAgent.send(reply);
                    belief.setIsAssigned(true);
                    System.out.println("Je suis le guide " + getAID().getName() + " et je prend la visite !");
                    step = 0;
                }
                case 2 -> {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    reply.setConversationId("ask_visite");
                    reply.setContent("NOT OK");
                    myAgent.send(reply);
                    System.out.println("Je suis le guide " + getAID().getName() + " et je prend pas la visite !");
                    step = 0;
                }
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }

    public class WaitTourist extends Behaviour {
        private int step = 0;
        ACLMessage msg;
        private int actual_nb = 0;

        @Override
        public void action() {

            switch (step) {
                case 0 -> {
                    if (belief.getPosX() == belief.getPosXAppointment() && belief.getPosY() == belief.getPosYAppointment()) {
                        step = 1;
                    }
                }
                case 1 -> {

                    System.out.println("Fournisseur de Service 1 : " + belief.getTourists().length + " éléments");
                    for (DFAgentDescription dfAgentDescription : belief.getTourists()) {
                        System.out.println(" " + dfAgentDescription.getName().getLocalName());
                        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                        AID aid = dfAgentDescription.getName();
                        msg.addReceiver(aid);
                        msg.setConversationId("wait");
                        msg.setContent(String.valueOf(4));
                        send(msg);
                    }
                    step = 2;
                }
                case 2 -> {

                    MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("wait"),
                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
                    msg = myAgent.receive(mt);
                    if (msg != null) {
                        actual_nb++;
                    }
                    if (belief.getTourists().length== actual_nb) {
                        System.out.println("Les visiteurs sont tous en place pour la visite");
                        step = 3;
                        belief.setStepTotal(1);
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return step == 3;
        }
    }

    private void SendPos() {
        doWait(500);
        Pos pos = new Pos(belief.getPosX(), belief.getPosY());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId("Guide_pos");

        for (DFAgentDescription dfAgentDescription : belief.getMuseum()) {
            AID aid = dfAgentDescription.getName();
            msg.addReceiver(aid); // sellerAID is the AID of the Seller agent
            msg.setLanguage(codec.getName());
            msg.setOntology(ontology.getName());
            try {
                getContentManager().fillContent(msg, pos);
                send(msg);
            } catch (Codec.CodecException | OntologyException ce) {
                ce.printStackTrace();
            }
        }
    }

    public class GiveTouristNextPosition extends Behaviour {

        @Override
        public void action() {

            for (DFAgentDescription dfAgentDescription : belief.getTourists()) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                Pos pos = new Pos(belief.getTableauList().get(tour).posX, belief.getTableauList().get(tour).posY);
                AID aid = dfAgentDescription.getName();
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());
                msg.addReceiver(aid);
                msg.setConversationId("next_position");
                try {
                    getContentManager().fillContent(msg, pos);
                    send(msg);
                } catch (Codec.CodecException | OntologyException ce) {
                    ce.printStackTrace();
                }
            }
            belief.setPosXAppointment(belief.getTableauList().get(tour).posX);
            belief.setPosYAppointment(belief.getTableauList().get(tour).posY);
        }

        @Override
        public boolean done() {
            return true;
        }


    }

    public class SendInformations extends Behaviour {

        @Override
        public void action() {
            for (DFAgentDescription dfAgentDescription : belief.getTourists()) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                AID aid = dfAgentDescription.getName();
                msg.addReceiver(aid);
                msg.setConversationId("Informations_tableaux");
                String information = belief.getTableauList().get(belief.getStepVisit()).name + " " +belief.getTableauList().get(belief.getStepVisit()).date + " " + belief.getTableauList().get(belief.getStepVisit()).auteur;
                msg.setContent(String.valueOf(information));
                send(msg);
            }
        }

        @Override
        public boolean done() {
            return true;
        }
    }
    public static class AnswerQuestion extends Behaviour {
        private int step = 0;
        ACLMessage msg;


        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("QUESTION"),
                    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.setConversationId("QUESTION");
                reply.setContent("Explanation");
                myAgent.send(reply);
                step = 1;
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }
}