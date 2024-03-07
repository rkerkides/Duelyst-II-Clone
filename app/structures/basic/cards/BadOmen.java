package structures.basic.cards;

import akka.actor.ActorRef;
import akka.stream.StreamRefMessages;
import commands.BasicCommands;
import structures.GameService;
import structures.GameState;
import structures.basic.BigCard;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import static utils.BasicObjectBuilders.loadUnit;
import static utils.StaticConfFiles.card_badOmen;

public class BadOmen extends Unit {

    int BadOmenCount = 0;


    // method to check if bad omen is onthe board and increment attack each time someone dies.
    public static void BadOmenDeathwatch( ActorRef out, GameState gameState, GameService gs) {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getName().equals("Bad Omen")) {
                gameState.removeFromTotalUnits(1); // method that removes one unit from the total count
                int newAttack = unit.getAttack() + 1;
                //BasicCommands.setUnitAttack(out, unit, unit.getAttack() + 1); //inceremnt attack if true;
                gs.updateUnitAttack(unit, newAttack);
                break;
            }
        }

        System.out.println("Units on the board: " + gameState.getUnitsOnBoard().size());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
