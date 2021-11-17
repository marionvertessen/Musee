package Belief;

import jade.domain.FIPAAgentManagement.DFAgentDescription;

public class BeliefTourist {

    private int posX =3;
    private int posY =3;
    private int posXAppointment = 9;
    private int posYAppointment = 9;
    private boolean isAssigned = false;
    private  DFAgentDescription[] museum = new DFAgentDescription[0];
    private  DFAgentDescription[] guide = new DFAgentDescription[0];
    private boolean first = true;
    private  String language;
    private int stepTotal = 0;

    public int getStepTotal() {
        return stepTotal;
    }

    public void setStepTotal(int stepTotal) {
        this.stepTotal = stepTotal;
    }

    public DFAgentDescription[] getGuide() {
        return guide;
    }

    public void setGuide(DFAgentDescription[] guide) {
        this.guide = guide;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosXAppointment() {
        return posXAppointment;
    }

    public int getPosYAppointment() {
        return posYAppointment;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public DFAgentDescription[] getMuseum() {
        return museum;
    }

    public boolean isFirst() {
        return first;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public void setPosXAppointment(int posXAppointment) {
        this.posXAppointment = posXAppointment;
    }

    public void setPosYAppointment(int posYAppointment) {
        this.posYAppointment = posYAppointment;
    }

    public void setAssigned(boolean assigned) {
        this.isAssigned = assigned;
    }

    public void setMuseum(DFAgentDescription[] museum) {
        this.museum = museum;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }
}
