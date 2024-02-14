package structures.basic;

public class Board {
    private Tile[][] tiles;

    public Board() {
        tiles = new Tile[9][5];
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
