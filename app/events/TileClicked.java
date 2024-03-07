package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Actionable;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.cards.Card;
import structures.basic.cards.Wraithling;
import structures.basic.player.Hand;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;

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

	private ActorRef out;

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
			if (lastAction instanceof Card && !((Card) lastAction).isCreature()) {
				handleSpellCasting(gameState, (Card) lastAction, tile);
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
	private void handleSpellCasting(GameState gameState, Card card, Tile tile) {
	    // Spell casting logic
		if (gameState.getCurrentPlayer() instanceof HumanPlayer) {
			Player player = gameState.getCurrentPlayer();
			Hand hand = player.getHand();
			int handPosition = gameState.getCurrentCardPosition();

		    if (card.getCardname().equals("Horn of the Forsaken")) {
		        if (gameState.getHuman().getMana() >= card.getManacost()) {
		            // Sufficient mana for casting the spell
		            gameState.gameService.HornOfTheForesaken(card);//to change to fit other spell	
		            gameState.getCurrentPlayer().setRobustness(player.getRobustness() + 3);	            
		            System.out.println("Player's robustness: " + player.getRobustness());
		            hand.removeCardAtPosition(handPosition);
		    		gameState.gameService.updateHandPositions(player.getHand());
		    		
		        } else {
		            // Insufficient mana for casting the spell
		        	BasicCommands.addPlayer1Notification(out, "Not enough mana", 2);
		        	System.out.println("Not enough mana");
		    	    gameState.gameService.removeHighlightFromAll();
		    	    gameState.gameService.notClickingCard();

		        }
		    }
			if (card.getCardname().equals("Dark Terminus") &&
					tile.getUnit().getOwner() != gameState.getHuman()) {
				    gameState.gameService.performUnitDeath(tile.getUnit());
				    
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("Dark Terminus is casted");
				    Wraithling.summonWraithlingToTile(tile, out, gameState);
		    	
		    }
		    
		    // Remove highlight from all tiles
		    gameState.gameService.removeHighlightFromAll();
			
		}

	}
	
	// Process unit move or attack based on targetTile's state
	private void handleUnitAction(GameState gameState, Unit unit, Tile targetTile) {
		// Early return if targetTile is null
		if (targetTile == null) {
			System.out.println("Target tile is null.");
			gameState.gameService.removeHighlightFromAll();
			return;
		}

		if (unit == null) {
			System.out.println("Unit is null.");
			gameState.gameService.removeHighlightFromAll();
			return;
		}

		// Determine action based on tile's occupancy and highlight mode
		if (!targetTile.isOccupied()) {
			// Assuming all valid moves are already checked, directly move the unit
			gameState.gameService.updateUnitPositionAndMove(unit, targetTile);
			System.out.println("Unit " + unit.getId() + " moved to " + targetTile.getTilex() + ", " + targetTile.getTiley());
		} else if (targetTile.getHighlightMode() == 2) {
			// Directly handle attack as validity should have been ensured beforehand
			System.out.println("Attacking unit on tile " + targetTile.getTilex() + ", " + targetTile.getTiley());
			Tile attackerTile = unit.getCurrentTile(gameState.getBoard());

			if (gameState.gameService.isWithinAttackRange(attackerTile, targetTile)) {
				// Attack adjacent unit
				if (targetTile.isOccupied()) {
					System.out.println("Target tile is occupied by " + targetTile.getUnit());
				}
				gameState.gameService.adjacentAttack(unit, targetTile.getUnit());
				unit.setAttackedThisTurn(true);
				unit.setMovedThisTurn(true);
			} else {
				// Move and attack
				if (targetTile.isOccupied()) {
					System.out.println("Target tile is occupied by " + targetTile.getUnit() + " and is attacked by " + unit);
				}
				gameState.gameService.moveAndAttack(unit, targetTile.getUnit());
				unit.setAttackedThisTurn(true);
				unit.setMovedThisTurn(true);
			}

			// Remove highlight from all tiles after action
			gameState.gameService.removeHighlightFromAll();
		}
	}

	// Place unit card on board if tile is valid
	private void handleCardSummoning(GameState gameState, Card card, Tile tile) {
		if (gameState.gameService.isValidSummon(card, tile)) {
			gameState.gameService.removeCardFromHandAndSummon(card, tile);
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