package agents;

import java.util.UUID;

import org.drools.runtime.StatefulKnowledgeSession;

import actions.ProposeRuleChange;
import enums.RuleFlavor;
import enums.VoteType;
import facts.RuleDefinition;

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
	protected ProposeRuleChange chooseProposal() {
		RuleDefinition definition = ruleClassificationService.getInActiveRuleWithHighestFlavor(RuleFlavor.WINCONDITION);
		
		if (definition != null) {
			ProposeRuleChange ruleChange = definition.getRuleChange(this);
			
			scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
			
			if (isPreferred(scenarioService.getPreference())) {
				return ruleChange;
			}
		}
		
		return super.chooseProposal();
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
