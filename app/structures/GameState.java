package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Board;
import structures.basic.Player;

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

	private Player human;
	private Player ai;
	private Board board;

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

	public void setHuman(Player human) {
		this.human = human;
	}

	public Player getAi() {
		return this.ai;
	}

	public void setAi(Player ai) {
		this.ai = ai;
	}

	public void endGame(ActorRef out) {
		if (this.ai != null && this.ai.getHealth() == 0) {
			BasicCommands.addPlayer1Notification(out, "You Won!", 1000);
		} else if (this.human != null && this.human.getHealth() == 0) {
			BasicCommands.addPlayer1Notification(out, "You Lost", 1000);
		}
	}

}
