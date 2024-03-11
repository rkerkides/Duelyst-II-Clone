package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameState;

public class CardAction {

  ActorRef out;
  GameState gameState;
  int handPosition;

  public CardAction(GameState gameState, int handPosition) {
    this.gameState = gameState;
    this.handPosition = handPosition;
    preAction();

  }

  private void preAction() {

    // CLear all highlighted tiles
    gameState.gameService.removeHighlightFromAll();

    // Set the current card clicked to the card at the specified position in the
    // player's hand
    gameState.gameService.setCurrentCardClickedAndHighlight(handPosition);
  }

  public void creaturePreAction() {
   
	Card currentCard = gameState.getCurrentCardClicked();

    // Highlight the summon range of the current card clicked
    // Highlight the summon range of the current card clicked
    gameState.gameService.highlightSummonRange();

    // Push the current card clicked to the action history
    gameState.getActionHistory().push(currentCard);

    // For debug
  }


    public void spellPreAction() {
        	preAction();
        	
            Card currentCard = gameState.getCurrentCardClicked();
            if(!currentCard.isCreature) {
            	gameState.gameService.highlightSpellRange(currentCard, gameState.getHuman());

                // Push the current card clicked to the action history
                gameState.getActionHistory().push(currentCard);

                // For debug
            }

        
        }
    

  public void spellAction(Card card) {

  }



  public void badOmen() {

  }

  public void hornOfForSaken() {

  }

  public void gloomChaser() {

  }

  public void shadowWatcher() {

  }

  public void wraithlingSwarm() {

  }

  public void nightSorrowAssasin() {

  }

  public void rockPulversier() {

  }

  public void darkTerminus() {

  }

  public void bloodMoonPriestess() {

  }

  public void shadowDancer() {

  }

}
