package com.scoopsoup.parser;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.scoopsoup.helpers.Constants;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Parser {
    public HashMap<Integer, String> myDocsContentMap;
    public HashMap<Integer, String> myDocsTestContentMap;
    public static HashMap<Integer, String> myDocsFileNameReferenceMap;
    public Set<String> stopWords;
    public int[] trainLabels;

    public Parser(String filepath, String stopWordFilePath) {
        File train = null;
        File stopword = null;
        try{
             train=  ResourceUtils.getFile(filepath);
             stopword=  ResourceUtils.getFile(stopWordFilePath);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        /**
         * Adopt a proper data structure to store the given stop word list and use it to efficiently remove all the
         * stop words in the documents. Skip it in case of using a poisting list and using Phrase Queries
         */
        if (stopWordFilePath != null) {
            createStopWordList(stopword);
        }
        /**
         * Create the list of documents after removing all the stopwords.
         */
        parseDocuments(train);
    }

    /**
     * @param stopWordFile file for stopWordFile.
     */
    private void createStopWordList(File stopWordFile) {
        Path path = stopWordFile.toPath();
        /**
         * TreeSet guarantees log(n) time cost for the basic operations (add, remove and contains).
         */
        stopWords = new TreeSet<>();
        try (Scanner document = new Scanner(path, Constants.ENCODING.name())) {
            while (document.hasNextLine()) {
                stopWords.add(document.nextLine());
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * @param file that needs to be read.
     */
    private void parseDocuments(File file) {

        /**
         * Using two maps as the listFiles() does not return a sorted file list hence storing references to each file.
         * and their respective positions
         */
        myDocsContentMap = new HashMap<Integer, String>();
        myDocsFileNameReferenceMap = new HashMap<Integer, String>();
        myDocsTestContentMap = new HashMap<Integer, String>();
        int index = 0;
        CsvMapper mapper = new CsvMapper();

        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        File csvFile = file; // or from String, URL etc
        MappingIterator<String[]> it = null;
        try {
            it = mapper.readerFor(String[].class).readValues(csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        trainLabels = new int[20800];
        if (it.hasNext())
            it.next();
        while (it.hasNext() && index < 5000) { // 80% of training data set used for training
            String[] row = it.next();
            //System.out.println(row[0]);
            myDocsContentMap.put(index, row[3].toLowerCase()); //case Folding
            myDocsFileNameReferenceMap.put(index, row[1]);
            trainLabels[index] = Integer.parseInt(row[4]);
            index++;
        }
        System.out.println("Starting Testing Data" + index);
//        while (it.hasNext() && index < 2000) { // 20% used for testData
//            String[] row = it.next();
//            //System.out.println(row[0]);
//            myDocsTestContentMap.put(index, row[3].toLowerCase()); //case Folding
//            myDocsFileNameReferenceMap.put(index, row[1]);
//            trainLabels[index] = Integer.parseInt(row[4]);
//            index++;
//        }
        System.out.println("Total Data " + index);
    }



}
