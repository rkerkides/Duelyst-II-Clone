package structures.basic.cards;

import structures.GameState;

public class CardAction {
    GameState gameState;
    int handPosition;
    public CardAction(GameState gameState, int handPosition){
        this.gameState = gameState;
        this.handPosition = handPosition;
    };
    public void preAction() {
		// Set the current card clicked to the card at the specified position in the player's hand
		gameState.gameService.setCurrentCardClickedAndHighlight(handPosition);
    }

    public void creaturePreAction() {
        preAction();

        Card currentCard = gameState.getCurrentCardClicked();

		// Highlight the summon range of the current card clicked
		gameState.gameService.highlightSummonRange(currentCard, gameState.getHuman());



        // Push the current card clicked to the action history
        gameState.getActionHistory().push(currentCard);

        // For debug
        System.out.println("Pushed to action history: " + currentCard.getCardname() + " " + currentCard.getId());
    }

}
