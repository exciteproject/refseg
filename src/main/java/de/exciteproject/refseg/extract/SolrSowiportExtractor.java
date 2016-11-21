package de.exciteproject.refseg.extract;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class SolrSowiportExtractor {

    public static void main(String[] args) throws SolrServerException, IOException {
        // URL is not publicly reachable
        String urlString = "http://sowiportbeta.gesis.org/devwork/service/solr/solr_query.php";

        SolrClient solr = new HttpSolrClient(urlString);

        SolrQuery query = new SolrQuery();
        query.set("q", "person_author_txtP_mv:*");
        query.set("fl", "id,person_author_txtP_mv");
        query.set("rows", "100");

        QueryResponse response = solr.query(query);
        SolrDocumentList list = response.getResults();

        System.out.println(list.size());
        System.out.println("---");
        System.out.println(list.get(0));
        solr.close();

    }

}