package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameMapRaw {
    private short width;
    private short height;
    private List<String> rawLines;


    private GameMapRaw(short w, short h, List<String> rawLines) {
        this.width = w;
        this.height = h;
        this.rawLines = rawLines;
    }

    public double solve() {
        Map<Mine, SortedSet<Zombie>> mineToZombies = new HashMap<>();
        LinkedList<Mine> minesInOrder = new LinkedList<>();
        minesInOrder.add(Mine.EMPTY_MINE);
        mineToZombies.put(Mine.EMPTY_MINE, new TreeSet<>());

        Iterator<String> iterator = rawLines.iterator();


        //todo: Read in the data
        //todo: Prune the useless mines out

        //todo: run through the inputs in order

        for (short rowNum = 0; rowNum < height; rowNum++) {
            char[] line = iterator.next().toCharArray();
            for (short colNum = 0; colNum < width; colNum++) {
                char currentChar = line[colNum];
                if (currentChar == '.') {
                    continue;
                }
                Coord c = new Coord(colNum, rowNum);
                if (currentChar == 'M') {
                    Mine newMine = new Mine(c);
                    addMineAndStealZombies(newMine, minesInOrder, mineToZombies);
                    continue;
                }
                if (currentChar == 'Z') {
                    Zombie newZombie = new Zombie(c);
                    addZombieToCorrectMine(newZombie, minesInOrder, mineToZombies);
                    //todo: Add this zombie to the correct mine's list in the correct order;
                    continue;
                }
            }
        }

        List<SortedSet<Zombie>> sortedSetStream = mineToZombies.values().stream()
                .filter(zombieList -> !zombieList.isEmpty()).collect(Collectors.toList());

        Double answer = sortedSetStream.stream().map(zombieList -> zombieList.first().nearestMineDistance)
                .max(Double::compare).get();

        return answer;
    }

    private void addZombieToCorrectMine(Zombie newZombie, LinkedList<Mine> minesInOrder, Map<Mine, SortedSet<Zombie>> mineToZombies) {

        Mine currentBestMine = minesInOrder.getLast();
        double currentBestRadius = newZombie.coord.distance(currentBestMine.coord);
        Coord furthestBackPossible = furthestBackPossible(newZombie, currentBestRadius);

        ListIterator<Mine> mineListIterator = minesInOrder.listIterator(minesInOrder.size());
        Mine nextMine;
        while (mineListIterator.hasPrevious()) {
            nextMine = mineListIterator.previous();
            if (isFirstCoordBeforeSecond(nextMine.coord,furthestBackPossible)) {
                //we've gone back too far and should already have our best in currentBest
                break;
            }
            // this mine is not too far back
            double radius = newZombie.coord.distance(nextMine.coord);
            if (radius < currentBestRadius) {
                currentBestMine = nextMine;
                currentBestRadius = radius;
            }
        }

        newZombie.nearestMineDistance = currentBestRadius;
        mineToZombies.get(currentBestMine).add(newZombie);
    }

    private void addMineAndStealZombies(Mine newMine, LinkedList<Mine> minesInOrder, Map<Mine, SortedSet<Zombie>> mineToZombies) {
        ListIterator<Mine> mineListIterator = minesInOrder.listIterator(minesInOrder.size());

        SortedSet<Zombie> zombiesForNewMine = new TreeSet<>();
        Mine existingMine;
        while (mineListIterator.hasPrevious()) {
            existingMine = mineListIterator.previous();
            //TODO: figure out when a mine is TOO far back to steal from

            SortedSet<Zombie> zombiesForExistingMine = mineToZombies.get(existingMine);
            List<Zombie> stolenZombiesForMine = new ArrayList<>();

            for (Zombie existingZombie : zombiesForExistingMine) {
                double radiusToNewMine = existingZombie.coord.distance(newMine.coord);
                if (radiusToNewMine < existingZombie.nearestMineDistance) {
                    //steal this!
                    existingZombie.nearestMineDistance = radiusToNewMine;
                    zombiesForNewMine.add(existingZombie);
                    stolenZombiesForMine.add(existingZombie);
                } else {
                    //no more zombies to steal from this mine
                    break;
                }

            }
            zombiesForExistingMine.removeAll(stolenZombiesForMine);
        }

        mineToZombies.put(newMine, zombiesForNewMine);
        minesInOrder.add(newMine);
    }


    private Coord furthestBackPossible(Zombie newZombie, double radius) {
        //just get the beginning of the row for now, just to see if this works
        //todo: limit how far back in the row we need to look
        short topRow = (short) (newZombie.coord.y - (Math.floor(radius)));
        return new Coord(newZombie.coord.x, topRow);
    }

    private boolean isFirstCoordBeforeSecond(Coord c1, Coord c2) {
        //if c2.y > c1.y, then c2 is in a lower row than c1; c1 comes first
        if (c2.y > c1.y) {
            return true;
        }
        if (c2.y < c1.y) {
            return false;
        }
        //c2.y == c1.y
        //return true if c2.x comes AFTER c1.x;
        return c2.x > c1.x; //what about if they are the same coord?
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

        public GameMapRaw build() throws IOException {
            StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(), " ");
            short width = Short.parseShort(stringTokenizer.nextToken());
            short height = Short.parseShort(stringTokenizer.nextToken());

            rawLines = new ArrayList<>(height);

            String rawLine;
            for (short h = 0; h < height; h++) {
                rawLines.add(reader.readLine());
            }
            return new GameMapRaw(width, height, rawLines);
        }
    }
}
