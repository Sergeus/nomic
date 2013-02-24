package plugins;

import java.util.UUID;

import org.apache.log4j.Logger;

import services.NomicService;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.plugin.Plugin;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import actions.Vote;

import com.google.inject.Inject;

public class VoteStoragePlugin implements Plugin {
	
	private final Logger logger = Logger.getLogger(VoteStoragePlugin.class);
	
	private StorageService storage;
	
	private final EnvironmentMembersService membersService;
	private final NomicService nomicService;
	
	public VoteStoragePlugin() {
		super();
		storage = null;
		nomicService = null;
		membersService = null;
		logger.info("No storage service, no plugin.");
	}
	
	@Inject
	public VoteStoragePlugin(EnvironmentServiceProvider serviceProvider,
			Time t) throws UnavailableServiceException {
		this.storage = null;
		this.membersService = serviceProvider.getEnvironmentService(EnvironmentMembersService.class);
		this.nomicService = serviceProvider.getEnvironmentService(NomicService.class);
	}
	
	@Inject(optional = true)
	public void setStorage(StorageService storage) {
		this.storage = storage;
	}

	@Override
	public void incrementTime() {
		if (storage != null) {
			for (UUID pid : membersService.getParticipants()) {
				Vote vote = nomicService.getVote(pid);
				
				TransientAgentState state = storage.getAgentState(pid, nomicService.getTurnNumber());
				state.setProperty("Vote", vote.getVote().toString());
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

}
