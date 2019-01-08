package com.oma.group1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

public class Setup {
    public static class ConfQueryKey {
        int config;
        int query;

        public ConfQueryKey(int config, int query) {
            this.config = config;
            this.query = query;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ConfQueryKey))
                return false;
            ConfQueryKey tmp = (ConfQueryKey) obj;
            return this.config == tmp.config &&
                    this.query == tmp.query;
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + Integer.hashCode(config);
            hash = hash * 31 + Integer.hashCode(query);
            return hash;
        }
    }
    public static String filename;

    private static Setup instance;

    private int nQueries;
    private int nIndex;
    private int nConfiguration;
    private int memoryLimit;
    private int[] indexFixedCost;
    private int[] indexMemoryOccupation;
    private HashMap<ConfQueryKey, Integer> confQueryGainMap;
    private ArrayList<TreeSet<Integer>> confIndexes;

    public int getQueriesSize() {
        return nQueries;
    }

    public int getIndexSize() {
        return nIndex;
    }

    public int getConfigurationSize() {
        return nConfiguration;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public int[] getIndexFixedCosts() {
        return indexFixedCost;
    }

    public int[] getIndexMemoryOccupation() {
        return indexMemoryOccupation;
    }

    public int getGain(int config, int query) {
        Setup.ConfQueryKey key = new Setup.ConfQueryKey(config, query);
        if (!confQueryGainMap.containsKey(key))
            return 0;

        return confQueryGainMap.get(key);
    }

    private Setup() throws IOException {
        readInputFromFile(filename);
    }

    public TreeSet<Integer> getConfigurationIndexList(int configurationIndex){
        return confIndexes.get(configurationIndex);
    }

    public static synchronized Setup getInstance() {
        if (instance == null) {
            try {
                instance = new Setup();
            } catch (IOException e) {
                e.printStackTrace();
                instance = null;
            }
        }
        return instance;
    }

    private void readInputFromFile(String filename) throws IOException {
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(filename);
            bufferedReader = new BufferedReader(fileReader);

            String sCurrentLine;
            int[] confValues = new int[4];
            for (int i = 0; i < 4; i++) {
                sCurrentLine = bufferedReader.readLine();
                confValues[i] = Integer.parseInt(sCurrentLine.split(" ")[1]);
            }
            nQueries = confValues[0];
            nIndex = confValues[1];
            nConfiguration = confValues[2];
            memoryLimit = confValues[3];
            // skip description
            bufferedReader.readLine();
            confIndexes = new ArrayList<>(nConfiguration);
            for (int i = 0; i < nConfiguration; i++) {
                sCurrentLine = bufferedReader.readLine();
                String[] splittedLine = sCurrentLine.split(" ");
                confIndexes.add(new TreeSet<>());
                for (int j = 0; j < nIndex; j++) {
                    if (splittedLine[j].equals("0"))
                        continue;
                    else {
                        confIndexes.get(i).add(j);
                    }
                }
            }
            // skip description
            bufferedReader.readLine();
            indexFixedCost = new int[nIndex];
            for (int i = 0; i < nIndex; i++) {
                sCurrentLine = bufferedReader.readLine();
                indexFixedCost[i] = Integer.parseInt(sCurrentLine);
            }
            // skip description
            bufferedReader.readLine();
            indexMemoryOccupation = new int[nIndex];
            for (int i = 0; i < nIndex; i++) {
                sCurrentLine = bufferedReader.readLine();
                indexMemoryOccupation[i] = Integer.parseInt(sCurrentLine);
            }
            // skip description
            bufferedReader.readLine();
            confQueryGainMap = new HashMap<>();
            for (int i = 0; i < nConfiguration; i++) {
                sCurrentLine = bufferedReader.readLine();
                String[] splittedLine = sCurrentLine.split(" ");
                for (int j = 0; j < nQueries; j++) {
                    if (splittedLine[j].equals("0"))
                        continue;
                    else {
                        int value = Integer.parseInt(splittedLine[j]);
                        Setup.ConfQueryKey key = new Setup.ConfQueryKey(i, j);
                        confQueryGainMap.put(key, value);
                    }
                }
            }
        }finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
