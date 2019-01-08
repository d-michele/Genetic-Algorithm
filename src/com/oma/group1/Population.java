package com.oma.group1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.*;

public class Population {

    static int POPULATION_SIZE = 200;

    private List<Chromosome> chromosomesList;

    private List<Chromosome> nextGeneration;

    public Population() {
        chromosomesList = new ArrayList<Chromosome>(POPULATION_SIZE);
        nextGeneration = Collections.synchronizedList(new ArrayList<Chromosome>(POPULATION_SIZE));
    }

    public Population(boolean random){
        this();
        if (random) {
            for (int i = 0; i < POPULATION_SIZE; i++) {
                Chromosome chromosome = new Chromosome(true);
                chromosomesList.add(chromosome);
            }
        }
    }

    Chromosome startGA(String outName, int timeLimit) throws Exception {
        Chromosome bestChromosome = null;
        int threadNum = Runtime.getRuntime().availableProcessors() - 1;
        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        BufferedWriter writer = null;

        bestChromosome = null;
        ArrayList<Callable<Void>> callableTasks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (long currentTime = System.currentTimeMillis(); currentTime < startTime + timeLimit * 1000;
             currentTime = System.currentTimeMillis()) {
            nextGeneration.clear();
            loadTasks(callableTasks, threadNum);
            createNextGeneration();
            executorService.invokeAll(callableTasks);
            callableTasks.clear();
            chromosomesList = new ArrayList<Chromosome>(nextGeneration);
            Chromosome fittestChromosome = chromosomesList.stream().reduce((x, y) -> {
                if (x.getFitness() > y.getFitness())
                    return x;
                else
                    return y;
            }).get();
            Chromosome fittestImproved = new Chromosome(fittestChromosome);
            fittestImproved.removeNegativeNetGainConfigurations();

            if (fittestImproved.getFitness() > fittestChromosome.getFitness()) {
                fittestChromosome = fittestImproved;
                fittestChromosome.calculateFitnessAndLoadedIndexes();
            }
//            System.out.println(fittestChromosome);
            if (bestChromosome == null || (fittestChromosome.getFitness() > bestChromosome.getFitness())) {

                bestChromosome = new Chromosome(fittestChromosome);
                System.out.println("current best fitness: " + bestChromosome.getFitness());
                writer = new BufferedWriter(new FileWriter(outName +".sol"));
                bestChromosome.toFile(writer);
                writer.close();
            }
        }


        executorService.shutdown();
        return bestChromosome;
    }

    private void loadTasks(ArrayList<Callable<Void>> callableTasks ,int threadNum) {
        for (int t = 0; t < threadNum; t++) {
            Callable<Void> task = () -> {
                createNextGeneration();
                return null;
            };
            callableTasks.add(task);
        }
    }

    private void createNextGeneration() {
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        int i = 0;
        while (nextGeneration.size() <= POPULATION_SIZE) {
            int coin = generator.nextInt(0, 100);
            if (coin < 70) {
                Chromosome mother;
                Chromosome father = tournamentSelection(2);
                do {
                    mother = tournamentSelection(2);
                } while (father == mother);
                Chromosome newChromosome = father.crossover(mother);
                nextGeneration.add(newChromosome);
            } else {
                Chromosome newChromosome1 = tournamentSelection(4);
                nextGeneration.add(newChromosome1);
                Chromosome newChromosome2 = tournamentSelection(4);
                nextGeneration.add(newChromosome2);
            }
        }
    }

    Chromosome tournamentSelection(int k) {
        Chromosome best = null;
        Chromosome ind;
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        for (int i = 0; i < k; i++) {
            ind = chromosomesList.get(generator.nextInt(0, POPULATION_SIZE));
            if (best == null || ind.getFitness() > best.getFitness())
                best = ind;
        }
        return best;
    }


    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Chromosome chromosome : chromosomesList) {
            out.append(chromosome.toString()).append("\n");
        }
        return out.toString();
    }
}
