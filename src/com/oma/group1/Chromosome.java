package com.oma.group1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Chromosome extends Solution implements Comparable<Chromosome>{
    // 1 / solution length probability
    public static int MUTATION1_PROBABILITY = 10;
    // 1 / solution length probability
    public static int MUTATION2_PROBABILITY = 10;

    private double fitness;

    private boolean activeGenes[];

    public Chromosome() {
        super();
        Setup setup = Setup.getInstance();
        activeGenes = new boolean[setup.getQueriesSize()];
    }

    public Chromosome(Chromosome chromosome) {
        super(chromosome);
        Setup setup = Setup.getInstance();
        activeGenes = new boolean[setup.getQueriesSize()];
        this.fitness = chromosome.fitness;
        for (int i = 0; i < chromosome.size; i++) {
            this.activeGenes[i] = chromosome.activeGenes[i];
            this.solution[i] = chromosome.solution[i];
        }
    }

    public Chromosome(boolean random) {
        this();
        if (random) {
            Setup setup = Setup.getInstance();
            int memoryLimit = setup.getMemoryLimit();
            int configurationBound = Setup.getInstance().getConfigurationSize();
            ThreadLocalRandom generator = ThreadLocalRandom.current();
            for (int i = 0; i < solution.length; i++) {
                int tmp = generator.nextInt(0, configurationBound);
                solution[i] = tmp;
                activeGenes[i] = false;
            }
            // choose random order of configuration to activate
            int rand[] = generator.ints(0, setup.getQueriesSize())
                    .distinct().limit(setup.getQueriesSize()).toArray();
            for (int i = 0; i < this.size; i ++) {
                int configurationOccupation = 0;
                TreeSet<Integer> indexList = setup.getConfigurationIndexList(solution[rand[i]]);
                for (Integer index : indexList) {
                    if (!loadedIndexes.contains(index)) {
                        configurationOccupation += setup.getIndexMemoryOccupation()[index];
                    }
                }
                int tmpOccupation = memoryOccupation + configurationOccupation;
                if (tmpOccupation <= memoryLimit) {
                    activeGenes[rand[i]] = true;
                    memoryOccupation = tmpOccupation;
                    loadedIndexes.addAll(indexList);
                }
            }
            assert memoryOccupation < memoryLimit : "Infeasible";
            this.calculateFitness();
        }
    }

    public void calculateFitness() {
        fitness = 0;
        Setup setup = Setup.getInstance();
        for (int i = 0; i < this.size; i++) {
            if (activeGenes[i]) {
                fitness += setup.getGain(solution[i], i);
            }
        }
        for (Integer index : loadedIndexes) {
            fitness -= setup.getIndexFixedCosts()[index];
        }
    }

    public void setActiveGenes(boolean[] activeGenes) {
        this.activeGenes = activeGenes;
    }

    public boolean[] getActiveGenes() {

        return activeGenes;
    }

    public boolean isActive(int index) {
        return activeGenes[index];
    }

    public void activateGene(int index) {
        activeGenes[index] = true;
    }

    public void deactivateGene(int index) {
        activeGenes[index] = false;
    }


    public void calculateFitnessAndLoadedIndexes() {
        fitness = 0;
        TreeSet<Integer> indexes = new TreeSet<>();
        Setup setup = Setup.getInstance();
        for (int i = 0; i < this.size; i++) {
            if (activeGenes[i]) {
                fitness += setup.getGain(solution[i], i);
                indexes.addAll(setup.getConfigurationIndexList(solution[i]));
            }
        }
        loadedIndexes = indexes;
        memoryOccupation = 0;
        for (Integer index : loadedIndexes) {
            fitness -= setup.getIndexFixedCosts()[index];
            memoryOccupation += setup.getIndexMemoryOccupation()[index];
        }
    }

    private void deltaUpdateFitness(HashMap<Integer, Integer> indexCount, int conf, boolean confActive,
                                    int previousConf, boolean previousConfActive, int query) {
        Setup setup = Setup.getInstance();
        if (previousConfActive) {
            for (int index : setup.getConfigurationIndexList(previousConf)) {
                int value = indexCount.get(index) - 1;
                if (value == 0) {
                    fitness += setup.getIndexFixedCosts()[index];
                    loadedIndexes.remove(index);
                    indexCount.remove(index);
                } else {
                    indexCount.put(index, value);
                }
            }
            fitness -= setup.getGain(previousConf, query);
        }

        if (confActive) {
            for (int index : setup.getConfigurationIndexList(conf)) {
                if (!indexCount.containsKey(index)) {
                    fitness -= setup.getIndexFixedCosts()[index];
                    loadedIndexes.add(index);
                    indexCount.put(index, 1);
                } else {
                    int value = indexCount.get(index) + 1;
                    indexCount.put(index, value);
                    indexCount.put(index, value);
                }
            }
            fitness += setup.getGain(conf, query);
        }
    }

    public double getFitness() {
        return fitness;
    }

    public Chromosome crossover(Chromosome chromosome) {
        Setup setup = Setup.getInstance();
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        int rand[] = generator.ints(0, setup.getQueriesSize())
                .distinct().limit(setup.getQueriesSize()).toArray();

        Chromosome child1 = new Chromosome();
        Chromosome child2 = new Chromosome();
        for (int randomIndex : rand) {
            int coin = generator.nextInt(0, 2);
            int chosenGene1;
            int chosenGene2;
            boolean active1;
            boolean active2;
            if (coin == 0) {
                active1 = this.activeGenes[randomIndex];
                active2 = chromosome.activeGenes[randomIndex];
                chosenGene1 = this.solution[randomIndex];
                chosenGene2 = chromosome.solution[randomIndex];
            } else {
                active1 = chromosome.activeGenes[randomIndex];
                active2 = this.activeGenes[randomIndex];
                chosenGene1 = chromosome.solution[randomIndex];
                chosenGene2 = this.solution[randomIndex];
            }

            // mutation
            int bound = setup.getQueriesSize();
            if (generator.nextInt(0, bound) <= MUTATION1_PROBABILITY) {
                int randConf = generator.nextInt(0, setup.getConfigurationSize());
                chosenGene1 = randConf;
            }
            if (generator.nextInt(0, bound) <= MUTATION1_PROBABILITY) {
                int randConf = generator.nextInt(0, setup.getConfigurationSize());
                chosenGene2 = randConf;
            }

            child1.solution[randomIndex] = chosenGene1;
            child2.solution[randomIndex] = chosenGene2;

            if (!active1 && generator.nextInt(0,bound) <= MUTATION2_PROBABILITY) {
                active1 = true;
            }
            if (!active2 && generator.nextInt(0,bound) <= MUTATION2_PROBABILITY) {
                active2 = true;
            }

            if (active1) {
                activateGeneIfFitInMemory(child1, randomIndex);
            } else {
                child1.activeGenes[randomIndex] = false;
            }
            if (active2) {
                activateGeneIfFitInMemory(child2, randomIndex);
            } else {
                child2.activeGenes[randomIndex] = false;
            }
        }

        // if I have memory i try to activate some other configurations
        rand = generator.ints(0, setup.getQueriesSize())
                .distinct().limit(setup.getQueriesSize()).toArray();
        for (int randomIndex : rand) {
            if (!child1.activeGenes[randomIndex]) {
                activateGeneIfFitInMemory(child1, randomIndex);
            }
        }
        for (int randomIndex : rand) {
            if (!child2.activeGenes[randomIndex]) {
                activateGeneIfFitInMemory(child2, randomIndex);
            }
        }

        child1.calculateFitness();
        child2.calculateFitness();

        child1.loadedConfigurationLocalSearch();
        child2.loadedConfigurationLocalSearch();

        return child1.compareTo(child2) > 0 ? child1 : child2;
    }

    public void loadedConfigurationLocalSearch() {
        Setup setup = Setup.getInstance();

        HashMap<Integer, Integer> indexCount = new HashMap<>();

        for (int i = 0; i < setup.getQueriesSize(); i++) {
            if (activeGenes[i]) {
                int conf = solution[i];
                TreeSet<Integer> confIndexes = setup.getConfigurationIndexList(conf);
                for (int index : confIndexes) {
                    if (indexCount.containsKey(index)) {
                        int value = indexCount.get(index) + 1;
                        indexCount.put(index, value);
                    } else {
                        indexCount.put(index, 1);
                    }
                }
            }
        }

        ArrayList<Integer> activableConfList = new ArrayList<>();
        for (int c = 0; c < setup.getConfigurationSize(); c++) {
            TreeSet<Integer> confIndexes = setup.getConfigurationIndexList(c);
            if (this.loadedIndexes.containsAll(confIndexes)) {
                activableConfList.add(c);
            }
        }
        double bestFitness = this.getFitness();
        for (int j = 0; j < this.solution.length; j++) {
            for (int conf : activableConfList) {
                boolean previousStateActive = activeGenes[j];
                activeGenes[j] = true;
                int previousConf = this.solution[j];
                if (previousConf == conf)
                    continue;
                this.solution[j] = conf;
                // remove previous conf fixed cost
                deltaUpdateFitness(indexCount, conf, true, previousConf, previousStateActive, j);

                if (this.getFitness() > bestFitness) {
                    bestFitness = this.getFitness();
                } else {
                    this.solution[j] = previousConf;
                    activeGenes[j] = previousStateActive;
                    deltaUpdateFitness(indexCount, previousConf, previousStateActive, conf, true, j);
                }
            }
            // last try to deactivate gene and check if solution is better
            if (activeGenes[j]) {
                activeGenes[j] = false;
                deltaUpdateFitness(indexCount, solution[j], false, solution[j], true, j);

                if (this.getFitness() >= bestFitness) {
                    bestFitness = this.getFitness();
                } else {
                    activeGenes[j] = true;
                    deltaUpdateFitness(indexCount, solution[j], true, solution[j], false, j);
                }
            }
        }
    }

    public void removeNegativeNetGainConfigurations() {
        //local search to remove configuration that have net gain negative
        calculateFitnessAndLoadedIndexes();
        for (int j = 0; j < size; j++) {
            if (isActive(j)) {
                boolean[] actives;
                double currentFitness = getFitness();
                int conf = solution[j];
                actives = Arrays.copyOf(getActiveGenes(), size);
                deactivateGene(j);
                for (int l = j + 1; l < size; l++) {
                    if (isActive(l) && solution[l] == conf) {
                        deactivateGene(l);
                    }
                }
                calculateFitnessAndLoadedIndexes();
                if (currentFitness < getFitness()) {
                    currentFitness = getFitness();
                } else {
                    setActiveGenes(actives);
                    calculateFitnessAndLoadedIndexes();
                }
            }
        }
        calculateFitnessAndLoadedIndexes();
    }

    static private void activateGeneIfFitInMemory(Chromosome child1, int genePosition) {
        Setup setup = Setup.getInstance();

        int configurationOccupation = 0;
        TreeSet<Integer> indexList = setup.getConfigurationIndexList(child1.solution[genePosition]);
        for (Integer index : indexList) {
            if (!child1.loadedIndexes.contains(index)) {
                configurationOccupation += setup.getIndexMemoryOccupation()[index];
            }
        }
        int tmpOccupation = child1.memoryOccupation + configurationOccupation;
        if (tmpOccupation <= setup.getMemoryLimit()) {
            child1.activeGenes[genePosition] = true;
            child1.memoryOccupation = tmpOccupation;
            child1.loadedIndexes.addAll(indexList);
        } else {
            child1.activeGenes[genePosition] = false;
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("|");
        for (int i = 0; i < size; i++) {
            if (activeGenes[i]) {
                out.append(solution[i]).append("|");
            } else {
                out.append(" |");
            }
        }
        out.append(" fitness:").append(fitness).append(" mem: ").append(memoryOccupation);
        return out.toString();
    }

    @Override
    public int compareTo(Chromosome chromosome) {
        return Double.compare(this.fitness, chromosome.fitness);
    }

    public void toFile(BufferedWriter writer) throws IOException {
        Setup setup = Setup.getInstance();
        for (int i = 0; i < setup.getConfigurationSize(); i++) {
            StringBuilder out = new StringBuilder();
            for (int j = 0; j < size; j++) {
                if (i == solution[j] && activeGenes[j]) {
                    out.append("1");
                } else {
                    out.append("0");
                }
                out.append(" ");
            }
            out.append("\n");
            writer.write(out.toString());
        }
        writer.flush();
    }
}
