package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.cards.BeamShock;
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

		// Reset all units' movedThisTurn status and unhighlight all tiles
		gameState.gameService.removeHighlightFromAll();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if (gameState.getBoard().getTile(i, j).isOccupied()) {
					Unit unit = gameState.getBoard().getTile(i, j).getUnit();
					unit.setMovedThisTurn(false);
					unit.setAttackedThisTurn(false);
				}
			}
		}

		// Clear the action history, as the turn has ended
		gameState.getActionHistory().clear();



		if (gameState.getCurrentPlayer() instanceof AIPlayer) {
			((AIPlayer) gameState.getCurrentPlayer()).takeTurn(out, message);
		}
	}

}
