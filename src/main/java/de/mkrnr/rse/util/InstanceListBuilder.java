package de.mkrnr.rse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;

public class InstanceListBuilder {

    public static InstanceList build(File file, SerialPipes serialPipes) {
        Pattern pattern = Pattern.compile("^\\s*$");

        InstanceList instanceList = new InstanceList(serialPipes);
        try {
            instanceList.addThruPipe(new LineGroupIterator(
                    new BufferedReader(new InputStreamReader(new FileInputStream(file))), pattern, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return instanceList;
    }

}
