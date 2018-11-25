package multiset;

import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HashMultiSetTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testbuildFromCollection() {
	    exception.expect(IllegalArgumentException.class);
	    exception.expectMessage("Method should be invoked with a non null file path");
	    HashMultiSet<String, Integer> hmSet = new HashMultiSet<>();
	    hmSet.buildFromCollection(null);
	}
	
	@Test
	public void testElementFrequency() {
	    HashMultiSet<Integer, Integer> hmSet = new HashMultiSet<>();
	    hmSet.addElement(1);
	    hmSet.addElement(1);	    
	    assertEquals("Equal", true, hmSet.getElementFrequency(1) == 2);
	}

}
