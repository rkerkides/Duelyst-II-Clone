package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import structures.GameState;
import structures.basic.*;
import structures.basic.cards.BeamShock;
import structures.basic.cards.Card;

import java.util.*;

public class AIPlayer extends Player {
	private final GameState gameState;

	public AIPlayer(GameState gameState) {
		super();
		this.gameState = gameState;
	}

	// restrained to just end-turn to facilitate testing before implementing AI
	public void takeTurn(ActorRef out, JsonNode message) {
		
		
		System.out.println(BeamShock.stunnedUnit+" stunned unit is not stunned anymore");
		BeamShock.stunnedUnit=null;
		
		// make best move
		makeBestMove();
		
		for (Card card : this.hand.getCards()) {
			System.out.println("Card: " + card.getCardname());
		}

		// ends turn
		EndTurnClicked endTurn = new EndTurnClicked();
		BasicCommands.addPlayer1Notification(out, "   AI ends its turn", 2);
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
				if (summon.card.getManacost() < summon.card.getBigCard().getAttack()) {
					summon.moveQuality = 6;
				}
				else {
					summon.moveQuality = 5;
				}
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
			System.out.println("Best summon found: Tile " + bestSummon.tile.getTilex() + " " + bestSummon.tile.getTiley() + " and Unit " + bestSummon.card.getCardname() + " with value = " + bestSummon.moveQuality);
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



	private void makeBestMove() {
		try {
			performCardActions();
			performAttacks();
			performMovements();
		} catch (Exception e) {
			// Handle other exceptions or log them
		}
	}

	private void performCardActions() {
		while (true) {
			ArrayList<Card> cards = (ArrayList<Card>) this.hand.getCards();
			if (cards.isEmpty()) {
				System.out.println("Cards is empty");
				return;
			}
			PossibleSpell bestSpell = returnBestSpell();
			PossibleSummon bestSummon = returnBestSummon();
			if (bestSpell != null && bestSummon != null) {
				if (bestSpell.moveQuality > bestSummon.moveQuality) {/*
					gameState.gameService.removeCardFromHandAndCastSpell(bestSpell.card, bestSpell.tile);*/
				} else {
					gameState.gameService.removeCardFromHandAndSummon(bestSummon.card, bestSummon.tile);
				}
			} else if (bestSpell != null) {
				gameState.gameService.removeFromHandAndCast(gameState, bestSpell.card, bestSpell.tile);
			} else if (bestSummon != null) {
				gameState.gameService.removeCardFromHandAndSummon(bestSummon.card, bestSummon.tile);
			} else {
				return;
			}
		}
	}

	// Returns the best summon to perform
	private PossibleSummon returnBestSummon() {
		ArrayList<PossibleSummon> possibleSummons = returnAllSummons(gameState);
		if (possibleSummons.isEmpty()) {
			System.out.println("Summons is empty");
			return null;
		}
		Set<PossibleSummon> rankedSummons = new HashSet<>(rankSummons(possibleSummons));
		PossibleSummon bestSummon = findBestSummon(rankedSummons);
		return bestSummon;
	}

	// Returns the best spell card to cast
	private PossibleSpell returnBestSpell() {
		// Implement spell card casting
		ArrayList<PossibleSpell> allSpells = returnAllSpells(gameState);
		if (allSpells.isEmpty()) {
			System.out.println("Spells is empty");
			return null;
		}
		System.out.println("Spells is not empty, ranking spells to return the best one...to cast");
		Set<PossibleSpell> rankedSpells = new HashSet<>(rankSpells(allSpells));
		PossibleSpell bestSpell = findBestSpell(rankedSpells);
		System.out.println("Best spell found: " + bestSpell.toString());
		return bestSpell;
	}

	private Set<PossibleSpell> rankSpells(ArrayList<PossibleSpell> possibleSpells) {
	    // Create a comparator to compare spells based on their effectiveness
	    Comparator<PossibleSpell> spellComparator = Comparator.comparingInt(spell -> {
	        if (spell.card.getCardname().equals("Sundrop Elixir")) {
	            // Sundrop Elixir should be ranked higher because it heals for 4 health at a low cost
	            return 4 * spell.card.getManacost();
	        } else if (spell.card.getCardname().equals("True Strike")) {
	            // True Strike deals 2 damage at a low cost
	            return 2 * spell.card.getManacost();
	        } else if (spell.card.getCardname().equals("Beam Shock")) {
	            return 5; // Arbitrarily assigning a value of 5 for Beam Shock
	        } else {
	            // Default value for other spells
	            return 0;
	        }
	    });

	    // Sort the spells based on the comparator
	    possibleSpells.sort(spellComparator);
	    
        System.out.println("Spells ranked:");
        
        Iterator<PossibleSpell> iterator = possibleSpells.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
	    // Return a HashSet of the sorted spells
	    return new HashSet<>(possibleSpells);
	    
	}


	private PossibleSpell findBestSpell(Set<PossibleSpell> rankedSpells) {
	    // Initialise variables to keep track of the best spell and its rank
	    PossibleSpell bestSpell = null;
	    int bestRank = Integer.MIN_VALUE;

	    // Iterate over the set of ranked spells
	    for (PossibleSpell spell : rankedSpells) {
	        // Check if the current spell has a higher rank than the best spell found so far
	        if (getSpellRank(spell) > bestRank) {
	            // Update the best spell and its rank
	            bestSpell = spell;
	            bestRank = getSpellRank(spell);
	        }
	    }
	    System.out.println("Best spell found: " + bestSpell.toString() + " with rank " + bestRank);

	    // Return the best spell found
	    return bestSpell;
	}

	// Helper method to calculate the rank of a spell
	private int getSpellRank(PossibleSpell spell) {
	    if (spell.card.getCardname().equals("Sundrop Elixir")) {
	        return 4 * spell.card.getManacost();
	    } else if (spell.card.getCardname().equals("True Strike")) {
	        return 2 * spell.card.getManacost();
	    } else if (spell.card.getCardname().equals("Beam Shock")) {
	        return 5;
	    } else {
	        return 0;
	    }
	}



	private ArrayList<PossibleSpell> returnAllSpells(GameState gameState) {
		ArrayList<PossibleSpell> spells = new ArrayList<>();
		for (Card card : this.hand.getCards()) {
			if (!card.isCreature()) {
				Set<Tile> positions;
				positions = gameState.gameService.getSpellRange(card);
				for (Tile tile : positions) {
					spells.add(new PossibleSpell(card, tile));
					System.out.println("Adding spell " + card.getCardname() + " to list of possible spells");
				}
			}
		}
		System.out.println(Arrays.toString(spells.toArray())+ "Spells");
		
		return spells;
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
					if (!tile.isOccupied()) {
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
				gameState.gameService.attack(bestAttack.unit, bestAttack.tile.getUnit());
			}
		}
	}
	private void performMovements() {
	    ArrayList<PossibleMovement> possibleMoves = returnAllMovements(gameState);
	    if (possibleMoves.isEmpty()) {
	        System.out.println("No more moves left on the board");
	        return;
	    }

	    for (PossibleMovement move : possibleMoves) {
	        if (move.unit.movedThisTurn()) {
	            continue; // Skip this movement if the unit has already moved
	        }

	        // Check if the destination tile is unoccupied
	        if (!move.tile.isOccupied()) {
	            // Perform the move
	            gameState.gameService.updateUnitPositionAndMove(move.unit, move.tile);

	            // Mark the unit as moved
	            move.unit.setMovedThisTurn(true);

	            // Check for adjacent enemy units and attack if found
	            performAttacksAfterMovement(move.unit);
	        }
	    }
	}

