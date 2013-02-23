package actions;

import agents.NomicAgent;
import enums.RuleChangeType;

public class ProposeRuleAddition extends ProposeRuleChange {
	String newRule;
	
	public ProposeRuleAddition(NomicAgent agent, String newRule) {
		super(agent);
		this.newRule = newRule;
		Type = RuleChangeType.ADDITION;
	}
	
	public String getNewRule() {
		return newRule;
	}
}
