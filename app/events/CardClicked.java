package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a card.
 * The event returns the position in the player's hand the card resides within.
 * 
 * { 
 *   messageType = “cardClicked”
 *   position = <hand index position [1-6]>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class CardClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		int index = message.get("position").asInt() - 1;


		if (gameState.currentPlayer.getHand().getCardAtIndex(index) != null) {
			// Set the current card clicked to the card at the specified position in the player's hand
			gameState.currentCardClicked = gameState.currentPlayer.getHand().getCardAtIndex(index);
			// Highlight the summon range of the current card clicked
			gameState.gameService.highlightSummonRange(gameState.currentCardClicked, gameState.getBoard());
		}
	}

}
