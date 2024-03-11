package structures.basic.cards;

import java.util.Comparator;
import java.util.List;

import structures.GameState;
import structures.basic.Unit;

public class BeamShock {
	
	public static Unit stunnedUnit=null;
	
	
	public static void stunUnit( GameState gs) {
		
		List<Unit> humanUnits = gs.getHuman().getUnits();
		// Use streams to find the unit with the highest attack value
		Unit u = humanUnits.stream().max(Comparator.comparingInt(Unit::getAttack)).orElse(null);
		stunnedUnit = u;
		gs.gameService.stunning(u.getCurrentTile(gs.getBoard()));
		
	}
}
