package structures.basic;

import java.util.ArrayList;

public class Hand {
	
    private ArrayList<Card> cards;
    private int size;

    public Hand() {
        this.cards = new ArrayList<>();
        this.size = 0;
    }

    public void addCard(Card drawnCard) {
        cards.add(drawnCard);
        size++;
    }
    public Card getCardAtIndex(int index) {
        if (index >= 0 && index < size) {
            return cards.get(index);
        } else {
            return null; 
        }
    }
}


