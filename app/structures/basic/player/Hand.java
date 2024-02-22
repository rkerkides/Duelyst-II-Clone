package structures.basic.player;

import structures.basic.cards.Card;

import java.util.ArrayList;
import java.util.List;

public class Hand {
	
    private ArrayList<Card> cards;
    

	public Hand() {
        this.cards = new ArrayList<>();
        
    }

    public void addCard(Card drawnCard) {
        cards.add(drawnCard);
        
    }
    public Card getCardAtIndex(int index) {
        if (index >= 0 && index < cards.size()) {
            return cards.get(index);
        } else {
            return null; 
        }
    }
    
    public int getSize() {
        return cards.size();
    }

    // Public getter for Jackson to use for serialization
    public List<Card> getCards() {
        return cards;
    }
    
   

}


