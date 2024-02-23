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
	private int turn;
	private Deck<Card> deck;
	
	

	public Deck<Card> getDeck() {
		return deck;
	}

	public void setDeck(Deck<Card> deck) {
		this.deck = deck;
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

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public Player() {
		super();
		this.health = 20;
		this.mana = 0;
		this.hand = null;
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

	public abstract void drawCards(int i);

}