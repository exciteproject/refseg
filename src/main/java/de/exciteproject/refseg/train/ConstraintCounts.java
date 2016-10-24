package de.exciteproject.refseg.train;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConstraintCounts {

    private Map<String, Map<String, Integer>> wordsCounts;

    public ConstraintCounts() {
        this.wordsCounts = new HashMap<String, Map<String, Integer>>();
    }

    public void addCount(String word, String label, int count) {
        if (!this.wordsCounts.containsKey(word)) {
            this.wordsCounts.put(word, new HashMap<String, Integer>());
        }

        if (this.wordsCounts.get(word).containsKey(label)) {
            int currentCount = this.wordsCounts.get(word).get(label);
            this.wordsCounts.get(word).put(label, currentCount + count);
        } else {
            this.wordsCounts.get(word).put(label, count);
        }
    }

    public int getTotalCounts(String word) {
        int totalCount = 0;
        if (this.wordsCounts.containsKey(word)) {
            for (Entry<String, Integer> entry : this.wordsCounts.get(word).entrySet()) {
                totalCount += entry.getValue();
            }
        }
        return totalCount;
    }

    public Map<String, Integer> getWordCounts(String word) {
        return this.wordsCounts.get(word);
    }

    public Set<String> getWords() {
        return this.wordsCounts.keySet();
    }
}
