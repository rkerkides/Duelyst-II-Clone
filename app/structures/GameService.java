package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;

import static utils.BasicObjectBuilders.loadUnit;

public class GameService {
	private ActorRef out;

	public GameService(ActorRef out) {
		this.out = out;
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
				Tile currentTile = tiles[i][j];
				currentTile.setHighlightMode(0);
				BasicCommands.drawTile(out, currentTile, 0);
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
    public void highlightSummonRange(Card card, Board board) {
        Tile[][] tiles = board.getTiles();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                Tile currentTile = tiles[i][j];
                // Check if tile is occupied by a friendly unit
                if (currentTile.isOccupied() && currentTile.getUnit().getOwner() == card.getOwner()) {
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
                                // Highlight the tile if it's not occupied
                                if (!adjTile.isOccupied()) {
                                    updateTileHighlight(adjTile, 1); // Use 1 for summonable highlight mode
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
	
	public void drawingCards(int cardsToDraw, int handPosition) {

	    int currentCardIndex = 0;

	    for (int i = 0; i < cardsToDraw; i++) {
	        Card card = OrderedCardLoader.getPlayer1Cards(1).get(currentCardIndex);
	        BasicCommands.drawCard(out, card, handPosition, 0);
	        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
	        handPosition++;
	        currentCardIndex++;
	    }
	}

    /*// remove card from hand and summon unit
    public void removeCardFromHandAndSummonUnit(Board board, Card card, Tile tile, Hand hand) {
        // remove card from hand
        hand.removeCard(card);

        // summon unit (should handle ids differently)
        Unit unit = loadUnit(card.getUnitConfig(), 3, Unit.class);

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
    }*/
}
