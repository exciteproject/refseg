package de.mkrnr.rse.preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class that downloads a specified number of randomly selected articles from
 * a given list in JSON format via the OAI portal
 * (https://dbk.gesis.org/dbkoai/?verb=Identify).
 */
public class OAIPaperExtractor {

    public static void main(String[] args) {
        OAIPaperExtractor oaiPaperExtractor = new OAIPaperExtractor();
        // oaiPaperExtractor.writeRandomArticles(new File(args[0]), new
        // File(args[1]));
        oaiPaperExtractor.downloadArticles(250, 499, new File(args[1]), new File(args[2]));

    }

    public void downloadArticles(int startIndex, int endIndex, File jsonFile, File outputDirectory) {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // read input json file
        JSONObject jsonObject = this.readFile(jsonFile);

        JSONArray articles = jsonObject.getJSONArray("articles");
        int articleIndex = 0;
        for (Object articleObject : articles) {
            if (articleIndex < startIndex) {
                articleIndex++;
                continue;
            }
            if (articleIndex > endIndex) {
                break;
            }
            JSONObject article = (JSONObject) articleObject;
            for (String key : article.keySet()) {
                String[] keySplit = key.split("/");

                String pdfName = keySplit[keySplit.length - 1];
                System.out.println("current index: " + articleIndex);
                System.out.println("download: " + pdfName);
                try {
                    FileUtils.copyURLToFile(new URL(article.getString(key)),
                            new File(outputDirectory.getAbsolutePath() + File.separator + pdfName));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                articleIndex++;
            }
        }
    }

    private JSONObject readFile(File jsonFile) {
        JSONObject jsonObject = null;
        try {
            InputStream inputStream = new FileInputStream(jsonFile);
            String jsonString = IOUtils.toString(inputStream);
            jsonObject = new JSONObject(jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Writes the read article list in a random order.
     *
     * @param outputFile
     */
    public void writeRandomArticles(File jsonFile, File outputFile) {
        // read input json file
        JSONObject jsonObject = this.readFile(jsonFile);

        Iterator<String> keys = jsonObject.keys();

        // store article keys in array
        ArrayList<String> articleKeys = new ArrayList<String>();
        while (keys.hasNext()) {
            articleKeys.add(keys.next());
        }

        // shuffle the list of article keys
        Collections.shuffle(articleKeys);

        JSONObject shuffledJSONObject = new JSONObject();
        JSONArray shuffledJSONArray = new JSONArray();

        for (String articleKey : articleKeys) {
            JSONObject articleObject = new JSONObject();
            articleObject.put(articleKey, jsonObject.get(articleKey));
            shuffledJSONArray.put(articleObject);
        }

        shuffledJSONObject.put("articles", shuffledJSONArray);

        String shuffledJSONString = shuffledJSONObject.toString(4);

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            bufferedWriter.write(shuffledJSONString);
            bufferedWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
