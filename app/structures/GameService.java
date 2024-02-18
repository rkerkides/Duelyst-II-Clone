package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.Unit;

public class GameService {
    private ActorRef out;

    public GameService(ActorRef out) {
        this.out = out;
    }

    // remove highlight from all tiles
    public void removeHighlightFromAll(Board board) {
        Tile[][] tiles = board.getTiles();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                Tile currentTile = tiles[i][j];
                currentTile.setTileHighlighted(false);
                BasicCommands.drawTile(out, currentTile, 0);
            }
        }
    }

    // highlight tiles for movement
    public void highlightMoveRange(Unit unit, Board board) {
        Tile[][] tiles = board.getTiles();
        int baseX = unit.getPosition().getTilex();
        int baseY = unit.getPosition().getTiley();

        // Loop to cover 2 tiles in each direction and 1 tile diagonally
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                // Check for diagonal movement (skip tiles that are 2 tiles away diagonally)
                if (Math.abs(x) == 2 && Math.abs(y) == 2) continue;
                if (Math.abs(x) == 2 && Math.abs(y) == 1) continue;
                if (Math.abs(x) == 1 && Math.abs(y) == 2) continue;

                // Calculate the target tile's coordinates
                int targetX = baseX + x;
                int targetY = baseY + y;

                // Check if coordinates are within board bounds
                if (targetX < 0 || targetY < 0 || targetX >= 9 || targetY >= 5) {
                    continue; // Skip tiles outside the board bounds
                }

                // Retrieve and highlight the tile if it's within the board
                Tile targetTile = tiles[targetX][targetY];
                updateTileHighlight(targetTile, true);
            }
        }
    }

    // update tile highlight
    public void updateTileHighlight(Tile tile, boolean highlightTile) {
        tile.setTileHighlighted(highlightTile);
        if (highlightTile) {
            BasicCommands.drawTile(out, tile, 1);
        } else {
            BasicCommands.drawTile(out, tile, 0);
        }
    }

    public void updateUnitPositionAndMove(Unit unit, Tile currentTile, Tile newTile, Board board) {
        // update unit position
        currentTile.setUnit(null);
        newTile.setUnit(unit);
        unit.setPositionByTile(newTile);

        // remove highlight from all tiles
        removeHighlightFromAll(board);

        // draw unit on new tile and wait for animation to play out
        BasicCommands.moveUnitToTile(out, unit, newTile);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
