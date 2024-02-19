package structures.basic;

import akka.actor.ActorRef;
import commands.BasicCommands;
import utils.OrderedCardLoader;

public class HumanPlayer extends Player {
		
	ActorRef out;
	private HumanDeck humanDeck;
	

    public HumanPlayer(int health, int mana) {
        super(health,mana); 
        this.hand=new Hand();
        this.humanDeck = new HumanDeck();
 //this.setHand(new Hand());alternative if setting to private initial value
 //       drawInitialCards(ActorRef out); 
    }


    public void drawInitialCards(ActorRef out) {
    	int handPosition = 1;
                
//                hand.addCard(humanDeck.getCards().get(0));
//                humanDeck.getCards().remove(i);
// alternative one in case we will need to track the cards in the deck       
                
		
		for (Card card : OrderedCardLoader.getPlayer1Cards(1)) {
			BasicCommands.drawCard(out, card, handPosition, 0);
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			hand.addCard(humanDeck.getCards().get(handPosition));
			
			handPosition++;
			BasicCommands.addPlayer1Notification(out, ""+hand.getCardAtIndex(1), 2);
			if (handPosition>3) break;
			

		}
    } 
    
}

