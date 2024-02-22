package structures.basic;

import java.util.Stack;

import structures.basic.cards.Card;

public class aiDeck {
	private Stack<Card> cards;

    public aiDeck() {
        this.cards = new Stack<>();
        // Add more cards to the deck
    }

    public Stack<Card> getCards() {
        return cards;
    }

    public Card drawCard() {
        return cards.pop();
    }
}
