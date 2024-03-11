package structures.basic.cards;

import structures.GameState;
import structures.basic.Unit;
import structures.basic.player.HumanPlayer;

public class Strike {

	public static void TrueStrike(GameState gs, Unit u) {
		
		if (u != null && u.getOwner() instanceof HumanPlayer) {
			
			gs.gameService.updateUnitHealth(u, u.getHealth() - 2);
			gs.gameService.strike(u.getCurrentTile(gs.getBoard()));
		}
		
		
	}
}
