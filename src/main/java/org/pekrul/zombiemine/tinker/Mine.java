package org.pekrul.zombiemine.tinker;

import org.pekrul.zombiemine.Coord;

import java.util.Comparator;

public class Mine implements Comparator<Mine>, Comparable<Mine> {

    public static final Mine EMPTY_MINE = new Mine(new Coord(Short.MIN_VALUE, Short.MIN_VALUE), 0, 0);

    public final Coord coord;
    public double explodeDistance;
    public final int orderedId;

    public Mine(Coord c) {
        this.coord = c;
        this.explodeDistance = 0;
        this.orderedId = 0;
    }

    public Mine(Coord c, int orderedId) {
        this.coord = c;
        this.explodeDistance = 0;
        this.orderedId = orderedId;
    }

    public Mine(Coord c, int orderedId, double explodeDistance) {
        this.coord = c;
        this.explodeDistance = explodeDistance;
        this.orderedId = orderedId;
    }

    @Override
    public int compare(Mine o1, Mine o2) {
        return Integer.compare(o1.orderedId, o2.orderedId);
    }

    @Override
    public int compareTo(Mine o) {
        return Integer.compare(this.orderedId, o.orderedId);
    }
}
