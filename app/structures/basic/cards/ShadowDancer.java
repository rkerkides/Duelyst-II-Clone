package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

public class ShadowDancer extends Unit {
	
	public static void Deathwatch(GameState gs, ActorRef out) {
		
		Unit humanAvatar=gs.getHuman().getAvatar();
		Unit aiAvatar=gs.getAi().getAvatar();
		
        for (Unit unit : gs.getUnits()) {
            if (unit.getName().equals("Shadowdancer")) {
            	
            	gs.gameService.updateUnitHealth(humanAvatar, humanAvatar.getHealth() + 1);
            	gs.gameService.healing(humanAvatar.getCurrentTile(gs.getBoard()));
            	
            	gs.gameService.updateUnitHealth(aiAvatar, aiAvatar.getHealth() - 1);
            	gs.gameService.strike(aiAvatar.getCurrentTile(gs.getBoard()));

				BasicCommands.addPlayer1Notification(out, "Shadowdancer heals avatar and hurts enemy!", 3);

			}
        }
		System.err.println("ShadowDance");
	}

}
