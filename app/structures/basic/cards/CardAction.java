package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameService;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.player.Hand;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

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
    System.out.println("Pushed to action history: " + currentCard.getCardname() + " " + currentCard.getId());
  }

  public void spellAction(Card card) {

  }

  public void spellPreAction() {

    Card currentCard = gameState.getCurrentCardClicked();

    // Highlight the summon range of the current card clicked
    // Highlight the summon range of the current card clicked
    gameState.gameService.highlightSpellRange(currentCard, gameState.getCurrentPlayer());

    // Push the current card clicked to the action history
    gameState.getActionHistory().push(currentCard);

    // For debug
    System.out.println("Pushed to action history: " + currentCard.getCardname() + " " + currentCard.getId());
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
