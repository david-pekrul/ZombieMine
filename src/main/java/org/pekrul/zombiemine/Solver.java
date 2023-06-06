package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class Solver {
    private static final DecimalFormat df = new DecimalFormat("0.0000000");

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/k.in"));
        int numberOfMaps = Integer.parseInt(reader.readLine());
        for(int i = 0; i < numberOfMaps; i++){
            GameMapRaw currentMap = (new GameMapRaw.Builder().withInput(reader)).build();
            double answer = currentMap.solve();
            System.out.println(df.format(answer));
        }
    }
}
