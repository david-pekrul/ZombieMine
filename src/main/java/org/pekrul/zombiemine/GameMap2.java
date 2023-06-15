package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameMap2 implements IGameMap {
    private List<Integer> zombies;
    private Set<Integer> mines;
    private short width;
    private short height;

    private GameMap2(short w, short h, List<Integer> z, Set<Integer> m) {
        this.width = w;
        this.height = h;
        this.zombies = z;
        this.mines = m;
    }

    public double solve() {
        double maxOfMinRadii = 0;
        pruneMines();
        Set<Integer> remainingMines = new HashSet<>(mines);
        Set<Integer> remainingZombies = new TreeSet<>(zombies);
        Map<Integer, Double> zombieToShortestKnownRadius = new HashMap<>(zombies.size());
        zombies.forEach(z -> zombieToShortestKnownRadius.put(z, Double.MAX_VALUE));


        //From the previous best mine, get as FAR away as possible and solve that zombie.
        //This works really well for maps where the zombies and mines are partitioned a bit.
        Integer furthestZombie = -1;


        while (!remainingZombies.isEmpty()) {
            Integer currentZombie;
            if (furthestZombie != -1) {
                currentZombie = furthestZombie;
                furthestZombie = -1;
            } else {
                currentZombie = remainingZombies.stream().findAny().get();
            }

            double closestMineRadius = Double.MAX_VALUE;
            remainingZombies.remove(currentZombie);

            if (zombieToShortestKnownRadius.getOrDefault(currentZombie, Double.MAX_VALUE) < maxOfMinRadii) {
                //already found a mine closer to this zombie than the answer.
                continue;
            }

            //Find the smallest radius from this zombie to any mine.
            int bestMine = -1;
            boolean foundUselessZombie = false;
            for (Integer currentMine : remainingMines) {
                double currentRadius = calcDistancce(currentZombie, currentMine);
                if (currentRadius < closestMineRadius) {
                    closestMineRadius = currentRadius;
                    bestMine = currentMine;
                }
                if (currentRadius < maxOfMinRadii) {
                    foundUselessZombie = true;
                    bestMine = currentMine;
                    break;
                }
            }

            if (foundUselessZombie) {
                continue;
            }

            //STATE: currentZombie is absolutely closest to bestMine.
            if (maxOfMinRadii < closestMineRadius) {
                maxOfMinRadii = closestMineRadius;
            }

            List<Integer> zombiesToRemove = new ArrayList<>();

            double furthestZombieRadius = 0;
            final List<Integer> zombieArray = remainingZombies.stream().toList();

            for (int i = 0; i < zombieArray.size(); i++) {
                int remainingZombie = zombieArray.get(i);
                //from the mine, remove any zombies that it has to wipe out
                double currentRadius = calcDistancce(remainingZombie, bestMine);
                if (currentRadius <= maxOfMinRadii) {
                    remainingZombies.remove(remainingZombie);
//                    zombiesToRemove.add(remainingZombie);
                }
//                zombieToShortestKnownRadius.compute(remainingZombie, (k, v) -> Math.min(v, currentRadius));
                if (furthestZombieRadius < currentRadius) {
                    furthestZombieRadius = currentRadius;
                    furthestZombie = remainingZombie;
                }
            }
//            remainingZombies.removeAll(zombiesToRemove);
//            System.out.println(String.format("Removed %d zombies at %f radius. \tZombies remaining: %d\tMine count: %d", zombiesToRemove.size(), maxOfMinRadii, remainingZombies.size(), numberOfMines));
//            System.out.print("");
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
        Set<Integer> mineCoords = mines.stream().collect(Collectors.toSet());
        Set<Integer> usefulMines = new HashSet<>(mineCoords.size());

        for (Integer mine : mines) {
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

    private boolean mineInCenterPlus(Integer mine, Set<Integer> mineCoords) {
        //up
        int x = mine % OFFSET;
        int y = mine / OFFSET;
        if (!mineCoords.contains(generateSingleCoord(x, y - 1))) {
            return false;
        }
        //down
        if (!mineCoords.contains(generateSingleCoord(x, y + 1))) {
            return false;
        }
        //left
        if (!mineCoords.contains(generateSingleCoord(x - 1, y))) {
            return false;
        }
        //right
        return mineCoords.contains(generateSingleCoord(x + 1, y));
    }

    private boolean mineInCenterOfX(Integer mine, Set<Integer> mineCoords) {
        int x = mine % OFFSET;
        int y = mine / OFFSET;
        //up-left
        if (!mineCoords.contains(generateSingleCoord(x - 1, y - 1))) {
            return false;
        }
        //up-right
        if (!mineCoords.contains(generateSingleCoord(x + 1, y - 1))) {
            return false;
        }
        //down-left
        if (!mineCoords.contains(generateSingleCoord(x - 1, y + 1))) {
            return false;
        }
        //down-right
        return mineCoords.contains(generateSingleCoord(x + 1, y + 1));
    }

    private boolean mineInCenterOfY(Integer mine, Set<Integer> mineCoords) {
        int x = mine % OFFSET;
        int y = mine / OFFSET;
        boolean upLeft = mineCoords.contains(generateSingleCoord(x - 1, y - 1));
        boolean up = mineCoords.contains(generateSingleCoord(x, y - 1));
        boolean upRight = mineCoords.contains(generateSingleCoord(x + 1, y - 1));
        boolean left = mineCoords.contains(generateSingleCoord(x - 1, y));
        boolean right = mineCoords.contains(generateSingleCoord(x + 1, y));
        boolean downLeft = mineCoords.contains(generateSingleCoord(x - 1, y + 1));
        boolean downRight = mineCoords.contains(generateSingleCoord(x + 1, y + 1));
        boolean down = mineCoords.contains(generateSingleCoord(x, y + 1));

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

    final static int OFFSET = 10_000;

    static int generateSingleCoord(int x, int y) {
        return x + OFFSET * y;
    }

    static double calcDistancce(int coordA, int coordB) {
        return Math.sqrt(Math.pow(coordB % OFFSET - coordA % OFFSET, 2) + Math.pow(coordB / OFFSET - coordA / OFFSET, 2));
    }

    public static class Builder {
        private BufferedReader reader;

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

            String rawLine;
            List<Integer> zombies = new ArrayList<>();
            Set<Integer> mines = new HashSet<>();
            for (short h = 0; h < height; h++) {
                rawLine = reader.readLine();
                char[] chars = rawLine.toCharArray();
                for (short w = 0; w < width; w++) {
                    char charToken = chars[w];

                    switch (charToken) {
                        case 'Z': {
                            zombies.add(generateSingleCoord(w, h));
                            break;
                        }
                        case 'M': {
                            mines.add(generateSingleCoord(w, h));
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
