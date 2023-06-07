package org.pekrul.zombiemine;

import java.io.*;
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

/*    public void write(int i) throws IOException {
        File file = new File("" + i + ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append("1\r\n");
        String dimensions = "" + width + " " + height + "\r\n";
        writer.append(dimensions);
        writer.append(String.join("\r\n", rawLines));
        writer.close();
    }*/

    public double solve() {
        Map<Mine, SortedSet<Zombie>> mineToZombies = new HashMap<>();
        LinkedList<Mine> minesInOrder = new LinkedList<>();
        SortedSet<Mine> minesInExplodeOrder = new TreeSet<>(new Comparator<Mine>() {
            @Override
            public int compare(Mine o1, Mine o2) {
                return Double.compare(o2.explodeDistance, o1.explodeDistance);
            }
        });


        minesInOrder.add(Mine.EMPTY_MINE);
        mineToZombies.put(Mine.EMPTY_MINE, new TreeSet<>());
        minesInExplodeOrder.add(Mine.EMPTY_MINE);


        Iterator<String> iterator = rawLines.iterator();


        //todo: Read in the data
        //todo: Prune the useless mines out

        //todo: run through the inputs in order
        int mineId = 1;
        for (short rowNum = 0; rowNum < height; rowNum++) {
            char[] line = iterator.next().toCharArray();
            for (short colNum = 0; colNum < width; colNum++) {
                char currentChar = line[colNum];
                if (currentChar == '.') {
                    continue;
                }
                Coord c = new Coord(colNum, rowNum);
                if (currentChar == 'M') {
                    Mine newMine = new Mine(c, mineId);
                    mineId++;
                    addMineAndStealZombies2(newMine, minesInExplodeOrder, minesInOrder, mineToZombies);
                    continue;
                }
                if (currentChar == 'Z') {
                    Zombie newZombie = new Zombie(c);
                    addZombieToCorrectMine(newZombie, minesInExplodeOrder, minesInOrder, mineToZombies);
                    //todo: Add this zombie to the correct mine's list in the correct order;
                    continue;
                }
            }
        }

        List<SortedSet<Zombie>> nonEmptyMineToZombies = mineToZombies.values().stream()
                .filter(zombieList -> !zombieList.isEmpty()).collect(Collectors.toList());

        List<List<Zombie>> sortedResults = nonEmptyMineToZombies.stream()
                .map(zombieList -> zombieList.stream()
                        .sorted(Zombie::compareTo)
                        .collect(Collectors.toList()))
                .sorted(Comparator.comparingInt(List::size))
                .collect(Collectors.toList());

        List<List<Zombie>> sortedZombieLists = sortedResults.stream()
                .map(zombieList -> zombieList.stream()
                        .sorted(Zombie::compareTo)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());


        double answer = sortedZombieLists.stream()
                .map(zombieList -> zombieList.stream().findFirst().get().nearestMineDistance)
                .max(Double::compareTo).get();

        return answer;
    }

    private void addZombieToCorrectMine(Zombie newZombie, SortedSet<Mine> minesInExplodeOrder, LinkedList<Mine> minesInOrder, Map<Mine, SortedSet<Zombie>> mineToZombies) {

        Mine currentBestMine = minesInOrder.getLast();
        double currentBestRadius = newZombie.coord.distance(currentBestMine.coord);
        Coord furthestBackPossible = furthestBackPossible(newZombie, currentBestRadius);

        ListIterator<Mine> mineListIterator = minesInOrder.listIterator(minesInOrder.size());
        Mine nextMine;
        while (mineListIterator.hasPrevious()) {
            nextMine = mineListIterator.previous();
            if (isFirstCoordBeforeSecond(nextMine.coord, furthestBackPossible)) {
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

        if (currentBestRadius > currentBestMine.explodeDistance) {
            //update this mine!
            minesInExplodeOrder.remove(currentBestMine);
            currentBestMine.explodeDistance = currentBestRadius;
            minesInExplodeOrder.add(currentBestMine);

        }
    }

    private void addMineAndStealZombies2(Mine newMine, SortedSet<Mine> minesInExplodeOrder, LinkedList<Mine> minesInOrder, Map<Mine, SortedSet<Zombie>> mineToZombies) {
        boolean done = false;
        Mine existingMine;
        Mine lastExistingMine = null;
        newMine.explodeDistance = 0;

        SortedSet<Zombie> zombiesForNewMine = new TreeSet<>();
        while (!done) {
            existingMine = minesInExplodeOrder.first();
            if (existingMine.explodeDistance <= newMine.explodeDistance || existingMine.equals(lastExistingMine)) {
                break; //there is no point of stealing further, as it won't change the current overall result.
            }
            SortedSet<Zombie> zombiesForExistingMine = mineToZombies.get(existingMine);
            if (zombiesForExistingMine.isEmpty()) {
                break;
            }
            List<Zombie> stolenZombiesForMine = new ArrayList<>();

            for (Zombie existingZombie : zombiesForExistingMine) {
                double radiusToNewMine = existingZombie.coord.distance(newMine.coord);
                if (radiusToNewMine < existingZombie.nearestMineDistance) {
                    //steal this!
                    existingZombie.nearestMineDistance = radiusToNewMine;
                    zombiesForNewMine.add(existingZombie);
                    stolenZombiesForMine.add(existingZombie);
                }
            }
            if (!stolenZombiesForMine.isEmpty()) {
                zombiesForExistingMine.removeAll(stolenZombiesForMine);
                zombiesForNewMine.addAll(stolenZombiesForMine);

                //remove and re-insert the mine that was stolen from back into the set
                minesInExplodeOrder.remove(existingMine);
                existingMine.explodeDistance = mineToZombies.get(existingMine).stream().map(z -> z.nearestMineDistance).findFirst().orElseGet(() -> 0.0);
                minesInExplodeOrder.add(existingMine);
                newMine.explodeDistance = zombiesForNewMine.first().nearestMineDistance;
            }
            lastExistingMine = existingMine;
        }

        if (!zombiesForNewMine.isEmpty()) {
            newMine.explodeDistance = zombiesForNewMine.first().nearestMineDistance;
        }

        mineToZombies.put(newMine, zombiesForNewMine);
        minesInExplodeOrder.add(newMine);
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
