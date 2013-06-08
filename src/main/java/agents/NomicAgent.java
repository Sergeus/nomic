package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import actions.ProposeNoRuleChange;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;
import enums.RuleFlavor;
import enums.VoteType;
import exceptions.NoExistentRuleChangeException;
import facts.RuleDefinition;

/**
 * Parent class for all agents that wish to play Nomic. For an example of which functions should be overriden
 * to define agent AI behavior, see <code>ExampleAgent</code>.
 * @author Stuart Holland
 *
 */
public class NomicAgent extends AbstractParticipant {
	
	private int SequentialID;
	
	NomicService nomicService;
	
	ScenarioService scenarioService;
	
	RuleClassificationService ruleClassificationService;
	
	Random rand = new Random();
	
	int points = 0;
	
	Map<RuleFlavor, Integer> Flavors;

	public NomicAgent(UUID id, String name) {
		super(id, name);
	}
	
	/**
	 * This is part of an incomplete functionality set that allows agents to define their preferences for certain
	 * flavors at a class level.
	 * @return
	 */
	protected Map<RuleFlavor, Integer> chooseFlavorPreferences() {
		return new HashMap<RuleFlavor, Integer>();
	}

	@Override
	protected void processInput(Input arg0) {
		
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
	/**
	 * Most agent AIs should not override this function (though it is left non-final for cases where that may be
	 * useful). The default behavior will query chooseProposal() and chooseVote(ProposeRuleChange) for information
	 * when it is relevant.
	 */
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
	
	/**
	 * New Agent AIs should override this function and use it to decide on a new rule change proposal.
	 * By default it will return a blank proposal that leads to this agent's turn to propose changes being skipped.
	 * @return
	 */
	protected ProposeRuleChange chooseProposal() {
		return new ProposeNoRuleChange(this);
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
	
	/**
	 * New agent AIs should override this function to decide on their vote for the parameter proposed rule change.
	 * 
	 * By default, voting will be random 50/50 for Yes/No.
	 * @param ruleChange
	 * @return
	 */
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		if (rand.nextBoolean()) {
			return VoteType.YES;
		}
		else {
			return VoteType.NO;
		}
	}
	
	/**
	 * Given an integer, x, where 0 <= x <= 100, this function will return YES or NO
	 * as a function of that number treated as a probability. (Eg. 40 is 40%)
	 * @param chance
	 * @return
	 */
	public VoteType chooseVoteFromProbability(Integer chance) {
		if (rand.nextInt(100) < chance) {
			return VoteType.YES;
		}
		else {
			return VoteType.NO;
		}
	}
	
	/**
	 * Given an integer, x, where 0 <= x <= 100, this function will return true or
	 * false defined by that probability. (Eg. a parameter of 60 means 60% chance of true)
	 * @param chance
	 * @return
	 */
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
	
	public void increasePoints(int points) {
		this.points += points;
	}
	
	public void decreasePoints(int points) {
		this.points -= points;
	}
	
	/**
	 * New agent AIs should override this function if they wish to have new behavior defined by any proposals
	 * succeeding.
	 * 
	 * Does nothing by default.
	 * @param ruleChange
	 */
	public void voteSucceeded(ProposeRuleChange ruleChange) {
		
	}
	
	/**
	 * New agent AIs should override this function if they wish to have new behavior defined by any proposals
	 * failing.
	 * 
	 * Does nothing by default.
	 * @param ruleChange
	 */
	public void voteFailed(ProposeRuleChange ruleChange) {
		
	}
	
	public void Win() {
		nomicService.Win(this);
	}
	
	/**
	 * New agent AIs should override this function if they want the proxy that represents their agent
	 * to have any special characteristics.
	 * 
	 * Creates a simple random voter and random proposer (instance of <code>ProxyAgent</code>) by default.
	 * @return
	 */
	public ProxyAgent getRepresentativeProxy() {
		ProxyAgent proxy = new ProxyAgent(uk.ac.imperial.presage2.core.util.random.Random.randomUUID(), 
				"proxy " + getName());
		proxy.setOwner(this);
		proxy.setPoints(getPoints());
		proxy.setSequentialID(getSequentialID());
		
		return proxy;
	}
	
	/**
	 * New agent AIs should override this function if they wish to have an easily defined function for 
	 * deciding on subsimulation length based on rule changes.
	 * 
	 * By default, uses the rule's COMPLEXITY flavor to decide length.
	 * @param ruleChange
	 * @return
	 */
	public int getSubsimulationLength(ProposeRuleChange ruleChange) {
		// Default to 0 complexity for 'ProposeNoRuleChange' proposals
		Integer complexity = 0;
		
		// Grab the complexity from the rule definitions
		switch (ruleChange.getRuleChangeType()) {
		case MODIFICATION :
			ProposeRuleModification modification = (ProposeRuleModification)ruleChange;
			
			RuleDefinition definition = ruleClassificationService.getRule(modification.getNewRuleName());
			complexity = definition.getFlavorAmount(RuleFlavor.COMPLEX);
			break;
		case ADDITION :
			ProposeRuleAddition addition = (ProposeRuleAddition)ruleChange;
			
			RuleDefinition addDefinition = ruleClassificationService.getRule(addition.getNewRuleName());
			complexity = addDefinition.getFlavorAmount(RuleFlavor.COMPLEX);
			break;
		case REMOVAL :
			ProposeRuleRemoval removal = (ProposeRuleRemoval)ruleChange;
			
			RuleDefinition remDefinition = ruleClassificationService.getRule(removal.getOldRuleName());
			complexity = remDefinition.getFlavorAmount(RuleFlavor.COMPLEX);
			break;
		}
		
		Integer NumAgents = nomicService.getNumberOfAgents();
		
		if (complexity < 33) {
			// Low complexity, only predict one round (plus our own turn again)
			return NumAgents * 2 + 2;
		}
		else if (complexity < 67) {
			// Middling complexity, let's do two rounds
			return NumAgents * 4 + 2;
		}
		else {
			// High complexity, three rounds!
			return NumAgents * 6 + 2;
		}
	}
	
	/**
	 * New agent AIs should override this function to tell the <code>ScenarioService</code> where to find
	 * the rules file that defines this AI's preferences.
	 * @return
	 */
	public String getProxyRulesFile() {
		return "src/main/resources/TestProxy.drl";
	}
	
	public int getNumSubSimsRun() {
		return scenarioService.getNumSubSimsRun();
	}
	
	public int getAverageSubSimLength() {
		return scenarioService.getAverageSubSimLength();
	}
	
	public ArrayList<RuleFlavor> getPositiveFlavors() {
		ArrayList<RuleFlavor> positives = new ArrayList<RuleFlavor>();
		
		for (RuleFlavor flavor : Flavors.keySet()) {
			if (Flavors.get(flavor) > 50)
				positives.add(flavor);
		}
		
		return positives;
	}
}
