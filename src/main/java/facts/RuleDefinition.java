package facts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import agents.NomicAgent;
import enums.RuleFlavor;
import exceptions.InvalidRuleStateException;


public class RuleDefinition {
	public static final String RulePackage = "defaultpkg";
	
	String name;
	String ruleContent;
	ArrayList<RuleDefinition> replacedRules;
	Map<RuleFlavor, Integer> Flavors;
	boolean Active;
	
	public RuleDefinition(String name, String ruleContent) {
		this.name = name;
		this.ruleContent = ruleContent;
		
		replacedRules = new ArrayList<RuleDefinition>();
		Flavors = new HashMap<RuleFlavor, Integer>();
	}
	
	public ProposeRuleChange getRuleChange(NomicAgent proposer) {
		if (isReplacesOther()) {
			String replacedName = null;
			
			for (RuleDefinition replacedDef : replacedRules) {
				if (replacedDef.isActive())
					replacedName = replacedDef.getName();
			}
			
			// the replaced rules might all have been removed
			if (replacedName == null) {
				return new ProposeRuleAddition(proposer, name, ruleContent);
			}
			
			return new ProposeRuleModification(proposer, name, ruleContent, replacedName, RulePackage);
		}
		else {
			return new ProposeRuleAddition(proposer, name, ruleContent);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRuleContent() {
		return ruleContent;
	}

	public void setRuleContent(String ruleContent) {
		this.ruleContent = ruleContent;
	}
	
	public int getFlavorAmount(RuleFlavor flavorType) {
		return Flavors.get(flavorType);
	}
	
	public RuleFlavor getPrevailingFlavor() {
		Integer maxFlavor = 50;
		RuleFlavor prevailingFlavor = null;
		for (RuleFlavor flavor : Flavors.keySet()) {
			
			Integer currentFlavor = Flavors.get(flavor);
			if (currentFlavor > maxFlavor) {
				maxFlavor = currentFlavor;
				prevailingFlavor = flavor;
			}
		}
		
		return prevailingFlavor;
	}
	
	public RuleFlavor getOpposingFlavor() {
		Integer minFlavor = 50;
		RuleFlavor lowFlavor = null;
		
		for (RuleFlavor flavor : Flavors.keySet()) {
			Integer currentFlavor = Flavors.get(flavor);
			
			if (currentFlavor < minFlavor) {
				minFlavor = currentFlavor;
				lowFlavor = flavor;
			}
		}
		
		return lowFlavor;
	}
	
	public boolean isHasPositiveFlavor(RuleFlavor flavor) {
		return Flavors.get(flavor) > 50;
	}
	
	public boolean isHasPositiveForFlavors(Collection<RuleFlavor> flavors) {
		for (RuleFlavor flavor : flavors) {
			if (Flavors.get(flavor) < 50)
				return false;
		}
		
		return true;
	}
	
	public void setFlavorAmount(RuleFlavor flavorType, Integer amount) {
		Flavors.put(flavorType, amount);
	}
	
	public void setFlavors(Integer complex, Integer destructive, Integer simple,
			Integer desperation, Integer beneficial, Integer wincondition,
			Integer stable, Integer detrimental) {
		Flavors.put(RuleFlavor.COMPLEX, complex);
		Flavors.put(RuleFlavor.DESTRUCTIVE, destructive);
		Flavors.put(RuleFlavor.SIMPLE, simple);
		Flavors.put(RuleFlavor.DESPERATION, desperation);
		Flavors.put(RuleFlavor.BENEFICIAL, beneficial);
		Flavors.put(RuleFlavor.WINCONDITION, wincondition);
		Flavors.put(RuleFlavor.STABLE, stable);
		Flavors.put(RuleFlavor.DETRIMENTAL, detrimental);
	}
	
	public Map<RuleFlavor, Integer> getFlavors() {
		return Flavors;
	}

	public boolean isReplacesOther() {
		return replacedRules.size() > 0;
	}
	
	public boolean isReplaces(String oldRuleName) {
		for (RuleDefinition definition : replacedRules) {
			if (definition.getName().equals(oldRuleName))
				return true;
		}
		
		return false;
	}
	
	public boolean isReplacesAny(ArrayList<RuleDefinition> rules) {
		if (!isReplacesOther())
			return false;
		
		for (RuleDefinition definition : rules) {
			if (isReplaces(definition.getName()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Thresholded check for this rule's propensity toward the parameter flavor.
	 * Basically, it's more strict than "isHasPositiveFlavor", requiring a higher
	 * affinity for the given flavor to return true.
	 * @param flavor
	 * @return
	 */
	public boolean is(RuleFlavor flavor) {
		return Flavors.get(flavor) > 67;
	}
	
	/**
	 * Counterpart to "is", can be used to work out if this rule is relatively neutral to the
	 * given flavor  
	 * @param flavor
	 * @return
	 */
	public boolean isNot(RuleFlavor flavor) {
		return Flavors.get(flavor) < 33;
	}
	
	/**
	 * If this rule is within an acceptable threshold of 50 propensity toward the given flavor,
	 * returns true.
	 * @param flavor
	 * @return
	 */
	public boolean isNeutral(RuleFlavor flavor) {
		return !is(flavor) && !isNot(flavor);
	}
	
	public void addReplacedRule(RuleDefinition rule) {
		replacedRules.add(rule);
	}

	public boolean isActive() {
		return Active;
	}

	public void setActive(boolean active) {
		Active = active;
	}
}
