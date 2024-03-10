package structures.basic.cards;

import java.util.List;

import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

public class Nightsorrow {

	public static void assassin(Tile tile, GameState gameState) {
		
		        // Check adjacent tiles for enemy units
		        List<Tile> adjacentTiles = gameState.getBoard().getAdjacentTiles(tile);
		        
		        for (Tile adjacentTile : adjacentTiles) {
		        	if (adjacentTile == null) {
                        continue; } // Skip this iteration if the tile is null
		        	if(adjacentTile.getUnit()!=null){		        		
		        		Unit unit = adjacentTile.getUnit();
                		if(unit.getOwner()!=gameState.getCurrentPlayer()) {
                			if(unit.getHealth()<2) {
                				gameState.gameService.performUnitDeath(unit);
                				break;
                			}
                		}
		        	}
		        }
		        
	}
}
	


