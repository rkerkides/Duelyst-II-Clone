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

		} else {
			avatarTile = board.getTile(7, 2);
			avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);
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

	// Update a unit's health on the board
	public void updateUnitHealth(Unit unit, int newHealth) {
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		unit.setHealth(newHealth);
		BasicCommands.setUnitHealth(out, unit, newHealth);
	}

	// Update a unit's attack on the board
	public void updateUnitAttack(Unit unit, int newAttack) {
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		unit.setAttack(newAttack);
		BasicCommands.setUnitAttack(out, unit, newAttack);
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

	// Highlight tiles for movement and attack
	public void highlightMoveAndAttackRange(Unit unit) {
		Tile[][] tiles = gs.getBoard().getTiles();
		Set<Tile> validMovementTiles = calculateValidMovement(tiles, unit);
		Set<Tile> validAttackTiles = determineTargets(unit);

		// Highlight valid movement tiles
		if (validMovementTiles != null) {
			for (Tile tile : validMovementTiles) {
				if (!tile.isOccupied()) {
					// Highlight tile for movement
					updateTileHighlight(tile, 1); // Assuming 1 is the highlight mode for movement
				}
			}
		}

		// Highlight valid attack tiles
		for (Tile tile : validAttackTiles) {
			if (tile.isOccupied() && tile.getUnit().getOwner() != unit.getOwner()) {
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

	// Helper method to add a valid tile to the set of valid actions if the conditions are met
	private void addValidTileInDirection(Tile[][] board, Unit unit, int dx, int dy, Set<Tile> validTiles) {
		// Calculate new position based on direction offsets
		int x = unit.getPosition().getTilex() + dx;
		int y = unit.getPosition().getTiley() + dy;

		// Check if the new position is within board bounds and potentially valid for action
		if (isValidTile(x, y)) {
			Tile tile = board[x][y];
			// Add the tile to valid tiles if it is unoccupied
			if (tile.getUnit() == null) {
				validTiles.add(tile);
			}
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
		Set<Tile> validAttackTiles = determineTargets(unit);

		// Highlight valid attack tiles
		validAttackTiles.forEach(tile -> updateTileHighlight(tile, 2)); // 2 for attack highlight mode
	}

	public Set<Tile> determineTargets(Unit unit) {
		Set<Tile> validAttacks = new HashSet<>();
		Player opponent = gs.getInactivePlayer();

		// Special ability or ranged attack logic (not implemented yet)
		/*if (unit instanceof RangedAttack && !unit.attackedThisTurn()) {
			return ((RangedAttack) unit).specialAbility(gs.getBoard());
		}*/

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

	// Simplified to demonstrate concept
	public Set<Tile> getValidTargets(Unit unit, Player opponent) {
		Set<Tile> validAttacks = new HashSet<>();
		Tile unitTile = unit.getCurrentTile(gs.getBoard());

		opponent.getUnits().stream()
				.map(opponentUnit -> opponentUnit.getCurrentTile(gs.getBoard()))
				.filter(opponentTile -> isWithinAttackRange(unitTile, opponentTile))
				.forEach(validAttacks::add);

		return validAttacks;
	}

	private boolean isWithinAttackRange(Tile unitTile, Tile targetTile) {
		int dx = Math.abs(unitTile.getTilex() - targetTile.getTilex());
		int dy = Math.abs(unitTile.getTiley() - targetTile.getTiley());
		return dx < 2 && dy < 2;
	}

	private Set<Tile> findProvokedTargets(Unit unit) {
		// Logic to identify tiles when unit is provoked
		return new HashSet<>();
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
		if (newTile.getHighlightMode() != 1) {
			System.out.println("New tile is not highlighted for movement");
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

		// load unit
		Unit unit = loadUnit(unit_conf, unit_id, Unit.class);

        // set unit position
        tile.setUnit(unit);
        unit.setPositionByTile(tile);
		unit.setOwner(player);
		player.addUnit(unit);
		gs.addToTotalUnits(1);

        // remove highlight from all tiles
        removeHighlightFromAll();

        // draw unit on new tile and play summon animation
		EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
		BasicCommands.playEffectAnimation(out, effect, tile);
        BasicCommands.drawUnit(out, unit, tile);

		// update unit health and attack
		updateUnitHealth(unit, unit.getHealth());
		updateUnitAttack(unit, unit.getAttack());

		// wait for animation to play out
		try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
