package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameMapFastInteger implements IGameMap {
    /* Use Integers for storing coordinates. Saves from creating yet another object. However, I didn't do extensive benchmarks to verify this is necessary. */
    private List<Integer> zombies;
    private Set<Integer> mines;
    private short width;
    private short height;
    Random random = new Random();


    private GameMapFastInteger(short w, short h, List<Integer> z, Set<Integer> m) {
        this.width = w;
        this.height = h;
        this.zombies = z;
        this.mines = m;
    }

    public double solve() {
        // This stores the answer.
        double maxOfMinRadii = 0;

        //Get rid of any mines that are geometrically impossible to be the closest to a mine.
        pruneMines();
        List<Integer> remainingMines = new ArrayList<>(mines);
        Set<Integer> remainingZombies = new TreeSet<>(zombies); //I don't know why, but re-hashing this seems to really make a huge difference.


        //From the previous best mine, get as FAR away as possible and solve that zombie.
        //This works really well for maps where the zombies and mines are partitioned a bit.
        Integer furthestZombie = -1;


        while (!remainingZombies.isEmpty()) {
            Integer currentZombie;

            //Determine which Zombie we are going to use next.
            if (furthestZombie != -1) {
                currentZombie = furthestZombie;
                furthestZombie = -1;
            } else {
                currentZombie = remainingZombies.stream().findAny().get();
            }

            //We have a zombie to calculate. Remove it from the remaining set.
            remainingZombies.remove(currentZombie);

            //For this zombie, we start the radius at the max value, and try to shrink it as we go.
            double closestMineRadius = Double.MAX_VALUE;

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
                    //Found a mine that was closer to this zombie than our current maxOfMinRadii.
                    //This zombie is not a limiting factor, so we can skip over it.
                    foundUselessZombie = true;
                    bestMine = currentMine;
                    break;
                }
            }

            if (foundUselessZombie) {
                continue;
            }

            //STATE: currentZombie is absolutely closest to bestMine.
            //If we got this far, every mine was further away than the previous maxOfMinRadii
            //Still doing the check because it reads better.
            if (maxOfMinRadii < closestMineRadius) {
                maxOfMinRadii = closestMineRadius;
            }


            // We now have a mine that was closest to a zombie.
            // From this mine, I want to get "the hell outta Dodge".
            // Find a zombie that is "reasonably far away"
            furthestZombie = findNextZombie(remainingZombies, maxOfMinRadii, bestMine);
        }
        return maxOfMinRadii;
    }

    private int findNextZombie(Set<Integer> zombies, double maxOfMinRadii, Integer bestMine) {
        Integer furthestZombie = -1;
        double furthestDistanceSoFar = 0;
//        SortedMap<Double, Integer> radiusToZombie = new TreeMap<>();
//        List<Integer> zombieArray = new ArrayList<>(zombies);
        List<Integer> zombiesInAscendingDistance = new ArrayList<>(zombies.size());
//        for (int i = 0; i < zombieArray.size(); i++) {
//        for (Iterator<Integer> i = zombies.iterator(); i.hasNext(); ) {
        Iterator<Integer> i = zombies.iterator();
        int size = zombies.size();
        for (int idx = 0; idx < size; idx++) {
//            int remainingZombie = i.next();
//            int remainingZombie = zombieArray.get(i);
            int remainingZombie = i.next();
            //from the mine, remove any zombies that it has to wipe out

            double currentRadius = calcDistancce(remainingZombie, bestMine);
            if (currentRadius <= maxOfMinRadii) {
//                zombies.remove(remainingZombie);
                i.remove();
                continue;
            }
            if (currentRadius > furthestDistanceSoFar) {
//                radiusToZombie.putIfAbsent(currentRadius, remainingZombie);
                zombiesInAscendingDistance.add(remainingZombie);
                furthestDistanceSoFar = currentRadius;
            }
        }

//        List<Map.Entry<Double, Integer>> entries = radiusToZombie.entrySet().stream().toList();
//        int entrySize = entries.size();
        int entrySize = zombiesInAscendingDistance.size();
        if (entrySize > 1) {
//            furthestZombie = entries.get(random.nextInt(0, entrySize)).getValue();
//            furthestZombie = entries.get((int) (entrySize * 0.9)).getValue();
//            furthestZombie = zombiesInAscendingDistance.get((int) (entrySize * 0.75));
            furthestZombie = zombiesInAscendingDistance.get(random.nextInt(0, entrySize));
        } else {
            furthestZombie = -1;
        }
        return furthestZombie;
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

        public GameMapFastInteger build() throws IOException {


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

            return new GameMapFastInteger(width, height, zombies, mines);
        }
    }

}
