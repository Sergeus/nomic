package actions;

import enums.RuleChangeType;

public abstract class ProposeRuleChange extends TimeStampedAction {
	protected RuleChangeType Type;
	
	public ProposeRuleChange() {
		super();
	}
	
	public RuleChangeType getRuleChangeType() {
		return Type;
	}
}
