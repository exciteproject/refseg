package de.mkrnr.rse.pipe;

import java.util.ArrayList;
import java.util.List;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;

public class SerialPipesBuilder {

    private FeaturePipeProvider featurePipeProvider;

    public SerialPipesBuilder(FeaturePipeProvider featurePipeProvider) {
        this.featurePipeProvider = featurePipeProvider;
    }

    public SerialPipes createSerialPipes(List<String> featuresNames) {
        ArrayList<Pipe> pipes = new ArrayList<Pipe>();

        pipes.add(new SimpleTaggerSentence2TokenSequence());

        // TODO handle conjunctions and suffixes somewhere else
        int[][] conjunctions = new int[2][];
        conjunctions[0] = new int[] { -1 };
        conjunctions[1] = new int[] { 1 };

        // pipes.add(new TokenTextCharSuffix("C1=", 1));
        // pipes.add(new TokenTextCharSuffix("C2=", 2));
        // pipes.add(new TokenTextCharSuffix("C3=", 3));

        // pipes.add(new OffsetConjunctions(conjunctions));

        // String[] featureNames = { "[^\\p{L}]*\\p{Lu}.*" };
        // int[] offsets = { 0 };
        // pipes.add(new OffsetFeatureConjunction("TEST", featureNames,
        // offsets));

        for (String featureName : featuresNames) {
            pipes.add(this.featurePipeProvider.getPipe(featureName));
        }

        pipes.add(new TokenSequence2FeatureVectorSequence());

        return new SerialPipes(pipes);
    }

}
