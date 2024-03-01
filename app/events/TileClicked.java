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

		// Ignore events when the AI is taking its turn
		if (gameState.getCurrentPlayer().equals(gameState.getAi())) {
			return;
		}

		/*System.out.println("Last event" + " -> tileclicked");

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		// find the tile that was clicked
		Tile tile = gameState.getBoard().getTile(tilex, tiley);

		// Process event based on tile's unit and ownership
		if (tile.isOccupied() && tile.getUnit().getOwner() == gameState.getCurrentPlayer()) {
			// Unhighlight all tiles if a unit is already clicked
			if (gameState.getCurrentUnitClicked() != null) {
				gameState.gameService.removeHighlightFromAll(gameState.getBoard());
			}
			Unit unit = tile.getUnit();
			gameState.setCurrentUnitClicked(unit);
			// Highlight move range if unit has not moved this turn
			if (!unit.isMovedThisTurn()) {
				gameState.gameService.highlightMoveRange(unit, gameState.getBoard());
			}
		}

		// Summon unit if last event was a card click and the current tile is highlighted
		if (gameState.lastEvent.equals("cardclicked") && gameState.getCurrentCardClicked() != null) {
			System.out.println("Trying to summon " + gameState.getCurrentCardClicked().getCardname() + " to tile " + tilex + ", " + tiley);
			if (gameState.gameService.isValidSummon(gameState.getCurrentCardClicked(), tile)) {
				gameState.gameService.removeCardFromHandAndSummonUnit
						(gameState.getBoard(), gameState.getCurrentCardClicked(),
						tile, gameState.getCurrentPlayer().getHand(), gameState.getCurrentCardPosition(), gameState.getCurrentPlayer());
				gameState.setCurrentCardClicked(null);
			} else {
				gameState.gameService.removeHighlightFromAll(gameState.getBoard());
			}
		}

		// Print the current unit clicked for debugging
		System.out.println("Unit clicked: " + gameState.getCurrentUnitClicked());


		// Handle movement if last event was a tile click and the current unit clicked is not null
		if (gameState.lastEvent.equals("tileclicked") && gameState.getCurrentUnitClicked() != null) {
			if (!tile.isOccupied() && gameState.getCurrentUnitClicked().getOwner() == gameState.getCurrentPlayer()) {
				// Move the unit if the tile is highlighted
				if (gameState.gameService.isValidMove(gameState.getCurrentUnitClicked(), tile)) {
					gameState.gameService.updateUnitPositionAndMove(gameState.getCurrentUnitClicked(), tile, gameState.getBoard());
					gameState.getCurrentUnitClicked().setMovedThisTurn(true);
				} else {
					gameState.gameService.removeHighlightFromAll(gameState.getBoard());
				}
				gameState.setCurrentUnitClicked(null);
			}
			*//*if (tile.isOccupied() && tile.getUnit().getOwner() != gameState.currentPlayer) {
				// Attack the enemy unit if it's within attack range
				if (gameState.gameService.isValidAttack(gameState.getCurrentUnitClicked(), tile.getUnit())) {
					gameState.gameService.attack(gameState.getCurrentUnitClicked(), tile.getUnit(), gameState.getBoard());
					// or move and attack
					gameState.gameService.moveAndAttack(gameState.getCurrentUnitClicked(), tile.getUnit(), gameState.getBoard());
					gameState.getCurrentUnitClicked().setAttackedThisTurn(true);
				} else {
					gameState.gameService.removeHighlightFromAll(gameState.getBoard());
				}
				gameState.getCurrentUnitClicked() = null;
			}*/
		}
	}
