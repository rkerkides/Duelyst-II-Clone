package structures.basic;

import akka.actor.ActorRef;
import commands.BasicCommands;
import utils.BasicObjectBuilders;

public class Board {
    private Tile[][] tiles = new Tile[9][5];;

    public Board(ActorRef out) {

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                Tile tile = BasicObjectBuilders.loadTile(i, j);
                this.setTile(tile, i, j);
                BasicCommands.drawTile(out, tile, 0);
            }
        }


    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    public void setTile(Tile tile, int x, int y) {
        tiles[x][y] = tile;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }
}
