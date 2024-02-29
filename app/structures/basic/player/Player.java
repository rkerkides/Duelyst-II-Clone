package structures.basic.player;

import structures.basic.*;
import structures.basic.cards.Card;
import akka.actor.ActorRef;

/**
 * A basic representation of of the Player. A player has health and mana.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public abstract class Player {

	protected int health;
	protected int mana;
	protected Hand hand;
	private int turn = 1;
	private Deck deck;


	public Deck getDeck() {
		return deck;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public int getTurn() {
		return turn;
	}

	public void incrementTurn() {
		this.turn++;
	}

	public Player() {
		super();
		this.health = 20;
		this.mana = 0;
		this.hand = new Hand();
		this.deck=new Deck(this);
	}

	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getMana() {
		return mana;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public Card drawCard() {
		if (this.deck == null) {
			// Deck is not initialized, handle accordingly
			System.err.println("Deck is null, cannot draw a card.");
			// For example, initialize the deck here if appropriate
			// this.deck = new Deck();
			return null;
		}
        Card card = this.deck.drawCard();
		System.out.println("Player " + this + " drew card " + card + " from deck");
        this.hand.addCard(card);
		return card;
	}

}
