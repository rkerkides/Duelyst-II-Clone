package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import structures.GameState;

public class AIPlayer extends Player {

	// restrained to just end-turn to facilitate testing before implementing AI
	public void takeTurn(ActorRef out, GameState gameState, JsonNode message) {
		EndTurnClicked endTurn = new EndTurnClicked();
		BasicCommands.addPlayer1Notification(out, "AI takes turn (and immediately ends it)", 2);
		endTurn.processEvent(out, gameState, message);
	}

	@Override
	public String toString() {
		return "AIPlayer";
	}

}
