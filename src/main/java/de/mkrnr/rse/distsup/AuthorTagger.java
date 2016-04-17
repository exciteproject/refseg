package de.mkrnr.rse.distsup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;

public class AuthorTagger {

    public static void main(String[] args) {
        String sequence = "Beck, Martin / Shabafrouz, Miriam (2007): Iran – gewichtiger Gegenspieler westlicher Interessen, Hamburg, GIGA Focus Nahost, Nr. 10.";
        TrieBuilder trieBuilder = Trie.builder().removeOverlaps().onlyWholeWords().caseInsensitive();

        String[] firstNames = { "Martin" };
        String[] lastNames = { "Beck" };
        Name name = new Name(firstNames, lastNames);
        Map<String, String> variationsMap = name.getVariationsXMLMap();

        HashMap<String, String> variationLookup = new HashMap<String, String>();

        for (Entry<String, String> variationEntry : variationsMap.entrySet()) {
            trieBuilder.addKeyword(variationEntry.getKey());
            variationLookup.put(variationEntry.getKey(), variationEntry.getValue());
        }

        Trie trie = trieBuilder.build();
        Collection<Token> tokens = trie.tokenize(sequence);
        StringBuffer taggedSequence = new StringBuffer();
        for (Token token : tokens) {
            if (token.isMatch()) {
                taggedSequence.append(variationLookup.get(token.getFragment()));

            } else {
                taggedSequence.append(token.getFragment());
            }
        }
        System.out.println(sequence);
        System.out.println(taggedSequence);

    }

}
