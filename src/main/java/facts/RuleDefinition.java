package facts;

import java.util.Currency;
import java.util.Map;

import enums.RuleFlavor;


public class RuleDefinition {
	String name;
	String ruleContent;
	boolean replacesOther;
	String otherName;
	Map<RuleFlavor, Integer> Flavors;
	boolean Active;
	
	public RuleDefinition(String name, String ruleContent) {
		this.name = name;
		this.ruleContent = ruleContent;
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
		Integer maxFlavor = -1;
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
	
	public void setFlavorAmount(RuleFlavor flavorType, Integer amount) {
		Flavors.put(flavorType, amount);
	}
	
	public Map<RuleFlavor, Integer> getFlavors() {
		return Flavors;
	}

	public boolean isReplacesOther() {
		return replacesOther;
	}

	public void setReplacesOther(boolean replacesOther) {
		this.replacesOther = replacesOther;
	}

	public String getOtherName() {
		return otherName;
	}

	public void setOtherName(String otherName) {
		this.otherName = otherName;
	}

	public boolean isActive() {
		return Active;
	}

	public void setActive(boolean active) {
		Active = active;
	}
}
