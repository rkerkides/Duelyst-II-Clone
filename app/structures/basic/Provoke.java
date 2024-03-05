package structures.basic;

import java.util.HashSet;
import java.util.Set;
import structures.GameState;


public class Provoke {
    private String name = null;
    private Set<Tile> validAttacks;


    /**
     * Returns the tile on which the provoke unit is positioned
     *	@param tile
     */
    public Set<Tile> attractAttack(Tile tile){

        /*
         * All this can be deleted...
         */
        System.out.println("Attack Attracted");
        /*
         * How many provoke units does the enemy has on the boards
         */
        validAttacks = new HashSet<>();
        validAttacks.add(GameState.board[this.getPosition().getTilex()][this.getPosition().getTiley()]);

        return validAttacks;
    }
    /**
     * Disables the movement of a provoked unit
     * @param other
     */
    public void disableUnit(Unit other) {

        System.out.println("Unit disabled");
        other.setMoved();

    }
}
