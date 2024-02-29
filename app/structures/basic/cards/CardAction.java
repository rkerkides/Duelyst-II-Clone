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
		gameState.currentCardClicked = gameState.currentPlayer.getHand().getCardAtPosition(handPosition);

		// Mark the position of the card clicked
		gameState.currentCardPosition = handPosition;

    }

    public void creaturePreAction() {
        preAction();
		// Highlight the summon range of the current card clicked
		gameState.gameService.highlightSummonRange(gameState.currentCardClicked, gameState.getBoard(), gameState.getHuman());
    }

}
