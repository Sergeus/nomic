package unit;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.imperial.presage2.core.util.random.Random;

import agents.NomicAgent;

public class NomicAgentTest {

	@Test
	public void test() {
		NomicAgent agent = new NomicAgent(Random.randomUUID(), "testAgent");
		
		assertTrue(agent.UnitTestTest() == 2);
	}
}
