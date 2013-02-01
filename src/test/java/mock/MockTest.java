package mock;

import java.util.Collection;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.compiler.DroolsParserException;
import org.drools.definition.KnowledgePackage;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.RunWith;

import services.NomicService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;

import agents.Agent;

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
	
	@Test
	public void NomicServiceSingleStringRuleAdditionTest() {
		String newRule = "import agents.NomicAgent "
				+ "rule \"Dynamic rule!\""
				+ "when"
				+ "	$agent : NomicAgent(SequentialID == 1)"
				+ "then"
				+ "	System.out.println(\"Found agent 1!\");"
				+ "end";
		
		final EnvironmentSharedStateAccess ss = context.mock(EnvironmentSharedStateAccess.class);
		final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
		final EventBus e = context.mock(EventBus.class);
		final KnowledgeBase base = context.mock(KnowledgeBase.class);
		
		context.checking(new Expectations() {{
			oneOf(e).subscribe(with(any(NomicService.class)));
			oneOf(session).getKnowledgeBase(); returnValue(base);
			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
		}});
		
		final NomicService service = new NomicService(ss, session, e);
		
		try {
			service.addRule(newRule);
		} catch (DroolsParserException e1) {
			fail("Rule was not parsed correctly.");
		}
		
		context.assertIsSatisfied();
	}
}
