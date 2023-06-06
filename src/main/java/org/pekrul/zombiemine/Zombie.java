package org.pekrul.zombiemine;

import java.util.Comparator;

public class Zombie implements Comparator<Zombie>, Comparable<Zombie> {
    public Coord coord;
    public double nearestMineDistance;

    public Zombie(Coord c) {
        this.coord = c;
        this.nearestMineDistance = Float.MAX_VALUE;
    }

    @Override
    public int compare(Zombie o1, Zombie o2) {
        //force sorting from largest to smallest!!!
        return Double.compare(o2.nearestMineDistance, o1.nearestMineDistance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zombie zombie = (Zombie) o;

        return coord.equals(zombie.coord);
    }

    @Override
    public int hashCode() {
        return coord.hashCode();
    }

    @Override
    public int compareTo(Zombie o) {
        return Double.compare(o.nearestMineDistance,this.nearestMineDistance);
    }
}
