package agents;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

import actions.ProposeNoRuleChange;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import enums.RuleFlavor;
import enums.VoteType;
import facts.RuleDefinition;

public class DestructiveAgent extends NomicAgent {
	
	int votesRequired;
	
	String MajorityRule;
	
	String IWinRule;
	
	public DestructiveAgent(UUID id, String name) {
		super(id, name);
	}
	
	@Override
	protected ProposeRuleChange chooseProposal() {
		// First we'll check if we like the current state of affairs
		ProposeNoRuleChange noChange = new ProposeNoRuleChange(this);
		scenarioService.RunQuerySimulation(noChange, getSubsimulationLength(noChange));
		
		// We're happy with what everything's like now, so let's add some destructive rules
		if (isPreferred(scenarioService.getPreference())) {
			ArrayList<RuleDefinition> rules = ruleClassificationService.getAllInActiveRulesWithFlavor(RuleFlavor.DESTRUCTIVE);
			
			if (!rules.isEmpty()) {
				RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
				
				ProposeRuleChange ruleChange = definition.getRuleChange(this);
				
				scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
				
				if (isPreferred(scenarioService.getPreference())) {
					return ruleChange;
				}
			}
			// Nothing destructive to do? How about desperation?
			else {
				ArrayList<RuleDefinition> desperationRules = ruleClassificationService.getAllInActiveRulesWithFlavor(RuleFlavor.DESPERATION);
				
				if (!desperationRules.isEmpty()) {
					RuleDefinition definition = desperationRules.get(rand.nextInt(desperationRules.size()));
					
					ProposeRuleChange ruleChange = definition.getRuleChange(this);
					
					scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
					
					if (isPreferred(scenarioService.getPreference())) {
						return ruleChange;
					}
				}
			}
		}
		// We're unhappy with the current state of affairs
		else {
			// We're destructive, we don't want anyone to win! Remove the win conditions!
			if (scenarioService.isSimWon()) {
				ArrayList<RuleDefinition> winRules = ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.WINCONDITION);
				
				if (!winRules.isEmpty()) {
					RuleDefinition definition = winRules.get(rand.nextInt(winRules.size()));
					
					ProposeRuleRemoval winRemoval = new ProposeRuleRemoval(this, definition.getName(), RuleDefinition.RulePackage);
					return winRemoval;
				}
			}
			// Otherwise this sim could do with some instability, let's remove some stable rules
			else {
				ArrayList<RuleDefinition> stableRules = ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.STABLE);
				
				if (!stableRules.isEmpty()) {
					RuleDefinition definition = stableRules.get(rand.nextInt(stableRules.size()));
					
					ProposeRuleRemoval stableRemoval = new ProposeRuleRemoval(this, definition.getName(), RuleDefinition.RulePackage);
					return stableRemoval;
				}
			}
		}
		
		// If we've gotten this far, this agent can't decide what to do, so give up on proposals for this turn
		return super.chooseProposal();
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		
		// Let's see if we like this proposal
		scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
		
		return chooseVoteFromProbability(scenarioService.getPreference());
	}
	
	@Override
	public void incrementTime() {
		if (getTime().intValue() == 0) {
			votesRequired = (int) (Math.floor(nomicService.getNumberOfAgents() / 2) + 1);
			
			logger.info("Setting votes required to " + votesRequired + ".");
		}
		
		super.incrementTime();
	}
	
	@Override
	public void voteSucceeded(ProposeRuleChange ruleChange) {
		if (ruleChange.getProposer().getID() == getID()
				&& ruleChange instanceof ProposeRuleModification
				&& ((ProposeRuleModification)ruleChange).getNewRule().compareTo(MajorityRule) == 0) {
			votesRequired--;
			logger.info("I see the number of votes required has decreased!");
		}
		
		super.voteSucceeded(ruleChange);
	}
	
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/DestructiveProxy.drl";
	}
}
