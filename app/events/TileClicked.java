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

		System.out.println("Last event" + " -> tileclicked");

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		// find the tile that was clicked
		Tile tile = gameState.getBoard().getTile(tilex, tiley);

		// Process event based on tile's unit and ownership
		if (tile.isOccupied() && tile.getUnit().getOwner() == gameState.currentPlayer) {
			Unit unit = tile.getUnit();
			gameState.currentUnitClicked = unit;
			// Highlight move range if unit has not moved this turn
			if (!unit.isMovedThisTurn()) {
				gameState.gameService.highlightMoveRange(unit, gameState.getBoard());
			}
		}

		// Summon unit if last event was a card click and the current tile is highlighted
		if (gameState.lastEvent.equals("cardclicked") && gameState.currentCardClicked != null) {
			System.out.println("Trying to summon " + gameState.currentCardClicked.getCardname() + " to tile " + tilex + ", " + tiley);
			if (gameState.gameService.isValidSummon(gameState.currentCardClicked, tile)) {
				gameState.gameService.removeCardFromHandAndSummonUnit
						(gameState.getBoard(), gameState.currentCardClicked,
						tile, gameState.currentPlayer.getHand(), gameState.currentCardPosition, gameState.currentPlayer);
				gameState.currentCardClicked = null;
			} else {
				gameState.gameService.removeHighlightFromAll(gameState.getBoard());
			}
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
			/*if (tile.isOccupied() && tile.getUnit().getOwner() != gameState.currentPlayer) {
				// Attack the enemy unit if it's within attack range
				if (gameState.gameService.isValidAttack(gameState.currentUnitClicked, tile.getUnit())) {
					gameState.gameService.attack(gameState.currentUnitClicked, tile.getUnit(), gameState.getBoard());
					// or move and attack
					gameState.gameService.moveAndAttack(gameState.currentUnitClicked, tile.getUnit(), gameState.getBoard());
					gameState.currentUnitClicked.setAttackedThisTurn(true);
				} else {
					gameState.gameService.removeHighlightFromAll(gameState.getBoard());
				}
				gameState.currentUnitClicked = null;
			}*/
		}
	}

}
