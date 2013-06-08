package plugins;

import java.util.UUID;

import org.apache.log4j.Logger;

import services.NomicService;
import services.RuleClassificationService;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.plugin.Plugin;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import actions.ProposeNoRuleChange;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;

import com.google.inject.Inject;

import enums.TurnType;
import exceptions.NoExistentRuleChangeException;

/**
 * Plugin that stores all data relevant to Nomic from a single simulation. All data is stored when the simulation ends.
 * @author Stuart Holland
 *
 */
public class StoragePlugin implements Plugin {
	
	private final Logger logger = Logger.getLogger(StoragePlugin.class);
	
	private StorageService storage;
	
	private final EnvironmentMembersService membersService;
	private final NomicService nomicService;
	private final RuleClassificationService ruleClassificationService;
	
	public StoragePlugin() {
		super();
		storage = null;
		nomicService = null;
		membersService = null;
		ruleClassificationService = null;
		logger.info("No storage service, no plugin.");
	}
	
	@Inject
	public StoragePlugin(EnvironmentServiceProvider serviceProvider,
			Time t) throws UnavailableServiceException {
		this.storage = null;
		this.membersService = serviceProvider.getEnvironmentService(EnvironmentMembersService.class);
		this.nomicService = serviceProvider.getEnvironmentService(NomicService.class);
		this.ruleClassificationService = serviceProvider.getEnvironmentService(RuleClassificationService.class);
	}
	
	@Inject(optional = true)
	public void setStorage(StorageService storage) {
		this.storage = storage;
	}
	
	private void StoreChange(ProposeRuleChange ruleChange) {
		PersistentEnvironment env = storage.getSimulation().getEnvironment();
		
		Integer time = ruleChange.getSimTime();
		
		env.setProperty("Proposer", time, ruleChange.getProposer().getName());
		env.setProperty("Type", time, ruleChange.getRuleChangeType().toString());
		env.setProperty("Success", time, "" + ruleChange.getSucceeded());
		env.setProperty("Turn", time, nomicService.getTurnNumber().toString());
		
		if (ruleChange instanceof ProposeRuleAddition) {
			
			ProposeRuleAddition addition = (ProposeRuleAddition)ruleChange;
			env.setProperty("NewRuleName", time, addition.getNewRuleName());
			env.setProperty("NewRule", time, addition.getNewRule());
			
		}
		else if (ruleChange instanceof ProposeRuleModification) {
			
			ProposeRuleModification modification = (ProposeRuleModification)ruleChange;
			
			env.setProperty("OldRuleName", time, modification.getOldRuleName());
//			env.setProperty("OldRule", time, 
//					ruleClassificationService.getRuleBody(modification.getOldRuleName()));
			env.setProperty("NewRuleName", time, 
					modification.getNewRuleName());
			env.setProperty("NewRule", time, modification.getNewRule());
			
		}
		else if (ruleChange instanceof ProposeRuleRemoval) {
			
			ProposeRuleRemoval removal = (ProposeRuleRemoval)ruleChange;
			env.setProperty("OldRuleName", time, removal.getOldRuleName());
//			env.setProperty("OldRule", time, 
//					ruleClassificationService.getRuleBody(removal.getOldRuleName()));
			
		}
	}
	
	private void StoreVote(Vote vote) {
		UUID pid = vote.getVoter().getID();
		
		if (vote != null) {
			TransientAgentState state = storage.getAgentState(pid, vote.getSimTime());
			state.setProperty("CasterName", nomicService.getAgentName(pid));
			state.setProperty("Vote", vote.getVote().toString());
			state.setProperty("TurnNumber", nomicService.getTurnNumber().toString());
		}
	}
	
	@Override
	public void initialise() {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public void execute() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSimulationComplete() {
		if (storage != null) {
			logger.info("Storing the universe.");
			
			// Store all rule changes
			for (ProposeRuleChange ruleChange : nomicService.getSimRuleChanges()) {
				StoreChange(ruleChange);
			}
			
			for (Vote vote : nomicService.getSimVotes()) {
				StoreVote(vote);
			}
			
			// Store final agent information
			for (UUID pid : membersService.getParticipants()) {
				storage.getAgent(pid).setProperty("SeqID", nomicService.getSequentialID(pid).toString());
				storage.getAgent(pid).setProperty("NumSubSims", nomicService.getNumSubSimsRun(pid).toString());
				storage.getAgent(pid).setProperty("AverageSubSimLength", nomicService.getAverageSubSimLength(pid).toString());
			}
			
			// Store final simulation information
			storage.getSimulation().addParameter("NumTurns", nomicService.getTurnNumber().toString());
			storage.getSimulation().addParameter("NumRounds", nomicService.getRoundNumber().toString());
			storage.getSimulation().addParameter("NumAgents", nomicService.getNumberOfAgents().toString());
			
			storage.getSimulation().addParameter("Won", "" + nomicService.isGameWon());
			
			if (nomicService.isGameWon()) {
				storage.getSimulation().addParameter("Winner", nomicService.getWinner().getName());
				storage.getSimulation().addParameter("WinTime", nomicService.getWinTime().toString());
			}
		}
	}

	@Override
	public void incrementTime() {
		
	}
}
