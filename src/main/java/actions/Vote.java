package actions;

import agents.NomicAgent;
import enums.VoteType;

public class Vote extends TimeStampedAction {
	protected NomicAgent Voter;
	
	protected VoteType Vote;
	
	protected Vote(NomicAgent voter, VoteType vote) {
		Voter = voter;
		
		Vote = vote;
	}

	public NomicAgent getVoter() {
		return Voter;
	}

	public VoteType getVote() {
		return Vote;
	}
}
