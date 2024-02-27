package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.player.AIPlayer;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * the end-turn button.
 * 
 * { 
 *   messageType = “endTurnClicked”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class EndTurnClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		gameState.endTurn();

		// Reset all units' movedThisTurn status
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if (gameState.getBoard().getTile(i, j).isOccupied()) {
					gameState.getBoard().getTile(i, j).getUnit().setMovedThisTurn(false);
				}
			}
		}

		if (gameState.currentPlayer instanceof AIPlayer) {
			((AIPlayer) gameState.currentPlayer).takeTurn(out, gameState, message);
		}
	}

}
