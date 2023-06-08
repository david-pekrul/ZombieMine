package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameMap2 implements IGameMap {
    private Set<Zombie> zombies;
    private Set<Mine> mines;
    private short width;
    private short height;
    private List<String> rawLines;

    private GameMap2(short w, short h, Set<Zombie> z, Set<Mine> m) {
        this.width = w;
        this.height = h;
        this.zombies = z;
        this.mines = m;
    }

    public double solve() {
        double furthestRadiusSoFar = 0;
        pruneMines();

        for (Zombie currentZombie : zombies) {
            double closestMineRadius = Double.MAX_VALUE;
            for (Mine currentMine : mines) {
                double currentRadius = currentZombie.coord.distance(currentMine.coord);
                if (currentRadius < closestMineRadius) {
                    closestMineRadius = currentRadius;
                }
                if (closestMineRadius < furthestRadiusSoFar) {
                    //we already found a mine that is further from any mine than this one.
                    break;
                }
            }

            if (furthestRadiusSoFar < closestMineRadius) {
                furthestRadiusSoFar = closestMineRadius;
            }

        }

        return furthestRadiusSoFar;
    }

    private void pruneMines() {
        Set<Coord> mineCoords = mines.stream().map(mine -> mine.coord).collect(Collectors.toSet());
        Set<Mine> usefulMines = new HashSet<>(mineCoords.size());

        for (Mine mine : mines) {
            //up-down-left-right
            if (mineInCenterPlus(mine, mineCoords)) {
                continue;
            }
            if (mineInCenterOfX(mine, mineCoords)) {
                continue;
            }
            usefulMines.add(mine);
        }
//        double ratio = (usefulMines.size() * 1.0) / mines.size();
//        System.out.println("Reduced mines to % of original: " + ratio);
        mines = usefulMines;
    }

    private boolean mineInCenterPlus(Mine mine, Set<Coord> mineCoords) {
        //up
        if (!mineCoords.contains(new Coord(mine.coord.x, mine.coord.y - 1))) {
            return false;
        }
        //down
        if (!mineCoords.contains(new Coord(mine.coord.x, mine.coord.y + 1))) {
            return false;
        }
        //left
        if (!mineCoords.contains(new Coord(mine.coord.x - 1, mine.coord.y))) {
            return false;
        }
        //right
        return mineCoords.contains(new Coord(mine.coord.x + 1, mine.coord.y));
    }

    private boolean mineInCenterOfX(Mine mine, Set<Coord> mineCoords) {
        //up-left
        if (!mineCoords.contains(new Coord(mine.coord.x - 1, mine.coord.y - 1))) {
            return false;
        }
        //up-right
        if (!mineCoords.contains(new Coord(mine.coord.x + 1, mine.coord.y - 1))) {
            return false;
        }
        //down-left
        if (!mineCoords.contains(new Coord(mine.coord.x - 1, mine.coord.y + 1))) {
            return false;
        }
        //down-right
        return mineCoords.contains(new Coord(mine.coord.x + 1, mine.coord.y + 1));
    }

    public static class Builder {
        private BufferedReader reader;
        private List<String> rawLines;

        public Builder() {
        }

        public Builder withInput(BufferedReader reader) {
            this.reader = reader;
            return this;
        }

        public GameMap2 build() throws IOException {


            StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(), " ");
            short width = Short.parseShort(stringTokenizer.nextToken());
            short height = Short.parseShort(stringTokenizer.nextToken());

            rawLines = new ArrayList<>(height);

            String rawLine;
            Set<Zombie> zombies = new HashSet<>(width * height);
            Set<Mine> mines = new HashSet<>(width * height);
            for (short h = 0; h < height; h++) {
                rawLine = reader.readLine();
                char[] chars = rawLine.toCharArray();
                for (short w = 0; w < width; w++) {
                    char charToken = chars[w];

                    switch (charToken) {
                        case 'Z': {
                            zombies.add(new Zombie(new Coord(w, h)));
                            break;
                        }
                        case 'M': {
                            mines.add(new Mine(new Coord(w, h)));
                            break;
                        }
                        default: {
                        }
                    }
                }
            }

            return new GameMap2(width, height, zombies, mines);
        }
    }

}
