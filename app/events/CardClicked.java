package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.cards.CardAction;

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

		// Ignore events when the AI is taking its turn
		if (gameState.getCurrentPlayer().equals(gameState.getAi())) {
			return;
		}

		int handPosition = message.get("position").asInt();

		// CLear all highlighted tiles
		gameState.gameService.removeHighlightFromAll();



		CardAction cardAction = new CardAction(gameState, handPosition);
		
		cardAction.creaturePreAction();	
		cardAction.spellPreAction();
	}

}
