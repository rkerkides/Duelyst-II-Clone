package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.cards.ShadowWatcher;
import structures.basic.cards.Wraithling;
import structures.basic.player.Hand;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import structures.basic.cards.BadOmen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static utils.BasicObjectBuilders.loadUnit;

public class GameService {
	
	private final ActorRef out;
	private final GameState gs;


	public GameService(ActorRef out, GameState gs) {
		this.out = out;
		this.gs = gs;
	}

	public void updatePlayerHealth(Player player, int newHealth) {
		// Set the new health value on the player object first
		player.setHealth(newHealth);

		// Now update the health on the frontend using the BasicCommands
		if (player instanceof HumanPlayer) {
			BasicCommands.setPlayer1Health(out, player);
		} else {
			BasicCommands.setPlayer2Health(out, player);
		}
	}

	public void updatePlayerMana(Player player, int newMana) {
		// Set the new mana value on the player object first
		player.setMana(newMana);

		// Now update the mana on the frontend using the BasicCommands
		if (player instanceof HumanPlayer) {
			BasicCommands.setPlayer1Mana(out, player);
		} else {
			BasicCommands.setPlayer2Mana(out, player);
		}
	}

	// initial board setup
	public Board loadBoard() {
		Board board = new Board();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile tile = BasicObjectBuilders.loadTile(i, j);
				tile.setHighlightMode(0);
				board.setTile(tile, i, j);
				BasicCommands.drawTile(out, tile, 0);
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return board;
	}


