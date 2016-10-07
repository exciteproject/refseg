package de.exciteproject.refseg.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import de.exciteproject.refseg.preproc.NamePreprocessor;

public class GNDAuthorExtractor extends AuthorExtractor {

    /**
     *
     * @param args[0]:
     *            tdb directory
     * @param args[1]:
     *            output directory for name files
     */
    public static void main(String[] args) {
        File tdbDirectory = new File(args[0]);
        File outputDirectory = new File(args[1]);
        int maxNumberOfNames = Integer.parseInt(args[2]);
        boolean onlyDifferentiated = Boolean.parseBoolean(args[3]);

        GNDAuthorExtractor gndAuthorExtractor = new GNDAuthorExtractor();
        List<String> nameStringList = gndAuthorExtractor.extractAuthorNames(tdbDirectory, onlyDifferentiated);

        gndAuthorExtractor.addNameStringListToMaps(nameStringList, maxNumberOfNames);
        gndAuthorExtractor.writeMaps(outputDirectory);
    }

    /**
     *
     * @param tdbDirectory
     *            file path to tdb database containing the GND rdf data
     *
     */
    public GNDAuthorExtractor() {
    }

    public List<String> extractAuthorNames(File tdbDirectory, boolean onlyDifferentiated) {
        Dataset dataset = TDBFactory.createDataset(tdbDirectory.getAbsolutePath());
        Model model = dataset.getDefaultModel();

        List<String> nameStringList = new ArrayList<String>();
        String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
        prefixes += "PREFIX gndo: <http://d-nb.info/standards/elementset/gnd#> \n";
        String queryString = prefixes + "SELECT ?forename ?surname WHERE " + "{ ";
        if (onlyDifferentiated) {
            queryString += "?person rdf:type gndo:DifferentiatedPerson . \n";
        } else {
            queryString += "{" + "?person rdf:type gndo:UndifferentiatedPerson . \n" + "} UNION {"
                    + "?person rdf:type gndo:DifferentiatedPerson . \n" + "}";
        }
        queryString += "?person gndo:preferredNameEntityForThePerson ?nameEntity . \n"
                + "?nameEntity gndo:forename ?forename . \n" + "?nameEntity gndo:surname ?surname . " + "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();

        while (results.hasNext()) {
            QuerySolution binding = results.nextSolution();

            LiteralImpl firstNameLiteral = (LiteralImpl) binding.get("forename");
            LiteralImpl lastNameLiteral = (LiteralImpl) binding.get("surname");

            String firstNames = NamePreprocessor.preprocessName(firstNameLiteral.toString());
            String lastNames = NamePreprocessor.preprocessName(lastNameLiteral.toString());

            nameStringList.add(lastNames + ", " + firstNames);

        }

        model.close();
        dataset.close();
        return nameStringList;

    }

}
