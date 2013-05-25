package actions;

import agents.NomicAgent;
import enums.RuleChangeType;

public class ProposeRuleAddition extends ProposeRuleChange {
	String newRuleName;
	
	String newRule;
	
	public ProposeRuleAddition(NomicAgent agent, String newRuleName, String newRule) {
		super(agent);
		this.newRuleName = newRuleName;
		this.newRule = newRule;
		Type = RuleChangeType.ADDITION;
	}
	
	public String getNewRuleName() {
		return newRuleName;
	}
	
	public String getNewRule() {
		return newRule;
	}
}
