package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameService;
import structures.GameState;
import structures.basic.Unit;

public class BloodmoonPriestess {




    // method to check if bad omen is onthe board and increment attack each time someone dies.

    public static void BloodmoonPriestessDeathwatch( ActorRef out, GameState gameState, GameService gs) {

        for(Unit unit:gameState.getUnits()){
            System.out.println(unit.getName()+ " is on the board");
            if (unit.getName().equals("Bloodmoon Priestess")){
                System.out.println("Bloodmoon Priestess is on the board");
                Wraithling.summonWraithlingForBloodmoonPriestess(unit, out, gameState, gs);
                //Wraithling.wraihtlingStats(out, gameState, gs);
                break;
            }
        }
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();
        }
    }
}

