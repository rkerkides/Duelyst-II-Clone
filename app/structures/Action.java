package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Board;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;

public class Action {
    private ActorRef out;
    public GameService gameService;

    public Action(ActorRef out) {
        this.out = out;
        this.gameService = new GameService(out);
    }

    // move unit
    public void moveIfValid(Unit unit, Tile newTile, Board board) {
        if (isValidMove(unit, newTile)) {
            // get position of unit and find the tile it is on
            Position position = unit.getPosition();
            Tile currentTile = board.getTile(position.getTilex(), position.getTiley());

            // update unit position and move
            gameService.updateUnitPositionAndMove(unit, currentTile, newTile, board);
        }
    }

    // check if move is valid
    public boolean isValidMove(Unit unit, Tile tile) {
        return tile.isTileHighlighted() && tile.getUnit() == null;
    }

    // show move range
    public void showMoveRange(Unit unit, Board board) {
        gameService.highlightMoveRange(unit, board);
    }
}
