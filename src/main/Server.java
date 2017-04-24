package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.sql.*;
import java.util.logging.*;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import include.Search_Fields;
import include.Thread_Hurt;
import javax.mail.*;

public class Server {
	public static void main(String[] args) {
		Search_Fields s_f_a = null;
		System.out.println("������ �������");
		boolean hurt_Connect = false;
		Socket hurt = null;
	    //��������� �������	 
		while(true) {
		    try (ServerSocket toClient = new ServerSocket(1337)) {
		    	System.out.println();
		    	System.out.println("������ ��������!"); 
				
				if(!hurt_Connect) {
		    		hurt = toClient.accept();
					hurt_Connect = true;
		    		new Thread_Hurt(hurt);
		    	} else {
		    		System.out.println("���������� �����������!");
		    	}
				
		    	try {
		    		//����� �� ���������
			    	Socket client = toClient.accept();
			    	ObjectInputStream deserializer = new ObjectInputStream(client.getInputStream());
			    	s_f_a = (Search_Fields) deserializer.readObject();
			    	System.out.println(s_f_a.getS_name());
			    	System.out.println(s_f_a.getS_group());
			    	System.out.println(s_f_a.getD_start());
			    	System.out.println(s_f_a.getD_end());
			    	System.out.println(s_f_a.getS_email());
			    	
			    	//ϳ��������� �� ����
					Connection connection = null;
			        String url = "jdbc:postgresql://127.0.0.1:5432/DBKPI";
			        String name = "DBKPIAdmin";
			        String password = "adminpassword";
			        String eText;
			        String n_z = "";
		            String stat = "";
		            String num_room = "";
            		int num_hurt = 0;
            		int currentID = 0;
			        Session ses = initPropEm();
			        try {
			        	//��������� �������
			            Class.forName("org.postgresql.Driver");
			            System.out.println("ϳ��������� �� ����");
			            //��������� ����������
			            connection = DriverManager.getConnection(url, name, password);
			            System.out.println("ϳ��������� ������");
			            boolean res_Stud_Gr_Hurt = false, res_Ses = false, 
			            		res_Zapret = false , res_Free_Room = false, res_Perep = false;
			            boolean res_Zas_Room = false; //���������� � ������?
			            PreparedStatement pst = null;
			            
			            res_Stud_Gr_Hurt = studentExists(pst, connection, s_f_a.getS_name(), s_f_a.getS_group());
			            if (!res_Stud_Gr_Hurt) {
			            	System.out.println("������ �������� �� ����!");
			            } else {
			            	n_z = getID(pst, connection, s_f_a.getS_name());
			            	stat = getStat(pst, connection, s_f_a.getS_name());
			            	res_Ses = currectSess(pst, connection, s_f_a.getD_start(), s_f_a.getD_end());
				            if (!res_Stud_Gr_Hurt | !res_Ses){
				            	try{
					            	eText = "��������(��) " + s_f_a.getS_name() + ", �������� ��� ����� ���������, "
					            			+ "������� �� ���������� � ����� ������ � ��������� ������. "
					            			+ "���� �� �� ��� - ��������� �� ������ ��������.";
					            	sendEmail(ses, s_f_a.getS_email(), eText);
					            } catch( MessagingException me){
					            	System.out.println("���� ��������� ���������: " + me);
					            }
				            }
				            if(!res_Ses){
				            	System.out.println("�� ���������� ���� ���!");
				            } else {
				            	res_Zapret = zaborStud(pst, connection, n_z);
					            if (res_Zapret) {
					            	System.out.println("������� ����������� �� ��������� � ����������!");
					            	try{
						            	eText = "��������(��) " + s_f_a.getS_name() + ", ������, �� � ������� ������ �� "
						            			+ "��������� � ����������.";
						            	sendEmail(ses, s_f_a.getS_email(), eText);
						            } catch( MessagingException me) {
						            	System.out.println("���� ��������� ���������: " + me);
						            }
					            } else {
					            	res_Free_Room = findPlace(pst, connection, stat);				            	
					            	if (!res_Free_Room){
					            		System.out.println("���� ������� ���� ��� ����� ����!");
					            		try{
							            	eText = "��������(��) " + s_f_a.getS_name() + ", ������, ������� ���� � �����������"
							            			+ " ����";
							            	sendEmail(ses, s_f_a.getS_email(), eText);
							            } catch( MessagingException me) {
							            	System.out.println("���� ��������� ���������: " + me);
							            }
					            	} else {
					            		System.out.println("��� ����� ���� � ����� ����");
					            		try{
							            	eText = "��������(��) " + s_f_a.getS_name() + ", ��� ��������� � ���������� �� ��� ���, "
							            			+ "��� ��������� �������� ����������. ��������: " + "\n" + "������������ "
							            			+ "������: ����\"�ϲ\"" + "\n" + "�/�: 31254289213853 ��� 820172 "
							            			+ "������ �.���� ������� 02070921" + "\n"
							            			+ "����������� �������: ���������� ��������." + "\n"
							            			+ "ֳ�� �� ���� �� ������ 70 ���.";
							            	sendEmail(ses, s_f_a.getS_email(), eText);
							            } catch( MessagingException me){
							            	System.out.println("���� ��������� ���������: " + me);
							            }
					            		num_room = getRoom(pst, connection, stat);
					            		num_hurt = getHurt(pst, connection, num_room);
					            		res_Perep = existsInDB(pst, connection, n_z);
					            		if(res_Perep){
					            			currentID = studentWithPerep(pst, connection, n_z, s_f_a.getD_start(),
					            					s_f_a.getD_end(), num_hurt);
					            			res_Zas_Room = InsertZas_StudAndRoom(pst, connection, currentID, n_z, num_room);
					            			writeToFile(s_f_a.getS_name(), s_f_a.getD_start(), s_f_a.getD_end(), num_room, num_hurt);
					            			updateRoom(pst, connection, num_room, stat);
					            		}
					            		if (!res_Perep & !res_Zas_Room) {
					            			currentID = studentWithoutPerep(pst, connection, n_z, s_f_a.getS_group(),
					            					s_f_a.getD_start(), s_f_a.getD_end(), num_hurt);
					            			res_Zas_Room = InsertZas_StudAndRoom(pst, connection, currentID, n_z, num_room);
					            			writeToFile(s_f_a.getS_name(), s_f_a.getD_start(), s_f_a.getD_end(), num_room, num_hurt);
					            			updateRoom(pst, connection, num_room, stat);
					            		}//������ ���������� � ��������� ��������
					            		if (res_Zas_Room){
					            			String address = new String();
					            			String SQLText = "SELECT ������ FROM ���������� "
					            					+ "WHERE �_���������� = ? ";
					            			pst = connection.prepareStatement(SQLText);
					            			pst.setInt(1, num_hurt);
					            			ResultSet result1 = pst.executeQuery();
					            			if (result1.next()) {
					            				address = result1.getString("������");
					            			}
					            			try {
					    		            	eText = "��� ��������� � ���������� �: " + num_hurt + " � ������: " + num_room
					    		            			+ "." + "\n������ ����������: " + address + "."
					    		            			+ "\n���� � ��� ���� ����������, ��������� �� ���� ���������� �� �������: "
					    		            			+ "���������� � 7 ���. ��������, 3";
					    		            	sendEmail(ses, s_f_a.getS_email(), eText);
					    		            } catch( MessagingException me) {
					    		            	System.out.println("���� ��������� ���������: " + me);
					    		            }
					            		}
					            	}//����� ���� � ����������
					            } // �� � ������� ������ 
				            } //����������� ���� ���
			            } //��������� �������� 
			        } catch (ClassNotFoundException | SQLException e) {
			        	System.out.println("ERROR: " + e);
			        } finally {
			            if (connection != null) {
			                try {
			                    connection.close();
			                } catch (SQLException ex) {
			                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
			                }
			            }
			        }
		    	} catch (Exception e) {
		    		System.out.println("Error: " + e);
		    	}
		    //������ - �����
		    } catch (IOException e) {
		      System.out.println("���� �� ��� � ������ 1337");
		      System.exit(-1);
		    }
		}//while (true)
	}//main()
	
