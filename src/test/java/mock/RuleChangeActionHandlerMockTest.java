package mock;

import junit.framework.TestCase;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.PortableInterceptor.SUCCESSFUL;

import services.NomicService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import actionHandlers.RuleChangeActionHandler;

@RunWith(JMock.class)
public class RuleChangeActionHandlerMockTest extends TestCase {
	Mockery context = new JUnit4Mockery();
	
	final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
	final EnvironmentServiceProvider serviceProvider = context.mock(EnvironmentServiceProvider.class);
	final EventBus e = context.mock(EventBus.class);
	final EnvironmentSharedStateAccess sharedState = context.mock(EnvironmentSharedStateAccess.class);
	
	@Test
	public void canHandleTest() {
		context.assertIsSatisfied();
	}
	
	@Test
	public void handleTest() {
		context.setImposteriser(ClassImposteriser.INSTANCE);
		
		final NomicService service = context.mock(NomicService.class);
		
		try {
			context.checking(new Expectations() {{
				oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); will(returnValue(service));
			}});
		} catch (UnavailableServiceException e1) {
			fail("Mock object generated an exception?");
		}
		
		RuleChangeActionHandler handler = new RuleChangeActionHandler(session, serviceProvider);
	}
}
