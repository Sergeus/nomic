package agents;

import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class NomicAgent extends AbstractParticipant {

	public NomicAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
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
	public void incrementTime() {
		System.out.println("Nomic says 'hi'.");
		super.incrementTime();
	}
}
