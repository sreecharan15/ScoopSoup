package com.scoopsoup.model;


public class Doc {
    public int docId;
    public double tw;


    public Doc(int did, double weight) {
        docId = did;
        tw = weight;
    }


    public String toString() {
        String docIdString = docId + ":" + tw;
        return docIdString;
    }
}