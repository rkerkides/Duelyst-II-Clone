package structures.basic.cards;

import static utils.BasicObjectBuilders.loadUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;

import structures.basic.Board;

import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class Wraithling extends Unit{
	
	public static int WraithlingSwarm=3;
	private static int id=90;
	

	
	public static void summonGloomChaserWraithling(Tile tile, ActorRef out, GameState gameState) {
		
	    	
	        Tile toTheLeft = findTileToLeft(tile, gameState);
	        
	        if (toTheLeft != null && !toTheLeft.isOccupied()) {
	          summonWraithlingToTile(toTheLeft, out, gameState);}
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

	    
	    }


	private static Tile findTileToLeft(Tile base, GameState gameState) {
		
			Tile currentTile = base;
	    
			int newTileX = currentTile.getTilex() - 1; // Two tiles to the left
			int newTileY = currentTile.getTiley(); // Same Y coordinate
	    
	    // Check for array bounds
			if (newTileX < 0 || newTileX >= 8) {
	        return null; // Out of bounds, return null
	    }
	    
	    return gameState.getBoard().getTile(newTileX, newTileY);
	}


	public static void summonWraithlingToTile(Tile tile, ActorRef out, GameState gameState) {	
		
		if(tile==null || tile.isOccupied()) {
			System.out.println("Tile is null or occupied");
			return;
		}
				
	    Unit wraithling = loadUnit(StaticConfFiles.wraithling, id, Unit.class);
		// set unit position
		tile.setUnit(wraithling);
		wraithling.setPositionByTile(tile);
	    wraithling.setOwner(gameState.getHuman());
		gameState.getHuman().addUnit(wraithling);
		gameState.addToTotalUnits(1);
		gameState.addUnitstoBoard(wraithling);
		wraithling.setName("Wraithling_" + id);
	    id++;
		
		System.out.println("Wraithling is added to board, all units: " + ( gameState.getUnitsOnBoard()).size());


		// draw unit on new tile and play summon animation
	    EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.wsummon);
		BasicCommands.playEffectAnimation(out, effect, tile);
		BasicCommands.drawUnit(out, wraithling, tile);


		wraithling.setAttack(1);
		wraithling.setHealth(1);

		wraithling.setMovedThisTurn(true);
		wraithling.setAttackedThisTurn(true);
		// wait for animation to play out
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	   
	    System.out.println("Wraithling summoned to " + tile.getTilex() + ", " + tile.getTiley()+
				" with id: " + wraithling.getId() + " and name: " + wraithling.getName()
				+ " and attack: " + wraithling.getAttack() + " and health: "
				+ wraithling.getHealth());
	    BasicCommands.setUnitHealth(out, wraithling, 1);
	    BasicCommands.setUnitAttack(out, wraithling, 1);
        gameState.gameService.removeHighlightFromAll();

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


	public static void summonAvatarWraithling(ActorRef out, GameState gs) {
		
		
		Tile tile = getRandomAdjacentUnoccupiedTile(gs.getHuman().getAvatar()
				.getCurrentTile(gs.getBoard()), gs.getBoard());			
		summonWraithlingToTile(tile, out, gs);
	}





			
}



//if (card.getCardname().equals("Wraithling Swarm")) {
//	
//	int x=tile.getTilex();
//	int y=tile.getTiley();
//	for (int i = x; i < x+3; i++) {
//		Tile tempTile = gameState.getBoard().getTile(i, y);
//		if(tempTile!=null && !tempTile.isOccupied()) {
//			summonWraithlingToTile(tempTile, out, gameState);
//		}
//	}
	
//}


