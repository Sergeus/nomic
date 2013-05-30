package agents;

import java.util.ArrayList;
import java.util.UUID;

import actions.ProposeNoRuleChange;
import actions.ProposeRuleChange;
import actions.ProposeRuleRemoval;
import enums.RuleFlavor;
import enums.VoteType;
import facts.RuleDefinition;

public class VindictiveAgent extends NomicAgent {
	
	String nemesisName;

	public VindictiveAgent(UUID id, String name) {
		super(id, name);
	}
	
	@Override
	public void initialise() {
		//ChooseNemesis();
		
		super.initialise();
	}
	
	public void ChooseNemesis() {
		ArrayList<UUID> IDs = new ArrayList<UUID>();
		
		for (UUID pid : nomicService.getAgentIDs()) {
			if (pid != getID())
				IDs.add(pid);
		}
		
		UUID nemesis = IDs.get(rand.nextInt(IDs.size()));
		
		nemesisName = nomicService.getAgentName(nemesis);
		
		logger.info(nemesisName + " is my nemesis! Damn you, " + nemesisName + "!");
	}
	
	@Override
	protected ProposeRuleChange chooseProposal() {
		// First we'll see if we like the current state of things
		ProposeNoRuleChange noChange = new ProposeNoRuleChange(this);
		scenarioService.RunQuerySimulation(noChange, getSubsimulationLength(noChange));
		
		// If we're fine with the way things are going, let's add some rules
		if (isPreferred(scenarioService.getPreference())) {
			ArrayList<RuleFlavor> flavors = new ArrayList<RuleFlavor>();
			flavors.add(RuleFlavor.SIMPLE);
			flavors.add(RuleFlavor.WINCONDITION);
			flavors.add(RuleFlavor.BENEFICIAL);
			
			ArrayList<RuleDefinition> rules = ruleClassificationService.getAllInActiveRulesWithFlavors(flavors);
			
			RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
			
			ProposeRuleChange ruleChange = definition.getRuleChange(this);
			
			scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
			
			if (isPreferred(scenarioService.getPreference())) {
				return ruleChange;
			}
		}
		else {
			// If the subsim was won and we didn't like it, then our nemesis must have won! We must stop him!
			if (scenarioService.isSimWon()) {
				ArrayList<RuleFlavor> flavors = new ArrayList<RuleFlavor>();
				flavors.add(RuleFlavor.DESPERATION);
				
				ArrayList<RuleDefinition> rules = ruleClassificationService.getAllInActiveRulesWithFlavors(flavors);
				
				
				if (!rules.isEmpty()) {
					RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
					
					ProposeRuleChange ruleChange = definition.getRuleChange(this);
					
					scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
					
					if (isPreferred(scenarioService.getPreference())) {
						return ruleChange;
					}
				}
				// We have no desperation tactics to use, let's just remove a win condition and hope
				else {
					ArrayList<RuleDefinition> winRules = ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.WINCONDITION);
					
					if (!winRules.isEmpty()) {
						RuleDefinition definition = winRules.get(rand.nextInt(winRules.size()));
						
						ProposeRuleRemoval winRemoval = new ProposeRuleRemoval(this, definition.getName(), RuleDefinition.RulePackage);
						
						return winRemoval;
					}
				}
			}
			// If the subsim wasn't won, then let's try modifying/removing rules that can help our nemesis
			else {
				ArrayList<RuleFlavor> dislikedFlavors = new ArrayList<RuleFlavor>();
				dislikedFlavors.add(RuleFlavor.BENEFICIAL);
				dislikedFlavors.add(RuleFlavor.STABLE);
				
				ArrayList<RuleDefinition> rules = ruleClassificationService.getInActiveRulesThatReplaceActiveRulesWithFlavors(dislikedFlavors);
				
				// We can modify one of the rules that might be helping our nemesis. Let's do it!
				if (!rules.isEmpty()) {
					RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
					
					ProposeRuleChange ruleChange = definition.getRuleChange(this);
					
					scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
					
					if (isPreferred(scenarioService.getPreference())) {
						return ruleChange;
					}
				}
				// There aren't any modifiable rules that might be helping our nemesis, so let's remove one that might be
				else {
					ArrayList<RuleDefinition> removalRules = ruleClassificationService.getAllActiveRulesWithFlavors(dislikedFlavors);
					
					if (!removalRules.isEmpty()) {
						RuleDefinition definition = removalRules.get(rand.nextInt(removalRules.size()));
						
						ProposeRuleChange ruleChange = definition.getRuleChange(this);
						
						scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
						
						if (isPreferred(scenarioService.getPreference())) {
							return ruleChange;
						}
					}
				}
			}
		}
		
		return super.chooseProposal();
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
		
		logger.info("This vindictive subsimulation had a preference of: " + scenarioService.getPreference());
		
		return chooseVoteFromProbability(scenarioService.getPreference());
	}
	
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/VindictiveProxy.drl";
	}
	
	@Override
	public ProxyAgent getRepresentativeProxy() {
		VindictiveProxyAgent proxy = new VindictiveProxyAgent(uk.ac.imperial.presage2.core.util.random.Random.randomUUID(), 
				"proxy " + getName());
		proxy.setOwner(this);
		proxy.setPoints(getPoints());
		proxy.setSequentialID(getSequentialID());
		proxy.setNemesisName(nemesisName);
		
		return proxy;
	}

}
