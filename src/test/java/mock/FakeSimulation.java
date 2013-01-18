package mock;
import agents.NomicAgent;


public class FakeSimulation {
	NomicAgent agent;
	
	public FakeSimulation(NomicAgent ag) {
		agent = ag;
	}
	
	public void run() {
		agent.incrementTime();
	}
}