	//����������� ��� e-mail
	static Session initPropEm(){
		Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.socketFactory.port", 465);
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", 465);
        Session s = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
        	protected PasswordAuthentication getPasswordAuthentication() {
        		return new PasswordAuthentication ("oneguyfromdbkpi@gmail.com", 
            					"the_best_password_in_the_WORLD!");
        	}
        });
        return s;
	}
	
	//³���������� �-mail
	static void sendEmail(Session curSes, String em_adr, String em_message) throws AddressException, MessagingException{
		Message emess = new MimeMessage(curSes);
    	emess.setFrom(new InternetAddress("oneguyfromdbkpi@gmail.com"));
    	emess.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em_adr));
    	emess.setSubject("��������� � ����������");
    	emess.setText(em_message);
    	Transport.send(emess);
    	System.out.println("������ ����������� �����");
	}
	
	//����� �� �����
	static void writeToFile(String name, java.util.Date start, java.util.Date end, String room, int hurt){
		String str = new String();
		java.sql.Date s_d_start = dateToSQL(start);
		java.sql.Date s_d_end = dateToSQL(end);
		str = hurt + "-> " + name + "\t" + room + "\t" + s_d_start + "\t" + s_d_end + "\n";
		String file_Path = "C://da/Workspace/DBKPI/src/temp_Files/" + hurt + ".txt";
		File file = new File(file_Path);
		try{
			if(!file.exists()){
	            file.createNewFile();
	        }
	        try{
	        	FileWriter writer = new FileWriter(file_Path, true);
		        BufferedWriter bufferWriter = new BufferedWriter(writer);
		        bufferWriter.write(str);
		        bufferWriter.close();
		    } catch (IOException e){
		    	System.out.println("Error: " + e);
		    }
	    } catch(IOException e){
	        System.out.println("Error: " + e);
	    }
	}
	
	//����� ������� ����?
	static boolean studentExists(PreparedStatement ps, Connection con, String name, 
			String group) throws SQLException{
		boolean exists = false;
		String SQLText = "SELECT �_�������_������, ��, �_�����, �����, �����������_���������� "
        		+ "FROM ������� WHERE ��= ? AND �_�����= ? AND �����������_����������= true";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, name);
        ps.setString(2, group);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	exists = true;
        	System.out.println("#" + result1.getRow() +"\t"
                    + result1.getString("�_�������_������") + "\t"
                    + result1.getString("��") + "\t"
                    + result1.getString("�����") + "\t"
                    + result1.getString("�_�����") + "\t"
                    + result1.getBoolean("�����������_����������") + "\t");
        }
		return exists;
	}
	
	//��������� �_�������_������
	static String getID(PreparedStatement ps, Connection con, String name) throws SQLException{
		String ID = "";
		String SQLText = "SELECT �_�������_������ "
        		+ "FROM ������� WHERE ��= ? AND �����������_����������= true";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, name);
        ResultSet result1 = ps.executeQuery();
        if(result1.next()){
        	ID = result1.getString("�_�������_������");
        }
		return ID;
	}
	
	//��������� ����� ��������
	static String getStat(PreparedStatement ps, Connection con, String name) throws SQLException{
		String stat = "";
		String SQLText = "SELECT ����� "
        		+ "FROM ������� WHERE ��= ? AND �����������_����������= true";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, name);
        ResultSet result1 = ps.executeQuery();
        if(result1.next()){
        	stat = result1.getString("�����");
        }
		return stat;
	}
	
	//������������ �� java.util.Date � java.sql.Date
	static java.sql.Date dateToSQL(java.util.Date dTS){
		java.sql.Date ud = new java.sql.Date(new java.util.Date().getTime());
		ud = new java.sql.Date(dTS.getTime());
		return ud;
	}
	
	//���� ������� ���� - ������� ����
	static boolean currectSess(PreparedStatement ps, Connection con, java.util.Date start, java.util.Date end) throws SQLException{
		boolean currSes = false;
		System.out.println("����� ������� ����!");
        String SQLText = "SELECT �_�����, �������_���, �����_��� "
        		+ "FROM ����� WHERE �������_��� = ? AND �����_��� = ?";
        ps = con.prepareStatement(SQLText);
        
        //������������ �� java.util.Date � java.sql.Date
        java.sql.Date s_d_start = dateToSQL(start);
        System.out.println("SQL_DATE_START: " + s_d_start);
        java.sql.Date s_d_end = dateToSQL(end);
        System.out.println("SQL_DATE_END: " + s_d_end);
        
        ps.setDate(1, s_d_start);
        ps.setDate(2, s_d_end);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	currSes = true;
        	System.out.println("#" + result1.getRow() + "\t"
        			+ result1.getString("�_�����") + "\t"
        			+ result1.getDate("�������_���") + "\t"
        			+ result1.getDate("�����_���") + "\t");
        }
		return currSes;
	}
	
	//���� ���� ������� � �� - ����������, �� �� ����������� ������� �� ���������
	static boolean zaborStud(PreparedStatement ps, Connection con, String numb) throws SQLException{
		boolean zS = false;
		System.out.println("���� ��� ����������");	
    	String SQLText = "SELECT �_�������_������ "
        		+ "FROM ���������_��_��������� WHERE �_�������_������ = ?";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, numb);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	zS = true;
        	System.out.println("�������� ��������� �������� � ����������!");
        	System.out.println("n_z: " + numb);
        }
		return zS;
	}
	
	//���� ������� �� ����������� �� ��������� - ����� ������� ���� � ����������
	static boolean findPlace(PreparedStatement ps, Connection con, String st) throws SQLException{
		boolean findPlace = false;
		System.out.println("������� ���� ���������� � ����������");
    	String SQLText = "SELECT �_������, �������_���� "
    			+ "FROM ������ WHERE �������_���� > 0 AND ����� = ? "
    			+ "OR ����� = ' ' " ;
    	ps = con.prepareStatement(SQLText);
    	ps.setString(1, st);
    	ResultSet result1 = ps.executeQuery();			            	
    	while (result1.next()) {
    		findPlace = true;
    		System.out.println("#:" + result1.getRow() + "\t"
    				+ result1.getString("�_������") + "\t"
    				+ result1.getInt("�������_����") + "\t");
    	}
		return findPlace;
	}
	
	//����� ������ � ����������
	static String getRoom(PreparedStatement ps, Connection con, String st) throws SQLException {
		String room = new String();
		String SQLText = "SELECT �_������, �������_���� "
    			+ "FROM ������ WHERE �������_���� > 0 AND ����� = ? "
    			+ "OR ����� = ' ' " ;
    	ps = con.prepareStatement(SQLText);
    	ps.setString(1, st);
    	ResultSet result1 = ps.executeQuery();
    	if (result1.next()) {
    		room = result1.getString("�_������");
			System.out.println("����� ������ " + room);
		}
    	return room;
	}
	
	//����� ����������
	static int getHurt(PreparedStatement ps, Connection con, String room){
		int hurt;
		char ch[] = new char [1];
		ch[0] = room.charAt(1);
		if (ch[0]=='9') {
			hurt = 19;
		} else 
			hurt = 18;
		System.out.println("����� ����������: " + hurt);
		return hurt;
	}
	
	//������� � � ��� ����������?
	static boolean existsInDB(PreparedStatement ps, Connection con, String numb) throws SQLException{
		boolean existsInDB = false;
		String SQLText = "SELECT ��, �_�������_������ "
        		+ "FROM ���������� WHERE �_�������_������ = ? ";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, numb);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	existsInDB = true;
        }
		return existsInDB;
	}
	
	//��������� �������� �� ������ �����������
	static int studentWithPerep(PreparedStatement ps, Connection con, String numb,	java.util.Date start, 
			java.util.Date end, int hurt) throws SQLException{
		int currentID = 0;
		String SQLText = "SELECT ��, �_�������_������ "
        		+ "FROM ���������� WHERE �_�������_������ = ? ";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, numb);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()){
        	System.out.println("������ ��� ������� ��������");
			currentID = result1.getInt("��");
			//O��������� �����
			SQLText = "UPDATE ���������� "
					+ "SET �_���������� = ?, ����������_� = ?, "
					+ "����������_�� = ? WHERE �� = ?";
			ps = con.prepareStatement(SQLText);
			java.sql.Date s_d_start = dateToSQL(start);
	        java.sql.Date s_d_end = dateToSQL(end);
			ps.setInt(1, hurt);
			ps.setDate(2, s_d_start);
			ps.setDate(3, s_d_end);
			ps.setInt(4, currentID);
			ps.executeUpdate();
			System.out.println("������ ����������!");
        }
        return currentID;
	}
	
	//��������� �������� ��� ����������
	static int studentWithoutPerep(PreparedStatement ps, Connection con, String numb, String group,
			java.util.Date start, java.util.Date end, int hurt) throws SQLException{
		int last_ID = 0;
		System.out.println("� ��� ���������� ������ �������� �� ����");
		//O����� �������� �� � ����������
		String SQLText = "SELECT max(��) FROM ���������� ";
		ps = con.prepareStatement(SQLText);
		ResultSet result1 = ps.executeQuery();
		if (result1.next()) {
				last_ID = result1.getInt("max");
				System.out.println("������� �� = " + last_ID);
    			last_ID+=1;
		}
		System.out.println("�� ������ ��������: " + last_ID);
		//���������� ���� ���������� ��� ��������
		SQLText = "INSERT INTO ���������� (��, �_�������_������, "
				+ "�_�����, �_����������, ����������_�, ����������_��) "
				+ "VALUES (?, ?, ?, ?, ?, ?) ";
		ps = con.prepareStatement(SQLText);
		java.sql.Date s_d_start = dateToSQL(start);
        java.sql.Date s_d_end = dateToSQL(end);
		ps.setInt(1, last_ID);
		ps.setString(2, numb);
		ps.setString(3, group);
		ps.setInt(4, hurt);
		ps.setDate(5, s_d_start);
		ps.setDate(6, s_d_end);
		ps.executeUpdate();
		System.out.println("������ ���������� ��� ��������");
		return last_ID;
	}
	
	//���������
	static boolean InsertZas_StudAndRoom(PreparedStatement ps, Connection con, int current,
			String numb, String room) throws SQLException{
		boolean isSWP = false;
		int last_ID_s = 0;
		System.out.println("��������� ��������");
		String SQLText = "SELECT max(�_����������_��������) FROM ���������_������� ";
		ps = con.prepareStatement(SQLText);
		ResultSet result1 = ps.executeQuery();
		if (result1.next()) {
			last_ID_s = result1.getInt("max");
			System.out.println("last_ID_s = " + last_ID_s);
			last_ID_s+=1;
		}
		System.out.println("�� ��� ������ ������ ����������� ��������: " + last_ID_s);
		//���������� ������ ����������� �������� �� ����
		SQLText = "INSERT INTO ���������_������� (�_����������_��������,"
												 + " �_�������_������, "
												 + "��, �_������) "
												 + "VALUES (?, ?, ?, ?)";
		ps = con.prepareStatement(SQLText);
		ps.setInt(1, last_ID_s);
		ps.setString(2, numb);
		ps.setInt(3, current);
		ps.setString(4, room);
		ps.executeUpdate();
		System.out.println("�������� ���������!");
		isSWP = true;
		return isSWP;
	}
	
	//������ ������� ������� ���� ��� ������
	static void updateRoom(PreparedStatement ps, Connection con, String room, String st) throws SQLException{
		String SQLText = "SELECT �������_���� FROM ������ "
				+ "WHERE �_������ = ?";
		ps = con.prepareStatement(SQLText);
		ps.setString(1, room);
		ResultSet result1 = ps.executeQuery();
		int free=0;
		if (result1.next()) {
			free = result1.getInt("�������_����");
			free-=1;
		}
		SQLText = "UPDATE ������ SET �������_���� = ?, ����� = ? "
				+ "WHERE �_������ = ? ";
		ps = con.prepareStatement(SQLText);
		ps.setInt(1, free);
		ps.setString(2, st);
		ps.setString(3, room);
		ps.executeUpdate();
		System.out.println("³���� ���� ������ � �����: " + room);
	}
	
}