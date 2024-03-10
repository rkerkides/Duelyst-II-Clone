package structures.basic.cards;

import structures.GameState;
import structures.basic.Unit;

public class BeamShock {
	
	public static Unit stunnedUnit=null;
	
	
	public static void stunUnit(Unit u, GameState gs) {
		
		stunnedUnit = u;
		gs.gameService.stunning(u.getCurrentTile(gs.getBoard()));
		
	}
}
