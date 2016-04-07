package de.mkrnr.rse.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.tdb.TDBFactory;

public class GNDAuthorExtractor {

    /**
     *
     * @param args[0]:
     *            tdb directory
     * @param args[1]:
     *            output file for forenames
     * @param args[2]:
     *            output file for surnames
     */
    public static void main(String[] args) {
        File tdbDirectory = new File(args[0]);
        GNDAuthorExtractor gndAuthorExtractor = new GNDAuthorExtractor(tdbDirectory);

        gndAuthorExtractor.extractAuthorNames(new File(args[1]), new File(args[2]));
        gndAuthorExtractor.close();
    }

    private Dataset dataset;
    private Model model;

    /**
     *
     * @param tdbDirectory
     *            file path to tdb database containing the GND rdf data
     *
     */
    public GNDAuthorExtractor(File tdbDirectory) {
        this.dataset = TDBFactory.createDataset(tdbDirectory.getAbsolutePath());
        this.model = this.dataset.getDefaultModel();
    }

    public void close() {
        this.model.close();
        this.dataset.close();
    }

    public void extractAuthorNames(File forenameOutputFile, File surnameOutputFile) {
        String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
        prefixes += "PREFIX gndo: <http://d-nb.info/standards/elementset/gnd#> \n";
        String queryString = prefixes + "SELECT ?forename ?surname WHERE " + "{ " + "{"
                + "?person rdf:type gndo:UndifferentiatedPerson . \n" + "} UNION {"
                + "?person rdf:type gndo:DifferentiatedPerson . \n" + "}"
                + "?person gndo:preferredNameEntityForThePerson ?nameEntity ."
                + "?nameEntity gndo:forename ?forename . " + "?nameEntity gndo:surname ?surname . " + "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
        ResultSet results = qexec.execSelect();

        HashMap<String, Integer> forenameMap = new HashMap<String, Integer>();
        HashMap<String, Integer> surnameMap = new HashMap<String, Integer>();

        while (results.hasNext()) {
            QuerySolution binding = results.nextSolution();

            LiteralImpl forename = (LiteralImpl) binding.get("forename");
            LiteralImpl surname = (LiteralImpl) binding.get("surname");

            this.addNamesToMap(forename.toString(), forenameMap);
            this.addNamesToMap(surname.getString(), surnameMap);

        }

        this.writeMapToFile(forenameMap, forenameOutputFile);
        this.writeMapToFile(surnameMap, surnameOutputFile);

    }

    private void addNamesToMap(String names, HashMap<String, Integer> map) {
        String[] namesSplit = names.split(" ");
        for (String name : namesSplit) {
            if (map.containsKey(name)) {
                map.put(name, map.get(name) + 1);
            } else {
                map.put(name, 1);
            }
        }

    }

    private void writeMapToFile(HashMap<String, Integer> map, File outputFile) {
        try {
            BufferedWriter forenameWriter = new BufferedWriter(new FileWriter(outputFile));

            for (Entry<String, Integer> entry : map.entrySet()) {
                forenameWriter.write(entry.getKey() + "\t" + entry.getValue() + System.lineSeparator());
            }

            forenameWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
