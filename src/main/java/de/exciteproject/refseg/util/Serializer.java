package de.exciteproject.refseg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Serializer {

    public static void serialize(Object object, File serializedFile) throws IOException {
	ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializedFile));
	objectOutputStream.writeObject(object);
	objectOutputStream.close();
	System.out.printf("Serialized data is saved in: " + serializedFile);
    }
}
