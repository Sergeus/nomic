package services;

import java.util.ArrayList;

import facts.RuleDefinition;

public class RuleStringRepository {
	
	private ArrayList<RuleDefinition> Rules;
	
	public RuleStringRepository() {
		Init();
	}
	
	public ArrayList<RuleDefinition> getRules() {
		return Rules;
	}
	
	/**
	 * Please forgive me for this function.
	 */
	private void Init() {
		
		Rules = new ArrayList<RuleDefinition>();
		
		String imports = " import agents.*; "
				+ " import actions.*; "
				+ " import facts.*; "
				+ " import enums.*; ";
		
		// ----------------------------------------------------------------
		String firstName = "Whose turn is it";
		String firstRule = imports
				+ " rule \"Whose turn is it\" "
				+ " when "
				+ " $agent : NomicAgent($ID : sequentialID) "
				+ " $n : Number() from accumulate ( $sgc : NomicAgent( ) count( $sgc ) ) "
				+ " $turn : Turn($n > 0 && (number % $n.intValue()) == ($ID) && activePlayer != $agent) "
			+ " then "
				+ " logger.info(\"It's this guy's turn: \" + $agent.getName()); "
				+ " modify ($turn) { "
					+ " setActivePlayer($agent) "
				+ " }; "
		+ " end ";
		
		RuleDefinition firstDefinition = new RuleDefinition(firstName, firstRule);
		firstDefinition.setFlavors(50, 0, 80, 50, 75, 50, 100);
		Rules.add(firstDefinition);
		
		// ----------------------------------------------------------------
		String secondName = "Unanimous votes succeed for first two rounds";
		String secondRule = imports
				+ " rule \"Unanimous votes succeed for first two rounds\" "
				+ " when "
				+ " $vote : Vote($turnNumber : t, vote == VoteType.YES) "
				+ " $n : Number() from accumulate ( $sgc : Vote(t == $turnNumber, vote == VoteType.YES) count( $sgc ) ) "
				+ " $agents :  Number() from accumulate ( $sgc : NomicAgent( ) count( $sgc ) ) "
				+ " eval($agents.intValue() == $n.intValue()) "
				+ " $turn : Turn(number == $turnNumber, $turnNumber < ($agents.intValue() * 2)) "
				+ " $ruleChange : ProposeRuleChange(t == $turnNumber, succeeded == false) "
			+ " then "
				+ " logger.info(\"Unanimous vote succeeded\"); "
				+ " modify($ruleChange) { "
					+ " setSucceeded(true); "
				+ " }; "
		+ " end ";
		
		RuleDefinition secondDefinition = new RuleDefinition(secondName, secondRule);
		secondDefinition.setFlavors(60, 10, 50, 50, 55, 50, 60);
		Rules.add(secondDefinition);
		
		// ----------------------------------------------------------------
		String thirdName = "Majority votes succeed after second round";
		String thirdRule = imports
				+ " rule \"Majority votes succeed after second round\" "
				+ " salience 20 "
				+ " when "
					+ " $vote : Vote($turnNumber : t, vote == VoteType.YES) "
					+ " $n : Number() from accumulate ( $sgc : Vote(t == $turnNumber, vote == VoteType.YES) count( $sgc ) ) "
					+ " $agents :  Number() from accumulate ( $sgc : NomicAgent( ) count( $sgc ) ) "
					+ " eval($n.intValue() > ($agents.intValue() / 2)) "
					+ " $turn : Turn(number == $turnNumber, $turnNumber >= ($agents.intValue() * 2)) "
					+ " $ruleChange : ProposeRuleChange(t == $turnNumber, succeeded == false) "
				+ " then "
					+ " logger.info(\"Majority vote succeeded\"); "
					+ " modify($ruleChange) { "
						+ " setSucceeded(true); "
					+ " }; "
			+ " end ";
		
		RuleDefinition thirdDefinition = new RuleDefinition(thirdName, thirdRule);
		thirdDefinition.setFlavors(50, 0, 50, 50, 60, 50, 100);
		Rules.add(thirdDefinition);
		
		// ----------------------------------------------------------------
		String fourthName = "All Agents Must Vote";
		String fourthRule = imports
				+ "rule \"All Agents Must Vote\" "
				+ " when "
				+ " $turn : Turn($turnNumber : number, allVoted == false) "
				+ " $n : Number() from accumulate ( $sgc : Vote(t == $turnNumber) count( $sgc ) ) "
				+ " $agents : Number() from accumulate ( $a : NomicAgent( ) count( $a ) ) "
				+ " eval($agents.intValue() == $n.intValue()) "
			+ " then "
				+ " logger.info(\"All agents have voted.\"); "
				+ " modify($turn) { "
					+ " setAllVoted(true); "
				+ " }; "
		+ " end ";
		
		RuleDefinition fourthDefinition = new RuleDefinition(fourthName, fourthRule);
		fourthDefinition.setFlavors(50, 0, 55, 50, 60, 50, 100);
		Rules.add(fourthDefinition);
		
		// ----------------------------------------------------------------
		String fifthName = "Each Agent Can Only Vote Once Per Turn";
		String fifthRule = imports
				+ " rule \"Each Agent Can Only Vote Once Per Turn\" "
				+ " when "
				+ " $vote : Vote($turnNumber : t, $agent: voter, $id : voteID) "
				+ " $vote2 : Vote(t == $turnNumber, voter.getID() == $agent.getID(), voteID != $id) "
			+ " then "
				+ " logger.info($agent.getName() + \" has attempted to vote twice, and been refused.\"); "
				+ " logger.info(\"Turn number: \" + $turnNumber); "
				+ " retract($vote2); "
		+ " end ";
		
		RuleDefinition fifthDefinition = new RuleDefinition(fifthName, fifthRule);
		fifthDefinition.setFlavors(50, 10, 60, 50, 65, 50, 70);
		Rules.add(fifthDefinition);
		
		// ----------------------------------------------------------------
		String sixthName = "Winner Is First To 100 Points";
		String sixthRule = imports
				+ " rule \"Winner Is First To 100 Points\" "
				+ " when "
				+ " $agent : NomicAgent(points >= 100) "
			+ " then "
				+ " logger.info($agent.getName() + \" wins.\"); "
				+ " insert(new Win($agent)); "
		+ " end ";
		
		RuleDefinition sixthDefinition = new RuleDefinition(sixthName, sixthRule);
		sixthDefinition.setFlavors(20, 0, 80, 0, 80, 100, 50);
		Rules.add(sixthDefinition);
		
		// ----------------------------------------------------------------
		String seventhName = "Proposing a losing vote costs you 10 points";
		String seventhRule = imports
				+ " rule \"Proposing a losing vote costs you 10 points\" " 
				+ " salience 10 "
				+ " when "
					+ " $turn : Turn($turnNumber : number, allVoted == true) "
					+ " $ruleChange : ProposeRuleChange(t == $turnNumber, succeeded == false, $agent : proposer) "
				+ " then "
					+ " logger.info($agent.getName() + \" loses 10 points for proposing a failed rule!\"); "
					+ " modify($agent) { "
						+ " setPoints($agent.getPoints() - 10); "
					+ " }; "
			+ " end ";
		
		RuleDefinition seventhDefinition = new RuleDefinition(seventhName, seventhRule);
		seventhDefinition.setFlavors(40, 60, 50, 50, 0, 50, 50);
		Rules.add(seventhDefinition);
		
		// ----------------------------------------------------------------
		String eighthName = "Voting against a successful proposal gains you 10 points";
		String eighthRule = imports
				+ " rule \"Voting against a successful proposal gains you 10 points\" "
				+ " salience 10 "
				+ " when "
					+ " $turn : Turn($turnNumber : number, allVoted == true) "
					+ " $vote : Vote(vote == VoteType.NO, $agent : voter, t == $turnNumber) "
					+ " $ruleChange : ProposeRuleChange(t == $turnNumber, succeeded == true) "
				+ " then "
					+ " logger.info($agent.getName() + \" gains 10 points for voting against a successful proposal.\"); "
					+ " modify($agent) { "
						+ " setPoints($agent.getPoints() + 10); "
					+ " }; "
			+ " end ";
		
		RuleDefinition eighthDefinition = new RuleDefinition(eighthName, eighthRule);
		eighthDefinition.setFlavors(80, 40, 30, 50, 30, 50, 50);
		Rules.add(eighthDefinition);
		
		// ----------------------------------------------------------------
		String ninthName = "You gain 1d6 points at the end of your turn";
		String ninthRule = imports
				+ " rule \"You gain 1d6 points at the end of your turn\" "
				+ " salience 10 "
				+ " when "
					+ " $turn : Turn(allVoted == true, $agent : activePlayer) "
				+ " then "
					+ " if (rand == null) "
						+ " rand = new java.util.Random(); "
					+ " int r = rand.nextInt(6) + 1; "
					+ " logger.info($agent.getName() + \" gains \" + r + \" points at the end of their turn.\"); "
					+ " modify($agent) { "
						+ " setPoints($agent.getPoints() + r); "
					+ " }; "
			+ " end ";
			
		RuleDefinition ninthDefinition = new RuleDefinition(ninthName, ninthRule);
		ninthDefinition.setFlavors(30, 20, 75, 50, 80, 50, 50);
		Rules.add(ninthDefinition);
		
		// ALL INITIALLY ACTIVE RULES ARE ABOVE
		for (RuleDefinition definition : Rules) {
			definition.setActive(true);
		}
		
		String ReverseOrderName = "Backwards Turns";
		String ReverseOrderRule = "import agents.NomicAgent "
				+ "import facts.* "
				+ "global org.apache.log4j.Logger logger "
				+ "rule \"Backwards Turns\" "
				+ "when"
				+ "	$agent : NomicAgent($ID : sequentialID)"
				+ "	$n : Number() from accumulate ( $sgc : NomicAgent( ) count( $sgc ) )"
				+ "	$turn : Turn($n > 0 && (($n.intValue() - 1)  - (number % $n.intValue())) == ($ID) && activePlayer != $agent)"
				+ "then"
				+ "	logger.info(\"It's this guy's turn: \" + $agent.getName());"
				+ "	modify ($turn) {"
				+ "		setActivePlayer($agent)"
				+ "	}; "
				+ "end";
		
		RuleDefinition backwardsTurns = new RuleDefinition(ReverseOrderName, ReverseOrderRule);
		backwardsTurns.setReplacesOther(true);
		backwardsTurns.setOtherName("Whose turn is it");
		backwardsTurns.setFlavors(60, 45, 50, 50, 50, 50, 100);
		Rules.add(backwardsTurns);
	}
}
