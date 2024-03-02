package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Actionable;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.cards.Card;
import structures.basic.cards.CreatureCard;
import structures.basic.cards.SpellCard;

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
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Ignore events when the AI is taking its turn
		if (gameState.getCurrentPlayer().equals(gameState.getAi())) {
			return;
		}

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();
		Tile tile = gameState.getBoard().getTile(tilex, tiley);

		// Check if there's an action in history
		if (!gameState.getActionHistory().isEmpty()) {
			Actionable lastAction = gameState.getActionHistory().peek();

			// Handle spell casting or unit interaction based on last action type
			if (lastAction instanceof SpellCard) {
				handleSpellCasting(gameState, (SpellCard) lastAction, tile);
			} else if (lastAction instanceof Unit) {
				handleUnitAction(gameState, (Unit) lastAction, tile);
			// Change this to instanceof CreatureCard in the future
			} else if (lastAction instanceof Card) {
				handleCardSummoning(gameState, (Card) lastAction, tile);
			}

			// Clear last action if it's not related to current tile interaction
			System.out.println("Popped " + gameState.getActionHistory().pop());;
		} else {
			// No prior action, check for unit on tile for possible movement or attack highlighting
			if (tile.isOccupied() && tile.getUnit().getOwner() == gameState.getCurrentPlayer()) {
				Unit unit = tile.getUnit();
				highlightUnitActions(gameState, unit, tile);
				gameState.getActionHistory().push(unit);
			}
		}
	}


	// Process spell casting based on target tile
	private void handleSpellCasting(GameState gameState, SpellCard spellCard, Tile tile) {
		// Spell casting logic
		System.out.println("NO SPELL CASTING LOGIC IMPLEMENTED YET");
	}

	// Process unit move or attack based on targetTile's state
	private void handleUnitAction(GameState gameState, Unit unit, Tile targetTile) {
		// Ensure targetTile is not null before proceeding
		if (targetTile == null) {
			System.out.println("Target tile is null.");
			gameState.gameService.removeHighlightFromAll();
			return;
		}

		// Check if the tile is not occupied for a move action
		if (!targetTile.isOccupied()) {
			// Move unit if tile is valid for movement
			if (gameState.gameService.isValidMove(unit, targetTile)) {
				System.out.println("Moving unit " + unit.getId() + " to " + targetTile.getTilex() + ", " + targetTile.getTiley());
				gameState.gameService.updateUnitPositionAndMove(unit, targetTile);
				System.out.println("Unit " + unit.getId() + " moved to " + targetTile.getTilex() + ", " + targetTile.getTiley());
				unit.setMovedThisTurn(true);
			}
		} else {
			// Handle attack action if the tile is occupied by an enemy unit
			if (targetTile.getHighlightMode() == 2) {
				// Placeholder for attack logic
				System.out.println("Attacking unit on tile " + targetTile.getTilex() + ", " + targetTile.getTiley());
				// Implement attack logic here
				System.out.println("NO ATTACK LOGIC IMPLEMENTED YET");
			}
		}

		// Always remove highlight from all tiles after action
		gameState.gameService.removeHighlightFromAll();
	}


	// Place unit card on board if tile is valid
	private void handleCardSummoning(GameState gameState, Card card, Tile tile) {
		if (gameState.gameService.isValidSummon(card, tile)) {
			gameState.gameService.removeCardFromHandAndSummonUnit(card, tile);
		} else {
			gameState.gameService.removeHighlightFromAll();
		}
	}

	// Highlight valid moves and attacks for unit
	private void highlightUnitActions(GameState gameState, Unit unit, Tile tile) {
		// Clear all highlighted tiles
		gameState.gameService.removeHighlightFromAll();

		// Highlight move and attack range based on unit's turn state
		if (!unit.attackedThisTurn() && !unit.movedThisTurn()) {
			gameState.gameService.highlightMoveAndAttackRange(unit);
		// Highlight attack range only, if unit has moved but not attacked
		} else if (unit.movedThisTurn()) {
			gameState.gameService.highlightAttackRange(unit);
		}
	}
}