package structures.basic.cards;

import static utils.BasicObjectBuilders.loadUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameService;
import structures.GameState;
import structures.basic.Board;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class Wraithling extends Unit{
	
	private static int id=1000;
	
	private static int GloomChaserWraitlingsCount = 0;

	
	public static void check( ActorRef out, GameState gameState, GameService gs) {
		
		for(Unit unit:gameState.getUnitsOnBoard()){
			System.out.println(unit.getName()+ " is on the board");
			if (unit.getName().equals("Gloom Chaser")&& GloomChaserWraitlingsCount<1) {
				System.out.println("Gloom Chaser is on the board");
					summonWraithling(unit, out, gameState, gs);
					GloomChaserWraitlingsCount++;
				break;
				}        
			}
		System.out.println("Units on the board: "+gameState.getUnitsOnBoard().size());
		
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();
		}
	}
	
	private static Unit summonWraithling(Unit parent, ActorRef out, GameState gameState, GameService gs) {
	    if (parent.getName().equals("Player Avatar") && gameState.getCurrentPlayer().getRobustness() > 0) {
	        Tile currentTile = parent.getCurrentTile(gameState.getBoard());
	        Tile randomAdjacentTile = getRandomAdjacentUnoccupiedTile(currentTile, gameState.getBoard());
	        
	        if (randomAdjacentTile != null) {
	            summonWraithlingToTile(randomAdjacentTile, out, gameState);
	            return null;
	        }
	    } else if (parent.getName().equals("Gloom Chaser")) {
	        Tile toTheLeft = findTileToLeft(parent, gameState);
	        
	        if (toTheLeft != null && !toTheLeft.isOccupied()) {
	            return summonWraithlingToTile(toTheLeft, out, gameState);
	        }
	    }
	    
	    try {
	        Thread.sleep(30);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	    
	    return null;
	}

	private static Tile findTileToLeft(Unit parent, GameState gameState) {
	    Tile currentTile = parent.getCurrentTile(gameState.getBoard());
	    int newTileX = currentTile.getTilex() - 1; // Two tiles to the left
	    int newTileY = currentTile.getTiley(); // Same Y coordinate
	    return gameState.getBoard().getTile(newTileX, newTileY);
	}

	private static Unit summonWraithlingToTile(Tile tile, ActorRef out, GameState gameState) {
	    Unit unit = loadUnit(StaticConfFiles.wraithling, id, Unit.class);
	    id++;
	    tile.setUnit(unit);
	    unit.setPositionByTile(tile);
	    unit.setOwner(gameState.getHuman());
	    unit.setName("Wraithling" + id);
	    gameState.getHuman().addUnit(unit);
	    EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.wsummon);
	    BasicCommands.playEffectAnimation(out, effect, tile);
	    BasicCommands.drawUnit(out, unit, tile);
	    unit.setAttack(1);
	    unit.setHealth(1);
	    BasicCommands.setUnitHealth(out, unit, 1);
	    BasicCommands.setUnitAttack(out, unit, 1);
	    return unit;
	}

	private static Tile getRandomAdjacentUnoccupiedTile(Tile currentTile, Board board) {
	    int currentX = currentTile.getTilex();
	    int currentY = currentTile.getTiley();
	    
	    // List to store adjacent tiles
	    List<Tile> adjacentTiles = new ArrayList<>();
	    
	    // Add adjacent tiles to the list
	    adjacentTiles.add(board.getTile(currentX + 1, currentY)); // Right
	    adjacentTiles.add(board.getTile(currentX - 1, currentY)); // Left
	    adjacentTiles.add(board.getTile(currentX, currentY + 1)); // Down
	    adjacentTiles.add(board.getTile(currentX, currentY - 1)); // Up
	    
	    // Shuffle the list to randomize the selection
	    Collections.shuffle(adjacentTiles);
	    
	    // Iterate over the shuffled list
	    for (Tile tile : adjacentTiles) {
	        // Check if the tile is within bounds and unoccupied
	        if (tile != null && !tile.isOccupied()) {
	            return tile; // Return the first unoccupied tile found
	        }
	    }
	    
	    return null; // Return null if no unoccupied tile is found
	}


			
}



