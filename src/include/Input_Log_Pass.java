package include;

import java.io.Serializable;

public class Input_Log_Pass implements Serializable{
	private static final long serialVersionUID = 3L;
	String number;
	boolean result;
	
	Input_Log_Pass (String number, boolean result){
		this.number = number;
		this.result = result;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
		
}