package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import actions.ProposeNoRuleChange;
import actions.ProposeRuleChange;
import actions.ProposeRuleRemoval;

import enums.RuleFlavor;
import enums.VoteType;
import facts.RuleDefinition;

public class HarmoniousAgent extends NomicAgent {

	public HarmoniousAgent(UUID id, String name) {
		super(id, name);
	}
	
	@Override
	protected Map<RuleFlavor, Integer> chooseFlavorPreferences() {
		Map<RuleFlavor, Integer> preferences = new HashMap<RuleFlavor, Integer>();
		
		preferences.put(RuleFlavor.BENEFICIAL, 90);
		preferences.put(RuleFlavor.STABLE, 75);
		preferences.put(RuleFlavor.WINCONDITION, 75);
		
		preferences.put(RuleFlavor.DETRIMENTAL, 5);
		
		return preferences;
	}
	
	@Override
	protected ProposeRuleChange chooseProposal() {
		
		// First let's see if we like the current state of affairs
		ProposeNoRuleChange noChange = new ProposeNoRuleChange(this);
		scenarioService.RunQuerySimulation(noChange, getSubsimulationLength(noChange));
		
		// If we like the way things are going, let's add some beneficial and stability rules
		// to keep it this way while helping others at the same time
		if (isPreferred(scenarioService.getPreference())) {
			ArrayList<RuleFlavor> flavors = new ArrayList<RuleFlavor>();
			flavors.add(RuleFlavor.BENEFICIAL);
			flavors.add(RuleFlavor.STABLE);
			flavors.add(RuleFlavor.WINCONDITION);
			
			ArrayList<RuleDefinition> rules = ruleClassificationService.getAllInActiveRulesWithFlavors(flavors);
			
			// If there are rules we can add, let's choose one, make sure it's good and then
			// propose it if it is
			if (!rules.isEmpty()) {
				RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
				
				ProposeRuleChange ruleChange = definition.getRuleChange(this);
				
				scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
				
				if (isPreferred(scenarioService.getPreference())) {
					return ruleChange;
				}
			}
			// Otherwise there are no beneficial rules to add to the game, let's remove some 
			// destructive/unstable ones
			else {
				ArrayList<RuleFlavor> dislikedFlavors = new ArrayList<RuleFlavor>();
				dislikedFlavors.add(RuleFlavor.DESTRUCTIVE);
				dislikedFlavors.add(RuleFlavor.DETRIMENTAL);
				
				ArrayList<RuleDefinition> dislikedRules = ruleClassificationService.getAllActiveRulesWithFlavors(dislikedFlavors);
				
				dislikedRules.addAll(ruleClassificationService.getAllActiveRulesWithLowFlavor(RuleFlavor.STABLE));
				
				if (!dislikedRules.isEmpty()) {
					RuleDefinition definition = dislikedRules.get(rand.nextInt(dislikedRules.size()));
					
					ProposeRuleRemoval removal = new ProposeRuleRemoval(this, definition.getName(), RuleDefinition.RulePackage);
					
					scenarioService.RunQuerySimulation(removal, getSubsimulationLength(removal));
					
					if (isPreferred(scenarioService.getPreference())) {
						return removal;
					}
				}
			}
		}
		// Otherwise we must be doing better than people, and we wouldn't want to put ourselves above them
		else {
			// If we didn't like the subsim and it was won, we must have won it, so we need to sabotage ourselves somehow
			if (scenarioService.isSimWon()) {
				// Removing win conditions seems like a good idea
				
				ArrayList<RuleDefinition> winRules = ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.WINCONDITION);
				
				// winRules is basically guaranteed not to be empty, this is just to make sure
				// we don't cause a nullPointerException if something goes wrong (eg. badly
				// assigned flavors).
				// Conceptually, if we've won the subsim, then there must be some active
				// win condition rule that makes us win it.
				if (!winRules.isEmpty()) {
					RuleDefinition winRule = winRules.get(rand.nextInt(winRules.size()));
					
					ProposeRuleRemoval removal = new ProposeRuleRemoval(this, winRule.getName(), RuleDefinition.RulePackage);
					
					// If there are win conditions we can remove, anything is preferrable to
					// us stealing victory from someone else, so we'll just remove the win
					// condition without running a subsim. If we did run a subsim there's a chance
					// we'd decide not to do anything (if that sim wasn't preferred for some reason)
					// but anything (even a bad future) is better than us winning, because we're
					// selfless.
					return removal;
				}
			}
			// It's better to promote positive gain for others than introduce negatives 
			// in the hopes of holding ourselves back, so we'll try to bring in some beneficial
			// rule changes and other win conditions, in the hopes someone else will benefit
			// from them more than us. (Probability shows it's more likely for a given 
			// universally beneficial rule to help any one opponent rather than us, because there
			// are many opponents.)
			else {
				ArrayList<RuleFlavor> favoredFlavors = new ArrayList<RuleFlavor>();
				favoredFlavors.add(RuleFlavor.BENEFICIAL);
				favoredFlavors.add(RuleFlavor.WINCONDITION);
				favoredFlavors.add(RuleFlavor.STABLE);
				
				ArrayList<RuleDefinition> rules = ruleClassificationService.getAllInActiveRulesWithFlavors(favoredFlavors);
				
				// If there are beneficial or win condition rules we can add, let's test
				// they're good and then do that
				if (!rules.isEmpty()) {
					RuleDefinition definition = rules.get(rand.nextInt(rules.size()));
					ProposeRuleChange ruleChange = definition.getRuleChange(this);
					
					scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
					
					if (isPreferred(scenarioService.getPreference())) {
						return ruleChange;
					}
				}
				// Otherwise, let's remove some detrimental or destructive rules
				else {
					ArrayList<RuleFlavor> dislikedFlavors = new ArrayList<RuleFlavor>();
					dislikedFlavors.add(RuleFlavor.DESTRUCTIVE);
					dislikedFlavors.add(RuleFlavor.DETRIMENTAL);
					
					ArrayList<RuleDefinition> dislikedRules = ruleClassificationService.getAllActiveRulesWithFlavors(dislikedFlavors);
					dislikedRules.addAll(ruleClassificationService.getAllActiveRulesWithLowFlavor(RuleFlavor.STABLE));
					
					if (!dislikedRules.isEmpty()) {
						RuleDefinition removalDefinition = dislikedRules.get(rand.nextInt(dislikedRules.size()));
						
						ProposeRuleRemoval removal = new ProposeRuleRemoval(this, removalDefinition.getName(), RuleDefinition.RulePackage);
						
						scenarioService.RunQuerySimulation(removal, getSubsimulationLength(removal));
						
						if (isPreferred(scenarioService.getPreference())) {
							return removal;
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
		
		return chooseVoteFromProbability(scenarioService.getPreference());
	}
	 
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/HarmoniousProxy.drl";
	}
}
