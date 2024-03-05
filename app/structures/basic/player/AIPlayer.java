package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import structures.GameState;
import structures.basic.PossibleMove;
import structures.basic.PossibleMovement;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class AIPlayer extends Player {

	private final GameState gameState;

	public AIPlayer(GameState gameState) {
		super();
		this.gameState = gameState;
	}

	// restrained to just end-turn to facilitate testing before implementing AI
	public void takeTurn(ActorRef out, JsonNode message) {
		// make best move
		makeBestMove(out);

		// ends turn
		EndTurnClicked endTurn = new EndTurnClicked();
		BasicCommands.addPlayer1Notification(out, "AI takes turn (and immediately ends it)", 2);
		endTurn.processEvent(out, gameState, message);
	}

	private ArrayList<PossibleMovement> returnAllMovements(GameState gameState) {
		System.out.println("Returning all movements for AIPlayer");
		ArrayList<PossibleMovement> movements = new ArrayList<>();

		for (Unit unit : this.units) {
			Set<Tile> positions = new HashSet<>();

			if (unit.attackedThisTurn() && unit.movedThisTurn()) {
				continue;
			} else if (!unit.movedThisTurn() && !unit.attackedThisTurn()) {
				positions = gameState.gameService.calculateValidMovement(gameState.getBoard().getTiles(), unit);

			}
			for (Tile tile : positions) {
				movements.add(new PossibleMovement(unit, tile));
			}
		}

		return movements;
	}

	private Set<PossibleMovement> rankMovements(ArrayList<PossibleMovement> movements) {
		System.out.println("Ranking possible movements...");
		if (movements == null) {
			return null;
		}

		Set<PossibleMovement> moves = new HashSet<>(movements);

		for (PossibleMovement move : moves) {
			if (!move.unit.movedThisTurn() && !move.unit.attackedThisTurn()) {
				ArrayList<Unit> enemyUnits = (ArrayList<Unit>) gameState.getHuman().getUnits();

				int maxScore = 0; // Initialize to minimum possible score

				for (Unit enemy : enemyUnits) {
					int score = 0;
					// Calculate distance score inversely; closer enemies should increase score
					score += (9 - Math.abs(move.tile.getTilex() - enemy.getPosition().getTilex()));
					score += (9 - Math.abs(move.tile.getTiley() - enemy.getPosition().getTiley()));
					// Lower health enemies contribute more to the score
					score += (20 - enemy.getHealth());
					if (!enemy.equals(enemyUnits.get(0))) {
						score += 5; // Moving towards the avatar increases score
					}
					if (score > maxScore && !move.tile.isOccupied()) {
						move.moveQuality = score; // Higher score for more desirable moves
						maxScore = score;
					}
				}
			}
		}
		return moves;
	}

	private PossibleMovement findBestMovement(Set<PossibleMovement> moves) {
		System.out.println("AI finding best movement...");
		Integer minValue = 0;
		PossibleMovement bestMove = null;

		for (PossibleMovement move : moves) {
			if (move.moveQuality > minValue) { // Looking for a higher score
				minValue = move.moveQuality;
				bestMove = move;
			}
		}
		if (bestMove != null) {
			System.out.println("Move " + bestMove.unit + " value = " + bestMove.moveQuality);
		} else {
			System.out.println("No available moves");
		}
		return bestMove;
	}




	private void makeBestMove(ActorRef out) {
		// Your Minimax algorithm implementation will go here
		// This is a placeholder for demonstration
		int bestScore = Integer.MIN_VALUE;
		String bestMove = "";

		// Example: Loop through all possible moves
		for (String move : getAllPossibleMoves(gameState)) {
			// Apply the move temporarily
			applyMove(gameState, move);

			// Recursively get the score of the move
			int score = minimax( 0, false);

			// Undo the move
			undoMove(gameState, move);

			// Update best score and move
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
		}

		// Apply the best move
		applyMove(gameState, bestMove);
		BasicCommands.addPlayer1Notification(out, "AI makes its move: " + bestMove, 2);
	}

	// Example Minimax algorithm (needs to be implemented according to your game logic)
	private int minimax(int depth, boolean isMaximizingPlayer) {
		// Base case: check for game end
		if (gameOver(gameState)) {
			return evaluateGameState(gameState);
		}

		if (isMaximizingPlayer) {
			int bestScore = Integer.MIN_VALUE;
			// Loop through all possible moves
			for (String move : getAllPossibleMoves(gameState)) {
				// Apply the move
				applyMove(gameState, move);
				// Recurse
				int score = minimax(depth + 1, false);
				// Undo the move
				undoMove(gameState, move);
				// Update best score
				bestScore = Math.max(bestScore, score);
			}
			return bestScore;
		} else {
			int bestScore = Integer.MAX_VALUE;
			// Loop through all possible moves
			for (String move : getAllPossibleMoves(gameState)) {
				// Apply the move
				applyMove(gameState, move);
				// Recurse
				int score = minimax( depth + 1, true);
				// Undo the move
				undoMove(gameState, move);
				// Update best score
				bestScore = Math.min(bestScore, score);
			}
			return bestScore;
		}
	}

	// Placeholder methods for getAllPossibleMoves, applyMove, undoMove, gameOver, evaluateGameState
	private List<String> getAllPossibleMoves(GameState gameState) {
		// This method should return a list of all possible moves. Each move can be represented as a string or custom object.
		// For simplicity, let's assume it's a list of strings.
		List<String> possibleMoves = new ArrayList<>();
		// Add logic to populate this list based on the current game state
		return possibleMoves;
	}

	private void applyMove(GameState gameState, String move) {
		// Apply the move to the gameState. You'll need to parse the move string and apply its effects.
	}

	private void undoMove(GameState gameState, String move) {
		// Revert the move applied to the gameState.
	}

	private boolean gameOver(GameState gameState) {
		// Determine if the game has ended
		return gameState.isGameFinished;
	}

	private int evaluateGameState(GameState gameState) {
		// Evaluate the game state from the AI's perspective.
		// Simple heuristic: difference in health between AI and human player
		return gameState.getAi().getHealth() - gameState.getHuman().getHealth();
	}


	@Override
	public String toString() {
		return "AIPlayer";
	}

}
