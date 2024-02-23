package structures.basic;

import java.util.Iterator;
import java.util.LinkedHashSet;
import structures.basic.cards.*;

@SuppressWarnings({ "serial", "hiding" })
public class Deck<Card> extends LinkedHashSet<Card>{ 

	public Card pop() {
		Iterator <Card> iterator= iterator();
		if(iterator.hasNext()) {
			Card card = iterator.next();
			iterator.remove();
			return card;
		} else {		
			return null;
		}
		
		
	}
	
	public boolean isEmpty() {
		return super.isEmpty();
	}
	
    public Card drawCard() {
        return pop();
    }

}
