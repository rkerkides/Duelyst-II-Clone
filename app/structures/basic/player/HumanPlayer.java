package structures.basic.player;

import akka.actor.ActorRef;
import commands.BasicCommands;
import utils.OrderedCardLoader;
import structures.basic.HumanDeck;
import structures.basic.cards.Card;

public class HumanPlayer extends Player {

	ActorRef out;
	private HumanDeck humanDeck;
	 

	public HumanPlayer() {
		super();
		this.hand = new Hand();
		this.humanDeck = new HumanDeck();
	}

	public HumanPlayer(int health, int mana) {
		super(health, mana);
		this.hand = new Hand();
		this.humanDeck = new HumanDeck();
		// this.setHand(new Hand());alternative if setting to private initial value
		
	}

	public void drawCards(int cardsToDraw) {
	    for (int i = 0; i < cardsToDraw; i++) {
	        if (!humanDeck.getCards().isEmpty()) {
	            Card card = humanDeck.getCards().pop();
	            hand.addCard(card);
	        }
	    }
	}

}

