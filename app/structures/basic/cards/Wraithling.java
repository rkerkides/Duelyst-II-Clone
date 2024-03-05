package structures.basic.cards;

import static utils.BasicObjectBuilders.loadUnit;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameService;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class Wraithling extends Unit{
	
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
		Tile current = parent.getCurrentTile(gameState.getBoard());
		int newTileX = current.getTilex()-1; // Two tiles to the right
		int newTileY = current.getTiley(); // Same Y coordinate
		Tile toTheLeft = gameState.getBoard().getTile(newTileX, newTileY); // Get the tile at the new position
		
		if(!toTheLeft.isOccupied()) {
			Unit unit = loadUnit(StaticConfFiles.wraithling, 10, Unit.class);	
			toTheLeft.setUnit(unit);
			unit.setPositionByTile(toTheLeft);
			unit.setOwner(gameState.getHuman());
			unit.setName("Wraithling");
			gameState.getHuman().addUnit(unit);
			EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.wsummon);
			BasicCommands.playEffectAnimation(out, effect, toTheLeft);
			BasicCommands.drawUnit(out, unit, toTheLeft);
			unit.setAttack(1);
			unit.setHealth(1); 
			BasicCommands.setUnitHealth(out, unit, 1);
			BasicCommands.setUnitAttack(out, unit, 1);
			return unit;
		}
        
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		return null;


	}


			
}



