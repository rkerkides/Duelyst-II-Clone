package structures.basic.cards;

import java.util.Comparator;
import java.util.Optional;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

public class Elixir {

	public static void silverguardSquire( ActorRef out, GameState gs) {
		
		Tile avatarPosition= gs.getAi().getAvatar().getCurrentTile(gs.getBoard());
		boostAdjacentUnits(avatarPosition, 1, 1, gs);
		BasicCommands.addPlayer1Notification(out, "Silverguard Squire's Opening Gambit!", 3);
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	public static void boostAdjacentUnits(Tile tile, int healthBoost, int attackBoost, GameState gameState) {
	    // Get the x and y coordinates of the given tile
	    int x = tile.getTilex();
	    int y = tile.getTiley();


	    int newLeft = x - 1;
	    int newRight = x + 1;
		if (newLeft >= 0 ) {
			// Check the left tile
			Tile leftTile = gameState.getBoard().getTile(newLeft, y);
			
			if (leftTile != null && leftTile.isOccupied() &&
					leftTile.getUnit().getOwner()instanceof AIPlayer) {
				gameState.gameService.updateUnitHealth(leftTile.getUnit(), leftTile.getUnit().getHealth() + healthBoost);
				gameState.gameService.updateUnitAttack(leftTile.getUnit(), leftTile.getUnit().getAttack() + attackBoost);
				gameState.gameService.healing(leftTile);

	        }
			
		}
		if (newRight < 9 ) {
			// Check the right tile
			Tile rightTile = gameState.getBoard().getTile(newRight, y);
			
			if (rightTile != null && rightTile.isOccupied() &&
					rightTile.getUnit().getOwner() instanceof AIPlayer) {
				gameState.gameService.updateUnitHealth(rightTile.getUnit(), rightTile.getUnit().getHealth() + healthBoost);
				gameState.gameService.updateUnitAttack(rightTile.getUnit(), rightTile.getUnit().getAttack() + attackBoost);
				gameState.gameService.healing(rightTile);

			}

		}
	}
	
	public static void Sundrop(Unit unit, GameState gs, ActorRef out) {
        // Implement healing effect by 4 health
        if (unit != null && unit.getOwner() instanceof AIPlayer) {
            AIPlayer aiPlayer = (AIPlayer) gs.getAi(); // Assuming gs is your GameState object

            if (unit.getHealth() == unit.getMaxHealth()) {
                // Find the unit with the lowest health
                Optional<Unit> unitWithLowestHealth = aiPlayer.getUnits().stream()
                        .min(Comparator.comparingInt(Unit::getHealth));

                // Check if a unit with the lowest health is found
                Unit lowestHealthUnit = unitWithLowestHealth.orElse(null);

                if (lowestHealthUnit != null) {
                    int newHealth = lowestHealthUnit.getHealth() + 4;
                    if (newHealth > lowestHealthUnit.getMaxHealth()) {
                        gs.gameService.updateUnitHealth(lowestHealthUnit, lowestHealthUnit.getMaxHealth());
                    } else {
                        gs.gameService.updateUnitHealth(lowestHealthUnit, newHealth);
                    }
                }
            } else {
                int newHealth = unit.getHealth() + 4;
                if (newHealth > unit.getMaxHealth()) {
                    gs.gameService.updateUnitHealth(unit, unit.getMaxHealth());
                } else {
                    gs.gameService.updateUnitHealth(unit, newHealth);
                }
            }
        }
		BasicCommands.addPlayer1Notification(out, "Sundrop Elixir heals a unit!", 3);
        gs.gameService.healing(unit.getCurrentTile(gs.getBoard()));
    }

}
