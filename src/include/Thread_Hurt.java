package include;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Thread_Hurt extends Thread {
	Socket soc_hurt;
	Login_Hurt l_h;
	String s, num_hurt_meth;
	ObjectInputStream des_log;
	
	public Thread_Hurt(Socket s) {
		soc_hurt = s;
		
		System.out.println("Новий поток створенно!");
		try {
			des_log = new ObjectInputStream(soc_hurt.getInputStream());
	    	l_h = (Login_Hurt) des_log.readObject();
		}catch (IOException e) {
			System.out.println("Щось не так: " + e);
		}catch (ClassNotFoundException e) {
			System.out.println("Error: " + e);
		}
		
		start();
	}
	
	public void run() {
		String[][] log_pass = new String [2][3];
		log_pass [0][0] = "18";
		log_pass [0][1] = "do_it";
		log_pass [0][2] = "just_do_it";
		log_pass [1][0] = "19";
		log_pass [1][1] = "true_login";
		log_pass [1][2] = "_and_true_password";
		System.out.println("Гуртожиток");
	   	System.out.println(l_h.login);
	   	System.out.println(l_h.pass);
	   	String num_hurt = "";
	   	try {
	   		ObjectOutputStream oos = new ObjectOutputStream(soc_hurt.getOutputStream());
	   		if (l_h.login.equals(log_pass[0][1]) & l_h.pass.equals(log_pass [0][2])) {
	   			num_hurt = log_pass[0][0];
	   			System.out.println(num_hurt);
	    		Input_Log_Pass ilp18 = new Input_Log_Pass(num_hurt, true);
	    		oos.writeObject(ilp18);
	    		oos.flush();
				String result_String = send_message(num_hurt);
			    System.out.println(result_String);
			    try {
			    	Send_Mess s_m = new Send_Mess(result_String);
					oos.writeObject(s_m);
					oos.flush();
				}catch (IOException ie) {
					ie.printStackTrace();
				}	
			} else
				if (l_h.login.equals(log_pass[1][1]) & l_h.pass.equals(log_pass [1][2])) {
					num_hurt = log_pass[1][0];
	    			System.out.println(num_hurt);
					Input_Log_Pass ilp19 = new Input_Log_Pass(num_hurt, true);
		    		oos.writeObject(ilp19);
		    		oos.flush();
					String result_String = send_message(num_hurt);
		    		System.out.println(result_String);
		    		try {
		    			
						Send_Mess s_m = new Send_Mess(result_String);
						oos.writeObject(s_m);
						oos.flush();
		    		}catch (IOException ie) {
						ie.printStackTrace();
					}
				}else {
					String err = "Такого гуртожитку не існує";
					System.out.println(err);
					Input_Log_Pass ilp_Err = new Input_Log_Pass(err, false);
		    		oos.writeObject(ilp_Err);
		    		oos.flush();
				}
	   	}catch (IOException ie) {
			System.out.println("Error: " + ie);
		}
	   	
	   	//System.out.println("Дія поток завершена!");
	}
	
	//запит від гуртожитка
	public String send_message(String num_hurt) {
		StringBuilder sb = new StringBuilder();
		num_hurt_meth = num_hurt;
		String file_Path = "C://da/Workspace/DBKPI/src/temp_Files/" + num_hurt_meth + ".txt";
		File file = new File(file_Path);
		if(!file.exists()) {
			String not_Exists = "Нових заселених студентів немає!";
			sb.append(not_Exists);
			sb.append("\n");
		} else {
    		try(BufferedReader read = new BufferedReader(new FileReader (file_Path))) {
    			String tmp_Str;
    			while ((tmp_Str = read.readLine()) != null) {
    				sb.append(tmp_Str);
    				sb.append("\n");
    			}
    			read.close();
    			if(file.delete()) {
    				System.out.println("Deleting file successful");
    			} else System.out.println("Deleting file ERROR!");
    		} catch(FileNotFoundException fnfe) {
    			System.out.println("Error: " + fnfe);
			} catch (IOException ioe) {
				System.out.println("Error: " + ioe);
			}
		}
		return sb.toString();
	}
}