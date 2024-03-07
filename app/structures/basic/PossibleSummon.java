package structures.basic;

import structures.basic.cards.Card;

public class PossibleSummon extends PossibleMove{
    public Card card;

    public PossibleSummon(Card card, Tile tile) {
        super(null, tile);
        this.card = card;
    }
}
