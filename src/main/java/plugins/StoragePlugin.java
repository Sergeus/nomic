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
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;

import com.google.inject.Inject;

import exceptions.NoExistentRuleChangeException;

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

	@Override
	public void incrementTime() {
		if (storage != null) {
			// Vote section
			for (UUID pid : membersService.getParticipants()) {
				Vote vote = nomicService.getVote(pid);
				if (vote != null) {
					TransientAgentState state = storage.getAgentState(pid, vote.getT());
					state.setProperty("CasterName", nomicService.getAgentName(pid));
					state.setProperty("Vote", vote.getVote().toString());
					state.setProperty("TurnNumber", nomicService.getTurnNumber().toString());
				}
			}
			
			// Rule change section
			ProposeRuleChange ruleChange = nomicService.getPreviousRuleChange();
			
			if (ruleChange != null)
				StoreChange(ruleChange);
		}
	}
	
	private void StoreChange(ProposeRuleChange ruleChange) {
		PersistentEnvironment env = storage.getSimulation().getEnvironment();
		
		env.setProperty("Proposer", ruleChange.getT(), ruleChange.getProposer().getName());
		env.setProperty("Type", ruleChange.getT(), ruleChange.getRuleChangeType().toString());
		env.setProperty("Success", ruleChange.getT(), "" + ruleChange.getSucceeded());
		env.setProperty("Turn", ruleChange.getT(), nomicService.getTurnNumber().toString());
		
		if (ruleChange instanceof ProposeRuleAddition) {
			
			ProposeRuleAddition addition = (ProposeRuleAddition)ruleChange;
			env.setProperty("NewRuleName", addition.getT(), addition.getNewRuleName());
			env.setProperty("NewRule", ruleChange.getT(), addition.getNewRule());
			
		}
		else if (ruleChange instanceof ProposeRuleModification) {
			
			ProposeRuleModification modification = (ProposeRuleModification)ruleChange;
			
			env.setProperty("OldRuleName", ruleChange.getT(), modification.getOldRuleName());
			env.setProperty("OldRule", modification.getT(), 
					ruleClassificationService.getRuleBody(modification.getOldRuleName()));
			env.setProperty("NewRuleName", modification.getT(), 
					modification.getNewRuleName());
			env.setProperty("NewRule", ruleChange.getT(), modification.getNewRule());
			
		}
		else if (ruleChange instanceof ProposeRuleRemoval) {
			
			ProposeRuleRemoval removal = (ProposeRuleRemoval)ruleChange;
			env.setProperty("OldRuleName", ruleChange.getT(), removal.getOldRuleName());
			env.setProperty("OldRule", removal.getT(), 
					ruleClassificationService.getRuleBody(removal.getOldRuleName()));
			
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
			// Store last rule change
			try {
				StoreChange(nomicService.getCurrentRuleChange());
			} catch (NoExistentRuleChangeException e) {
				logger.warn("Final rule change not available.", e);
			}
			
			// Store final agent information
			for (UUID pid : membersService.getParticipants()) {
				storage.getAgent(pid).setProperty("NumSubSims", nomicService.getNumSubSimsRun(pid).toString());
				storage.getAgent(pid).setProperty("AverageSubSimLength", nomicService.getAverageSubSimLength(pid).toString());
			}
			
			// Store final simulation information
			storage.getSimulation().addParameter("NumTurns", nomicService.getTurnNumber().toString());
			storage.getSimulation().addParameter("NumRounds", nomicService.getRoundNumber().toString());
			storage.getSimulation().addParameter("NumAgents", nomicService.getNumberOfAgents().toString());
			
			storage.getSimulation().addParameter("Won", "" + nomicService.isGameWon());
			
			if (nomicService.isGameWon())
				storage.getSimulation().addParameter("Winner", nomicService.getWinner().getName());
		}
	}
}
