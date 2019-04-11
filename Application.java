package com.scoopsoup;

import com.scoopsoup.classifiers.VectorSpaceModel;
import com.scoopsoup.parser.Parser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class Application {
    public static void main(String[] args){

        Parser docParser = new Parser("classpath:train.csv", "classpath:stopwords.txt");
        VectorSpaceModel vsm = new VectorSpaceModel(docParser.myDocsContentMap, docParser.stopWords, docParser.trainLabels);
       // vsm.printTestStats(docParser.myDocsTestContentMap);
        SpringApplication.run(Application.class, args);

    }

}
