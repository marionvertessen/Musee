package Belief;

import Objects.Tableau;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.ArrayList;
import java.util.List;

public class BeliefGuide {
    private int posX =0;
    private int posY=0;
    private String language ="French";
    private int posXAppointment =9;
    private int posYAppointment =9;
    private boolean isAssigned = false;
    private List<List<Integer>> listePosition = new ArrayList<List<Integer>>();
    private DFAgentDescription[] museum = new DFAgentDescription[0];
    private DFAgentDescription[] tourists = new DFAgentDescription[0];
    private int stepVisit = 0;
    private int stepTotal = 0;

    public int getStepTotal() {
        return stepTotal;
    }

    public void setStepTotal(int stepTotal) {
        this.stepTotal = stepTotal;
    }

    public void setTableauList(List<Tableau> tableauList) {
        TableauList = tableauList;
    }

    public void setListePosition(List<List<Integer>> listePosition) {
        this.listePosition = listePosition;
    }

    private List<Tableau> TableauList= new ArrayList<Tableau>();

    public List<Tableau> getTableauList() {
        return TableauList;
    }

    public List<List<Integer>> getListePosition() {
        return listePosition;
    }



    public void setStepVisit(int stepVisit) {
        this.stepVisit = stepVisit;
    }

    public int getStepVisit() {
        return stepVisit;
    }

    public void setLanguage(String language) {
        this.language = language;
    }



    public void setTourists(DFAgentDescription[] tourists) {
        this.tourists = tourists;
    }

    public DFAgentDescription[] getTourists() {
        return tourists;
    }

    public DFAgentDescription[] getMuseum() {
        return museum;
    }

    public void setMuseum(DFAgentDescription[] museum) {
        this.museum = museum;
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

    public void setIsAssigned(boolean estAssigne) {
        this.isAssigned = estAssigne;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public String getLanguage() {
        return language;
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
}
