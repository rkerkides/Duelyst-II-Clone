package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a tile.
 * The event returns the x (horizontal) and y (vertical) indices of the tile that was
 * clicked. Tile indices start at 1.
 * 
 * { 
 *   messageType = “tileClicked”
 *   tilex = <x index of the tile>
 *   tiley = <y index of the tile>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		System.out.println(gameState.lastEvent + " -> tileclicked");

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		// find the tile that was clicked
		Tile tile = gameState.getBoard().getTile(tilex, tiley);

		// Process event based on tile's unit and ownership
		if (tile.isOccupied() && tile.getUnit().getOwner() == gameState.currentPlayer) {
			Unit unit = tile.getUnit();
			gameState.currentUnitClicked = unit;
			// Temporarily commented out to facilitate testing
//			if (!unit.isMovedThisTurn()) {
//				gameState.getAction().showMoveRange(unit, gameState);
//			}
			gameState.gameService.highlightMoveRange(unit, gameState.getBoard());
		}

		System.out.println("Unit clicked: " + gameState.currentUnitClicked);
		// Handle movement if last event was a tile click and the current unit clicked is not null
		if (gameState.lastEvent.equals("tileclicked") && gameState.currentUnitClicked != null) {
			if (!tile.isOccupied() && gameState.currentUnitClicked.getOwner() == gameState.currentPlayer) {
				if (gameState.gameService.isValidMove(gameState.currentUnitClicked, tile)) {
					gameState.gameService.updateUnitPositionAndMove(gameState.currentUnitClicked, tile, gameState.getBoard());
					gameState.currentUnitClicked.setMovedThisTurn(true);
				} else {
					gameState.gameService.removeHighlightFromAll(gameState.getBoard());
				}
				gameState.currentUnitClicked = null;
			}
		}
	}

}
