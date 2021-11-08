package Agents;

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
import jade.wrapper.AgentController;

public class Tourist extends Agent {

    public int posX =3;
    public int posY =3;
    public int posxrdv = 9;
    public int posyrdv = 9;
    public boolean enAttente = false;
    public final Ontology ontology = Ontology.getInstance();
    private Codec codec = new SLCodec();


    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        System.out.println("Le touriste " + getAID().getName() + " est ok !!");
        System.out.println("Le trouriste" + getAID().getName() + "est a la position (" + posX + ";" + posY + ")");
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Visite1");
        sd.setName(getLocalName());
        register(sd);
        addBehaviour(new GoRDV());
        addBehaviour(new waitGuide());
        addBehaviour(new SendPos());
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

    protected void takeDown() {
        System.out.println("Touriste " + getAID().getName() + " terminating.");
    }

    public class GoRDV extends Behaviour {
        public void action() {
            if (posX > posxrdv) {
                posX = posX - 1;
            } else if (posX < posyrdv) {
                posX = posX + 1;
            } else {
                if (posY > posyrdv) {
                    posY = posY - 1;
                } else {
                    posY = posY + 1;
                }
            }
            //System.out.println("L'agent " + getAID().getName() + " est à la position : (" + posX + ";" + posY + ")");
            doWait(2000);
        }

        @Override
        public boolean done() {
            if (posY == posyrdv && posX == posxrdv) {
                System.out.println("L'agent " + getAID().getName() + " est au point de rendez-vous en : (" + posX + ";" + posY + ")");
                enAttente = true;
                return true;
            } else {
                return false;
            }
        }
    }

    public class waitGuide extends Behaviour {
        private int step = 0;
        private MessageTemplate mt;
        ACLMessage msg;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("wait"),
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                    msg = myAgent.receive(mt);
                    if (msg != null) {
                        String message = msg.getContent();
                        enAttente = false;
                        if (posX == posxrdv && posY == posyrdv) {
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
                }
            }
        }

        @Override
        public boolean done() {
            return (step == 2);
        }
    }

    public class SendPos extends Behaviour {
        boolean first = true;
        @Override
        public void action() {
            if (first) {
                Object[] args = getArguments();
                int arg1 = (int) args[0]; // this returns the String 1
                int arg2 = (int)args[1]; // this returns the String arg2
                posX = arg1;
                posY = arg2;
                first =false;
            }
            doWait(1000);
            Pos pos = new Pos(posX, posY);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setConversationId("Tourist_pos");

            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            sd.setType("Musee");
            dfd.addServices(sd);
            DFAgentDescription[] result = new DFAgentDescription[0];
            try {
                result = DFService.search(getAgent(), dfd);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            for (DFAgentDescription dfAgentDescription : result) {
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

        @Override
        public boolean done() {
            return false;
        }
    }
}
