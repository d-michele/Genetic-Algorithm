package com.oma.group1;

import java.io.*;

public class Main {

    public static void main(String[] args) {

        if(args.length != 3 || !args[0].equals("-t")){
            String programName = new java.io.File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            System.out.println("Usage: " + programName + " -t time instancefilename");
            System.exit(1);
        }
        Setup.filename = args[2];
        String outName = args[2].split("\\.")[0];
        int timeLimit = Integer.parseInt(args[1]);

        Population population = new Population(true);
//        System.out.println(population);
        Chromosome bestSolution = null;
        try {
            bestSolution = population.startGA(outName, timeLimit);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(bestSolution);
    }


}
