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

import static utils.BasicObjectBuilders.loadUnit;

public class GameService {
	private ActorRef out;
	

	public GameService(ActorRef out) {
		this.out = out;
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

	// initial board setup
	public Board loadBoard() {
		Board board = new Board();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile tile = BasicObjectBuilders.loadTile(i, j);
				tile.setHighlightMode(0);
				board.setTile(tile, i, j);
				BasicCommands.drawTile(out, tile, 0);
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
		BasicCommands.drawUnit(out, avatar, avatarTile);
		avatar.setOwner(player);
		avatarTile.setUnit(avatar);
	}

	// remove highlight from all tiles
	public void removeHighlightFromAll(Board board) {
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

	// highlight tiles for movement
	public void highlightMoveRange(Unit unit, Board board) {
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
						continue; // Skip tiles with friendly units
					} else {
						updateTileHighlight(targetTile, 2);
					}
				} else {
					updateTileHighlight(targetTile, 1);
				}
			}
		}
	}

    // highlight tiles for summoning units (does not currently take into account special units)
	public void highlightSummonRange(Card card, Board board, Player player) {
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
		return tile.getHighlightMode() == 1;
	}

	public void updateUnitPositionAndMove(Unit unit, Tile newTile, Board board) {
		// get position of unit and find the tile it is on
		Position position = unit.getPosition();
		Tile currentTile = board.getTile(position.getTilex(), position.getTiley());

		// update unit position
		currentTile.removeUnit();
		newTile.setUnit(unit);
		unit.setPositionByTile(newTile);

		// remove highlight from all tiles
		removeHighlightFromAll(board);

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
    public void removeCardFromHandAndSummonUnit(Board board, Card card, Tile tile, Hand hand, int handPosition) {
		if (board == null || card == null || tile == null || hand == null) {
			System.out.println("removeCardFromHandAndSummonUnit: One or more arguments are null");
			return;
		}
		if (handPosition < 1 || handPosition > hand.getNumberOfCardsInHand()) {
			System.out.println("removeCardFromHandAndSummonUnit: handPosition is out of bounds");
			return;
		}

		// remove card from hand
		BasicCommands.deleteCard(out, handPosition + 1);
		hand.removeCardAtPosition(handPosition);

		// update the positions of the remaining cards
		updateHandPositions(hand);

		// summon unit (should handle ids differently)
		Unit unit = loadUnit(card.getUnitConfig(), 3, Unit.class);
		if (unit == null) {
			System.out.println("removeCardFromHandAndSummonUnit: Failed to load unit");
			return;
		}

        // set unit position
        tile.setUnit(unit);
        unit.setPositionByTile(tile);

        // remove highlight from all tiles
        removeHighlightFromAll(board);

        // draw unit on new tile
        BasicCommands.drawUnit(out, unit, tile);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
