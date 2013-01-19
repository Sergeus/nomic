package mock;
import agents.Agent;


public class FakeSimulation {
	Agent agent;
	
	public FakeSimulation(Agent ag) {
		agent = ag;
	}
	
	public void run() {
		agent.incrementTime();
	}
}
