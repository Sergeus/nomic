package actions;

import agents.NomicAgent;
import enums.RuleChangeType;
import uk.ac.imperial.presage2.core.Action;

public class ProposeRuleModification extends ProposeRuleChange implements Action {
	String newRuleName;
	
	String newRule;
	
	String oldRuleName;
	
	String oldRulePackage;

	public ProposeRuleModification(NomicAgent agent, 
			String newRuleName, String newRule, String oldRuleName, String oldRulePackage) {
		super(agent);
		this.newRuleName = newRuleName;
		this.newRule = newRule;
		this.oldRuleName = oldRuleName;
		this.oldRulePackage = oldRulePackage;
		Type = RuleChangeType.MODIFICATION;
	}

	public String getNewRuleName() {
		return newRuleName;
	}
	
	public String getNewRule() {
		return newRule;
	}
	
	public String getOldRuleName() {
		return oldRuleName;
	}
	
	public String getOldRulePackage() {
		return oldRulePackage;
	}
	
	@Override
	public String toString() {
		return super.toString() + getOldRuleName() + " replaced by " + getNewRuleName();
	}
}
