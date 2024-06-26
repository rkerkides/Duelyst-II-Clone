package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameService;
import structures.GameState;
import structures.basic.Unit;

import java.util.ArrayList;

public class ShadowWatcher extends Unit{

    // method to check if shadow watcher is on the board and increment attack and health by 1 each time someone dies.
    public static void ShadowWatcherDeathwatch(ActorRef out, GameState gameState, GameService gs) {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getName().equals("Shadow Watcher")) {
                System.out.println("Found ShadowWatcher");
                gs.updateUnitAttack(unit, unit.getAttack() + 1);
                gs.updateUnitHealth(unit, unit.getHealth() + 1);
                
            }
        }

    }
}