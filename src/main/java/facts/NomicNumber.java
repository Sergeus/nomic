package facts;

public class NomicNumber {
	int number;
	String attribute;
	
	public NomicNumber(int number, String attribute) {
		super();
		this.number = number;
		this.attribute = attribute;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
}
