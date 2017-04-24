package include;

import java.io.Serializable;

 public class Send_Mess implements Serializable {
	private static final long serialVersionUID = 5L;
	String new_stud;
	
	Send_Mess (String new_stud) {
		this.new_stud = new_stud;
	}

	public String getNew_stud() {
		return new_stud;
	}

	public void setNew_stud(String new_stud) {
		this.new_stud = new_stud;
	}
	
}