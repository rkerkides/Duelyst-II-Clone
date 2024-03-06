package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import structures.GameState;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.cards.SpellCard;

import java.util.*;

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

	private ArrayList<PossibleAttack> returnAllAttacks(GameState gameState) {
		System.out.println("Returning all attacks for AIPlayer");
		ArrayList<PossibleAttack> attacks = new ArrayList<>();

		for (Unit unit : this.units) {
			if (unit.attackedThisTurn()) {
				continue;
			}
			Set<Tile> targets = gameState.gameService.calculateAttackTargets(unit);

			for (Tile tile : targets) {
				attacks.add(new PossibleAttack(unit, tile));
			}
		}
		return attacks;
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

	private Set<PossibleAttack> rankAttacks(ArrayList<PossibleAttack> attacks) {
		System.out.println("Ranking possible attacks...");
		if (attacks == null) {
			return null;
		}

		Set<PossibleAttack> rankedAttacks = new HashSet<>(attacks);

		for (PossibleAttack attack : rankedAttacks) {
			if (!attack.unit.movedThisTurn() && !attack.unit.attackedThisTurn()) {
				attack.moveQuality = 1;

				// Prioritize eliminating a unit by checking if the attack is lethal
				if (attack.tile.getUnit().getHealth() <= attack.unit.getAttack()) {
					attack.moveQuality = 10; // Assign the highest value for lethal attacks
					// Increase value for attacking the primary human player unit, unless it's by the AI's primary unit
				} else if (attack.tile.getUnit() == gameState.getHuman().getAvatar() && attack.unit != this.avatar) {
					attack.moveQuality = 8;
					// Value for attacking any unit not being the avatar by non-avatar AI units
				} else if (attack.tile.getUnit() != gameState.getHuman().getAvatar() && attack.unit != this.avatar) {
					attack.moveQuality = 5;
				} else if (attack.unit.getAttack() == 0) {
					attack.moveQuality = -5;
				}
			}

		}
		return rankedAttacks;
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


	private PossibleAttack findBestAttack(Set<PossibleAttack> attacks) {
		System.out.println("AI finding best attack...");
		if (attacks == null || attacks.isEmpty()) {
			System.out.println("No available attacks to evaluate.");
			return null;
		}

		int maxValue = 0;
		PossibleAttack bestAttack = null;

		for (PossibleAttack attack : attacks) {
			if (attack.moveQuality > maxValue) {
				maxValue = attack.moveQuality;
				bestAttack = attack;
			}
		}

		if (bestAttack != null) {
			System.out.println("Best action found: Tile " + bestAttack.tile + " and Unit " + bestAttack.unit + " with value = " + bestAttack.moveQuality);
		} else {
			System.out.println("No attack meets the evaluation criteria.");
		}
		return bestAttack;
	}



	private void makeBestMove(ActorRef out) {
		try {
			performAttacks();
			performMovements();
		} catch (Exception e) {
			// Handle other exceptions or log them
		}
	}


	private void performAttacks() {
		while (true) {
			ArrayList<PossibleAttack> attacks = returnAllAttacks(gameState);
			if (attacks == null || attacks.isEmpty()) {
				System.out.println("No more actions left on the board");
				break;
			}

			Set<PossibleAttack> rankedAttacks = new HashSet<>(rankAttacks(attacks));
			PossibleAttack bestAttack = findBestAttack(rankedAttacks);

			if (bestAttack == null || bestAttack.unit.attackedThisTurn()) {
				return;
			}
			if (gameState.gameService.isWithinAttackRange(bestAttack.unit.getCurrentTile(gameState.getBoard()), bestAttack.tile)) {
				System.out.println("Attacking unit on tile " + bestAttack.tile.getTilex() + ", " + bestAttack.tile.getTiley());
				gameState.gameService.adjacentAttack(bestAttack.unit, bestAttack.tile.getUnit());
			}
		}
	}

	private void performMovements() {
		while (true) {
			ArrayList<PossibleMovement> possibleMoves = returnAllMovements(gameState);
			if (possibleMoves == null || possibleMoves.isEmpty()) {
				System.out.println("No more moves left on the board");
				return;
			}

			try { Thread.sleep(1000); } catch (InterruptedException e) {
				System.out.println("AIPlayer interrupted");
			}

			Set<PossibleMovement> rankedMovements = new HashSet<>(rankMovements(possibleMoves));
			PossibleMovement bestMove = findBestMovement(rankedMovements);
			gameState.gameService.updateUnitPositionAndMove(bestMove.unit, bestMove.tile);
		}
	}

	private Set<SpellCard> identifySpellCardsInHand() {
		Set<SpellCard> availableSpellCards = new HashSet<>();
		// Loop through each card in the AI player's hand
		for (Card card : this.hand.getCards()) {
			// Check if the card is a spell card and if there's enough mana to play it
			if (card instanceof SpellCard && this.mana >= card.getManacost()) {
				availableSpellCards.add((SpellCard) card);
			}
		}
		return availableSpellCards;
	}

	private HashMap<SpellCard, ArrayList<PossibleSpell>> generatePossibleSpells(Set<SpellCard> spellCards) {
		HashMap<SpellCard, ArrayList<PossibleSpell>> spellActions = new HashMap<>();
		// Loop through each identified spell card
		for (SpellCard card : spellCards) {
			ArrayList<PossibleSpell> possibleSpells = new ArrayList<>();
			// Determine valid targets based on the spell card's characteristics
			Set<Tile> validTargets = gameState.gameService.calculateSpellTargets(card);
			// Create a possible spell for each valid target
			for (Tile target : validTargets) {
				if (card.isValidTarget(target, gameState)) {
					possibleSpells.add(new PossibleSpell(card, target));
				}
			}
			if (!possibleSpells.isEmpty()) {
				spellActions.put(card, possibleSpells);
			}
		}
		return spellActions;
	}
	@Override
	public String toString() {
		return "AIPlayer";
	}

}
