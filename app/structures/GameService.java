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
		try {Thread.sleep(15);} catch (InterruptedException e) {e.printStackTrace();}
		unit.setHealth(newHealth);
		BasicCommands.setUnitHealth(out, unit, newHealth);
	}

	// Update a unit's attack on the board
	public void updateUnitAttack(Unit unit, int newAttack) {
		try {Thread.sleep(15);} catch (InterruptedException e) {e.printStackTrace();}
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

	// Highlight tiles for movement and attacking
	public void highlightMoveAndAttackRange(Unit unit) {
		Tile[][] tiles = gs.getBoard().getTiles();
		Set<Tile> validActions = calculateValidActions(tiles, unit);

		if (validActions == null) {
			return; // Unit is provoked or cannot move
		}

		// Highlight valid moves and attack ranges based on the set of valid tiles
		for (Tile validTile : validActions) {
			int x = validTile.getTilex();
			int y = validTile.getTiley();

			// Retrieve the target tile from the board
			Tile targetTile = tiles[x][y];

			// Determine highlighting based on occupancy and ownership
			if (!targetTile.isOccupied()) {
				// Highlight for movement
				updateTileHighlight(targetTile, 1);
			} else if (targetTile.getUnit().getOwner() != unit.getOwner()) {
				// Highlight for attack
				updateTileHighlight(targetTile, 2);
			}
			// Note: Tiles with friendly units are not highlighted
		}
	}

	// Method to calculate and return the set of valid actions (tiles) for a given unit
	public Set<Tile> calculateValidActions(Tile[][] board, Unit unit) {
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
		if (isValidTile(board, x, y)) {
			Tile tile = board[x][y];
			// Add the tile to valid actions if it is unoccupied or occupied by a friendly unit
			if (tile.getUnit() == null || isFriendlyUnit(tile.getUnit())) {
				validTiles.add(tile);
			}
		}
	}

	// Checks if a tile position is within the boundaries of the game board
	private boolean isValidTile(Tile[][] board, int x, int y) {
		return x >= 0 && y >= 0 && x < board.length && y < board[0].length;
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

	// highlight tiles for attacking only
	public void highlightAttackRange(Unit unit) {
		Board board = gs.getBoard();
		Tile[][] tiles = board.getTiles();

		int baseX = unit.getPosition().getTilex();
		int baseY = unit.getPosition().getTiley();

		// Loop to cover 2 tiles in each direction and 1 tile diagonally
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				// Check for diagonal movement (skip tiles that are 2 tiles away diagonally)
				if (Math.abs(x) == 2 && Math.abs(y) == 2)
					continue;
				if (Math.abs(x) == 2 && Math.abs(y) == 1)
					continue;
				if (Math.abs(x) == 1 && Math.abs(y) == 2)
					continue;

				// Calculate the target tile's coordinates
				int targetX = baseX + x;
				int targetY = baseY + y;

				// Check if coordinates are within board bounds
				if (targetX < 0 || targetY < 0 || targetX >= 9 || targetY >= 5) {
					continue; // Skip tiles outside the board bounds
				}

				// Retrieve the tile if it's within the board bounds
				Tile targetTile = tiles[targetX][targetY];

				// Handle differential highlighting for tiles with units
				if (targetTile.isOccupied()) {
					if (targetTile.getUnit().getOwner() == unit.getOwner()) {
						// Leave tiles with friendly units unhighlighted
					} else {
						// Highlight tiles with enemy units for attack
						updateTileHighlight(targetTile, 2);
					}
				}
			}
		}
	}

    // highlight tiles for summoning units (does not currently take into account special units)
	public void highlightSummonRange(Card card, Player player) {
		Board board = gs.getBoard();

		if (card == null || board == null || player == null) {
			System.out.println("One or more required parameters are null.");
			return;
		}

		// Check if card is a creature (spells can not be summoned)
		if (!card.isCreature()) {
			System.out.println(card.getCardname() + " is not a creature.");
			return;
		}

		System.out.println("highlightSummonRange for " + card.getCardname());
		Tile[][] tiles = board.getTiles();

		if (tiles == null) {
			System.out.println("Tiles array is null.");
			return;
		}

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile currentTile = tiles[i][j];
				if (currentTile == null) {
					System.out.println("Tile at position " + i + ", " + j + " is null.");
					continue;
				}
				// Check if tile is occupied by a friendly unit
				if (currentTile.isOccupied()) {
					Unit unit = currentTile.getUnit();
					if (unit != null && unit.getOwner() == player) {
						System.out.println("Tile " + i + ", " + j + " is occupied by a friendly unit");
						// Highlight adjacent tiles that are not occupied
						for (int x = -1; x <= 1; x++) {
							for (int y = -1; y <= 1; y++) {
								// Skip the current tile
								if (x == 0 && y == 0) {
									continue;
								}
								int adjX = i + x;
								int adjY = j + y;
								// Check if adjacent tile is within board bounds
								if (adjX >= 0 && adjX < 9 && adjY >= 0 && adjY < 5) {
									Tile adjTile = tiles[adjX][adjY];
									if (adjTile != null && !adjTile.isOccupied()) {
										updateTileHighlight(adjTile, 1); // Use 1 for summonable highlight mode
									}
								}
							}
						}
					}
				}
			}
		}
	}


	// check if summoning is valid
    public boolean isValidSummon(Card card, Tile tile) {
        // depending on cards, this may change
        // for now, all cards can move to tiles highlighted white
        return tile.getHighlightMode() == 1;
    }

	// helper method to update tile highlight
	public void updateTileHighlight(Tile tile, int tileHighlightMode) {
		tile.setHighlightMode(tileHighlightMode);
		BasicCommands.drawTile(out, tile, tileHighlightMode);
	}

	// check if move is valid
	public boolean isValidMove(Unit unit, Tile tile) {
		// depending on unit, this may change
		// for now, all units can move to tiles highlighted white
		System.out.println("isValidMove: " + tile.getHighlightMode());
		return tile.getHighlightMode() == 1;
	}

	public void updateUnitPositionAndMove(Unit unit, Tile newTile) {
		Board board = gs.getBoard();

		// get position of unit and find the tile it is on
		Position position = unit.getPosition();
		Tile currentTile = board.getTile(position.getTilex(), position.getTiley());

		// update unit position
		currentTile.removeUnit();
		newTile.setUnit(unit);
		unit.setPositionByTile(newTile);

		// remove highlight from all tiles
		removeHighlightFromAll();

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
		Board board = gs.getBoard();
		Player player = gs.getCurrentPlayer();
		Hand hand = player.getHand();
		int handPosition = gs.getCurrentCardPosition();

		// check if any of the required parameters are null
		if (board == null || card == null || tile == null || hand == null) {
			System.out.println("removeCardFromHandAndSummonUnit: One or more arguments are null");
			return;
		}
		if (handPosition < 1 || handPosition > hand.getNumberOfCardsInHand()) {
			System.out.println("removeCardFromHandAndSummonUnit: handPosition is out of bounds");
			return;
		}

		// check if enough mana
		if (player.getMana() < card.getManacost()) {
			BasicCommands.addPlayer1Notification(out, "Not enough mana to summon " + card.getCardname(), 2);
			return;
		}

		// update player mana
		player.setMana(player.getMana() - card.getManacost());
		BasicCommands.setPlayer1Mana(out, player);

		// remove card from hand
		BasicCommands.deleteCard(out, handPosition + 1);
		hand.removeCardAtPosition(handPosition);

		// update the positions of the remaining cards if the player is human
		if (player instanceof HumanPlayer) {
			updateHandPositions(hand);
		}

		// summon unit
		Unit unit = loadUnit(card.getUnitConfig(), card.getId(), Unit.class);
		if (unit == null) {
			System.out.println("removeCardFromHandAndSummonUnit: Failed to load unit");
			return;
		}

        // set unit position
        tile.setUnit(unit);
        unit.setPositionByTile(tile);
		unit.setOwner(player);

        // remove highlight from all tiles
        removeHighlightFromAll();

        // draw unit on new tile
        BasicCommands.drawUnit(out, unit, tile);

		// for now, set health and attack to default values
		// doesn't seem to work, idk why
		BasicCommands.setUnitHealth(out, unit, 20);
		BasicCommands.setUnitAttack(out, unit, 10);


		try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

	public void setCurrentCardClicked(int handPosition) {
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
