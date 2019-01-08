package com.oma.group1;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public class Solution {

    protected int[] solution;

    protected int size;

    protected int memoryOccupation;

    protected TreeSet<Integer> loadedIndexes;


    public Solution() {
        this.size = Setup.getInstance().getQueriesSize();
        solution = new int[size];
        loadedIndexes = new TreeSet<>();
    }

    public Solution(Solution solution){
        this.size = solution.size;
        this.solution = new int[size];
        loadedIndexes = new TreeSet<>(solution.loadedIndexes);
        this.memoryOccupation = solution.memoryOccupation;
        for (int i = 0; i < this.size; i++) {
            this.solution[i] = solution.solution[i];
        }
    }

}
