package actions;

import enums.RuleChangeType;
import agents.NomicAgent;

public class ProposeNoRuleChange extends ProposeRuleChange {

	public ProposeNoRuleChange(NomicAgent proposer) {
		super(proposer);
		Type = RuleChangeType.NONE;
	}

}
