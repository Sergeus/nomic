package actions;

import java.util.UUID;

import uk.ac.imperial.presage2.core.util.random.Random;
import agents.NomicAgent;
import enums.VoteType;

public class Vote extends TimeStampedAction {
	protected NomicAgent Voter;
	
	protected VoteType Vote;
	
	protected UUID voteID;
	
	public Vote(NomicAgent voter, VoteType vote) {
		Voter = voter;
		
		Vote = vote;
		
		voteID = Random.randomUUID();
	}

	public NomicAgent getVoter() {
		return Voter;
	}

	public VoteType getVote() {
		return Vote;
	}

	public UUID getVoteID() {
		return voteID;
	}
}
