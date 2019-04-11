package com.scoopsoup.dao;


import com.scoopsoup.classifiers.VectorSpaceModel;
import com.scoopsoup.model.Doc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class ScoopSoupDao {
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
            if (VectorSpaceModel.docClass[i] == 0) { //reliable
                winnerClass[0]++;
            } else if (VectorSpaceModel.docClass[i] == 1) { //unreliable
                winnerClass[1]++;
            }
        }
        return winnerClass[0] > winnerClass[1] ? 0 : 1;
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
            if (!VectorSpaceModel.docStopWords.contains(term)) {
                int index = VectorSpaceModel.termList.indexOf(term);
                if (index < 0)
                    continue;
                docList =VectorSpaceModel.docLists.get(index);

                double w_t = Math.log(VectorSpaceModel.myDocs.size() * 1.0 / docList.size());
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
}
