package structures.basic.cards;

import java.util.Comparator;
import java.util.Optional;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

public class Elixir {

	public static void silverguardSquire( ActorRef out, GameState gs) {
		
		Tile avatarPosition= gs.getAi().getAvatar().getCurrentTile(gs.getBoard());
		boostAdjacentUnits(avatarPosition, 1, 1, gs);
		
		
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
				gameState.gameService.updateUnitHealth(leftTile.getUnit(), healthBoost);
				gameState.gameService.updateUnitAttack(leftTile.getUnit(), attackBoost);
				gameState.gameService.healing(leftTile);

	        }
			
		}
		if (newRight < 9 ) {
			// Check the right tile
			Tile rightTile = gameState.getBoard().getTile(newRight, y);
			
			if (rightTile != null && rightTile.isOccupied() &&
					rightTile.getUnit().getOwner() instanceof AIPlayer) {
				gameState.gameService.updateUnitHealth(rightTile.getUnit(), healthBoost);
				gameState.gameService.updateUnitAttack(rightTile.getUnit(), attackBoost);
				gameState.gameService.healing(rightTile);

			}
			

		}	    	
	        
	        
	    }
	
	public static void Sundrop(Unit unit, GameState gs) {
		// implement healing effect by 4 health
		if (unit!= null && unit.getHealth() == unit.getMaxHealth() 
				&& unit.getOwner() instanceof AIPlayer) {
			  AIPlayer aiPlayer = (AIPlayer) gs.getAi(); // Assuming gs is your GameState object
		        
		        // Find the unit with the lowest health
		        Optional<Unit> unitWithLowestHealth = aiPlayer.getUnits().stream()
		                .min(Comparator.comparingInt(Unit::getHealth));

		        // Check if a unit with the lowest health is found
		        Unit lowestHealthUnit = unitWithLowestHealth.orElse(null);
		        
				int newHealth = lowestHealthUnit.getHealth() + 4;
				
				if (newHealth > unit.getMaxHealth()) {
			    	gs.gameService.updateUnitHealth(unit, unit.getMaxHealth());
				} else {
					gs.gameService.updateUnitHealth(unit, newHealth);
				}

			
			
		}
		if (unit != null && unit.getHealth() < unit.getMaxHealth() &&
				unit.getOwner() instanceof AIPlayer ) {
			
				int newHealth = unit.getHealth() + 4;
				
				if (newHealth > unit.getMaxHealth()) {
		    	gs.gameService.updateUnitHealth(unit, unit.getMaxHealth());
			} else {
				gs.gameService.updateUnitHealth(unit, newHealth);
			}
		}
		gs.gameService.healing(unit.getCurrentTile(gs.getBoard()));
	}


}
