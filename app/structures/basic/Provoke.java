package structures.basic;

import java.util.HashSet;
import java.util.Set;
import structures.GameState;


public class Provoke extends Unit {
    private String name = null;


    /**
     * Returns the tile on which the provoke unit is positioned
     *	@param tile
     */
    public Set<Tile> attractAttack(Tile tile, GameState gameState){
        System.out.println("Attack Attracted");

        // How many provoke units does the enemy has on the boards
        Board board = gameState.getBoard();
        Set<Tile> validAttacks = new HashSet<>();
        validAttacks.add(board.getTiles()[this.getPosition().getTilex()][this.getPosition().getTiley()]);

        return validAttacks;
    }
    // Disables the movement of a provoked unit
    public void disableUnit(Unit other) {

        System.out.println("Unit disabled");
        other.setMovedThisTurn(true);

    }
}
