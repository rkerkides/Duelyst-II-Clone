package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Board;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

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

	// Keep track of the aiAvatar that is currently clicked
	public Unit currentUnitClicked;

	// Keep track of the last event that was processed
	public String lastEvent;

	// Entity objects that are part of the game state
	public Action action;
	private Player human;
	private Player ai;
	private Board board;

	/**
	 * This function initialises all the assets
	 * Board, Player etc
	 * As well as tracking critical game states
	 * 
	 * @param out
	 */

	public void init(ActorRef out) {
		this.action = new Action(out);
		this.board = action.gameService.loadBoard();

		// Create the human and AI players
		this.human = new Player();
		this.ai = new Player();

		// Set the current player to the human player
		this.currentPlayer = human;


		// Place player1 avatar
		Tile playerAvatarTile = board.getTile(1, 2);
		Unit playerAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
		playerAvatar.setPositionByTile(playerAvatarTile);
		BasicCommands.drawUnit(out, playerAvatar, playerAvatarTile);
		playerAvatar.setOwner(human);
		playerAvatarTile.setUnit(playerAvatar);

		// Place player2 avatar
		Tile aiAvatarTile = board.getTile(7, 2);
		Unit aiAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);
		aiAvatar.setPositionByTile(aiAvatarTile);
		BasicCommands.drawUnit(out, aiAvatar, aiAvatarTile);
		aiAvatar.setOwner(ai);
		aiAvatarTile.setUnit(aiAvatar);
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
	 * Checks and see if the game has ended
	 * If so it will send the apropiate notifcation
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
