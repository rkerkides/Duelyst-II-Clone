import org.junit.Test;

import events.Initalize;
import structures.GameState;

/**
 * MyTest
 */
public class HumanPlayerTests {

  @Test
  public void checkHealth() {

    GameState gameState = new GameState(); // create state storage
    Initalize initalizeProcessor = new Initalize(); // create an initalize event processor

    ObjectNode eventMessage = Json.newObject(); // create a dummy message
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

  }

}
