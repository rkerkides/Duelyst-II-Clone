package structures.basic.cards;

import structures.GameState;
import structures.basic.Unit;

public class ShadowDancer extends Unit {
	
	public static void Deathwatch(GameState gs) {
		
		Unit humanAvatar=gs.getHuman().getAvatar();
		Unit aiAvatar=gs.getAi().getAvatar();
		
        for (Unit unit : gs.getUnits()) {
            if (unit.getName().equals("Shadowdancer")) {
            	
            	gs.gameService.updateUnitHealth(humanAvatar, humanAvatar.getHealth() + 1);
            	gs.gameService.healing(humanAvatar.getCurrentTile(gs.getBoard()));
            	
            	gs.gameService.updateUnitHealth(aiAvatar, aiAvatar.getHealth() - 1);
            	gs.gameService.stunning(aiAvatar.getCurrentTile(gs.getBoard()));

			}
        }
		System.err.println("ShadowDance");
	}

}
