package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
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
	            leftTile.getUnit().setHealth(leftTile.getUnit().getHealth() + 1);
	            leftTile.getUnit().setAttack(leftTile.getUnit().getAttack() + 1);
	        }
			
		}
		if (newRight < 9 ) {
			// Check the right tile
			Tile rightTile = gameState.getBoard().getTile(newRight, y);
			
			if (rightTile != null && rightTile.isOccupied() && rightTile.getUnit().getOwner() instanceof AIPlayer) {
				rightTile.getUnit().setHealth(rightTile.getUnit().getHealth() + 1);
				rightTile.getUnit().setAttack(rightTile.getUnit().getAttack() + 1);
			}
			

		}	    	
	        
	        
	    }


}
