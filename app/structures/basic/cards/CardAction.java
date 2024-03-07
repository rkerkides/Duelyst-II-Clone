package structures.basic.cards;

import structures.GameState;

public class CardAction {
    GameState gameState;
    int handPosition;
    public CardAction(GameState gameState, int handPosition){
        this.gameState = gameState;
        this.handPosition = handPosition;
    };

    public void creaturePreAction() {
        Card currentCard = gameState.getCurrentCardClicked();

		// Highlight the summon range of the current card clicked
		gameState.gameService.highlightSummonRange();



        // Push the current card clicked to the action history
        gameState.getActionHistory().push(currentCard);

        // For debug
        System.out.println("Pushed to action history: " + currentCard.getCardname() + " " + currentCard.getId());
    }

    public void spellPreAction() {

    }

}
