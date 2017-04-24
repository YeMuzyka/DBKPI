package include;

import java.io.Serializable;

public class Search_Fields implements Serializable {
	private static final long serialVersionUID = 1L;
	String s_name;
	String s_group;
	java.util.Date d_start;
	java.util.Date d_end;
	String s_email;
		
	public Search_Fields (String s_name, String s_group, java.util.Date d_start, 
			java.util.Date d_end, String s_email) {
		this.s_name = s_name;
		this.s_group = s_group;
		this.d_start = d_start;
		this.d_end = d_end;
		this.s_email = s_email;
	}

	public String getS_name() {
		return s_name;
	}

	public void setS_name(String s_name) {
		this.s_name = s_name;
	}

	public String getS_group() {
		return s_group;
	}

	public void setS_group(String s_group) {
		this.s_group = s_group;
	}

	public java.util.Date getD_start() {
		return d_start;
	}

	public void setD_start(java.util.Date d_start) {
		this.d_start = d_start;
	}

	public java.util.Date getD_end() {
		return d_end;
	}

	public void setD_end(java.util.Date d_end) {
		this.d_end = d_end;
	}

	public String getS_email() {
		return s_email;
	}

	public void setS_email(String s_email) {
		this.s_email = s_email;
	}
		
}