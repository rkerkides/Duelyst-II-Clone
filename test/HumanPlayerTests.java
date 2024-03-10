import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import events.Initalize;
import play.libs.Json;
import structures.GameState;

/**
 * MyTest
 */
public class HumanPlayerTests {

  @Test
  public void initialStats() {

    GameState gameState = new GameState(); // create state storage
    Initalize initalizeProcessor = new Initalize(); // create an initalize event processor

    ObjectNode eventMessage = Json.newObject(); // create a dummy message
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    assertEquals(gameState.getHuman().getHealth(), 20 );
    assertEquals(gameState.getHuman().getMana(), 2);
    assertEquals(gameState.getHuman().getHand().getNumberOfCardsInHand(), 3);

  }

}
