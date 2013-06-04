package mock;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import enums.RuleFlavor;
import facts.RuleDefinition;

import services.RuleClassificationService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;

@RunWith(JMock.class)
public class RuleClassificationServiceMockTest extends TestCase {
	Mockery context = new JUnit4Mockery();
	
	final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
	final EnvironmentServiceProvider serviceProvider = context.mock(EnvironmentServiceProvider.class);
	final EventBus e = context.mock(EventBus.class);
	final EnvironmentSharedStateAccess sharedState = context.mock(EnvironmentSharedStateAccess.class);
	
	final ArrayList<RuleFlavor> allFlavors = new ArrayList<RuleFlavor>();
	
	private void populateFlavors() {
		if (allFlavors.isEmpty()) {
			allFlavors.add(RuleFlavor.BENEFICIAL);
			allFlavors.add(RuleFlavor.COMPLEX);
			allFlavors.add(RuleFlavor.DESPERATION);
			allFlavors.add(RuleFlavor.DESTRUCTIVE);
			allFlavors.add(RuleFlavor.DETRIMENTAL);
			allFlavors.add(RuleFlavor.SIMPLE);
			allFlavors.add(RuleFlavor.STABLE);
			allFlavors.add(RuleFlavor.WINCONDITION);
		}
	}
	
	@Test
	public void getAnyRuleWithHighestFlavorTest() {
		populateFlavors();
		
		RuleClassificationService ruleClassService = new RuleClassificationService(sharedState, session);
		
		for (RuleFlavor flavor : allFlavors) {
			RuleDefinition definition = ruleClassService.getAnyRuleWithHighestFlavor(flavor);
			
			assertTrue("Flavor " + flavor.toString() + " for rule " + definition.getName() + " should be more than 50.", definition.getFlavorAmount(flavor) >= 50);
		}
	}
	
	@Test
	public void getActiveRuleWithHighestFlavorTest() {
		populateFlavors();
		
		RuleClassificationService ruleClassService = new RuleClassificationService(sharedState, session);
		
		for (RuleFlavor flavor : allFlavors) {
			RuleDefinition definition = ruleClassService.getActiveRuleWithHighestFlavor(flavor);
			
			assertTrue("Flavor " + flavor.toString() + " for rule " + definition.getName() + " should be more than 50.", definition.getFlavorAmount(flavor) >= 50);
			assertTrue("Rule " + definition.getName() + " should be active.", definition.isActive());
		}
	}
	
	@Test
	public void getInActiveRuleWithHighestFlavorTest() {
		populateFlavors();
		
		RuleClassificationService ruleClassService = new RuleClassificationService(sharedState, session);
		
		for (RuleFlavor flavor : allFlavors) {
			RuleDefinition definition = ruleClassService.getInActiveRuleWithHighestFlavor(flavor);
			
			assertTrue("Flavor " + flavor.toString() + " for rule " + definition.getName() + " should be more than 50.", definition.getFlavorAmount(flavor) >= 50);
			assertFalse("Rule " + definition.getName() + " should be inactive.", definition.isActive());
		}
	}
}
