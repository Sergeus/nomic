package actions;

import enums.RuleChangeType;

public abstract class ProposeRuleChange extends TimeStampedAction {
	protected RuleChangeType Type;
	
	public ProposeRuleChange(int time) {
		super(time);
	}
	
	public RuleChangeType getRuleChangeType() {
		return Type;
	}
}
