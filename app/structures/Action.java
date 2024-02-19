package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Board;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;

public class Action {
    public GameService gameService;

    public Action(ActorRef out) {
        this.gameService = new GameService(out);
    }

    // move unit
    public void moveUnit(Unit unit, Tile newTile, Board board) {
        // get position of unit and find the tile it is on
        Position position = unit.getPosition();
        Tile currentTile = board.getTile(position.getTilex(), position.getTiley());

        // update unit position and move
        gameService.updateUnitPositionAndMove(unit, currentTile, newTile, board);
    }

    // check if move is valid
    public boolean isValidMove(Unit unit, Tile tile) {
        // depending on unit, this may change
        // for now, all units can move to tiles highlighted white
        return tile.getHighlightMode() == 1;
    }

    // show move range
    public void showMoveRange(Unit unit, Board board) {
        gameService.highlightMoveRange(unit, board);
    }
}
