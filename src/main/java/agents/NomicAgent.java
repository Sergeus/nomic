package agents;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

import services.NomicService;
import services.RuleClassificationService;
import services.ScenarioService;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import actions.ProposeRuleChange;
import actions.Vote;
import enums.VoteType;
import exceptions.NoExistentRuleChangeException;

public class NomicAgent extends AbstractParticipant {
	
	private int SequentialID;
	
	NomicService nomicService;
	
	ScenarioService scenarioService;
	
	RuleClassificationService ruleClassificationService;
	
	String MyRulesFile;
	
	Random rand = new Random();
	
	int points = 0;

	public NomicAgent(UUID id, String name) {
		super(id, name);
	}

	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(new ParticipantSharedState("test", getName(), getID()));
		return ss;
	}
	
	@Override
	public void initialise() {
		super.initialise();
		
		try {
			this.nomicService = getEnvironmentService(NomicService.class);
			this.scenarioService = getEnvironmentService(ScenarioService.class);
			this.ruleClassificationService = getEnvironmentService(RuleClassificationService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Couldn't get Nomic Environment Service.", e);
		}
	}
	
	@Override
	public void incrementTime() {
		logger.info("I have " + getPoints() + " points.");
		if (nomicService.canProposeNow(this)) {
			logger.info("It's my turn to propose a rule!");
			doRuleChanges();
		}
		else if (nomicService.canVoteNow(this)) {
			try {
				doVoting();
			} catch (NoExistentRuleChangeException e) {
				logger.warn("Even though I can vote now, there is no rule to change.");
			}
		}
		else {
			logger.info("It isn't my turn, and we're not voting.");
		}
		super.incrementTime();
	}
	
	private void doRuleChanges() {
		ProposeRuleChange myChange = chooseProposal();
		
		if (myChange == null) {
			logger.info("No rule changes from me this turn.");
		}
		else {
			logger.info("I propose the following rule change: " + myChange);
			
			try {
				environment.act(myChange, getID(), authkey);
			} catch (ActionHandlingException e) {
				logger.warn("My rule change proposal has failed. Proposal: " + myChange, e);
			}
		}
	}
	
	protected ProposeRuleChange chooseProposal() {
		return null;
	}
	
	private void doVoting() throws NoExistentRuleChangeException {
		ProposeRuleChange ruleChange = nomicService.getCurrentRuleChange();
		
		Vote vote = new Vote(this, chooseVote(ruleChange));
		boolean success = false;
		try {
			environment.act(vote, getID(), authkey);
			success = true;
		} catch (ActionHandlingException e) {
			logger.warn("My attempt to vote " + vote.getVote() + " has failed.");
			e.printStackTrace();
		}
		
		if (success) {
			logger.info("I am voting " + vote.getVote() + " for this rule change.");
		}
	}
	
	public int getSequentialID() {
		return SequentialID;
	}
	
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		if (rand.nextBoolean()) {
			return VoteType.YES;
		}
		else {
			return VoteType.NO;
		}
	}
	
	public VoteType chooseVoteFromProbability(Integer chance) {
		if (rand.nextInt(100) < chance) {
			return VoteType.YES;
		}
		else {
			return VoteType.NO;
		}
	}
	
	public boolean isPreferred(Integer chance) {
		if (rand.nextInt(100) < chance) {
			return true;
		}
		else {
			return false;
		}
	}

	public void setSequentialID(int sequentialID) {
		SequentialID = sequentialID;
	}

	public int getPoints() {
		return points;
	}

	public synchronized void setPoints(int points) {
		this.points = points;
	}
	
	public void voteSucceeded(ProposeRuleChange ruleChange) {
		
	}
	
	public void voteFailed(ProposeRuleChange ruleChange) {
		
	}
	
	public void Win() {
		nomicService.Win(this);
	}
	
	public ProxyAgent getRepresentativeProxy() {
		ProxyAgent proxy = new ProxyAgent(uk.ac.imperial.presage2.core.util.random.Random.randomUUID(), 
				"proxy " + getName());
		proxy.setOwner(this);
		proxy.setPoints(getPoints());
		proxy.setSequentialID(getSequentialID());
		
		return proxy;
	}
	
	public int getSubsimulationLength(ProposeRuleChange ruleChange) {
		return 10;
	}
	
	public String getProxyRulesFile() {
		return "src/main/resources/TestProxy.drl";
	}
	
	public int getNumSubSimsRun() {
		return scenarioService.getNumSubSimsRun();
	}
	
	public int getAverageSubSimLength() {
		return scenarioService.getAverageSubSimLength();
	}
}
