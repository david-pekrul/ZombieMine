package org.pekrul.zombiemine.tinker;

import org.pekrul.zombiemine.Coord;
import org.pekrul.zombiemine.IGameMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameMap implements IGameMap {
    private List<Coord> zombies;
    private Set<Coord> mines;
    private short width;
    private short height;
    private List<String> rawLines;

    private GameMap(short w, short h, List<Coord> z, Set<Coord> m) {
        this.width = w;
        this.height = h;
        this.zombies = z;
        this.mines = m;
    }

    public double solve() {
        double maxOfMinRadii = 0;
        pruneMines();

        Set<Coord> remainingMines = new HashSet<>(mines);
        Set<Coord> remainingZombies = new HashSet<>(zombies);

        double minRadiusAlreadyCleared = 0;

        while (!remainingZombies.isEmpty()) {

            Coord currentZombie = remainingZombies.stream().findAny().get();

            double closestMineRadius = Double.MAX_VALUE;
            Coord closestMine = null;
            Coord lastBestMine = null;
            remainingZombies.remove(currentZombie);

            //Find the smallest radius from this zombie to any mine.
            boolean foundUselessZombie = false;
            for (Coord currentMine : remainingMines) {
                double currentRadius = Coord.distance(currentZombie, currentMine);
                if (currentRadius < closestMineRadius) {
                    closestMineRadius = currentRadius;
                    closestMine = currentMine;
                }
                if (currentRadius < maxOfMinRadii) {
                    foundUselessZombie = true;
                    lastBestMine = currentMine;
                    break;
                }
            }
//            if (foundUselessZombie) {
//                continue;
//            }

            if (maxOfMinRadii < closestMineRadius) {
                maxOfMinRadii = closestMineRadius;
            }

            if (lastBestMine != null) {
                //we can clear out the zombies from around this mine as well?
                expandFromMine(lastBestMine, 0, maxOfMinRadii, remainingZombies);
            }


            //We know at this point that maxOfMinRadii is non-zero and there is at least one zombie that requires this
            //explode radius. We can then remove all zombies that are at or closer than this radius to this mine.
//            Set<Coord> zombiesToRemove = new HashSet<>();
//            if (maxOfMinRadii > minRadiusAlreadyCleared) {
//                for (Coord zombie : remainingZombies) {
//                    if (zombie.distance(closestMine) <= maxOfMinRadii) {
//                        zombiesToRemove.add(zombie);
//                    }
//                }
//                remainingZombies.removeAll(zombiesToRemove);
//                minRadiusAlreadyCleared = maxOfMinRadii;
//            }

            // For this current mine what is the max radius that it would take for THIS mine to clear the map?
//            double maxRadiusForThisMineToClearMap = 0;
//            for (Coord Zombie : remainingZombies) {
//
//
//            }

        }
        return maxOfMinRadii;
    }

    private void expandFromMine(Coord mine, double minRadius, double maxRadius, Set<Coord> zombies) {
        Set<Coord> potentialZombieCoords = Coord.radiusToCoords.entrySet().stream()

                .takeWhile(kv -> kv.getKey() <= maxRadius)
                .filter(kv -> kv.getKey() >= minRadius)
                .flatMap(kv -> kv.getValue().stream())
                .flatMap(offset -> mine.applyOffsets(offset).stream())
                .collect(Collectors.toSet());

        int initial = zombies.size();
        zombies.removeAll(potentialZombieCoords);
        int after = zombies.size();

//        System.out.println("\tRemoving " + (initial - after) + " zombies.");
    }

    private void pruneMines() {
        Set<Coord> mineCoords = mines.stream().collect(Collectors.toSet());
        Set<Coord> usefulMines = new HashSet<>(mineCoords.size());

        for (Coord mine : mines) {
            //up-down-left-right
            if (mineInCenterPlus(mine, mineCoords)) {
                continue;
            }
            if (mineInCenterOfX(mine, mineCoords)) {
                continue;
            }
            if (mineInCenterOfY(mine, mineCoords)) {
                continue;
            }
            usefulMines.add(mine);
        }
//        double ratio = (usefulMines.size() * 1.0) / mines.size();
//        System.out.println("Reduced mines to % of original: " + ratio);
//        System.out.println("\tmines: " + mines.size() + "\t->\t" + usefulMines.size());
        mines = usefulMines;
    }

    private boolean mineInCenterPlus(Coord mine, Set<Coord> mineCoords) {
        //up
        if (!mineCoords.contains(new Coord(mine.x, mine.y - 1))) {
            return false;
        }
        //down
        if (!mineCoords.contains(new Coord(mine.x, mine.y + 1))) {
            return false;
        }
        //left
        if (!mineCoords.contains(new Coord(mine.x - 1, mine.y))) {
            return false;
        }
        //right
        return mineCoords.contains(new Coord(mine.x + 1, mine.y));
    }

    private boolean mineInCenterOfX(Coord mine, Set<Coord> mineCoords) {
        //up-left
        if (!mineCoords.contains(new Coord(mine.x - 1, mine.y - 1))) {
            return false;
        }
        //up-right
        if (!mineCoords.contains(new Coord(mine.x + 1, mine.y - 1))) {
            return false;
        }
        //down-left
        if (!mineCoords.contains(new Coord(mine.x - 1, mine.y + 1))) {
            return false;
        }
        //down-right
        return mineCoords.contains(new Coord(mine.x + 1, mine.y + 1));
    }

    private boolean mineInCenterOfY(Coord mine, Set<Coord> mineCoords) {
        boolean upLeft = mineCoords.contains(new Coord(mine.x - 1, mine.y - 1));
        boolean up = mineCoords.contains(new Coord(mine.x, mine.y - 1));
        boolean upRight = mineCoords.contains(new Coord(mine.x + 1, mine.y - 1));
        boolean left = mineCoords.contains(new Coord(mine.x - 1, mine.y));
        boolean right = mineCoords.contains(new Coord(mine.x + 1, mine.y));
        boolean downLeft = mineCoords.contains(new Coord(mine.x - 1, mine.y + 1));
        boolean downRight = mineCoords.contains(new Coord(mine.x + 1, mine.y + 1));
        boolean down = mineCoords.contains(new Coord(mine.x, mine.y + 1));

        if (up && downLeft && downRight) {
            return true;
        }
        if (down && upRight && upLeft) {
            return true;
        }
        if (left && upRight && downRight) {
            return true;
        }
        if (right && upLeft && downLeft) {
            return true;
        }
        return false;

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

        public GameMap build() throws IOException {


            StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(), " ");
            short width = Short.parseShort(stringTokenizer.nextToken());
            short height = Short.parseShort(stringTokenizer.nextToken());

            rawLines = new ArrayList<>(height);

            String rawLine;
            List<Coord> zombies = new LinkedList<>();
            Set<Coord> mines = new HashSet<>();
            for (short h = 0; h < height; h++) {
                rawLine = reader.readLine();
                char[] chars = rawLine.toCharArray();
                for (short w = 0; w < width; w++) {
                    char charToken = chars[w];

                    switch (charToken) {
                        case 'Z': {
                            zombies.add(new Coord(w, h));
                            break;
                        }
                        case 'M': {
                            mines.add(new Coord(w, h));
                            break;
                        }
                        default: {
                        }
                    }
                }
            }

            return new GameMap(width, height, zombies, mines);
        }
    }

}
