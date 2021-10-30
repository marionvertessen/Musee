package Agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class Guide extends Agent {
    private int posX;
    private int posY;
    private String language;
    private int posXrdv;
    private int posYrdv;
    private int nb_visteurs;
    private boolean estAssigne = false;
    private int nb_personne_visit = -1;

    //Initialisation de l'agent
    protected void setup() {
        System.out.println("Hello! Guide " +getAID().getName()+" is ready.");
        this.posXrdv = 10;
        this.posY =0;
        this.posX =0;
        this.posYrdv = 10;
        this.language = "Francais";
        System.out.println("Je suis a la position : (" + posX + ";"+posY+")");

        addBehaviour(new askNbTouristes());
        addBehaviour(new GoRdv());
    }

    protected void takeDown () {
        System.out.println("Guide "+getAID().getName()+" terminating.");
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
}
