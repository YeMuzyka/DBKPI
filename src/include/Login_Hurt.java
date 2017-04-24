package include;

import java.io.Serializable;

public class Login_Hurt implements Serializable {
	private static final long serialVersionUID = 2L;
	String login;
	String pass;
	
	public Login_Hurt (String login, String pass) {
		this.login = login;
		this.pass = pass;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
		
}