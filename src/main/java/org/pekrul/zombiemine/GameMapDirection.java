package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameMapDirection implements IGameMap {
    private List<Coord> zombies;
    private Set<Coord> mines;
    private Set<Coord> originalMines;
    private short width;
    private short height;
    private List<String> rawLines;

    private GameMapDirection(short w, short h, List<Coord> z, Set<Coord> m) {
        this.width = w;
        this.height = h;
        this.zombies = z;
        this.mines = m;
    }

    public double solve() {
        pruneMines();

        Set<Coord> remainingZombies = new HashSet<>(zombies);
        double radiusAnswer = 0;
        int rejectionCountAtRadius = 0;
        //pick the first zombie
        Coord nextZombie = null;
        while (!remainingZombies.isEmpty()) {
            Coord currentZombie;
            if (nextZombie == null) {
                currentZombie = remainingZombies.stream().findAny().get();
            } else {
                currentZombie = nextZombie;
                nextZombie = null;
            }

            double closestMineRadius = Double.MAX_VALUE;
            Coord closestMine = null;
            remainingZombies.remove(currentZombie);

            //Find the smallest radius from this zombie to any mine.
            boolean foundUselessZombie = false;
            for (Coord currentMine : mines) {
                double currentRadius = Coord.distance(currentZombie, currentMine);
                if (currentRadius < closestMineRadius) {
                    closestMineRadius = currentRadius;
                    closestMine = currentMine;
                }
                if (currentRadius < radiusAnswer) {
                    foundUselessZombie = true;
                    rejectionCountAtRadius++;
                    break;
                }
            }
            if (foundUselessZombie) {
                continue;
            }

            if (radiusAnswer < closestMineRadius) {
                radiusAnswer = closestMineRadius;
//                System.out.println(String.format("\tRejection Count: %d for radius %f", rejectionCountAtRadius, radiusAnswer));
                rejectionCountAtRadius = 0;
            }

            //At this point, we know that currentZombie is absolutely closest to closestMine
            //Start walking from this zombie away from the mine until either another zombie is found, or the edge of the map.
            Coord awayVector = getAwayVector(closestMine, currentZombie);

            Coord nextPossibleZombie = currentZombie.applyVector(awayVector);
            //walk the awayVector from the currentMine until I find a mine or am off the map;
            boolean done = false;
            while (!done) {
                if (nextPossibleZombie.x < 0 || nextPossibleZombie.y < 0 || nextPossibleZombie.x >= width || nextPossibleZombie.y > height || originalMines.contains(nextPossibleZombie)) {
                    nextZombie = null;
                    done = true;
                    break;
                }
                if (remainingZombies.contains(nextPossibleZombie)) {
                    //found one!
                    nextZombie = nextPossibleZombie;
                    break;
                }

                //keep walking away
                nextPossibleZombie = nextPossibleZombie.applyVector(awayVector);
            }
        }

//        System.out.println(String.format("\tRejection Count: %d for radius %f", rejectionCountAtRadius, radiusAnswer));
        return radiusAnswer;
    }

    Coord getAwayVector(Coord mine, Coord zombie) {
        Coord fullVector = new Coord(zombie.x - mine.x, zombie.y - mine.y);
        double slope = (fullVector.y * 1.0) / (fullVector.x * 1.0);
        double absSlope = Math.abs(slope);

        Coord resultVector = null;
        if (absSlope <= 0.25) {
            resultVector = new Coord((int) Math.copySign(1, fullVector.x), 0);
        } else if (absSlope <= 4) {
            resultVector = new Coord((int) Math.copySign(1, fullVector.x), (int) Math.copySign(1, fullVector.y));
        } else {
            resultVector = new Coord(0, (int) Math.copySign(1, fullVector.y));
        }


        return resultVector;
    }


    private void pruneMines() {
        Set<Coord> mineCoords = mines.stream().collect(Collectors.toSet());
        originalMines = new HashSet<>(mines);
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

        public GameMapDirection build() throws IOException {


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

            return new GameMapDirection(width, height, zombies, mines);
        }
    }

}
