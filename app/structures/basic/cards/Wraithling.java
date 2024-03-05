package structures.basic.cards;

import static utils.BasicObjectBuilders.loadUnit;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameService;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class Wraithling extends Unit{
	
	private int health =1;
	private int attack =1;
	
	
		
	public static void Summon( int numberOfinstances, Unit u, Player player, ActorRef out, GameState gameState, GameService gs) {
		System.out.println("Summoning Wraithling");

            // get the tile of the unit
			Tile current = u.getCurrentTile(gameState.getBoard());
			int newTileX = current.getTilex()-1; // Two tiles to the right
			int newTileY = current.getTiley(); // Same Y coordinate
			Tile toTheLeft = gameState.getBoard().getTile(newTileX, newTileY); // Get the tile at the new position
	
			Unit unit = loadUnit(StaticConfFiles.wraithling, 0, Unit.class);
			gameState.getHuman().addUnit(unit);
			// set unit position
			toTheLeft.setUnit(unit);
			unit.setPositionByTile(toTheLeft);
			unit.setOwner(player);
			unit.setName("Wraithling");
			player.addUnit(unit);
			BasicCommands.setUnitHealth(out, unit, 1);
            BasicCommands.setUnitAttack(out, unit, 1);
			EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
			BasicCommands.playEffectAnimation(out, effect, toTheLeft);
			BasicCommands.drawUnit(out, unit, toTheLeft);
		
		try {
		Thread.sleep(500);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
		
    }



	public int getHealth() {
		return health;
	}



	public void setHealth(int health) {
		this.health = health;
	}



	public int getAttack() {
		return attack;
	}



	public void setAttack(int attack) {
		this.attack = attack;
	}
	
			
	}



