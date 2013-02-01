package actions;

import agents.NomicAgent;
import enums.RuleChangeType;

public class ProposeRuleAddition extends ProposeRuleChange {
	NomicAgent agent;
	
	String newRule;
	
	public ProposeRuleAddition(int t, NomicAgent agent, String newRule) {
		super(t);
		this.agent = agent;
		this.newRule = newRule;
		Type = RuleChangeType.ADDITION;
	}

	public NomicAgent getAgent() {
		return agent;
	}

	public String getNewRule() {
		return newRule;
	}
}
