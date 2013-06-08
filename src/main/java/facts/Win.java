package facts;

import agents.NomicAgent;

/**
 * Simple object that triggers a win for the agent given in its constructor parameter.
 * @author Stuart Holland
 *
 */
public class Win {
	NomicAgent winner;

	public Win(NomicAgent winner) {
		this.winner = winner;
		this.winner.Win();
	}
	
	public NomicAgent getWinner() {
		return winner;
	}

	public void setWinner(NomicAgent winner) {
		this.winner = winner;
	}
}
