package services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DroolsParserException;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.Events;
import uk.ac.imperial.presage2.core.simulator.FinalizeEvent;
import uk.ac.imperial.presage2.core.util.random.Random;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;
import agents.NomicAgent;
import agents.ProxyAgent;

import com.google.inject.Inject;

import enums.RuleChangeType;
import enums.TurnType;
import exceptions.InvalidRuleProposalException;
import exceptions.NoExistentRuleChangeException;
import facts.Turn;

public class NomicService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * Controls access to the Drools KnowledgeBuilder class, because there were concurrency
	 * issues with creating many new knowledge packages simultaneously.
	 */
	public static Semaphore kBuilderSemaphore = new Semaphore(1);
	
	/**
	 * Controls 'refreshing' of the simulation. See refreshSession() for more details.
	 */
	public static Semaphore refreshSemaphore = new Semaphore(1);
	
	private EnvironmentServiceProvider serviceProvider;
	
	private RuleClassificationService ruleClassificationService;
	
	StatefulKnowledgeSession session;
	int TurnNumber = 0;
	
	/**
	 * List of all agents in this simulation.
	 */
	private ArrayList<NomicAgent> agents;
	
	/**
	 * List of all agent IDs in this simulation.
	 */
	private ArrayList<UUID> agentIDs;
	
	/**
	 * Map of votes made this turn.
	 */
	private Map<UUID,Vote> votesThisTurn;
	
	/**
	 * List of all votes made during this simulation.
	 */
	private ArrayList<Vote> SimVotes;
	
	/**
	 * List of all proposals from this simulation.
	 */
	private ArrayList<ProposeRuleChange> SimRuleChanges;
	
	/**
	 * Object that defines the state of the current turn.
	 */
	Turn currentTurn;
	
	/**
	 * Drools facthandle for above object.
	 */
	FactHandle turnHandle;
	
	/**
	 * Place holder agent used to instantiate the turn object at initialization without artificially choosing
	 * which agent will move first.
	 */
	NomicAgent placeHolderAgent = new NomicAgent(Random.randomUUID(), "placeholder");
	
	/**
	 * The rule change currently up for voting.
	 */
	ProposeRuleChange currentRuleChange;
	
	/**
	 * Previous rule change, from last turn.
	 */
	ProposeRuleChange previousRuleChange;
	
	/**
	 * The winner of this game of Nomic.
	 */
	NomicAgent Winner;
	
	/**
	 * The time this game was won.
	 */
	Integer WinTime = -1;
	
	EventBus eb;
	
	/**
	 * Nomic SimTime (not Presage2 time), maps to the defined phases of Nomic turns.
	 */
	Integer SimTime;
	
	@Inject
	public NomicService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider,
			StatefulKnowledgeSession session) {
		super(sharedState);
		this.session = session;
		this.serviceProvider = serviceProvider;
		
		currentTurn = new Turn(0, TurnType.INIT, placeHolderAgent);
		
		agents = new ArrayList<NomicAgent>();
		agentIDs = new ArrayList<UUID>();
		votesThisTurn = new HashMap<UUID, Vote>();
		SimVotes = new ArrayList<Vote>();
		SimRuleChanges = new ArrayList<ProposeRuleChange>();
		SimTime = 0;
	}
	
	@Inject
	public void SetEventBus(EventBus e) {
		e.subscribe(this);
		this.eb = e;
	}
	
	private RuleClassificationService getRuleClassificationService() {
		if (ruleClassificationService == null) {
			try {
				ruleClassificationService = serviceProvider.getEnvironmentService(RuleClassificationService.class);
			} catch (UnavailableServiceException e) {
				logger.warn("My attempts to get the rule classification service have been fruitless.", e);
			}
		}
		
		return ruleClassificationService;
	}
	
	@EventListener
	public void onInitialize(Events.Initialised e) {
		turnHandle = session.insert(currentTurn);
		
//		for (RuleDefinition definition : ruleClassificationService.getAllRules()) {
//			session.insert(definition);
//		}
	}
	
	@EventListener
	public void onIncrementTime(EndOfTimeCycle e) {
		// If someone has won, this game is over, let's stop everything.
		if (Winner != null) {
			currentTurn.setType(TurnType.GAMEOVER);
			currentRuleChange = null;
			previousRuleChange = null;
		}
		// If we're in initialization mode, then set up the first turn.
		else if (currentTurn.getType() == TurnType.INIT) {
			currentTurn.setType(TurnType.PROPOSE);
			currentTurn.setNumber(TurnNumber);
			SimTime++;
		}
		// Deals with proposal phase of a turn when we've received a proposal
		else if (currentTurn.getType() == TurnType.PROPOSE && currentRuleChange != null) {
			// If this is a non-blank proposal, move on to voting phase
			if (currentRuleChange.getRuleChangeType() != RuleChangeType.NONE) {
				currentTurn.setType(TurnType.VOTE);
				previousRuleChange = currentRuleChange;
				SimTime++;
			}
			// If this is a blank proposal, the agent couldn't decide what to do, so we skip them.
			else {
				currentTurn.setNumber(++TurnNumber);
				SimTime++;
			}
		}
		// Deals with voting phase of a turn
		else if (currentTurn.getType() == TurnType.VOTE) {
			// If the required number of agents have voted, let's evaluate the current proposal
			if (currentTurn.isAllVoted()) {
				votesThisTurn.clear();
				// Success is defined in the rules files, so we just need to check its value.
				if (currentRuleChange.getSucceeded()) {
					logger.info("This proposal has succeeded.");
					ApplyRuleChange(currentRuleChange);
					for (NomicAgent agent : agents) {
						agent.voteSucceeded(currentRuleChange);
					}
				}
				else {
					logger.info("This proposal has failed to pass.");
					for (NomicAgent agent : agents) {
						agent.voteFailed(currentRuleChange);
					}
				}
				// Move on to propose phase of the next turn.
				previousRuleChange = currentRuleChange;
				currentRuleChange = null;
				currentTurn.setType(TurnType.PROPOSE);
				currentTurn.setNumber(++TurnNumber);
				currentTurn.setAllVoted(false);
				SimTime++;
			}
		}
		
		refreshSession();
		
		// Update turn object for new turn (there may be no change if some agents are taking long)
		session.update(session.getFactHandle(currentTurn), currentTurn);
		
		session.fireAllRules();
		logger.info("Next move, turn: " + currentTurn.getNumber() + ", " + currentTurn.getType());
	}
	
	@EventListener
	public void onFinalizeEvent(FinalizeEvent e) {
		if (Winner != null) {
			logger.info("THIS SIMULATION'S WINNER IS: " + Winner.getName() + "!");
		}
		else {
			logger.info("THIS SIMULATION HAS NO WINNER!");
		}
	}
	
	private String TestRule = "import agents.Test; "
			+ " rule \"Refresher\" "
			+ " when "
			+ " Test( ) "
			+ " then "
			+ " end ";
	
	/**
	 * Workaround to deal with a bug in Drools. Moving between multiple instances of the Drools rule engine
	 * which share some rule definitions can lead to some cached data in the engine reporting incorrect
	 * loaded class definitions for those shared rules when the state differs between the two.
	 * 
	 * Loading and unloading a rule from the current knowledge base forces Drools to update its records of
	 * which rules are currently active. This reduces the prevalence of class definition errors, but does not
	 * eliminate them.
	 */
	public void refreshSession() {
		logger.info("Refreshing session.");
		
		while (!refreshSemaphore.tryAcquire()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("Waiting for refresh semaphore interrupted.", e);
			}
		}
		
		try {
			RemoveRule("defaultpkg", "Refresher");
			
			try {
				addRule(TestRule);
			} catch (DroolsParserException e) {
				logger.warn("Refreshing failed.", e);
			}
		} finally {
			refreshSemaphore.release();
		}
		
