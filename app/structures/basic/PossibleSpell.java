package structures.basic;

import structures.basic.cards.Card;

public class PossibleSpell extends PossibleMove{
    public Card card;
    public PossibleSpell(Card card, Unit unit, Tile tile) {
        super(unit, tile);
        this.card = card;
    }

    public PossibleSpell(Card card, Tile tile) {
        super(null, tile);
        this.card = card;
    }
}
