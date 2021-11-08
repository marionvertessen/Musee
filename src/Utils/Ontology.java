package Utils;

import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.OntologyException;
import jade.content.schema.PredicateSchema;

public class Ontology extends BeanOntology {
    // The name identifying this ontology
    public static final String ONTOLOGY_NAME = "ontology";
    // The singleton instance of this ontology
    private static final Ontology theInstance = new Ontology();

    // This is the method to access the singleton music shop ontology object
    public static Ontology getInstance() {
        return theInstance;
    }

    // Private constructor
    private Ontology() {
        // The music shop ontology extends the basic ontology
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        try {
           add(Pos.class);
        }
        catch (OntologyException oe) {
            oe.printStackTrace();
        }
    }


}