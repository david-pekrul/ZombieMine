package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class Solver {
    public static final DecimalFormat df = new DecimalFormat("0.0000000");
    public static final DecimalFormat TIME_IN_SECONDS_FORMAT = new DecimalFormat("0.000");


    public static void main(String[] args) throws IOException {
//        String filePath = "src/main/resources/individuals/7.txt";
        String filePath = "src/main/resources/k.in";
//        String filePath = "src/main/resources/k.test";

        long start = System.currentTimeMillis();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int numberOfMaps = Integer.parseInt(reader.readLine());
        for (int i = 0; i < numberOfMaps; i++) {
            long mapStart = System.currentTimeMillis();
//            IGameMap currentMap = (new GameMapRaw.Builder().withInput(reader)).build();
            IGameMap currentMap = (new GameMap2.Builder().withInput(reader)).build();
            double answer = currentMap.solve();
            long mapEnd = System.currentTimeMillis();
            System.out.println("" + (i + 1) + "\t-> " + df.format(answer) + "\t\t" + TIME_IN_SECONDS_FORMAT.format((mapEnd - mapStart) / 1000.0));
        }
        long end = System.currentTimeMillis();
        System.out.println("Total Time: " + TIME_IN_SECONDS_FORMAT.format((end - start) / 1000.0));

    }
}
