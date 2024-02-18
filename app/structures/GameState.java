package structures;

import structures.basic.Board;
import structures.basic.Player;
import structures.basic.Unit;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	
	public boolean gameInitalised = false;
	
	public boolean something = false;
	// List to hold all registered listeners

	// Keep track of the player currently taking their turn
	public Player currentPlayer;

	// Keep track of the unit that is currently clicked
	public Unit currentUnitClicked;

	// Keep track of the last event that was processed
	public String lastEvent;

	// Entity objects that are part of the game state
	private Action action;
	private Board board;
	private Player humanPlayer;
	private Player aiPlayer;

	// Getters and Setters
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Player getHumanPlayer() {
		return humanPlayer;
	}

	public void setHumanPlayer(Player humanPlayer) {
		this.humanPlayer = humanPlayer;
	}

	public Player getAiPlayer() {
		return aiPlayer;
	}

	public void setAiPlayer(Player aiPlayer) {
		this.aiPlayer = aiPlayer;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(Player currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	
}
