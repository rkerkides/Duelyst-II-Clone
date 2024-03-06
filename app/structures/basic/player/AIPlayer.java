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
		BasicCommands.addPlayer1Notification(out, "AI ends its turn", 2);
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
				if (!tile.isOccupied()) {
					movements.add(new PossibleMovement(unit, tile));
				}
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
					int distanceX = Math.abs(move.tile.getTilex() - enemy.getPosition().getTilex());
					int distanceY = Math.abs(move.tile.getTiley() - enemy.getPosition().getTiley());
					int distanceScore = 9 - (distanceX + distanceY);

					if (move.unit.getAttack() == 0) {
						// If unit has low attack, invert the score to prioritize moving away
						distanceScore = -(distanceScore);
					}

					if (move.unit.getName().equals("AI Avatar")) {
						// Prioritize keeping the avatar close to other friendly units

					}

					score += distanceScore;
					score += (20 - enemy.getHealth()); // More aggressively move towards lower health units

					if (enemy == gameState.getHuman().getAvatar()) {
						score += 5; // Prioritize attacking the primary human player unit
					}

					if (score > maxScore) {
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
				// Prioritize eliminating a unit by checking if the attack is lethal
				if (attack.tile.getUnit().getHealth() <= attack.unit.getAttack()) {
					attack.moveQuality = 10; // Assign the highest value for lethal attacks

					// Increase value for attacking the primary human player unit, unless it's by the AI's primary unit
				} else if (attack.tile.getUnit() == gameState.getHuman().getAvatar() && attack.unit != this.avatar) {
					attack.moveQuality = 8;

					// Value for attacking any unit not being the avatar by non-avatar AI units
				} else if (attack.tile.getUnit() != gameState.getHuman().getAvatar() && attack.unit != this.avatar) {
					attack.moveQuality = 5;

					// Generic attack value
				} else {
					attack.moveQuality = 0;
				}
				// Penalize units with no attack
				if (attack.unit.getAttack() == 0) {
					System.out.println("Unit " + attack.unit + " has no attack");
					attack.moveQuality = -1;
				}
			}

			System.out.println("Attack " + attack.unit + " value = " + attack.moveQuality);
		}
		return rankedAttacks;
	}

	private Set<PossibleSummon> rankSummons(ArrayList<PossibleSummon> summons) {
		System.out.println("Ranking possible summons...");
		if (summons == null) {
			return null;
		}

		Set<PossibleSummon> rankedSummons = new HashSet<>(summons);

		for (PossibleSummon summon : rankedSummons) {
			if (summon.card.getManacost() <= this.mana) {
				summon.moveQuality = 5;
			}
		}
		return rankedSummons;
	}

	private PossibleSummon findBestSummon(Set<PossibleSummon> summons) {
		System.out.println("AI finding best summon...");
		if (summons == null || summons.isEmpty()) {
			System.out.println("No available summons to evaluate.");
			return null;
		}

		int minValue = 0;
		PossibleSummon bestSummon = null;

		for (PossibleSummon summon : summons) {
			if (summon.moveQuality > minValue) {
				minValue = summon.moveQuality;
				bestSummon = summon;
			}
		}

		if (bestSummon != null) {
			System.out.println("Best action found: Tile " + bestSummon.tile + " and Unit " + bestSummon.card + " with value = " + bestSummon.moveQuality);
		} else {
			System.out.println("No summon meets the evaluation criteria.");
		}
		return bestSummon;
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

		int minValue = 0;
		PossibleAttack bestAttack = null;

		for (PossibleAttack attack : attacks) {
			if (attack.moveQuality > minValue) {
				minValue = attack.moveQuality;
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
			performCardActions();
			System.out.println("Went past card actions");
			performAttacks();
			performMovements();
		} catch (Exception e) {
			// Handle other exceptions or log them
		}
	}

	private void performCardActions() {
		while (true) {
			System.out.println("Performing card actions");
			ArrayList<Card> cards = (ArrayList<Card>) this.hand.getCards();
			System.out.println("Cards in hand: " + cards.size());
			if (cards.isEmpty()) {
				System.out.println("Cards is empty");
				return;
			}
			// Implement creature card summoning
			System.out.println("Creature card summoning");
			ArrayList<PossibleSummon> possibleSummons = returnAllSummons(gameState);
			if (possibleSummons.isEmpty()) {
				System.out.println("Summons is empty");
				return;
			}
			Set<PossibleSummon> rankedSummons = new HashSet<>(rankSummons(possibleSummons));
			PossibleSummon bestMove = findBestSummon(rankedSummons);
			if (bestMove != null) {
				gameState.gameService.removeCardFromHandAndSummonUnit(bestMove.card, bestMove.tile);
				System.out.println("Summoning unit " + bestMove.card.getCardname() + " on tile " + bestMove.tile.getTilex() + ", " + bestMove.tile.getTiley());
			} else {
				System.out.println("No summon found");
				return;
			}
		}
	}

	private ArrayList<PossibleSummon> returnAllSummons(GameState gameState) {
		ArrayList<PossibleSummon> summons = new ArrayList<>();
		for (Card card : this.hand.getCards()) {
			System.out.println("Checking card " + card.getCardname() + " for summoning");
			System.out.println("Card is creature: " + card.isCreature());
			System.out.println("Card manacost: " + card.getManacost());
			System.out.println("AIPlayer mana: " + this.mana);
			if (card.isCreature() && card.getManacost() <= this.mana) {
				Set<Tile> positions;
				positions = gameState.gameService.getValidSummonTiles();
				for (Tile tile : positions) {
					System.out.println("Returning summon for unit " + card.getCardname() + " on tile " + tile.getTilex() + ", " + tile.getTiley());
					if (!tile.isOccupied()) {
						System.out.println("Tile is not occupied");
						summons.add(new PossibleSummon(card, tile));
					}
				}
			}
		}
		return summons;
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
