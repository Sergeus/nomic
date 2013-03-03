package plugins;

import org.apache.log4j.Logger;

import services.NomicService;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.plugin.Plugin;
import actions.ProposeRuleChange;

import com.google.inject.Inject;

import exceptions.NoExistentRuleChangeException;

public class RuleChangeStoragePlugin implements Plugin {

	private final Logger logger = Logger.getLogger(VoteStoragePlugin.class);
	
	private StorageService storage;
	
	private final NomicService nomicService;
	
	private PersistentEnvironment persistentEnvironment;
	
	public RuleChangeStoragePlugin() {
		super();
		storage = null;
		nomicService = null;
		logger.info("No storage service, no plugin.");
	}
	
	@Inject
	public RuleChangeStoragePlugin(EnvironmentServiceProvider serviceProvider,
			Time t, EventBus e) throws UnavailableServiceException {
		this.storage = null;
		this.nomicService = serviceProvider.getEnvironmentService(NomicService.class);
		e.subscribe(this);
	}
	
	@Inject(optional = true)
	public void setStorage(StorageService storage) {
		this.storage = storage;
	}
	
	@Override
	public void incrementTime() {
		if (storage != null) {
			try {
				ProposeRuleChange ruleChange = nomicService.getCurrentRuleChange();
				
				PersistentEnvironment env = getPersistentEnvironment();
				
				System.out.println("Storing vote RESULTS.");
				
				env.setProperty("Proposer", ruleChange.getT(), ruleChange.getProposer().getName());
				env.setProperty("Type", ruleChange.getT(), ruleChange.getRuleChangeType().toString());
			} catch (NoExistentRuleChangeException e) {
				
			}
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
		// TODO Auto-generated method stub

	}

	public PersistentEnvironment getPersistentEnvironment() {
		if (persistentEnvironment == null && storage != null) {
			System.out.println("Getting persistent environment.");
			persistentEnvironment = storage.getSimulation().getEnvironment();
		}
		return persistentEnvironment;
	}
	
}
