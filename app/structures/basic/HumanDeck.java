package structures.basic;

import structures.basic.cards.BadOmen;
import structures.basic.cards.Card;
import structures.basic.cards.HornOfTheForsaken;
import java.util.Stack;

public class HumanDeck {
    private Stack<Card> cards;

    public HumanDeck() {
        this.cards = new Stack<>();
        cards.push(new BadOmen());
        cards.push(new HornOfTheForsaken());
        // Add more cards to the deck
    }

    public Stack<Card> getCards() {
        return cards;
    }

    public Card drawCard() {
        return cards.pop();
    }
}

