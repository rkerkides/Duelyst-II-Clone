package structures.basic;

import structures.basic.cards.Card;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.OrderedCardLoader;

import java.util.List;

public class Deck {
    private List<Card> deck;

    // Load the appropriate deck based on the player
    public Deck(Player player) {
        if (player instanceof HumanPlayer) {
            this.deck = OrderedCardLoader.getPlayer1Cards(2);
        } else {
            this.deck = OrderedCardLoader.getPlayer2Cards(2);
        }
    }

    public Card drawCard() {
        return deck.remove(0);
    }

    public List<Card> getDeck() {
        return deck;
    }
}

