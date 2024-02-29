package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.player.AIPlayer;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;

import java.util.List;
import java.util.Stack;

/**
 * This class can be used to hold information about the on-going game. Its
 * created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	public boolean gameInitalised = false;

	// Keep track of the player currently taking their turn
	private Player currentPlayer;

	// Keep track of the unit that is currently clicked
	private Unit currentUnitClicked;
	// Keep track of the card that is currently clicked
	private Card currentCardClicked;
	// Keep track of the position of the card that is currently clicked
	private int currentCardPosition;

	// Keep track of the previous plays of the current turn
	private Stack<String> previousPlays;

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

		// Initiliaze list of previous plays
		this.previousPlays = new Stack<>();

		// Create the human and AI players
		this.human = new HumanPlayer();
		this.ai = new AIPlayer();

		// Health initialised to 20
		gameService.updatePlayerHealth(human,20);
		gameService.updatePlayerHealth(ai,20);

		// Player mana initialised to 2
		gameService.updatePlayerMana(human, 2);

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
		if (this.currentPlayer == this.human) {
			this.currentPlayer = this.ai;
		} else {
			this.currentPlayer = this.human;
		}
	}

	public void endTurn(){
		if (this.currentPlayer == this.human){
			handleCardManagement();
		}
		currentPlayer.incrementTurn();
		this.gameService.updatePlayerMana(currentPlayer, 0);
		switchCurrentPlayer();
		this.gameService.updatePlayerMana(currentPlayer, currentPlayer.getTurn() + 1);
	}

	public void handleCardManagement() {
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

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(Player player) {
		currentPlayer = player;
	}

	public Unit getCurrentUnitClicked() {
		return currentUnitClicked;
	}

	public void setCurrentUnitClicked(Unit unit) {
		currentUnitClicked = unit;
	}

	public Card getCurrentCardClicked() {
		return currentCardClicked;
	}

	public void setCurrentCardClicked(Card card) {
		currentCardClicked = card;
	}

	public int getCurrentCardPosition() {
		return currentCardPosition;
	}

	public void setCurrentCardPosition(int position) {
		currentCardPosition = position;
	}

	public Stack<String> getPreviousPlays() {
		return previousPlays;
	}

	public void addPreviousPlay(String play) {
		previousPlays.add(play);
	}

	public void removePreviousPlay() {
		previousPlays.pop();
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
