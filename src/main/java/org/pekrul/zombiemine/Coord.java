package org.pekrul.zombiemine;

public class Coord {
    public short x;
    public short y;

    public Coord(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Coord c2) {
        return Coord.distance(this, c2);
    }

    public static double distance(Coord c1, Coord c2) {
        return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
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
