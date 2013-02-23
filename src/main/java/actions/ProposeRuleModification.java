package actions;

import agents.NomicAgent;
import enums.RuleChangeType;
import uk.ac.imperial.presage2.core.Action;

public class ProposeRuleModification extends ProposeRuleChange implements Action {
	String newRule;
	
	String oldRuleName;
	
	String oldRulePackage;

	public ProposeRuleModification(NomicAgent agent, 
			String newRule, String oldRuleName, String oldRulePackage) {
		super(agent);
		this.newRule = newRule;
		this.oldRuleName = oldRuleName;
		this.oldRulePackage = oldRulePackage;
		Type = RuleChangeType.MODIFICATION;
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
}
