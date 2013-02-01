package actions;

import agents.NomicAgent;
import enums.RuleChangeType;
import uk.ac.imperial.presage2.core.Action;

public class ProposeRuleModification extends ProposeRuleChange implements Action {
	NomicAgent agent;
	
	String newRule;
	
	String oldRuleName;
	
	String oldRulePackage;

	public ProposeRuleModification(int t, NomicAgent agent, 
			String newRule, String oldRuleName, String oldRulePackage) {
		super(t);
		this.agent = agent;
		this.newRule = newRule;
		this.oldRuleName = oldRuleName;
		this.oldRulePackage = oldRulePackage;
		Type = RuleChangeType.MODIFICATION;
	}

	public NomicAgent getAgent() {
		return agent;
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
