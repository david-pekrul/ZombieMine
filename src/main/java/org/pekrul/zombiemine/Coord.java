package org.pekrul.zombiemine;

import java.util.*;

public class Coord {

    public static double[][] precalcedDistances = null;

    public static final SortedMap<Double, List<Coord>> radiusToCoords = new TreeMap<>();
    public static final List<Coord> coordsInExpandingRadii = new ArrayList<>();
    private static boolean alreadyInit = false;

    public static void initDistances() {
        if (alreadyInit) {
            return;
        }
        System.out.println("starting initDistances");
        precalcedDistances = new double[2000][2000];
        SortedSet<Double> radii = new TreeSet<>();
        for (int i = 0; i < 2000; i++) {
            for (int j = i; j < 2000; j++) {
                double radiusFromOrigin = Math.sqrt(i * i + j * j);
                precalcedDistances[i][j] = radiusFromOrigin;
                precalcedDistances[j][i] = radiusFromOrigin;
                List<Coord> existingCoords = radiusToCoords.getOrDefault(radiusFromOrigin, new ArrayList<>());
                existingCoords.add(new Coord(i, j));
                radiusToCoords.put(radiusFromOrigin, existingCoords);
                radii.add(radiusFromOrigin);
            }
        }

        List<Coord> coordsInExpandingRadii = new ArrayList<>();
        for (double radius : radii) {
            coordsInExpandingRadii.addAll(radiusToCoords.get(radius));
        }
        System.out.println("finished initDistances");
        alreadyInit = true;
    }


    public int x;
    public int y;


    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Coord c2) {
        return Coord.distance(this, c2);
    }

    public static double distance(Coord c1, Coord c2) {
        if (precalcedDistances == null) {
            initDistances();
        }
        int i = Math.abs(c1.x - c2.x);
        int j = Math.abs(c1.y - c2.y);
        return precalcedDistances[i][j];
//        return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
    }

    public Coord applyVector(Coord vector) {
        return new Coord(this.x + vector.x, this.y + vector.y);
    }

    public Set<Coord> applyOffsets(Coord offset) {
        Set<Coord> offsets = new HashSet<>(8);
        offsets.add(new Coord(this.x + offset.x, this.y + offset.y));
        offsets.add(new Coord(this.x - offset.x, this.y + offset.y));
        offsets.add(new Coord(this.x - offset.x, this.y - offset.y));
        offsets.add(new Coord(this.x + offset.x, this.y - offset.y));
        offsets.add(new Coord(this.x + offset.y, this.y + offset.x));
        offsets.add(new Coord(this.x - offset.y, this.y + offset.x));
        offsets.add(new Coord(this.x - offset.y, this.y - offset.x));
        offsets.add(new Coord(this.x + offset.y, this.y - offset.x));
        return offsets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coord coord = (Coord) o;

        if (x != coord.x) return false;
        return y == coord.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + (int) y;
        return result;
    }
}
