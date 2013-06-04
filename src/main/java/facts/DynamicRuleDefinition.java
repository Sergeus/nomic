package facts;

import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import agents.NomicAgent;

public abstract class DynamicRuleDefinition extends RuleDefinition {
	
	public DynamicRuleDefinition(String name) {
		super(name, "");
	}

	public DynamicRuleDefinition(String name, String ruleContent) {
		super(name, ruleContent);
	}
	
	@Override
	public ProposeRuleChange getRuleChange(NomicAgent proposer) {
		String name = generateRuleName(proposer);
		String ruleContent = generateRuleBody(proposer);
		
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
	
	protected abstract String generateRuleName(NomicAgent proposer);
	
	protected abstract String generateRuleBody(NomicAgent proposer);
}
