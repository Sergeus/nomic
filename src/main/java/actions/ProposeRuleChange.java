package actions;

import agents.NomicAgent;
import enums.RuleChangeType;

/**
 * Parent class to structure common elements in all rule change proposals.
 * If trying to make a new proposal, you want to use <code>ProposeRuleAddition</code>,
 * <code>ProposeRuleRemoval</code>, <code>ProposalRuleModification</code>, or <code>ProposeNoRuleChange</code>.
 * 
 * Proposals are only that, proposals. They will not be applied to the currently active rules until voting
 * has taken place (unless some previous inventive rule changes has removed the need to vote).
 * @author Stuart Holland
 *
 */
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
	
	@Override
	public String toString() {
		return getRuleChangeType().toString() + " ";
	}
}
