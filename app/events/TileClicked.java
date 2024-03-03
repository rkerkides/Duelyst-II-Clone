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
		// Early return if targetTile is null
		if (targetTile == null) {
			System.out.println("Target tile is null.");
			gameState.gameService.removeHighlightFromAll();
			return;
		}

		// Determine action based on tile's occupancy and highlight mode
		if (!targetTile.isOccupied()) {
			// Assuming all valid moves are already checked, directly move the unit
			gameState.gameService.updateUnitPositionAndMove(unit, targetTile);
			System.out.println("Unit " + unit.getId() + " moved to " + targetTile.getTilex() + ", " + targetTile.getTiley());
			unit.setMovedThisTurn(true);
		} else if (targetTile.getHighlightMode() == 2) {
			// Directly handle attack as validity should have been ensured beforehand
			System.out.println("Attacking unit on tile " + targetTile.getTilex() + ", " + targetTile.getTiley());
			Tile attackerTile = unit.getCurrentTile(gameState.getBoard());

			if (gameState.gameService.isWithinAttackRange(attackerTile, targetTile)) {
				// Attack adjacent unit
				gameState.gameService.adjacentAttack(unit, targetTile.getUnit());
				System.out.println("Unit " + unit.getId() + " attacked unit " + targetTile.getUnit().getId());
				unit.setAttackedThisTurn(true);
			} else {
				// Move and attack
				gameState.gameService.moveAndAttack(unit, targetTile.getUnit());
				System.out.println("Unit " + unit.getId() + " attacked unit " + targetTile.getUnit().getId());
				unit.setAttackedThisTurn(true);
			}

			// Remove highlight from all tiles after action
			gameState.gameService.removeHighlightFromAll();
		}
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