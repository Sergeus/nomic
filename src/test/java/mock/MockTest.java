package mock;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import agents.NomicAgent;

public class MockTest extends TestCase {
	Mockery context = new Mockery();
	
	@Test
	public void mockTestInitialTest() {
		final NomicAgent agent = context.mock(NomicAgent.class);
		
		FakeSimulation simulation = new FakeSimulation(agent);
		
		context.checking(new Expectations() {{
			oneOf(agent).incrementTime();
		}});
		
		simulation.run();
		
		context.assertIsSatisfied();
	}
}
