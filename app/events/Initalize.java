package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import demo.CommandDemo;
import demo.Loaders_2024_Check;
import structures.Action;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		// hello this is a change
		
		gameState.gameInitalised = true;
		
		gameState.something = true;

		// Create Action object and add it to the game state
		Action action = new Action(out);
		gameState.setAction(action);

		// Load the game board and add it to the game state
		Board board = new Board();

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile tile = BasicObjectBuilders.loadTile(i, j);
				board.setTile(tile, i, j);
				BasicCommands.drawTile(out, tile, 0);
			}
		}

		gameState.setBoard(board);


		// Create players and add to game state
		Player humanPlayer = new Player(20, 1);
		Player aiPlayer = new Player(20, 2);
		gameState.setHumanPlayer(humanPlayer);
		gameState.setAiPlayer(aiPlayer);


		// User 1 makes a change
		gameState.setCurrentPlayer(humanPlayer);
		// Place player1 avatar
		BasicCommands.addPlayer1Notification(out, "Player 1's Turn", 2);
		Tile tile = board.getTile(2, 3);
		Unit unit = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
		unit.setPositionByTile(tile);
		BasicCommands.drawUnit(out, unit, tile);
		unit.setOwner(humanPlayer);
		tile.setUnit(unit);


	}

}


