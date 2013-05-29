package agents;

import java.util.UUID;

import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import enums.VoteType;

public class DestructiveAgent extends NomicAgent {
	
	int votesRequired;
	
	String MajorityRule;
	
	String IWinRule;
	
	public DestructiveAgent(UUID id, String name) {
		super(id, name);
	}
	
	@Override
	public void initialise() {
		super.initialise();
		
		IWinRule = "import agents.NomicAgent; "
				+ "import facts.*; "
				+ "global org.apache.log4j.Logger logger "
				+ "rule \"Agent " + getSequentialID() + " Wins\""
				+ "when "
				+ 	"$agent : NomicAgent($id : sequentialID, $id == " + getSequentialID() + ") "
				+ "then "
				+	"logger.info(\"Agent" + getSequentialID() + " Wins\"); "
				+	"insert(new Win($agent)); "
				+ "end";
	}
	
	private void UpdateMajorityRule() {
		MajorityRule = "import agents.NomicAgent; "
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
				+ 	"eval($n.intValue() >= " + (votesRequired - 1) + ") "
				+	"$turn : Turn(number == $turnNumber, $turnNumber >= ($agents.intValue() * 2)) "
				+	"$ruleChange : ProposeRuleChange(t == $turnNumber, succeeded == false) "
				+"then "
				+	"logger.info(\"Majority vote succeeded\"); "
				+	"modify($ruleChange) { "
				+		"setSucceeded(true); "
				+	"}; "
				+	"end";
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
	public void incrementTime() {
		if (getTime().intValue() == 0) {
			votesRequired = (int) (Math.floor(nomicService.getNumberOfAgents() / 2) + 1);
			
			logger.info("Setting votes required to " + votesRequired + ".");
		}
		
		super.incrementTime();
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
	
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/DestructiveProxy.drl";
	}
}
