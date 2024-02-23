package structures.basic.player;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import akka.actor.ActorRef;
import structures.basic.Deck;
import structures.basic.cards.BadOmen;
import structures.basic.cards.Card;
import structures.basic.cards.GloomChaser;
import structures.basic.cards.HornOfTheForsaken;

public class HumanPlayer extends Player {

	ActorRef out;
    private Deck<Card> deck;
    private Hand hand;
	 

	public Deck<Card> getDeck() {
		return deck;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public HumanPlayer() {
		super();
		this.hand = new Hand();
		this.deck=new Deck<Card>();
    	addCardsIntoDeck(); 
		this.drawCards(3);
	}

	public HumanPlayer(int health, int mana) {
		super(health, mana);
		this.hand = new Hand();
		this.deck = new Deck<Card>();
    	addCardsIntoDeck(); 
		this.drawCards(3);
		// this.setHand(new Hand());alternative if setting to private initial value
		
	}
    public void addCardsIntoDeck() {
    	Stream <Card> stream =Stream.of(new BadOmen(), 
    			new HornOfTheForsaken(),
    			new GloomChaser()/*,new Card()*/);
    	Collection<Card> cards = stream.collect(Collectors.toCollection(Deck::new));
    	this.deck=(Deck<Card>) cards;
    }

	public void drawCards(int cardsToDraw) {
	    for (int i = 0; i < cardsToDraw; i++) {
	        if (!deck.isEmpty()) {
	            Card card = deck.pop();
	            hand.addCard(card);
				System.out.println("Card drawn: " + card.getCardname());
	        }
	    }
	}

}

