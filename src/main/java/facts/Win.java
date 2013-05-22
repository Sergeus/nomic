package facts;

import agents.NomicAgent;

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
