package mock;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import agents.Agent;
import agents.NomicAgent;

@RunWith(JMock.class)
public class MockTest extends TestCase {
	Mockery context = new JUnit4Mockery();
	
	@Test
	public void mockTestInitialTest() {
		final Agent agent = context.mock(Agent.class);
		
		FakeSimulation simulation = new FakeSimulation(agent);
		
		context.checking(new Expectations() {{
			oneOf(agent).incrementTime();
		}});
		
		simulation.run();
		
		context.assertIsSatisfied();
	}
}
