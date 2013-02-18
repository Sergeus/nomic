package actions;

import enums.RuleChangeType;

public abstract class ProposeRuleChange extends TimeStampedAction {
	protected RuleChangeType Type;
	
	protected boolean succeeded;
	
	public ProposeRuleChange() {
		super();
	}
	
	public RuleChangeType getRuleChangeType() {
		return Type;
	}
	
	public boolean getSucceeded() {
		return succeeded;
	}
	
	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
	}
}
