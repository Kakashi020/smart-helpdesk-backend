package com.helpdesk.ticketing_system.util;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import java.io.InputStream;

public class WekaModelTrainer {

    /**
     * Loads the high-accuracy N-Gram dataset from the resources' folder.
     * This acts as the "Brain" for Layer 2.
     */
    public static Instances getTrainingData() throws Exception {
        // 1. Locate the ARFF file in src/main/resources/data/
        InputStream inputStream = WekaModelTrainer.class
                .getClassLoader()
                .getResourceAsStream("data/training_data.arff");

        if (inputStream == null) {
            // Fallback error message if you forgot to create the file
            throw new RuntimeException("❌ CRITICAL: training_data.arff not found! " +
                    "Check src/main/resources/data/training_data.arff");
        }

        // 2. Load the contextual N-Gram dataset
        ArffLoader loader = new ArffLoader();
        loader.setSource(inputStream);
        Instances dataStructure = loader.getDataSet();

        // 3. Set the target attribute (The 9-Category Priority_Category field)
        // This tells Weka to predict the LAST column in your ARFF file
        if (dataStructure.classIndex() == -1) {
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
        }

        System.out.println("📊 Weka Knowledge Base Loaded: " + dataStructure.numInstances() + " N-Gram patterns.");
        return dataStructure;
    }
}