//		logger.info(session.getKnowledgeBase().getStatefulKnowledgeSessions().size());
//		
//		logger.info(session.getKnowledgeBase().getKnowledgePackage("Rules").getName());
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		agents.add((NomicAgent)req.getParticipant());
		agentIDs.add(req.getParticipantID());
		//session.insert(req.getParticipant());
		super.registerParticipant(req);
	}
	
	public Integer getSequentialID(UUID agentID) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == agentID)
				return agent.getSequentialID();
		}
		
		return -1;
	}
	
	public Integer getNumSubSimsRun(UUID agentID) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == agentID)
				return agent.getNumSubSimsRun();
		}
		
		return 0;
	}
	
	public Integer getAverageSubSimLength(UUID agentID) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == agentID)
				return agent.getAverageSubSimLength();
		}
		
		return 0;
	}
	
	/**
	 * True if the parameter agent is the active agent and we are in the proposal phase of any turn.
	 * @param agent
	 * @return
	 */
	public boolean canProposeNow(NomicAgent agent) {
		return currentTurn.getType() == TurnType.PROPOSE &&
				currentTurn.getActivePlayer().getID() == agent.getID();
	}
	
	/**
	 * True if we are in the voting phase of any turn.
	 * @param agent
	 * @return
	 */
	public boolean canVoteNow(NomicAgent agent) {
		return currentTurn.type == TurnType.VOTE;
	}
	
	/**
	 * True if all required agents have voted for the success of the current proposal to be evaluated.
	 * @return
	 */
	public boolean isAllVoted() {
		return currentTurn.isAllVoted();
	}
	
	public void RemoveRule(String packageName, String ruleName) {		
		session.getKnowledgeBase().removeRule(packageName, ruleName);
	}
	
	public void Vote(Vote vote) {
		votesThisTurn.put(vote.getVoter().getID(), vote);
		SimVotes.add(vote);
	}
	
	public void Win(NomicAgent agent) {
		logger.info(agent.getName() + " has won!");
		Winner = agent;
		WinTime = getSimTime();
	}
	
	/**
	 * Called by the <code>ProposeRuleChangeActionHandler</code> to register a proposal with the Nomic service.
	 * @param ruleChange
	 * @throws InvalidRuleProposalException If we are not in the proposal phase.
	 */
	public void ProposeRuleChange(ProposeRuleChange ruleChange) 
			throws InvalidRuleProposalException {
		if (currentTurn.type != TurnType.PROPOSE) {
			throw new InvalidRuleProposalException("This turn has passed its proposal stage.");
		}
		
		currentRuleChange = ruleChange;
		SimRuleChanges.add(ruleChange);
	}
	
	/**
	 * Returns the current rule change proposal.
	 * @return
	 * @throws NoExistentRuleChangeException If there is no valid rule change.
	 */
	public ProposeRuleChange getCurrentRuleChange() throws NoExistentRuleChangeException {
		if (currentRuleChange == null)
			throw new NoExistentRuleChangeException("There is no valid rule change proposition.");
		else 
			return currentRuleChange;
	}
	
	/**
	 * Applies the parameter rule change to the currently active Drools knowledge base.
	 * @param ruleChange
	 */
	public void ApplyRuleChange(ProposeRuleChange ruleChange) {
		logger.info("I am a Nomic Service applying a rule change.");
		logger.info("My agents are: ");
		for (NomicAgent agent : agents) {
			logger.info(agent.getName());
		}
		
		RuleChangeType change = ruleChange.getRuleChangeType();
		if (change == RuleChangeType.MODIFICATION) {
			ProposeRuleModification ruleMod = (ProposeRuleModification)ruleChange;
			try {
				logger.info("Modifying rule \'" + ruleMod.getOldRuleName()
						+ "\'");
				RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
				addRule(ruleMod.getNewRule());
				
				getRuleClassificationService().setActive(ruleMod.getOldRuleName(), false);
				getRuleClassificationService().setActive(ruleMod.getNewRuleName(), true);
			} catch (DroolsParserException e) {
				logger.warn("Unable to parse new version of existing rule.", e);
				// TODO: add old rule back
			}
		}
		else if (change == RuleChangeType.ADDITION) {
			ProposeRuleAddition ruleMod = (ProposeRuleAddition)ruleChange;
			try {
				logger.info("Adding new rule " + ruleMod.getNewRuleName());
				
				addRule(ruleMod.getNewRule());
				
				getRuleClassificationService().setActive(ruleMod.getNewRuleName(), true);
			} catch (DroolsParserException e) {
				logger.warn("Unable to parse new rule.", e);
			}
		}
		else if (change == RuleChangeType.REMOVAL) {
			ProposeRuleRemoval ruleMod = (ProposeRuleRemoval)ruleChange;
			
			logger.info("Removing old rule " + ruleMod.getOldRuleName());
			
			RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
			
			getRuleClassificationService().setActive(ruleMod.getOldRuleName(), false);
		}
		else if (change == RuleChangeType.NONE) {
			logger.info("Blank change for current forecasting has been 'applied'.");
		}
	}
	
	public void addRule(Collection<String> imports, String ruleName,
			Collection<String> conditions, Collection<String> actions)
					throws DroolsParserException {
		String rule = "";
		
		for(String importe : imports) {
			rule += "import " + importe + " ";
		}
		
		rule += "rule \"" + ruleName + "\" ";
		
		rule += "when ";
		
		for (String condition : conditions) {
			rule += condition + " ";
		}
		
		rule += "then ";
		
		for (String action : actions) {
			rule += action + " ";
		}
		
		rule += "end";
		
		addRule(rule);
	}
	
	public void addRule(String rule) throws DroolsParserException {
		
		Collection<KnowledgePackage> packages = parseRule(rule);
		
		session.getKnowledgeBase().addKnowledgePackages(packages);
	}
	
	public void AddRuleFile(String filePath) throws DroolsParserException {
		Collection<KnowledgePackage> packages = parseRuleFile(filePath);
		
		session.getKnowledgeBase().addKnowledgePackages(packages);
	}
	
	/**
	 * Constructs Drools <code>KnowledgePackage</code>s for the parameter rule.
	 * @param rule
	 * @return
	 * @throws DroolsParserException If the rule cannot be parsed.
	 */
	public Collection<KnowledgePackage> parseRule(String rule) throws DroolsParserException {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		while (!kBuilderSemaphore.tryAcquire()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("Wait interrupted.", e);
			}
		}
		
		Resource myResource = ResourceFactory.newReaderResource(new StringReader(rule));
		kbuilder.add(myResource, ResourceType.DRL);
		kBuilderSemaphore.release();
		
		
		if (kbuilder.hasErrors()) {
			throw new DroolsParserException("Unable to parse new rule.\n"
					+ kbuilder.getErrors().toString());
		}
		
		return kbuilder.getKnowledgePackages();
	}
	
	/**
	 * Constructs Drools <code>KnowledgePackage</code>s from the rules in the file from the parameter file path.
	 * @param filePath
	 * @return
	 * @throws DroolsParserException
	 */
	public Collection<KnowledgePackage> parseRuleFile(String filePath)
			throws DroolsParserException {
		
		logger.info("Parsing rule file at " + filePath);
		KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		while (!kBuilderSemaphore.tryAcquire()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("Wait interrupted.", e);
			}
		}
		
		Resource myResource = ResourceFactory.newFileResource(filePath);
		kBuilder.add(myResource, ResourceType.DRL);
		kBuilderSemaphore.release();
		
		if (kBuilder.hasErrors()) {
			throw new DroolsParserException("Unable to parse new rule from file.\n"
					+ filePath + "\n"
					+ kBuilder.getErrors().toString());
		}
		
		return kBuilder.getKnowledgePackages();
	}
	
	public Collection<Rule> getRules() {
		Collection<KnowledgePackage> packages = session.getKnowledgeBase().getKnowledgePackages();
		
		Collection<Rule> rules = null;
		
		for (KnowledgePackage pack : packages) {
			if (rules == null) {
				rules = pack.getRules();
			}
			else {
				rules.addAll(pack.getRules());
			}
		}
		
		return rules;
	}
	
	/**
	 * Creates a new <code>StatefulKnowledgeSession</code> based on the current state of the game,
	 * with all valid rules, facts, and globals for subsimulation execution.
	 * @return
	 */
	public StatefulKnowledgeSession getNewStatefulKnowledgeSession() {
		refreshSession();
		
		StatefulKnowledgeSession newSession = session.getKnowledgeBase().newStatefulKnowledgeSession();
		
		newSession.setGlobal("logger", session.getGlobal("logger"));
		newSession.setGlobal("rand", session.getGlobal("rand"));
		newSession.setGlobal("storage", session.getGlobal("storage"));
		
		for (Object object : session.getObjects())
		{
			if (WantToCopyToNewSession(object))
				newSession.insert(object);
		}
		
		newSession.getAgenda().clear();
		
		return newSession;
	}
	
	private boolean WantToCopyToNewSession(Object object) {
		if (object instanceof NomicAgent)
			return false;
		
		if (object instanceof Turn)
			return false;
		
		if (object instanceof Vote && ((Vote)object).getT() == getTurnNumber())
			return false;
		
		if (object instanceof ProposeRuleChange && ((ProposeRuleChange)object).getT() == getTurnNumber())
			return false;
		
		return true;
	}
	
	public StatefulKnowledgeSession getActiveStatefulKnowledgeSession() {
		return session;
	}
	
	/**
	 * This is not actual Presage2 simulation time, it corresponds instead to phases in the turns of a game of Nomic.
	 * @return
	 */
	public Integer getSimTime() {
		return SimTime;
	}
	
	public Integer getTurnNumber() {
		return currentTurn.getNumber();
	}
	
	public Integer getRoundNumber() {
		return (int) Math.floor(currentTurn.getNumber() / agents.size());
	}
	
	public TurnType getTurnType() {
		return currentTurn.getType();
	}
	
	public Integer getNumberOfAgents() {
		return agents.size();
	}
	
	public Collection<UUID> getAgentIDs() {
		return agentIDs;
	}
	
	public ArrayList<Vote> getSimVotes() {
		return SimVotes;
	}
	
	public ArrayList<ProposeRuleChange> getSimRuleChanges() {
		return SimRuleChanges;
	}
	
	public String getAgentName(UUID pid) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == pid)
				return agent.getName();
		}
		
		return "";
	}
	
	public Collection<ProxyAgent> getProxyAgents() {
		Collection<ProxyAgent> proxies = new ArrayList<ProxyAgent>();
		for (NomicAgent agent : agents) {
			proxies.add(agent.getRepresentativeProxy());
		}
		
		return proxies;
	}
	
	public Vote getVote(UUID pid) {
		return votesThisTurn.get(pid);
	}
	
	public ArrayList<Vote> getVotesThisTurn() {
		ArrayList<Vote> thisTurn = new ArrayList<Vote>();
		
		for (Vote vote : SimVotes) {
			if (vote.getT() == currentTurn.getNumber())
				thisTurn.add(vote);
		}
		
		return thisTurn;
	}
	
	public ProposeRuleChange getPreviousRuleChange() {
		return previousRuleChange;
	}
	
	public boolean isGameWon() {
		return Winner != null;
	}
	
	public NomicAgent getWinner() {
		return Winner;
	}
	
	public Integer getWinTime() {
		return WinTime;
	}
	
	public boolean isActive(String ruleName) {
		for (Rule rule : getRules()) {
			if (rule.getName().equals(ruleName))
				return true;
		}
		
		return false;
	}
	
	public Map<String, Integer> getPointsMap() {
		HashMap<String, Integer> agentsToPoints = new HashMap<String, Integer>();
		for (NomicAgent agent : agents) {
			agentsToPoints.put(agent.getName(), agent.getSequentialID());
		}
		
		return agentsToPoints;
	}
}
