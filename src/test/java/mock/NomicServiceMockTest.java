package mock;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.compiler.DroolsParserException;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import services.NomicService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;
import actions.ProposeRuleAddition;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import agents.NomicAgent;

@SuppressWarnings("deprecation")
@RunWith(JMock.class)
public class NomicServiceMockTest extends TestCase {
	Mockery context = new JUnit4Mockery();
	
	final EnvironmentSharedStateAccess ss = context.mock(EnvironmentSharedStateAccess.class);
	final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
	final EventBus e = context.mock(EventBus.class);
	final KnowledgeBase base = context.mock(KnowledgeBase.class);
	
	final String correctRule = "import agents.NomicAgent "
			+ "rule \"Dynamic rule!\""
			+ "when"
			+ "	$agent : NomicAgent(SequentialID == 1)"
			+ "then"
			+ "	System.out.println(\"Found agent 1!\");"
			+ "end";
	
	@Test
	public void SingleStringRuleAdditionTest() {
		String newRule = correctRule;
		
		context.checking(new Expectations() {{
			oneOf(e).subscribe(with(any(NomicService.class)));
			oneOf(session).getKnowledgeBase(); will(returnValue(base));
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
	
	@Test
	public void MultipleStringRuleAdditionTest() {
		
		context.checking(new Expectations() {{
			exactly(1).of(e).subscribe(with(any(NomicService.class)));
			exactly(3).of(session).getKnowledgeBase(); will(returnValue(base));
			exactly(3).of(base).addKnowledgePackages(with(any(Collection.class)));
		}});
		
		final NomicService service = new NomicService(ss, session, e);
		
		ArrayList<String> imports = new ArrayList<String>();
		imports.add("agents.NomicAgent");
		
		String ruleName = "Dynamic Rule!";
		
		ArrayList<String> conditions = new ArrayList<String>();
		conditions.add("$agent : NomicAgent(SequentialID == 1)");
		
		ArrayList<String> actions = new ArrayList<String>();
		actions.add("System.out.println(\"Found agent 1!\");");
		
		try {
			service.addRule(imports, ruleName, conditions, actions);
		} catch (DroolsParserException e1) {
			fail("Simple rule was not parsed directly." + e1.getMessage());
		}
		
		conditions.add("$agent2 : NomicAgent(SequentialID == 2)");
		
		try {
			service.addRule(imports, ruleName, conditions, actions);
		} catch (DroolsParserException e2) {
			fail("Multiple condition failure.\n" + e2.getMessage());
		}
		
		actions.add("System.out.println(\"Testing multiple actions\");");
		
		try {
			service.addRule(imports, ruleName, conditions, actions);
		} catch (DroolsParserException e2) {
			fail("Multiple actions failure.\n" + e2.getMessage());
		}
		
		context.assertIsSatisfied();
	}
	
	@Test
	public void RemoveRuleTest() {
		
		final String packageName = "testPackage";
		final String ruleName = "testRule";
		
		context.checking(new Expectations() {{
			oneOf(e).subscribe(with(any(NomicService.class)));
			oneOf(session).getKnowledgeBase(); will(returnValue(base));
			oneOf(base).removeRule(packageName, ruleName);
			oneOf(session).fireAllRules();
		}});
		
		NomicService service = new NomicService(ss, session, e);
		service.RemoveRule(packageName, ruleName);
		
		context.assertIsSatisfied();
	}
	
	@Test
	public void ApplyProposedRuleTest() {
		context.setImposteriser(ClassImposteriser.INSTANCE);
		
		final NomicAgent mockAgent = context.mock(NomicAgent.class);
		
		final String newRule = correctRule;
		
		ProposeRuleAddition addition = new ProposeRuleAddition(mockAgent, newRule);
		
		context.checking(new Expectations() {{
			oneOf(e).subscribe(with(any(NomicService.class)));
			oneOf(session).getKnowledgeBase(); will(returnValue(base));
			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
		}});
		
		NomicService service = new NomicService(ss, session, e);
		service.ApplyRuleChange(addition);
		
		context.assertIsSatisfied();
		
		final String oldRuleName = "Old Rule Name";
		final String oldRulePackage = "Old Rule Package";
		ProposeRuleRemoval removal = new ProposeRuleRemoval(mockAgent, oldRuleName, oldRulePackage);
		
		context.checking(new Expectations() {{
			oneOf(session).getKnowledgeBase(); will(returnValue(base));
			oneOf(base).removeRule(oldRulePackage, oldRuleName);
			oneOf(session).fireAllRules();
		}});
		
		service.ApplyRuleChange(removal);
		
		context.assertIsSatisfied();
		
		ProposeRuleModification modification = new ProposeRuleModification(mockAgent, newRule, oldRuleName, oldRulePackage);
		
		context.checking(new Expectations() {{
			exactly(2).of(session).getKnowledgeBase(); will(returnValue(base));
			oneOf(base).removeRule(oldRulePackage, oldRuleName);
			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
			oneOf(session).fireAllRules();
		}});
		
		service.ApplyRuleChange(modification);
		
		context.assertIsSatisfied();
	}
}
