package structures.basic;

import java.util.ArrayList;
import java.util.List;
import structures.basic.cards.Card;
import utils.OrderedCardLoader;

public class HumanDeck {
	// for us potentially player only deck and for AI we will do another one

	private List<Card> cards;

	public HumanDeck() {
		this.cards = new ArrayList<>();
		/*
		 * // Add cards to the deck in the specified order cards.add(new BadOmen());
		 * cards.add(new HornOfTheForsaken()); cards.add(new GloomChaser()); //
		 * cards.add(new RockPulveriser()); // cards.add(new ShadowWatcher()); //
		 * cards.add(new NightsorrowAssassin()); // cards.add(new BloodmoonPriestess());
		 * // cards.add(new Shadowdancer()); // // cards.add(new WraithlingSwarm()); //
		 * cards.add(new DarkTerminus()); // cards.add(new Wraithling());
		 */
		cards = OrderedCardLoader.getPlayer1Cards(2);
	}

	public List<Card> getCards() {
		return cards;
	}

	public Card drawCard() {
		// TODO Auto-generated method stub
		return null;
	}
}