	private void performAttacksAfterMovement(Unit movedUnit) {
	    // Get the current tile of the moved unit
	    Tile currentTile = movedUnit.getCurrentTile(gameState.getBoard());

	    // Get adjacent tiles
	    List<Tile> adjacentTiles = gameState.getBoard().getAdjacentTiles(currentTile);

	    // Check each adjacent tile for enemy units
	    for (Tile tile : adjacentTiles) {
	        if (tile.isOccupied() && tile.getUnit().getOwner() != movedUnit.getOwner()) {
	            // Perform the attack
	            gameState.gameService.attack(movedUnit, tile.getUnit());

	            // Break the loop after performing one attack
	            return;
	        }
	    }
	}





	private void performAttacks(boolean postMove) {
	    ArrayList<PossibleAttack> attacks = returnAllAttacks(gameState);
	    if (attacks.isEmpty()) {
	        System.out.println("No more actions left on the board");
	        return;
	    }

	    for (PossibleAttack attack : attacks) {
	        if (postMove && !attack.unit.movedThisTurn()) continue; // Skip if the unit has moved but we're not in post-move mode
	        if (gameState.gameService.isWithinAttackRange(attack.unit.getCurrentTile(gameState.getBoard()), attack.tile)) {
	            gameState.gameService.attack(attack.unit, attack.tile.getUnit());
	        }
	    }
	}

//	private void performMovements() {
//		while (true) {
//			ArrayList<PossibleMovement> possibleMoves = returnAllMovements(gameState);
//			if (possibleMoves == null || possibleMoves.isEmpty()) {
//				System.out.println("No more moves left on the board");
//				return;
//			}
//
//			try { Thread.sleep(1000); } catch (InterruptedException e) {
//				System.out.println("AIPlayer interrupted");
//			}
//
//			Set<PossibleMovement> rankedMovements = new HashSet<>(rankMovements(possibleMoves));
//			PossibleMovement bestMove = findBestMovement(rankedMovements);
//			gameState.gameService.updateUnitPositionAndMove(bestMove.unit, bestMove.tile);
//		}
//	}


	@Override
	public String toString() {
		return "AIPlayer";
	}

}