	public void loadAvatar(Board board, Player player) {
		// check if player is human or AI
		Tile avatarTile;
		Unit avatar;
		if (player instanceof HumanPlayer) {
			avatarTile = board.getTile(1, 2);
			avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
			avatar.setName("Player Avatar");

		} else {
			avatarTile = board.getTile(7, 2);
			avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);
			avatar.setName("AI Avatar");
		}
		avatar.setPositionByTile(avatarTile);
		avatarTile.setUnit(avatar);
		BasicCommands.drawUnit(out, avatar, avatarTile);
		avatar.setOwner(player);
		player.setAvatar(avatar);
		player.addUnit(avatar);
		gs.addToTotalUnits(1);
		updateUnitHealth(avatar, 20);
		updateUnitAttack(avatar, 2);
		avatar.setHealth(20);
		avatar.setAttack(2);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitHealth(out, avatar, 20);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitAttack(out, avatar, 2);
	}


	
	public void updateUnitHealth(Unit unit, int newHealth) {

		if (newHealth > 20) {
			return;
		}

		if (unit.getName().equals("Player Avatar") && unit.getHealth() > newHealth){
			gs.getHuman().setRobustness(gs.getHuman().getRobustness()-1);
		}
		
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (newHealth <= 0) {
			performUnitDeath(unit);
			return;
		}
		if (unit.getHealth() > newHealth && unit.getName().equals("AI Avatar")) {
			zeal();
		}
		unit.setHealth(newHealth);
		BasicCommands.setUnitHealth(out, unit, newHealth);
		if (unit.getName().equals("Player Avatar") || unit.getName().equals("AI Avatar")) {
			updatePlayerHealth(unit.getOwner(), newHealth);
		}
	}

	// Update a unit's attack on the board
	public void updateUnitAttack(Unit unit, int newAttack) {
		if (newAttack <= 0) {
			return;
		} else if (newAttack > 20) {
			newAttack = 20;
		}

		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		unit.setAttack(newAttack);
		BasicCommands.setUnitAttack(out, unit, newAttack);
	}


	public void performUnitDeath(Unit unit) {
		//invoke Shadow Watcher Deathwatch ability
		ShadowWatcher.ShadowWatcherDeathwatch(out, gs, this);
		// Check for Bad Omen units after a unit dies
		BadOmen.BadOmenDeathwatch(out, gs, this);
		// remove unit from board
		unit.getCurrentTile(gs.getBoard()).removeUnit();
		unit.setHealth(0);
		Player owner = unit.getOwner();
		owner.removeUnit(unit);
		unit.setOwner(null);
		gs.removeFromTotalUnits(1);
		System.out.println("unit removed from totalunits");
		BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.death);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.deleteUnit(out, unit);


		if (unit.getName().equals("Player Avatar") || unit.getName().equals("AI Avatar")) {
			updatePlayerHealth(owner, 0);
		}

	}

	// remove highlight from all tiles
	public void removeHighlightFromAll() {
		Board board = gs.getBoard();
		Tile[][] tiles = board.getTiles();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if (tiles[i][j].getHighlightMode() != 0) {
					Tile currentTile = tiles[i][j];
					currentTile.setHighlightMode(0);
					BasicCommands.drawTile(out, currentTile, 0);
				}
			}
		}
		if (!gs.getHuman().getHand().getCards().isEmpty()) {
			notClickingCard();
		}
	}


	// Move to the closest adjacent unit to defender, and call adjacent attack
	public void moveAndAttack(Unit attacker, Unit attacked) {
		// Retrieve the tiles of the board and the valid movement tiles for the attacker
		Tile[][] tiles = gs.getBoard().getTiles();
		Set<Tile> validMovementTiles = calculateValidMovement(tiles, attacker);
		Tile defenderTile = attacked.getCurrentTile(gs.getBoard());

		// Initialize variables to keep track of the closest tile and its distance
		Tile closestTile = null;
		double closestDistance = Double.MAX_VALUE;

		// Iterate over each valid movement tile
		for (Tile tile : validMovementTiles) {
			// Ensure the tile is not occupied
			if (!tile.isOccupied()) {
				// Calculate the distance to the defender's tile
				double distance = Math.sqrt(Math.pow(tile.getTilex() - defenderTile.getTilex(), 2) + Math.pow(tile.getTiley() - defenderTile.getTiley(), 2));
				// If this tile is closer than any previously examined tile, update closestTile and closestDistance
				if (distance < closestDistance) {
					closestTile = tile;
					closestDistance = distance;
				}
			}
		}

		// If a closest tile has been found, move the attacker to this tile
		if (closestTile != null) {
			updateUnitPositionAndMove(attacker, closestTile);
			// Ensure a small delay to let the move action complete before attacking
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// After moving, perform the attack if the attacker is now adjacent to the defender
		if (isWithinAttackRange(attacker.getCurrentTile(gs.getBoard()), defenderTile)) {
			System.out.println("Attacker moved to adjacent tile and is now attacking.");
			adjacentAttack(attacker, attacked);
		} else {
			System.out.println("Attacker could not move close enough to perform an attack.");
		}
	}


	// Highlight tiles for movement and attack
	public void highlightMoveAndAttackRange(Unit unit) {
		Tile[][] tiles = gs.getBoard().getTiles();
		Set<Tile> validMovementTiles = calculateValidMovement(tiles, unit);
		Set<Tile> validAttackTiles = calculateAttackTargets(unit);

		// Highlight valid movement and attack tiles
		if (validMovementTiles != null) {
			for (Tile tile : validMovementTiles) {
				if (!tile.isOccupied()) {
					// Highlight tile for movement
					updateTileHighlight(tile, 1);
				} else if (tile.isOccupied() && tile.getUnit().getOwner() != unit.getOwner()) {
					// Highlight tile for attack
					updateTileHighlight(tile, 2);
				}
			}
		}

		// Highlight valid attack tiles
		for (Tile tile : validAttackTiles) {
			System.out.println("Tile is occupied: " + tile.isOccupied() + " and unit owner is: " + tile.getUnit().getOwner() + " and unit id is: " + tile.getUnit().getId());
			if (tile.isOccupied() && tile.getUnit().getOwner() != unit.getOwner()) {
				System.out.println("Highlighting attack tile");
				// Highlight tile for attack
				updateTileHighlight(tile, 2); // Assuming 2 is the highlight mode for attack
			}
		}
	}

	// Method to calculate and return the set of valid actions (tiles) for a given unit
	public Set<Tile> calculateValidMovement(Tile[][] board, Unit unit) {
		Set<Tile> validTiles = new HashSet<>();
		// Skip calculation if unit is provoked or has moved/attacked this turn
		if (checkProvoked(unit) || unit.movedThisTurn() || unit.attackedThisTurn()) {
			return validTiles;
		}

		Player currentPlayer = unit.getOwner();
		int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
		int[][] extendedDirections = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};

		// Handle immediate adjacent tiles (including diagonals)
		for (int[] direction : directions) {
			addValidTileInDirection(board, unit, direction[0], direction[1], validTiles, currentPlayer, false);
		}

		// Handle two tiles away horizontally or vertically, making sure not to pass through enemy units
		for (int[] direction : extendedDirections) {
			addValidTileInDirection(board, unit, direction[0], direction[1], validTiles, currentPlayer, true);
		}

		return validTiles;
	}


	public Set<Tile> calculateSpellTargets(Card card) {
		Set<Tile> validSpellTargets = new HashSet<>();
		// Logic to determine valid spell targets
		return validSpellTargets;
	}

	// Helper method to add a valid tile to the set of valid actions if the conditions are met
	private void addValidTileInDirection(Tile[][] board, Unit unit, int dx, int dy, Set<Tile> validTiles, Player currentPlayer, boolean extendedMove) {
		int x = unit.getPosition().getTilex() + dx;
		int y = unit.getPosition().getTiley() + dy;

		// For extended moves, check if the halfway tile is occupied by an enemy unit
		if (extendedMove && Math.abs(dx) <= 2 && Math.abs(dy) <= 2) {
			int halfwayX = unit.getPosition().getTilex() + (dx / 2);
			int halfwayY = unit.getPosition().getTiley() + (dy / 2);
			// Ensure halfway indices are within bounds before accessing the board
			if (isValidTile(halfwayX, halfwayY)) {
				Tile halfwayTile = board[halfwayX][halfwayY];
				if (halfwayTile.isOccupied() && halfwayTile.getUnit().getOwner() != currentPlayer) {
					// If the halfway tile is occupied by an enemy, this path is invalid
					return;
				}
			} else {
				// Halfway point is out of bounds; this direction is invalid
				return;
			}
		}

		// Ensure the tile is within the board's bounds and not occupied by an enemy
		if (isValidTile(x, y)) {
			Tile tile = board[x][y];
			validTiles.add(tile);
		}
	}


	// Checks if a tile position is within the boundaries of the game board
	private boolean isValidTile(int x, int y) {
		return x >= 0 && y >= 0 && x < 9 && y < 5; // Assuming a 9x5 board
	}

	// Determines if a unit is considered friendly based on current game state
	private boolean isFriendlyUnit(Unit unit) {
		return gs.getCurrentPlayer().getUnits().contains(unit);
	}

	// Checks if provoke unit is present on the board and around the tile on which an alleged enemy unit (target) is located
	public Set<Position> checkProvoker(Tile tile) {
		Set<Position> provoker = new HashSet<>();

		for (Unit unit : gs.getInactivePlayer().getUnits()) {
			int tilex = tile.getTilex();
			int tiley = tile.getTiley();

			if (Math.abs(tilex - unit.getPosition().getTilex()) < 2 && Math.abs(tiley - unit.getPosition().getTiley()) < 2) {
				if (unit.getName().equals("Rock Pulveriser") || unit.getName().equals("Swamp Entangler") ||
						unit.getName().equals("Silverguard Knight") || unit.getName().equals("Ironcliffe Guardian")) {
					System.out.println("Provoker " + unit.getName() + " in the house.");
					provoker.add(unit.getPosition());
				}
			}
		}
		return provoker;
	}

	// Returns true if the unit should be provoked based on adjacent opponents
	public boolean checkProvoked(Unit unit) {
		Player opponent = (gs.getCurrentPlayer() == gs.getHuman()) ? gs.getAi() : gs.getHuman();
		// Iterate over the opponent's units to check for adjacency and provoking units
		for (Unit other : opponent.getUnits()) {

			// Calculate the distance between the units
			int unitx = unit.getPosition().getTilex();
			int unity = unit.getPosition().getTiley();

			// Check if the opponent unit's name matches any provoking unit
			if (other.getName().equals("Rock Pulveriser") || other.getName().equals("Swamp Entangler") ||
					other.getName().equals("Silverguard Knight") || other.getName().equals("Ironcliffe Guardian")) {
				// Check if the opponent unit is adjacent to the current unit
				if (Math.abs(unitx - other.getPosition().getTilex()) <= 1 && Math.abs(unity - other.getPosition().getTiley()) <= 1) {
					BasicCommands.addPlayer1Notification(out, "Unit is provoked by " + other.getName(), 2);
					return true;
				}
			}
		}
		return false;
	}

	// Highlight tiles for attacking only
	public void highlightAttackRange(Unit unit) {
		Set<Tile> validAttackTiles = calculateAttackTargets(unit);

		// Highlight valid attack tiles
		validAttackTiles.forEach(tile -> updateTileHighlight(tile, 2)); // 2 for attack highlight mode
	}

	// Calculate and return the set of valid attack targets for a given unit
	public Set<Tile> calculateAttackTargets(Unit unit) {
		Set<Tile> validAttacks = new HashSet<>();
		Player opponent = gs.getInactivePlayer();

		// Default target determination
		if (!unit.attackedThisTurn()) {
			validAttacks.addAll(getValidTargets(unit, opponent));
		}

		return validAttacks;
	}

	// Returns the set of valid attack targets for a given unit
	public Set<Tile> getValidTargets(Unit unit, Player opponent) {
		Set<Tile> validAttacks = new HashSet<>();
		Set<Position> provokers = checkProvoker(unit.getCurrentTile(gs.getBoard()));
		Tile unitTile = unit.getCurrentTile(gs.getBoard());

		// Attack adjacent units if there are any
		if (!provokers.isEmpty()) {
			for (Position position : provokers) {
				System.out.println(position + "provoker position");
				Tile provokerTile = gs.getBoard().getTile(position.getTilex(), position.getTiley());
				validAttacks.add(provokerTile);
			}
			return validAttacks;
		}

		opponent.getUnits().stream()
				.map(opponentUnit -> opponentUnit.getCurrentTile(gs.getBoard()))
				.filter(opponentTile -> isWithinAttackRange(unitTile, opponentTile))
				.forEach(validAttacks::add);

		return validAttacks;
	}

	public boolean isWithinAttackRange(Tile unitTile, Tile targetTile) {
		int dx = Math.abs(unitTile.getTilex() - targetTile.getTilex());
		int dy = Math.abs(unitTile.getTiley() - targetTile.getTiley());
		return dx < 2 && dy < 2;
	}

	// Attack an enemy unit and play the attack animation
	public void adjacentAttack(Unit attacker, Unit attacked) {
		if (!attacker.attackedThisTurn()) {
			// remove highlight from all tiles
			removeHighlightFromAll();

			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			BasicCommands.playUnitAnimation(out, attacked, UnitAnimationType.hit);
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
			BasicCommands.playUnitAnimation(out, attacked, UnitAnimationType.idle);

			// update health
			updateUnitHealth(attacked, attacked.getHealth() - attacker.getAttack());
			if (attacker.getAttack() >= 0 && attacker.equals(gs.getHuman().getAvatar()) 
					&& gs.getHuman().getRobustness() > 0) {
 
					Wraithling.summonAvatarWraithling(out, gs);
				}
				
			}

			// Only counter attack if the attacker is the current player
			// To avoid infinitely recursive counter attacking
			if (attacker.getOwner() == gs.getCurrentPlayer()) {
				counterAttack(attacker, attacked);
			}

			attacker.setAttackedThisTurn(true);
			attacker.setMovedThisTurn(true);
		}
	

	// Counter attack an enemy unit and play the attack animation
	public void counterAttack(Unit originalAttacker, Unit counterAttacker) {
		if (counterAttacker.getHealth() > 0) {
			System.out.println("Counter attacking");
			adjacentAttack(counterAttacker, originalAttacker);
			counterAttacker.setAttackedThisTurn(false);
			counterAttacker.setMovedThisTurn(false);
		}
	}

	// highlight tiles for summoning units (does not currently take into account special units)
	public void highlightSpellRange(Card card, Player player) {
		// Validate inputs
		if (card == null  || player == null) {
			System.out.println("Invalid parameters for highlighting summon range.");
			return;
		}
		if (!card.isCreature()) {
			Set<Tile> validCastTiles = getSpellRange(card);
			if (card.getCardname().equals("Horn of the Forsaken")) {
				updateTileHighlight(gs.getHuman().getAvatar().getCurrentTile(gs.getBoard()), 1);
				BasicCommands.addPlayer1Notification(out, "Click on the Avatar to apply the spell", 2);
				return;
			}
			if (card.getCardname().equals("Dark Terminus")) {
				validCastTiles.forEach(tile -> updateTileHighlight(tile, 2));
				return;
			}
			if (card.getCardname().equals("Wraithling Swarm")) {
				validCastTiles.forEach(tile -> updateTileHighlight(tile, 1));
				return;
			}
		}

		System.out.println("Highlighting spellragne " + card.getCardname());
	}

	// highlight tiles for summoning units
	public void highlightSummonRange() {
		Set<Tile> validTiles = getValidSummonTiles();
		validTiles.forEach(tile -> updateTileHighlight(tile, 1));
	}

	public Set<Tile> getValidSummonTiles() {
		Player player = gs.getCurrentPlayer();
		Set<Tile> validTiles = new HashSet<>();
		Tile[][] tiles = gs.getBoard().getTiles();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				Tile currentTile = tiles[i][j];
				if (isAdjacentToFriendlyUnit(i, j, player) && !currentTile.isOccupied()) {
					validTiles.add(currentTile);
				}
			}
		}
		return validTiles;
	}
	
	//Check for spell range 
	// Check for spell range 
	public Set<Tile> getSpellRange(Card card) {
	    Set<Tile> validTiles = new HashSet<>();
	    Tile[][] tiles = gs.getBoard().getTiles();

	    if (card.getCardname().equals("Dark Terminus")) {
	        for (int i = 0; i < tiles.length; i++) {
	            for (int j = 0; j < tiles[i].length; j++) {
	                Tile currentTile = tiles[i][j];
	                Unit unit = currentTile.getUnit();
	                
	                // Check if the tile is adjacent to a friendly unit and not occupied
	                if (unit != null && !(unit.getOwner() instanceof HumanPlayer) && !unit.getName().equals("AI Avatar")) {
	                    validTiles.add(currentTile);
	                }
	            }
	        }
	    } else if (card.getCardname().equals("Wraithling Swarm")) {
	        for (int i = 0; i < 9; i++) {
	            for (int j = 0; j < 5; j++) {
	                Tile currentTile = tiles[i][j];
	                // Check if the tile is not occupied and three consecutive tiles in the row are free for placement
	                if (!currentTile.isOccupied() && checkConsecutiveFreeTilesInRow(i, j)) {
	                    validTiles.add(currentTile);
	                }
	            }
	        }
	    }
	    return validTiles;
	}

	// Check available three consecutive tiles
	private boolean checkConsecutiveFreeTilesInRow(int row, int col) {
	    int count = 0;
	    Tile[][] tiles = gs.getBoard().getTiles();
	    for (int i = col; i < col + 3 && i < tiles[row].length; i++) {
	        Tile currentTile = tiles[row][i];
	        if (!currentTile.isOccupied()) {
	            count++;
	            if (count == 3) {
	                return true;
	            }
	        } else {
	            return false;
	        }
	    }
	    return false;
	}



	// Check if a tile is adjacent to a friendly unit of the specified player
	private boolean isAdjacentToFriendlyUnit(int x, int y, Player player) {
		Tile[][] tiles = gs.getBoard().getTiles();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue; // Skip the current tile
				int adjX = x + dx;
				int adjY = y + dy;
				if (isValidTile(adjX, adjY)) {
					Tile adjTile = tiles[adjX][adjY];
					if (adjTile.isOccupied() && adjTile.getUnit().getOwner() == player) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// check if summoning is valid
	public boolean isValidSummon(Card card, Tile tile) {
		// depending on cards, this may change
		// for now, all cards can move to tiles highlighted white
		System.out.println("isValidSummon: " + tile.getHighlightMode());
		return tile.getHighlightMode() == 1;
	}

	// helper method to update tile highlight
	public void updateTileHighlight(Tile tile, int tileHighlightMode) {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		tile.setHighlightMode(tileHighlightMode);
		BasicCommands.drawTile(out, tile, tileHighlightMode);
	}

	public void updateUnitPositionAndMove(Unit unit, Tile newTile) {
		if (newTile.getHighlightMode() != 1 && gs.getCurrentPlayer() instanceof HumanPlayer) {
			System.out.println("New tile is not highlighted for movement");
			removeHighlightFromAll();
			return;
		}

		Board board = gs.getBoard();

		// get current tile
		Tile currentTile = unit.getCurrentTile(board);

		// update unit position
		currentTile.removeUnit();
		newTile.setUnit(unit);
		unit.setPositionByTile(newTile);

		// remove highlight from all tiles
		removeHighlightFromAll();

		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		// Move unit to tile according to the result of yFirst
		BasicCommands.moveUnitToTile(out, unit, newTile, yFirst(currentTile, newTile, unit));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		unit.setMovedThisTurn(true);
	}

	private boolean yFirst(Tile currentTile, Tile newTile, Unit unit) {
		Board board = gs.getBoard();

		// Calculate the movement direction in both axes
		int dx = newTile.getTilex() - currentTile.getTilex();
		int dy = newTile.getTiley() - currentTile.getTiley();

		// Check if the move is diagonal (both dx and dy are non-zero)
		if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
			// Determine if there's an enemy unit directly in front or behind
			int frontX = currentTile.getTilex() + dx;
			int frontY = currentTile.getTiley();
			int behindX = currentTile.getTilex();
			int behindY = currentTile.getTiley() - dy;

			boolean isEnemyInFront = isValidTile(frontX, frontY) && board.getTile(frontX, frontY).isOccupied() && board.getTile(frontX, frontY).getUnit().getOwner() != unit.getOwner();
			boolean isEnemyBehind = isValidTile(behindX, behindY) && board.getTile(behindX, behindY).isOccupied() && board.getTile(behindX, behindY).getUnit().getOwner() != unit.getOwner();

			// Set yFirst to true if there's an enemy directly in front or behind
			return isEnemyInFront || isEnemyBehind;
		}
		return false;
	}

	public void drawCards(Player player, int numberOfCards) {
		if (player instanceof HumanPlayer) {
			for (int i = 0; i < numberOfCards; i++) {
				Card cardDrawn = player.drawCard();
				int handPosition = player.getHand().getNumberOfCardsInHand();
				BasicCommands.drawCard(out, cardDrawn, handPosition, 0);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			for (int i = 0; i < numberOfCards; i++) {
				player.drawCard();
			}
		}
	}



    // remove card from hand and summon unit
    public void removeCardFromHandAndSummon(Card card, Tile tile) {
    	System.out.println("Removing card from hand and summoning" + card.getCardname());

		Player player = gs.getCurrentPlayer();
		Hand hand = player.getHand();
		int handPosition = gs.getCurrentCardPosition();

		if (handPosition == 0) {
			for (int i = 1; i <= hand.getNumberOfCardsInHand(); i++) {
				if (hand.getCardAtPosition(i).equals(card)) {
					handPosition = i;
					break;
				}
			}
		}
		System.out.println("Current card: " + card.getCardname() + " position " + handPosition);

		// check if enough mana
		if (player.getMana() < card.getManacost()) {
			BasicCommands.addPlayer1Notification(out, "Not enough mana to summon " + card.getCardname(), 2);
			return;
		}

		// update player mana
		updatePlayerMana(player, player.getMana() - card.getManacost());

		// update the positions of the remaining cards if the player is human
		if (player instanceof HumanPlayer) {
			// remove card from hand and delete from UI
			BasicCommands.deleteCard(out, handPosition + 1);
			hand.removeCardAtPosition(handPosition);
			updateHandPositions(hand);
		} else {
			hand.removeCardAtPosition(handPosition);
		}
		if (card.isCreature()) {
			// get unit config and id
			String unit_conf = card.getUnitConfig();
			int unit_id = card.getId();
			summonUnit(unit_conf, unit_id, card, tile, player);
		}
    }

	public void summonUnit(String unit_conf, int unit_id, Card card, Tile tile, Player player) {
		
		if (((Card) card).getCardname().equals("Gloom Chaser")) {
		Wraithling.summonGloomChaserWraithling(tile, out, gs);}
		

		// load unit
		Unit unit = loadUnit(unit_conf, unit_id, Unit.class);

		// set unit position
		tile.setUnit(unit);
		unit.setPositionByTile(tile);
		unit.setOwner(player);
		unit.setName(card.getCardname());
		player.addUnit(unit);
		gs.addToTotalUnits(1);
		gs.addUnitstoBoard(unit);
		System.out.println("Unit added to board: " + ( gs.getUnitsOnBoard()).size());
		// remove highlight from all tiles
		removeHighlightFromAll();

		// draw unit on new tile and play summon animation
		EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
		BasicCommands.playEffectAnimation(out, effect, tile);
		BasicCommands.drawUnit(out, unit, tile);


		// use BigCard data to update unit health and attack
		BigCard bigCard = card.getBigCard();
		updateUnitHealth(unit, bigCard.getHealth());
		updateUnitAttack(unit, bigCard.getAttack());

		unit.setMovedThisTurn(true);
		unit.setAttackedThisTurn(true);
		gs.addUnitstoBoard(unit);

		// wait for animation to play out
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		System.out.println("Summoning unit " + unit + " to tile " + tile.getTilex() + ", " + tile.getTiley());
	}

	public void setCurrentCardClickedAndHighlight(int handPosition) {
		notClickingCard();
		Card card = gs.getCurrentPlayer().getHand().getCardAtPosition(handPosition);
		gs.setCurrentCardPosition(handPosition);
		gs.setCurrentCardClicked(card);
		BasicCommands.drawCard(out, card, handPosition,1);
		if (card.isCreature()) {
			//displayMessageForSpell(card);
		}
	
	}

	public void notClickingCard() {
		gs.setCurrentCardClicked(null);
		gs.setCurrentCardPosition(0);

		for (int i = 1; i <= gs.getHuman().getHand().getNumberOfCardsInHand(); i++) {
			Card card = gs.getHuman().getHand().getCardAtPosition(i);
			BasicCommands.drawCard(out, card, i, 0);
		}
	}

	public void updateHandPositions(Hand hand) {
		if (hand.getNumberOfCardsInHand() == 0) {
			BasicCommands.deleteCard(out, 1);
		}

		// Iterate over the remaining cards in the hand
		for (int i = 0; i < hand.getNumberOfCardsInHand(); i++) {
			// Draw each card in its new position, positions are usually 1-indexed on the UI
			BasicCommands.deleteCard(out, i + 2);
			BasicCommands.drawCard(out, hand.getCardAtIndex(i), i + 1, 0);
		}
	}
	
	public void HornOfTheForesaken(Card card) {
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.something);
        BasicCommands.playEffectAnimation(out, effect, gs.getHuman().getAvatar().getCurrentTile(gs.getBoard()));
        notClickingCard();
        BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken gave you 3 more robustness", 2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void zeal() {
        for (Unit unit : gs.getAi().getUnits()) {
            if (unit.getName().equals("Silverguard Knight")) {
                int newAttack = unit.getAttack() + 2;
                updateUnitAttack(unit, newAttack);
				EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
				BasicCommands.playEffectAnimation(out, effect, unit.getCurrentTile(gs.getBoard()));
				System.out.println("BUFFED!");
            }
        }
    }
}