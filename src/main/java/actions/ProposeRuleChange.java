package actions;

import agents.NomicAgent;
import enums.RuleChangeType;

public abstract class ProposeRuleChange extends TimeStampedAction {
	protected NomicAgent proposer;
	
	protected RuleChangeType Type;
	
	protected boolean succeeded;
	
	public ProposeRuleChange(NomicAgent proposer) {
		super();
		this.proposer = proposer;
	}
	
	public RuleChangeType getRuleChangeType() {
		return Type;
	}
	
	public boolean getSucceeded() {
		return succeeded;
	}
	
	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
		System.out.println("Successful proposal object from " + proposer.getName());
	}

	public NomicAgent getProposer() {
		return proposer;
	}
}
