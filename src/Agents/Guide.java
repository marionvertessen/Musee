package Agents;


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

import javax.swing.text.Position;


public class Guide extends Agent {

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    private int posX;
    private int posY;
    private String language;
    private int posXrdv;
    private int posYrdv;
    private int nb_visteurs;
    private boolean estAssigne = false;
    private int nb_personne_visit = -1;
    public final Ontology ontology = Ontology.getInstance();
    private Codec codec = new SLCodec();


    //Initialisation de l'agent
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        System.out.println("Hello! Guide " +getAID().getName()+" is ready.");
        this.posXrdv = 9;
        this.posY =0;
        this.posX =0;
        this.posYrdv = 9;
        this.language = "Francais";
        System.out.println("Je suis a la position : (" + posX + ";"+posY+")");
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Guide");
        sd.setName(getLocalName());
        register(sd);

        addBehaviour(new askNbTouristes());
        addBehaviour(new SendPos());
        addBehaviour(new GoRdv());
        addBehaviour(new WaitTourist());

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
    protected void takeDown () {
        System.out.println("Guide "+getAID().getName()+" terminating.");
    }

    public class start_visit extends Behaviour {
        private int step = 0;
        @Override
        public void action() {
            addBehaviour(new askNbTouristes());
            addBehaviour(new GoRdv());
            doWait(200000);

            addBehaviour(new WaitTourist());
        }

        @Override
        public boolean done() {
            return false;
        }
    }

    public class GoRdv extends Behaviour {
        private int step = 0;
        public void action() {
            switch (step) {
                case 0 -> {
                    if (estAssigne) {
                        step = 1;
                    }
                }
                case 1 -> {
                    if (posX > posXrdv) {
                        posX = posX - 1;
                    } else if (posX < posXrdv) {
                        posX = posX + 1;
                    } else {
                        if (posY > posYrdv) {
                            posY = posY - 1;
                        } else {
                            posY = posY + 1;
                        }
                    }
                    System.out.println("L'agent " + getAID().getName() + " est à la position : (" + posX + ";" + posY + ")");
                    doWait(2000);
                }
            }
        }

        @Override
        public boolean done() {
            if (posY == posYrdv && posX == posXrdv && step == 1) {
                System.out.println("L'agent " + getAID().getName() + " est au point de rendez-vous en : (" + posX + ";" + posY + ")");
                return true;
            } else {
                return false;
            }
        }
    }

    public class askNbTouristes extends  Behaviour {
        private int step = 0;
        ACLMessage msg;
        private boolean message_ok = false;
        private MessageTemplate mt;
        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("ask_visite"),
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                    msg = myAgent.receive(mt);
                    if (msg != null) {
                        String message = msg.getContent();
                        nb_visteurs = Integer.parseInt(message);
                        System.out.println(nb_visteurs);
                        if (estAssigne) {
                            step = 2;
                        }
                        else {
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
                    message_ok = true;
                    estAssigne = true;
                    System.out.println("Je suis le guide " + getAID().getName() + " et je prend la visite !");
                }
                case 2 -> {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    reply.setConversationId("ask_visite");
                    reply.setContent("NOT OK");
                    myAgent.send(reply);
                    message_ok = true;
                    System.out.println("Je suis le guide " + getAID().getName() + " et je prend pas la visite !");
                }
            }
        }

        @Override
        public boolean done() {
            return ((step==1 || step == 0 )&& message_ok);
        }
    }

    public class WaitTourist extends Behaviour {
        private int step = 0;
        ACLMessage msg;
        private int actual_nb = 0;
        private MessageTemplate mt;
        @Override
        public void action() {

            switch (step){
                case 0 -> {
                    if (posX == posXrdv && posY == posYrdv) {
                        step = 1;
                    }
                }
                case 1 -> {
                    DFAgentDescription dfd = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();

                    sd.setType("Visite1");
                    dfd.addServices(sd);
                    DFAgentDescription[] result = new DFAgentDescription[0];
                    try {
                        result = DFService.search(getAgent(),dfd);
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Fournisseur de Service 1 : " + result.length + " éléments");
                    for (DFAgentDescription dfAgentDescription : result) {
                        System.out.println(" " + dfAgentDescription.getName().getLocalName());
                        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                        AID aid = dfAgentDescription.getName();
                        msg.addReceiver(aid);
                        msg.setConversationId("wait");
                        msg.setContent(String.valueOf(4));
                        send(msg);
                    }
                    step =2;
                }
                case 2 -> {

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("wait"),
                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
                    msg = myAgent.receive(mt);
                    if (msg != null) {
                        actual_nb++;
                    }
                    if (nb_visteurs == actual_nb) {
                        System.out.println("Les visiteurs sont tous en place pour la visite");
                        step = 3;
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return step == 3;
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
            msg.setConversationId("Guide_pos");

            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            sd.setType("Musee");
            dfd.addServices(sd);
            DFAgentDescription[] result = new DFAgentDescription[0];
            try {
                result = DFService.search(getAgent(),dfd);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            for (DFAgentDescription dfAgentDescription : result) {
                AID aid = dfAgentDescription.getName();
                msg.addReceiver(aid); // sellerAID is the AID of the Seller agent
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());
                try {
                    getContentManager().fillContent(msg,pos);
                    send(msg);
                }
                catch (Codec.CodecException | OntologyException ce) {
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
