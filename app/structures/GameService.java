package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.player.Hand;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.HashSet;
import java.util.Set;

import static utils.BasicObjectBuilders.loadUnit;

public class GameService {
	private final ActorRef out;
	private final GameState gs;
	

	public GameService(ActorRef out, GameState gs) {
		this.out = out;
		this.gs = gs;
	}

	public void updatePlayerHealth(Player player, int newHealth){
		// Set the new health value on the player object first
		player.setHealth(newHealth);

		// Now update the health on the frontend using the BasicCommands
		if (player instanceof HumanPlayer){
			BasicCommands.setPlayer1Health(out, player);
		} else {
			BasicCommands.setPlayer2Health(out, player);
		}
	}

	public void updatePlayerMana(Player player, int newMana){
		// Set the new mana value on the player object first
		player.setMana(newMana);

		// Now update the mana on the frontend using the BasicCommands
		if (player instanceof HumanPlayer){
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
				try {Thread.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
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
	}

	// load aiUnits for testing
	public void loadUnitsForTesting(Player player) {
		Deck aiDeck = player.getDeck();
		Card card = aiDeck.drawCard();
		Card card2 = aiDeck.drawCard();
		Tile tile = gs.getBoard().getTile(5, 2);
		Tile tile2 = gs.getBoard().getTile(6, 2);
		summonUnit(card.getUnitConfig(), card.getId(), card, tile, player);
		summonUnit(card2.getUnitConfig(), card2.getId(), card2, tile2, player);
	}

	// Update a unit's health on the board
	public void updateUnitHealth(Unit unit, int newHealth) {
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		if (newHealth <= 0) {
			performUnitDeath(unit);
			if (unit.getId() == 0 || unit.getId() == 1) {
				updatePlayerHealth(unit.getOwner(), newHealth);
			}
			return;
		}
		unit.setHealth(newHealth);
		BasicCommands.setUnitHealth(out, unit, newHealth);
		if (unit.getId() == 0 || unit.getId() == 1) {
			updatePlayerHealth(unit.getOwner(), newHealth);
		}
	}

	// Update a unit's attack on the board
	public void updateUnitAttack(Unit unit, int newAttack) {
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		unit.setAttack(newAttack);
		BasicCommands.setUnitAttack(out, unit, newAttack);
	}

	public void performUnitDeath (Unit unit) {
		// remove unit from board
		unit.getCurrentTile(gs.getBoard()).removeUnit();
		unit.setHealth(0);
		unit.getOwner().removeUnit(unit);
		unit.setOwner(null);
		gs.removeFromTotalUnits(1);
		BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.death);
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		BasicCommands.deleteUnit(out, unit);
		if (unit.getId() == 0 || unit.getId() == 1) {
			updatePlayerHealth(unit.getOwner(), 0);
		}
	}

	// remove highlight from all tiles
	public void removeHighlightFromAll() {
		Board board = gs.getBoard();
		Tile[][] tiles = board.getTiles();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if(tiles[i][j].getHighlightMode() != 0){
					Tile currentTile = tiles[i][j];
					currentTile.setHighlightMode(0);
					BasicCommands.drawTile(out, currentTile, 0);
				}
			}
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
			try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
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

		// Check if the unit is in a provoked state, which may restrict its actions
		if (checkProvoked(unit)) {
			return null;
		}

		// Only allow action calculation if the unit has not moved or attacked this turn
		if (!unit.movedThisTurn() && !unit.attackedThisTurn()) {
			// Extended directions array includes immediate adjacent and two steps away in cardinal directions
			int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};
			int[][] extendedDirections = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}}; // For two steps away

			// Check adjacent and diagonal tiles
			for (int[] direction : directions) {
				addValidTileInDirection(board, unit, direction[0], direction[1], validTiles);
			}

			// Check tiles two steps away in cardinal directions
			for (int[] direction : extendedDirections) {
				addValidTileInDirection(board, unit, direction[0], direction[1], validTiles);
			}
		}
		// Return the set of valid tiles/actions
		return validTiles;
	}

	public Set<Tile> calculateSpellTargets(Card card) {
		Set<Tile> validSpellTargets = new HashSet<>();
		// Logic to determine valid spell targets
		return validSpellTargets;
	}

	// Helper method to add a valid tile to the set of valid actions if the conditions are met
	private void addValidTileInDirection(Tile[][] board, Unit unit, int dx, int dy, Set<Tile> validTiles) {
		// Calculate new position based on direction offsets
		int x = unit.getPosition().getTilex() + dx;
		int y = unit.getPosition().getTiley() + dy;

		// Check if the new position is within board bounds and potentially valid for action
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


	// Returns true if the unit should be provoked based on adjacent opponents
	public boolean checkProvoked(Unit unit) {

		// Incomplete implementation yet to be tested, temporarily return false always
		/*for (Unit other : gs.getInactivePlayer().getUnits()) {

			int unitx = unit.getPosition().getTilex();
			int unity = unit.getPosition().getTiley();

			if(other.getId() == 3 || other.getId() == 10 || other.getId() == 6 || other.getId() == 16 || other.getId() == 20 || other.getId() == 30) {
				if (Math.abs(unitx - other.getPosition().getTilex()) <= 1 && Math.abs(unity - other.getPosition().getTiley()) <= 1) {
					System.out.println("Unit is provoked!");
					return true;
				}
			}

		}*/
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

		// Provocation check
		if (!unit.movedThisTurn() && checkProvoked(unit)) {
			return findProvokedTargets(unit);
		}

		// Default target determination
		if (!unit.attackedThisTurn()) {
			validAttacks.addAll(getValidTargets(unit, opponent));
		}

		return validAttacks;
	}

	// Returns the set of valid attack targets for a given unit
	public Set<Tile> getValidTargets(Unit unit, Player opponent) {
		Set<Tile> validAttacks = new HashSet<>();
		Tile unitTile = unit.getCurrentTile(gs.getBoard());

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

	private Set<Tile> findProvokedTargets(Unit unit) {
		// Logic to identify tiles when unit is provoked
		return new HashSet<>();
	}

	// Attack an enemy unit and play the attack animation
	public void adjacentAttack(Unit attacker, Unit attacked) {
		if (!attacker.attackedThisTurn()) {
			// remove highlight from all tiles
			removeHighlightFromAll();

			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
			try {Thread.sleep(1500);} catch (InterruptedException e) {e.printStackTrace();}

			BasicCommands.playUnitAnimation(out, attacked, UnitAnimationType.hit);
			try {Thread.sleep(750);} catch (InterruptedException e) {e.printStackTrace();}
			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
			BasicCommands.playUnitAnimation(out, attacked, UnitAnimationType.idle);

			// update health
			updateUnitHealth(attacked, attacked.getHealth() - attacker.getAttack());

			// Only counter attack if the attacker is the current player
			// To avoid infinitely recursive counter attacking
			if (attacker.getOwner() == gs.getCurrentPlayer()) {
				counterAttack(attacker, attacked);
			}

			attacker.setAttackedThisTurn(true);
			attacker.setMovedThisTurn(true);
		}
	}

	// Counter attack an enemy unit and play the attack animation
	public void counterAttack(Unit originalAttacker, Unit counterAttacker) {
		if (counterAttacker.getHealth() > 0) {
			System.out.println("Counter attacking");
			adjacentAttack(counterAttacker, originalAttacker);
		}
	}

	// highlight tiles for summoning units (does not currently take into account special units)
	public void highlightSummonRange(Card card, Player player) {
		// Validate inputs
		if (card == null || !card.isCreature() || player == null) {
			System.out.println("Invalid parameters for highlighting summon range.");
			return;
		}

		System.out.println("Highlighting summon range for " + card.getCardname());
		Tile[][] tiles = gs.getBoard().getTiles();

		// Iterate over all tiles on the board
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				Tile currentTile = tiles[i][j];

				// Check if tile is adjacent to a friendly unit
				if (isAdjacentToFriendlyUnit(i, j, player) && !currentTile.isOccupied()) {
					updateTileHighlight(currentTile, 1); // 1 for summonable highlight mode
				}
			}
		}
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
		try {Thread.sleep(20);} catch (InterruptedException e) {e.printStackTrace();}

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

		// get position of unit and find the tile it is on
		Position position = unit.getPosition();
		Tile currentTile = unit.getCurrentTile(board);

		// update unit position
		currentTile.removeUnit();
		newTile.setUnit(unit);
		unit.setPositionByTile(newTile);

		// remove highlight from all tiles
		removeHighlightFromAll();

		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		// draw unit on new tile and wait for animation to play out
		BasicCommands.moveUnitToTile(out, unit, newTile);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		unit.setMovedThisTurn(true);
	}
	
	public void drawCards(Player player, int numberOfCards) {
		if (player instanceof HumanPlayer) {
			for (int i = 0; i < numberOfCards; i++) {
				Card cardDrawn = player.drawCard();
				int handPosition = player.getHand().getNumberOfCardsInHand();
				BasicCommands.drawCard(out, cardDrawn, handPosition, 0);
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
			}
		} else {
			for (int i = 0; i < numberOfCards; i++) {
				player.drawCard();
			}
		}
	}

    // remove card from hand and summon unit
    public void removeCardFromHandAndSummonUnit(Card card, Tile tile) {
		Player player = gs.getCurrentPlayer();
		Hand hand = player.getHand();
		int handPosition = gs.getCurrentCardPosition();

		// check if enough mana
		if (player.getMana() < card.getManacost()) {
			BasicCommands.addPlayer1Notification(out, "Not enough mana to summon " + card.getCardname(), 2);
			return;
		}

		// update player mana
		updatePlayerMana(player, player.getMana() - card.getManacost());

		// get unit config and id
		String unit_conf = card.getUnitConfig();
		int unit_id = card.getId();

		// remove card from hand and delete from UI
		if (player.equals(gs.getHuman())) {
			BasicCommands.deleteCard(out, handPosition + 1);
		}
		hand.removeCardAtPosition(handPosition);

		// update the positions of the remaining cards if the player is human
		if (player instanceof HumanPlayer) {
			updateHandPositions(hand);
		}

		// summon unit
		summonUnit(unit_conf, unit_id, card, tile, player);
    }

	public void summonUnit(String unit_conf, int unit_id, Card card, Tile tile, Player player) {
		// load unit
		Unit unit = loadUnit(unit_conf, unit_id, Unit.class);

		// set unit position
		tile.setUnit(unit);
		unit.setPositionByTile(tile);
		unit.setOwner(player);
		unit.setName(card.getCardname());
		player.addUnit(unit);
		gs.addToTotalUnits(1);

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

		// wait for animation to play out
		try {
			Thread.sleep(500);
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
	}

	public void notClickingCard() {
		gs.setCurrentCardClicked(null);
		gs.setCurrentCardPosition(0);

		for (int i = 1; i <= gs.getCurrentPlayer().getHand().getNumberOfCardsInHand(); i++) {
			Card card = gs.getCurrentPlayer().getHand().getCardAtPosition(i);
			BasicCommands.drawCard(out, card, i, 0);
		}
	}

	private void updateHandPositions(Hand hand) {
		// Iterate over the remaining cards in the hand
		for (int i = 0; i < hand.getNumberOfCardsInHand(); i++) {
			// Draw each card in its new position, positions are usually 1-indexed on the UI
			BasicCommands.deleteCard(out, i + 2);
			BasicCommands.drawCard(out, hand.getCardAtIndex(i), i + 1, 0);
		}
	}
}
