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
		gameState.gameService.setCurrentCardClicked(handPosition);
    }

    public void creaturePreAction() {
        preAction();
		// Highlight the summon range of the current card clicked
		gameState.gameService.highlightSummonRange(gameState.getCurrentCardClicked(), gameState.getBoard(), gameState.getHuman());
    }

}
