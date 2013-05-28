package agents;

import java.util.UUID;

import org.drools.runtime.StatefulKnowledgeSession;

import actions.ProposeRuleChange;
import enums.VoteType;

public class SelfishAgent extends NomicAgent {

	public SelfishAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void incrementTime() {
		super.incrementTime();
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		logger.info("Run subsimulation for rule query now. Wish me luck.");
		scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
		
		logger.info("This simulation had a preference of: " + scenarioService.getPreference());
		
		return chooseVoteFromProbability(scenarioService.getPreference());
	}
	
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/SelfishProxy.drl";
	}
}
