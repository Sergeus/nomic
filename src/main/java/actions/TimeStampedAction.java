package actions;

import uk.ac.imperial.presage2.core.Action;

public abstract class TimeStampedAction implements Action {
	int t;

	protected TimeStampedAction() {
		super();
	}

	protected TimeStampedAction(int t) {
		super();
		this.t = t;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}
}
