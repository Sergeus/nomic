package agents;

import java.util.Collection;
import java.util.UUID;

import org.drools.definition.rule.Rule;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;

import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import enums.VoteType;

public class DestructiveAgent extends NomicAgent {
	
	int votesRequired;
	
	String MajorityRule = "import agents.NomicAgent; "
		+	"import actions.Vote; "
		+	"import actions.ProposeRuleChange; "
		+	"import enums.VoteType; "
		+	"import facts.*; "
		+	"global org.apache.log4j.Logger logger "
		+"rule \"Majority votes succeed after second round\" "
		+ "when "
		+	"$vote : Vote($turnNumber : t, vote == VoteType.YES) "
		+	"$n : Number() from accumulate ( $sgc : Vote(t == $turnNumber, vote == VoteType.YES) count( $sgc ) )" 
		+	"$agents :  Number() from accumulate ( $sgc : NomicAgent( ) count( $sgc ) ) "
		+ 	"eval($n.intValue() > " + (votesRequired - 1) + ") "
		+	"$turn : Turn(number == $turnNumber, $turnNumber >= ($agents.intValue() * 2)) "
		+	"$ruleChange : ProposeRuleChange(t == $turnNumber, succeeded == false) "
		+"then "
		+	"logger.info(\"Majority vote succeeded\"); "
		+	"modify($ruleChange) { "
		+		"setSucceeded(true); "
		+	"}; "
		+	"end";
	
	String IWinRule = "import agents.NomicAgent; "
			+ "global org.apache.log4j.Logger logger "
			+ "rule \"Agent " + getSequentialID() + " Wins\""
			+ "when "
			+ 	"$agent : NomicAgent($id : sequentialID, $id == " + getSequentialID() + ") "
			+ "then "
			+	"logger.info(\"Agent" + getSequentialID() + " Wins\"); "
			+ "end";
	
	public DestructiveAgent(UUID id, String name) {
		super(id, name);
	}
	
	@Override
	public void incrementTime() {
		if (getTime().intValue() == 0) {
			votesRequired = (int) (Math.floor(nomicService.getNumberOfAgents() / 2) + 1);
			
			logger.info("Setting votes required to " + votesRequired + ".");
		}
		
		super.incrementTime();
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		if (ruleChange.getProposer().getID() == getID())
			return VoteType.YES;
		
		if (ruleChange instanceof ProposeRuleRemoval) {
			if (rand.nextInt(100) > 32) {
				return VoteType.YES;
			}
			else {
				return VoteType.NO;
			}
		}
		else {
			return super.chooseVote(ruleChange);
		}
	}
	
	@Override
	protected void doRuleChanges() {
		boolean success = false;
		
		if (!success && votesRequired > 1) {
			Collection<Rule> rules = nomicService.getRules();
			
			for (Rule rule : rules) {
				if (rule.getName().compareTo("Majority votes succeed after second round") == 0) {
					ProposeRuleModification modification = new ProposeRuleModification(this, 
							MajorityRule, rule.getName(), rule.getPackageName());
					
					logger.info("Trying to reduce the number of votes required from " 
					+ votesRequired + " to " + (votesRequired - 1) + ".");
					
					try {
						environment.act(modification, getID(), authkey);
						success = true;
					} catch (ActionHandlingException e) {
						logger.warn("My rule modification failed. :(", e);
					}
				}
			}
		}
		else if (!success && votesRequired <= 1) {
			ProposeRuleAddition addition = new ProposeRuleAddition(this, IWinRule);
			logger.info("None of you can oppose me now! I propose a winning rule change!");
			
			try {
				environment.act(addition, getID(), authkey);
				success = true;
			} catch (ActionHandlingException e) {
				logger.warn("Oops, that was anticlimactic.", e);
			}
		}
		else if (rand.nextBoolean() && !success) {
			Collection<Rule> rules = nomicService.getRules();
			
			Rule removedRule = (Rule) rules.toArray()[rand.nextInt(rules.size())];
			
			ProposeRuleRemoval removal = new ProposeRuleRemoval(this, 
					removedRule.getName(), removedRule.getPackageName());
			
			logger.info("I propose removing the rule \'" + removedRule.getName()
					+ "\'.");
			try {
				environment.act(removal, getID(), authkey);
				success = true;
			} catch (ActionHandlingException e) {
				logger.warn("Failed to modify rule.", e);
			}
		}
		
		if (!success) {
			super.doRuleChanges();
		}
	}
	
	@Override
	public void voteSucceeded(ProposeRuleChange ruleChange) {
		if (ruleChange.getProposer().getID() == getID()
				&& ruleChange instanceof ProposeRuleModification
				&& ((ProposeRuleModification)ruleChange).getNewRule().compareTo(MajorityRule) == 0) {
			votesRequired--;
			logger.info("I see the number of votes required has decreased!");
		}
		
		super.voteSucceeded(ruleChange);
	}
}
