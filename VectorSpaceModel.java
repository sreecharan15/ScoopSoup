package com.scoopsoup.classifiers;

import com.scoopsoup.model.Doc;
import com.scoopsoup.parser.Parser;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class VectorSpaceModel {
    public static HashMap<Integer, String> myDocs;
    public static ArrayList<String> termList;
    public static ArrayList<ArrayList<Doc>> docLists;
    public static double[] docLength;
    public static  int[] docClass;
    public static  double[] classCentroid;
    public static  Set<String> docStopWords;

    /**
     * Construct an inverted index
     *
     * @param myDocsContentMap Map of input strings
     */
    public VectorSpaceModel(HashMap<Integer, String> myDocsContentMap, Set<String> stopWords, int[] labels) {
        myDocs = myDocsContentMap;
        docClass = labels;
        termList = new ArrayList<String>();
        docLists = new ArrayList<ArrayList<Doc>>();
        ArrayList<Doc> docList;
        docStopWords = stopWords;
        //parse the documents to construct the index and collect the raw frequencies.
        for (int i = 0; i < myDocs.size(); i++) {
            String[] tokens = myDocs.get(i).split("\\p{Punct}|\\s");
            String token;
            for (int j = 0; j < tokens.length; j++) {
                token = tokens[j];
                //System.out.println(token);
                if (!docStopWords.contains(token)) {
                    // System.out.println(token);
                    if (!termList.contains(token)) {
                        termList.add(token);
                        docList = new ArrayList<Doc>();
                        Doc doc = new Doc(i, 1); //initial raw frequency is 1
                        docList.add(doc);
                        docLists.add(docList);
                    } else {
                        int index = termList.indexOf(token);
                        docList = docLists.get(index);
                        int k = 0;
                        boolean match = false;

                        //search the postings list for a document id, if match, insert a new position number to the document id
                        for (Doc doc : docList) {
                            if (doc.docId == i) {
                                doc.tw++; //increase word count
                                match = true;
                                break;
                            }
                            k++;
                        }
                        //if no match, add a new document id along with the position number
                        if (!match) {
                            Doc doc = new Doc(i, 1);
                            docList.add(doc);
                        }
                    }
                }
            }
        }//end with parsing

        //LBE07: compute the tf-idf term weights and the doc lengths
        int N = myDocs.size();
        docLength = new double[N];
        //System.out.println(termList.size());
        for (int i = 0; i < termList.size(); i++) {
            docList = docLists.get(i);
            int df = docList.size();
            Doc doc;
            for (int j = 0; j < docList.size(); j++) {
                doc = docList.get(j);
                double tfidf = (1 + Math.log(doc.tw)) * Math.log(N / (df * 1.0));
                docLength[doc.docId] += Math.pow(tfidf, 2);
                doc.tw = tfidf;
                docList.set(j, doc);
            }
        }
        //update the length
        for (int i = 0; i < N; i++) {
            docLength[i] = Math.sqrt(docLength[i]);
        }


    }

    /**
     * Based on the scoring that is retrived find the nearest neighbors to the query document and get a winning class
     *
     * @param documnet
     * @param constant
     * @return
     */
    public int KNNModifiedClassifier(String documnet, int constant) {

        int constantK = constant;

        HashMap<Integer, Double> docs = rankDocuments(documnet);
        HashMap<Integer, Double> neighborsForClassification = new LinkedHashMap<>();
        AtomicReference<Double> averageNeighborsScore = new AtomicReference<>((double) 0);
        ArrayList<Integer> potentialWinners = new ArrayList<>(); //index of the winner document
        int[] winnerClass = new int[2];

        docs.entrySet().stream()
                .sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
                .forEach(k -> {
                    if (neighborsForClassification.size() < constantK + 1) {
                        neighborsForClassification.put(k.getKey(), k.getValue());
                        averageNeighborsScore.updateAndGet(v -> new Double((double) (v + k.getValue())));

                    }
                });
        //Find Potential winners based on the k neighbours found
        averageNeighborsScore.updateAndGet(v -> new Double((double) averageNeighborsScore.get() / constantK));
        for (Map.Entry<Integer, Double> neighbour : neighborsForClassification.entrySet()) {
            potentialWinners.add(neighbour.getKey());

        }
        // Find the winner class based on the document Ids

        for (Integer i : potentialWinners) {
            if (docClass[i] == 0) { //reliable
                winnerClass[0]++;
            } else if (docClass[i] == 1) { //unreliable
                winnerClass[1]++;
            }
        }
        return winnerClass[0] > winnerClass[1] ? 0 : 1;
    }

    /**
     * Print the testing params like accuracy and Fscore
     *
     * @param testDocs
     */
    public void printTestStats(HashMap<Integer, String> testDocs) {
        int tp = 0;
        int tn = 0;
        int fp = 0;
        int fn = 0;
        float precision;
        float recall;
        float fmeasure;
        float accuracy;
        int k = (int) Math.sqrt(testDocs.size()); // trying to mak
        for (Map.Entry<Integer, String> testDoc : testDocs.entrySet()) {
            // System.out.println(testDoc.getValue());
            int result = KNNModifiedClassifier(testDoc.getValue(), k);
            if (result == docClass[testDoc.getKey()] && docClass[testDoc.getKey()] == 0) { //reliable
                tn++;
            } else if (result == docClass[testDoc.getKey()] && docClass[testDoc.getKey()] == 1) { // unreliable
                tp++;
            } else if (result != docClass[testDoc.getKey()] && docClass[testDoc.getKey()] == 0) {
                fp++;
            } else if (result != docClass[testDoc.getKey()] && docClass[testDoc.getKey()] == 1) {
                fn++;
            }
        }

        precision = (float) tp / (float) (tp + fp);
        recall = (float) tp / (float) (tp + fn);
        fmeasure = 2 * ((precision * recall) / (precision + recall));
        accuracy = (float) (tp + tn) / (float) (tp + tn + fp + fn);
        System.out.println("Precision is : " + precision);
        System.out.println("Recall is : " + recall);
        System.out.println("Fmeasure is : " + fmeasure);
        System.out.println("Accuracy is : " + accuracy);
    }


    /**
     * LBE07: perform ranking of document
     *
     * @param document user document in free form text
     */
    public HashMap<Integer, Double> rankDocuments(String document) {
        String[] query = document.split("\\p{Punct}|\\s");
        HashMap<Integer, Double> docs = new HashMap<Integer, Double>();
        ArrayList<Doc> docList;
        for (String term : query) {
            if (!docStopWords.contains(term)) {
                int index = termList.indexOf(term);
                if (index < 0)
                    continue;
                docList = docLists.get(index);

                double w_t = Math.log(myDocs.size() * 1.0 / docList.size());
                Doc doc;
                for (int j = 0; j < docList.size(); j++) {
                    doc = docList.get(j);
                    double score = w_t * doc.tw;
                    if (!docs.containsKey(doc.docId)) {
                        docs.put(doc.docId, score);
                    } else {
                        score += docs.get(doc.docId);
                        docs.put(doc.docId, score);

                    }
                    // System.out.println(Parser.myDocsFileNameReferenceMap.get(33));
                }
            }
        }
        //System.out.println(docs);
        return docs;
    }

    /**
     * Return the string representation of a positional index
     */
    public String toString() {
        String matrixString = new String();
        ArrayList<Doc> docList;
        System.out.println("Aya");
        for (int i = 0; i < termList.size(); i++) {
            matrixString += String.format("%-15s", termList.get(i));
            docList = docLists.get(i);
            for (int j = 0; j < docList.size(); j++) {
                matrixString += docList.get(j) + "\t";
            }
            matrixString += "\n";
        }
        return matrixString;
    }


    public static void main(String[] args) {
        Parser docParser = new Parser("classpath:train.csv", "classpath:stopwords.txt");
        VectorSpaceModel vsm = new VectorSpaceModel(docParser.myDocsContentMap, docParser.stopWords, docParser.trainLabels);
       // vsm.printTestStats(docParser.myDocsTestContentMap);
    }

}


