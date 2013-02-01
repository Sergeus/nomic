package agents;

import java.util.Set;
import java.util.UUID;

import services.NomicService;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class NomicAgent extends AbstractParticipant {

	private int SequentialID;
	
	NomicService nomicService;

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
		} catch (UnavailableServiceException e) {
			logger.warn("Couldn't get Nomic Environment Service.", e);
		}
	}
	
	@Override
	public void incrementTime() {
		if (nomicService.isMyTurn(this)) {
			logger.info("It's my turn!");
		}
		super.incrementTime();
	}
	
	public int UnitTestTest() {
		return 2;
	}
	
	public int getSequentialID() {
		return SequentialID;
	}

	public void setSequentialID(int sequentialID) {
		SequentialID = sequentialID;
	}
}
