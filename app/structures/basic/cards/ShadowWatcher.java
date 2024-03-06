package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameService;
import structures.GameState;
import structures.basic.Unit;

import java.util.ArrayList;

public class ShadowWatcher extends Unit{

    int ShadowWatcherCount = 0;

    // method to check if shadow watcher is on the board and increment attack and health by 1 each time someone dies.
    public static void ShadowWatcherDeathwatch(ActorRef out, GameState gameState, GameService gs) {
        ArrayList<Unit> units = (ArrayList<Unit>) gameState.getAi().getUnits();
        units.addAll(gameState.getHuman().getUnits());
        for (Unit unit : units) {
            if (unit.getName().equals("Shadow Watcher")) {
                System.out.println("Found ShadowWatcher");
                int newHealth = unit.getHealth() + 1;
                int newAttack = unit.getAttack() + 1;
                gs.updateUnitAttack(unit, newAttack);
                gs.updateUnitHealth(unit, newHealth);
                break;
            }
        }

    }
}