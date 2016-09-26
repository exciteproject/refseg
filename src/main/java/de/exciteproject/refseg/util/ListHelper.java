package de.exciteproject.refseg.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class ListHelper {

    /**
     *
     * @param <T>
     * @param inputList
     * @param percentage
     *            percentage of elements that should be randomly extracted from
     *            inputFiles
     * @return
     */
    public static <T> List<T> getRandomSubList(List<T> inputList, double percentage) {
	if ((percentage < 0) || (percentage > 1)) {
	    throw new IllegalArgumentException("percentage has to be between 0 and 1");
	}

	int numberOfElements = (int) (inputList.size() * percentage);
	return getRandomSubList(inputList, numberOfElements);
    }

    /**
     *
     * @param <T>
     * @param inputList
     * @param numberOfFiles
     *            number of files that should be randomly extracted from
     *            inputFiles
     * @return
     */
    public static <T> List<T> getRandomSubList(List<T> inputList, int numberOfFiles) {
	if ((numberOfFiles < 0) || (numberOfFiles > inputList.size())) {
	    throw new IllegalArgumentException("numberOfFiles has to be between 0 and size of inputList");
	}

	List<T> shuffeledList = new ArrayList<T>(inputList);
	Collections.shuffle(shuffeledList);
	return shuffeledList.subList(0, numberOfFiles);
    }

    public static <T> List<T> removeElementsFromList(List<T> inputList, List<T> elementsToRemove) {
	if (elementsToRemove == null) {
	    return inputList;
	}
	List<T> filteredList = new ArrayList<T>(inputList);
	for (Object objectToRemove : elementsToRemove) {
	    for (int i = 0; i < filteredList.size(); i++) {
		if (filteredList.get(i).equals(objectToRemove)) {
		    filteredList.remove(i);
		}
	    }
	}
	return filteredList;
    }

    public static List<File> removeFilesFromList(List<File> inputList, List<File> elementsToRemove) {
	if (elementsToRemove == null) {
	    return inputList;
	}
	List<File> filteredList = new ArrayList<File>(inputList);
	for (File fileToCheck : elementsToRemove) {
	    String fileToCeckNameWithoutEnding = FilenameUtils.removeExtension(fileToCheck.getName());

	    for (int i = 0; i < filteredList.size(); i++) {
		String filterFileNameWithoutEnding = FilenameUtils.removeExtension(filteredList.get(i).getName());
		if (fileToCeckNameWithoutEnding.equals(filterFileNameWithoutEnding)) {
		    filteredList.remove(i);
		}
	    }
	}
	return filteredList;
    }

}
