package actions;

import enums.RuleChangeType;
import agents.NomicAgent;

public class ProposeRuleRemoval extends ProposeRuleChange {
	String oldRuleName, oldRulePackage;
	
	public ProposeRuleRemoval(NomicAgent agent, String oldRuleName, String oldRulePackage) {
		super(agent);
		this.oldRuleName = oldRuleName;
		this.oldRulePackage = oldRulePackage;
		this.Type = RuleChangeType.REMOVAL;
	}

	public String getOldRuleName() {
		return oldRuleName;
	}

	public String getOldRulePackage() {
		return oldRulePackage;
	}
	
	@Override
	public String toString() {
		return super.toString() + getOldRuleName();
	}
}
