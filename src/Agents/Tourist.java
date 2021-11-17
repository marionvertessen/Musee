package Agents;

import Belief.BeliefTourist;
import Utils.Ontology;
import Utils.Pos;
import jade.content.ContentElement;
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

public class Tourist extends Agent {

    public final Ontology ontology = Ontology.getInstance();
    private final Codec codec = new SLCodec();
    private final BeliefTourist belief = new BeliefTourist();


    protected void setup() {
        init();

        //setup ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        //Affichage
        System.out.println("The tourist " + getAID().getName() + " is ok !!");
        System.out.println("The tourist" + getAID().getName() + "in the position (" + belief.getPosX() + ";" + belief.getPosY() + ")");

        BeKnown();
        //Add behaviours
        addBehaviour(new BeginVisit());
        addBehaviour(new VisitWithQuestions());
    }

    private void init() {
        Object[] args = getArguments();
        int arg1 = (int) args[0]; // Position X
        int arg2 = (int) args[1]; // Position Y
        String arg3 = (String) args[2];
        belief.setPosX(arg1);
        belief.setPosY(arg2);
        belief.setLanguage(arg3);
    }

    protected void takeDown() {
        System.out.println("Touriste " + getAID().getName() + " terminating.");
    }

    /****************************************************************
     * DEFINITION OF DESIRES
     * - Be Known
     * - Begin the visit with the guide
     ****************************************************************/
    private void BeKnown() {
        //Add to visitor book
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Visit1");
        sd.setName(getLocalName());
        register(sd);

        //Take museum AID
        belief.setMuseum(TakeRegisterOf(this, "Museum"));
        belief.setGuide(TakeRegisterOf(this, "Guide"));
    }

    public class BeginVisit extends Behaviour {
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    addBehaviour(new GoAppointment());
                    step = 1;
                }
                case 1 -> {
                    if (belief.getPosX() == belief.getPosXAppointment() && belief.getPosY() == belief.getPosYAppointment()) {
                        addBehaviour(new waitGuide());
                        step = 2;
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return (step == 2);
        }
    }

    public class VisitWithQuestions extends Behaviour {
        private int step = 0;
        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    if (belief.getStepTotal()==1) {
                        step=1;
                    }
                }
                case 1 -> {
                    addBehaviour(new waitPosition());

                    if (belief.getPosX() != belief.getPosYAppointment() && belief.getPosY() != belief.getPosYAppointment()) {
                        step = 2;
                    }
                }
                case 2 -> {
                    addBehaviour(new GoAppointment());
                    step = 3;
                }
                case 3 -> {
                    if (belief.getPosX() == belief.getPosYAppointment() && belief.getPosY() == belief.getPosYAppointment()) {
                        step =4;
                    }
                }
                case 4 -> {
                    addBehaviour(new ListenInformations());
                    step = 5;
                }
                case 5 -> {
                    System.out.println("STEP 5");
                    if (belief.getStepTotal() ==3) {
                        step = 6;
                        System.out.println("STEP 5 +");
                    }
                }
                case 6-> {
                    System.out.println("STEP 6");
                    double rand = Math.random(); //Nombre aléatoire compris entre 0 et 1

                    if (rand<0.5) { //L'agent pose une question avec une probabilité de 30%
                        addBehaviour(new AskQuestion());
                    }

                    //doWait(5000);
                    step = 1;
                }
                case 7 -> {
                    step = 1;
                }
            }
        }

        @Override
        public boolean done() {
            return (step==8);
        }
    }


    /****************************************************************
     * DEFINITION OF INTENTIONS
     * - register : register him in the Guide register
     * - TakeRegisterOf : return a register
     * - GoAppointment (Behaviour): the agent go to the appointment destination
     * -
     ****************************************************************/

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
        return result;
    }

    public class GoAppointment extends Behaviour {
        public void action() {
            if (belief.getPosX() > belief.getPosXAppointment()) {
                belief.setPosX(belief.getPosX() - 1);
            } else if (belief.getPosX() < belief.getPosXAppointment()) {
                belief.setPosX(belief.getPosX() + 1);
            } else {
                if (belief.getPosY() > belief.getPosYAppointment()) {
                    belief.setPosY(belief.getPosY() - 1);
                } else if (belief.getPosY() < belief.getPosYAppointment()) {
                    belief.setPosY(belief.getPosY() + 1);
                }
            }
            //System.out.println("L'agent " + getAID().getName() + " est à la position : (" + posX + ";" + posY + ")");
            SendPos();
            doWait(1000);
        }

        @Override
        public boolean done() {
            if (belief.getPosY() == belief.getPosYAppointment() && belief.getPosX() == belief.getPosXAppointment()) {
                System.out.println("L'agent " + getAID().getName() + " est au point de rendez-vous en : (" + belief.getPosX() + ";" + belief.getPosY() + ")");
                belief.setAssigned(true);
                return true;
            } else {
                return false;
            }
        }
    }

    public class waitGuide extends Behaviour {
        private int step = 0;
        ACLMessage msg;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("wait"),
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                    msg = myAgent.receive(mt);
                    if (msg != null) {
                        if (belief.getPosX() == belief.getPosXAppointment() && belief.getPosY() == belief.getPosYAppointment()) {
                            step = 1;
                        }
                    }
                }
                case 1 -> {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    reply.setConversationId("wait");
                    reply.setContent("OK");
                    myAgent.send(reply);
                    step = 2;
                    belief.setStepTotal(1);
                }
            }
        }

        @Override
        public boolean done() {
            return (step == 2);
        }
    }

    private void SendPos() {
        doWait(500);
        Pos pos = new Pos(belief.getPosX(), belief.getPosY());
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId("Tourist_pos");

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

    public class waitPosition extends Behaviour {
        private int step = 0;
        ACLMessage msg;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.and(
                                    MessageTemplate.MatchOntology(ontology.getName()),
                                    MessageTemplate.MatchConversationId("next_position")));
                    ACLMessage msg = receive(mt);
                    ContentElement ce;
                    if (msg != null) {
                        try {
                            ce = getContentManager().extractContent(msg);
                            if (ce instanceof Pos pos) {
                                belief.setPosXAppointment(pos.getX());
                                belief.setPosYAppointment(pos.getY());
                                System.out.println(belief.getPosX());
                                System.out.println(belief.getPosY());
                                step = 1;
                                belief.setStepTotal(2);
                            }
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                    }
                }
                case 1 -> {

                }
            }
        }

        @Override
        public boolean done() {
            return (step ==1 );
        }
    }

    public class ListenInformations extends Behaviour {
        private int step = 0;
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Informations_tableaux"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                belief.setStepTotal(3);
                step = 1;
            }
        }

        @Override
        public boolean done() {
            return (step==1);
        }
    }

    public class AskQuestion extends Behaviour {

        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setConversationId("QUESTION");
            for (DFAgentDescription dfAgentDescription : belief.getGuide()) {
                msg.setContent("What is that ?");
                AID aid = dfAgentDescription.getName();
                msg.addReceiver(aid); // sellerAID is the AID of the Seller agent
                send(msg);
            }
        }

        @Override
        public boolean done() {
            return true;
        }
    }

}
