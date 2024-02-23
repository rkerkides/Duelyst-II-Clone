package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;

/**
 * This class can be used to hold information about the on-going game. Its
 * created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	public boolean gameInitalised = false;

	public boolean something = false;

	// Keep track of the player currently taking their turn
	public Player currentPlayer;

	// Keep track of the unit that is currently clicked
	public Unit currentUnitClicked;
	// Keep track of the card that is currently clicked
	public Card currentCardClicked;

	// Keep track of the last event that was processed
	public String lastEvent;

	// Entity objects that are part of the game state
	public GameService gameService;
	private HumanPlayer human;

	private Player ai;
	private Board board;

	/**
	 * This function initialises all the assets Board, Player etc As well as
	 * tracking critical game states
	 * 
	 * @param out
	 */

	public void init(ActorRef out) {
		this.gameService = new GameService(out);
		this.board = gameService.loadBoard();

		// Create the human and AI players
		this.human = new HumanPlayer();
		this.ai = new AIPlayer();

		// Create the human and AI avatars
		gameService.loadAvatar(board, human);
		gameService.loadAvatar(board, ai);

		// Set the current player to the human player
		this.currentPlayer = human;

		//Drawing initial 3 cards from the deck for the game start
		gameService.drawCards(human,3);
		System.out.println("Human hand: " + human.getHand().getCards());
		gameService.drawCards(ai,3);
		System.out.println(human.getHand().getCards());
	}

	// Switch the current player
	public void switchCurrentPlayer() {
		if (this.currentPlayer == human) {
			this.currentPlayer = ai;
		} else {
			this.currentPlayer = human;
		}
	}

	public void endTurn(){
		if (currentPlayer.equals(human)){
			handleCardManagement(this.currentPlayer);
		}
		switchCurrentPlayer();
	}

	public void handleCardManagement(Player currentPlayer) {
		if (currentPlayer.getHand().getNumberOfCardsInHand() >= 6) {
			// Discard the top card from the hand if it's at maximum size.
			currentPlayer.getDeck().drawCard();
		} else {
			// The hand is not full, draw a new card.
			gameService.drawCards(currentPlayer, 1);
		}
	}



	// Getters and Setters
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Player getHuman() {
		return this.human;
	}

	public Player getAi() {
		return this.ai;
	}

	/**
	 * Checks and see if the game has ended If so it will send the apropiate
	 * notifcation
	 * 
	 * @param out
	 */
	public void endGame(ActorRef out) {
		if (this.ai != null && this.ai.getHealth() == 0) {
			BasicCommands.addPlayer1Notification(out, "You Won!", 1000);
		} else if (this.human != null && this.human.getHealth() == 0) {
			BasicCommands.addPlayer1Notification(out, "You Lost", 1000);
		}
	}
}
