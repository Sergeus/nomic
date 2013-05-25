package unit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.imperial.presage2.core.util.random.Random;
import actions.ProposeRuleAddition;
import agents.NomicAgent;

public class NomicAgentTest {
	@Test
	public void ChooseVoteTest() {
		NomicAgent agent = new NomicAgent(Random.randomUUID(), "testAgent");
		
		ProposeRuleAddition ruleChange = new ProposeRuleAddition(agent, "Test", "Test Rule");
		
		assertTrue(agent.chooseVote(ruleChange).getClass().isEnum());
	}
}
