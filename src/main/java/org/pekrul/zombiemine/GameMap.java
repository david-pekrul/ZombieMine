package org.pekrul.zombiemine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class GameMap {
    private Set<Zombie> zombies;
    private Set<Mine> mines;
    private short width;
    private short height;
    private List<String> rawLines;

    private GameMap(short w, short h, Set<Zombie> z, Set<Mine> m) {
        this.width = w;
        this.height = h;
        this.zombies = z;
        this.mines = m;
        rawLines = new ArrayList<>(h);
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
            Set<Zombie> zombies = new HashSet<>(width*height);
            Set<Mine> mines = new HashSet<>(width*height);
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
                        default: {}
                    }
                }
            }

            return new GameMap(width, height, zombies, mines);
        }
    }

}
