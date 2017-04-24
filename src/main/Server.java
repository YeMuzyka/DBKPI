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
		System.out.println("Запуск сервера");
		boolean hurt_Connect = false;
		Socket hurt = null;
	    //створення сервера	 
		while(true) {
		    try (ServerSocket toClient = new ServerSocket(1337)) {
		    	System.out.println();
		    	System.out.println("Сервер запущено!"); 
				
				if(!hurt_Connect) {
		    		hurt = toClient.accept();
					hurt_Connect = true;
		    		new Thread_Hurt(hurt);
		    	} else {
		    		System.out.println("Гуртожиток підключенний!");
		    	}
				
		    	try {
		    		//запит на заселення
			    	Socket client = toClient.accept();
			    	ObjectInputStream deserializer = new ObjectInputStream(client.getInputStream());
			    	s_f_a = (Search_Fields) deserializer.readObject();
			    	System.out.println(s_f_a.getS_name());
			    	System.out.println(s_f_a.getS_group());
			    	System.out.println(s_f_a.getD_start());
			    	System.out.println(s_f_a.getD_end());
			    	System.out.println(s_f_a.getS_email());
			    	
			    	//Підключення до бази
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
			        	//Загружаємо драйвер
			            Class.forName("org.postgresql.Driver");
			            System.out.println("Підключення до бази");
			            //Створюємо підключення
			            connection = DriverManager.getConnection(url, name, password);
			            System.out.println("Підключення успішне");
			            boolean res_Stud_Gr_Hurt = false, res_Ses = false, 
			            		res_Zapret = false , res_Free_Room = false, res_Perep = false;
			            boolean res_Zas_Room = false; //заселенний в кімнату?
			            PreparedStatement pst = null;
			            
			            res_Stud_Gr_Hurt = studentExists(pst, connection, s_f_a.getS_name(), s_f_a.getS_group());
			            if (!res_Stud_Gr_Hurt) {
			            	System.out.println("Такого студента не існує!");
			            } else {
			            	n_z = getID(pst, connection, s_f_a.getS_name());
			            	stat = getStat(pst, connection, s_f_a.getS_name());
			            	res_Ses = currectSess(pst, connection, s_f_a.getD_start(), s_f_a.getD_end());
				            if (!res_Stud_Gr_Hurt | !res_Ses){
				            	try{
					            	eText = "Шановний(на) " + s_f_a.getS_name() + ", виконати ваш запит неможливо, "
					            			+ "можливо ви помилились в наборі тексту у відповідних формах. "
					            			+ "Якщо це не так - зверніться до вашого деканату.";
					            	sendEmail(ses, s_f_a.getS_email(), eText);
					            } catch( MessagingException me){
					            	System.out.println("Лист відправити неможливо: " + me);
					            }
				            }
				            if(!res_Ses){
				            	System.out.println("Не співпадають дати сесії!");
				            } else {
				            	res_Zapret = zaborStud(pst, connection, n_z);
					            if (res_Zapret) {
					            	System.out.println("Студент заборонений до поселення в гуртожиток!");
					            	try{
						            	eText = "Шановний(на) " + s_f_a.getS_name() + ", нажаль, Ви у чорному списку на "
						            			+ "заселення в гуртожиток.";
						            	sendEmail(ses, s_f_a.getS_email(), eText);
						            } catch( MessagingException me) {
						            	System.out.println("Лист відправити неможливо: " + me);
						            }
					            } else {
					            	res_Free_Room = findPlace(pst, connection, stat);				            	
					            	if (!res_Free_Room){
					            		System.out.println("Немає вільного місця для данної статі!");
					            		try{
							            	eText = "Шановний(на) " + s_f_a.getS_name() + ", нажаль, вільного місця в гуртожитках"
							            			+ " немає";
							            	sendEmail(ses, s_f_a.getS_email(), eText);
							            } catch( MessagingException me) {
							            	System.out.println("Лист відправити неможливо: " + me);
							            }
					            	} else {
					            		System.out.println("Для данної статі є вільне місце");
					            		try{
							            	eText = "Шановний(на) " + s_f_a.getS_name() + ", для заселення в гуртожиток на час сесії, "
							            			+ "Вам необхідно оплатити проживання. Реквізити: " + "\n" + "Постачальник "
							            			+ "послуг: НТУУ\"КПІ\"" + "\n" + "Р/Р: 31254289213853 МФО 820172 "
							            			+ "ГУДКСУ м.Києва ЕДРППОУ 02070921" + "\n"
							            			+ "Призначення платежу: проживання заочника." + "\n"
							            			+ "Ціна за одну ніч складає 70 грн.";
							            	sendEmail(ses, s_f_a.getS_email(), eText);
							            } catch( MessagingException me){
							            	System.out.println("Лист відправити неможливо: " + me);
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
					            		}//додано перепустку і заселення студента
					            		if (res_Zas_Room){
					            			String address = new String();
					            			String SQLText = "SELECT адреса FROM гуртожиток "
					            					+ "WHERE №_гуртожитка = ? ";
					            			pst = connection.prepareStatement(SQLText);
					            			pst.setInt(1, num_hurt);
					            			ResultSet result1 = pst.executeQuery();
					            			if (result1.next()) {
					            				address = result1.getString("адреса");
					            			}
					            			try {
					    		            	eText = "Вас заселенно в гуртожиток №: " + num_hurt + " в кімнату: " + num_room
					    		            			+ "." + "\nАдреса гуртожитку: " + address + "."
					    		            			+ "\nЯкщо у Вас немає перепустки, зверніться до Бюро Перепусток за адресою: "
					    		            			+ "Гуртожиток № 7 вул. Металістів, 3";
					    		            	sendEmail(ses, s_f_a.getS_email(), eText);
					    		            } catch( MessagingException me) {
					    		            	System.out.println("Лист відправити неможливо: " + me);
					    		            }
					            		}
					            	}//вільне місце в гуртожитку
					            } // не у чорному списку 
				            } //правильність дати сесії
			            } //існування студента 
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
		    //Сервер - сокет
		    } catch (IOException e) {
		      System.out.println("Щось не так з портом 1337");
		      System.exit(-1);
		    }
		}//while (true)
	}//main()
	
	//Ініціалізація для e-mail
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
	
	//Відправлення е-mail
	static void sendEmail(Session curSes, String em_adr, String em_message) throws AddressException, MessagingException{
		Message emess = new MimeMessage(curSes);
    	emess.setFrom(new InternetAddress("oneguyfromdbkpi@gmail.com"));
    	emess.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em_adr));
    	emess.setSubject("Заселення в гуртожиток");
    	emess.setText(em_message);
    	Transport.send(emess);
    	System.out.println("Успішне відправлення листа");
	}
	
	//Запис до файлу
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
	
	//Такий студент існує?
	static boolean studentExists(PreparedStatement ps, Connection con, String name, 
			String group) throws SQLException{
		boolean exists = false;
		String SQLText = "SELECT №_залікової_книжки, піп, №_групи, стать, необхідність_гуртожитку "
        		+ "FROM студент WHERE піп= ? AND №_групи= ? AND необхідність_гуртожитку= true";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, name);
        ps.setString(2, group);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	exists = true;
        	System.out.println("#" + result1.getRow() +"\t"
                    + result1.getString("№_залікової_книжки") + "\t"
                    + result1.getString("піп") + "\t"
                    + result1.getString("стать") + "\t"
                    + result1.getString("№_групи") + "\t"
                    + result1.getBoolean("необхідність_гуртожитку") + "\t");
        }
		return exists;
	}
	
	//Отримання №_залікової_книжки
	static String getID(PreparedStatement ps, Connection con, String name) throws SQLException{
		String ID = "";
		String SQLText = "SELECT №_залікової_книжки "
        		+ "FROM студент WHERE піп= ? AND необхідність_гуртожитку= true";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, name);
        ResultSet result1 = ps.executeQuery();
        if(result1.next()){
        	ID = result1.getString("№_залікової_книжки");
        }
		return ID;
	}
	
	//Отримання статті студента
	static String getStat(PreparedStatement ps, Connection con, String name) throws SQLException{
		String stat = "";
		String SQLText = "SELECT стать "
        		+ "FROM студент WHERE піп= ? AND необхідність_гуртожитку= true";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, name);
        ResultSet result1 = ps.executeQuery();
        if(result1.next()){
        	stat = result1.getString("стать");
        }
		return stat;
	}
	
	//Перетворення із java.util.Date в java.sql.Date
	static java.sql.Date dateToSQL(java.util.Date dTS){
		java.sql.Date ud = new java.sql.Date(new java.util.Date().getTime());
		ud = new java.sql.Date(dTS.getTime());
		return ud;
	}
	
	//Якщо студент існує - звіряємо сесію
	static boolean currectSess(PreparedStatement ps, Connection con, java.util.Date start, java.util.Date end) throws SQLException{
		boolean currSes = false;
		System.out.println("Такий студент існує!");
        String SQLText = "SELECT №_групи, початок_сесії, кінець_сесії "
        		+ "FROM група WHERE початок_сесії = ? AND кінець_сесії = ?";
        ps = con.prepareStatement(SQLText);
        
        //Перетворення із java.util.Date в java.sql.Date
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
        			+ result1.getString("№_групи") + "\t"
        			+ result1.getDate("початок_сесії") + "\t"
        			+ result1.getDate("кінець_сесії") + "\t");
        }
		return currSes;
	}
	
	//Якщо сесія співпадає з БД - перевіряємо, чи не заборонений студент до поселення
	static boolean zaborStud(PreparedStatement ps, Connection con, String numb) throws SQLException{
		boolean zS = false;
		System.out.println("Дати сесії співпадають");	
    	String SQLText = "SELECT №_залікової_книжки "
        		+ "FROM заборонені_до_поселення WHERE №_залікової_книжки = ?";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, numb);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	zS = true;
        	System.out.println("Студента неможливо поселити в гуртожиток!");
        	System.out.println("n_z: " + numb);
        }
		return zS;
	}
	
	//Якщо студент не заборонений до поселення - пошук вільного місця в гуртожитку
	static boolean findPlace(PreparedStatement ps, Connection con, String st) throws SQLException{
		boolean findPlace = false;
		System.out.println("Студент може поселитись в гуртожиток");
    	String SQLText = "SELECT №_кімнати, вільного_місця "
    			+ "FROM кімната WHERE вільного_місця > 0 AND стать = ? "
    			+ "OR стать = ' ' " ;
    	ps = con.prepareStatement(SQLText);
    	ps.setString(1, st);
    	ResultSet result1 = ps.executeQuery();			            	
    	while (result1.next()) {
    		findPlace = true;
    		System.out.println("#:" + result1.getRow() + "\t"
    				+ result1.getString("№_кімнати") + "\t"
    				+ result1.getInt("вільного_місця") + "\t");
    	}
		return findPlace;
	}
	
	//Номер кімнати і гуртожитку
	static String getRoom(PreparedStatement ps, Connection con, String st) throws SQLException {
		String room = new String();
		String SQLText = "SELECT №_кімнати, вільного_місця "
    			+ "FROM кімната WHERE вільного_місця > 0 AND стать = ? "
    			+ "OR стать = ' ' " ;
    	ps = con.prepareStatement(SQLText);
    	ps.setString(1, st);
    	ResultSet result1 = ps.executeQuery();
    	if (result1.next()) {
    		room = result1.getString("№_кімнати");
			System.out.println("Номер кімнати " + room);
		}
    	return room;
	}
	
	//Номер гуртожитку
	static int getHurt(PreparedStatement ps, Connection con, String room){
		int hurt;
		char ch[] = new char [1];
		ch[0] = room.charAt(1);
		if (ch[0]=='9') {
			hurt = 19;
		} else 
			hurt = 18;
		System.out.println("Номер гуртожитка: " + hurt);
		return hurt;
	}
	
	//Студент є у базі перепусток?
	static boolean existsInDB(PreparedStatement ps, Connection con, String numb) throws SQLException{
		boolean existsInDB = false;
		String SQLText = "SELECT ід, №_залікової_книжки "
        		+ "FROM перепустка WHERE №_залікової_книжки = ? ";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, numb);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()) {
        	existsInDB = true;
        }
		return existsInDB;
	}
	
	//Заселення студента із старою перепусткою
	static int studentWithPerep(PreparedStatement ps, Connection con, String numb,	java.util.Date start, 
			java.util.Date end, int hurt) throws SQLException{
		int currentID = 0;
		String SQLText = "SELECT ід, №_залікової_книжки "
        		+ "FROM перепустка WHERE №_залікової_книжки = ? ";
        ps = con.prepareStatement(SQLText);
        ps.setString(1, numb);
        ResultSet result1 = ps.executeQuery();
        if (result1.next()){
        	System.out.println("Апдейт для данного студента");
			currentID = result1.getInt("ід");
			//Oбновлення даних
			SQLText = "UPDATE перепустка "
					+ "SET №_гуртожитка = ?, перепустка_з = ?, "
					+ "перепустка_до = ? WHERE ід = ?";
			ps = con.prepareStatement(SQLText);
			java.sql.Date s_d_start = dateToSQL(start);
	        java.sql.Date s_d_end = dateToSQL(end);
			ps.setInt(1, hurt);
			ps.setDate(2, s_d_start);
			ps.setDate(3, s_d_end);
			ps.setInt(4, currentID);
			ps.executeUpdate();
			System.out.println("Апдейт завершенно!");
        }
        return currentID;
	}
	
	//Заселення студента без перепустки
	static int studentWithoutPerep(PreparedStatement ps, Connection con, String numb, String group,
			java.util.Date start, java.util.Date end, int hurt) throws SQLException{
		int last_ID = 0;
		System.out.println("В базі перепусток такого студента не існує");
		//Oстаннє значення ід в перепустці
		String SQLText = "SELECT max(ід) FROM перепустка ";
		ps = con.prepareStatement(SQLText);
		ResultSet result1 = ps.executeQuery();
		if (result1.next()) {
				last_ID = result1.getInt("max");
				System.out.println("Останній ІД = " + last_ID);
    			last_ID+=1;
		}
		System.out.println("ІД нового студента: " + last_ID);
		//Добавлення нової перепустки для студента
		SQLText = "INSERT INTO перепустка (ід, №_залікової_книжки, "
				+ "№_групи, №_гуртожитка, перепустка_з, перепустка_до) "
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
		System.out.println("Додано перепустку для студента");
		return last_ID;
	}
	
	//Заселення
	static boolean InsertZas_StudAndRoom(PreparedStatement ps, Connection con, int current,
			String numb, String room) throws SQLException{
		boolean isSWP = false;
		int last_ID_s = 0;
		System.out.println("Заселення студента");
		String SQLText = "SELECT max(№_заселеного_студента) FROM заселений_студент ";
		ps = con.prepareStatement(SQLText);
		ResultSet result1 = ps.executeQuery();
		if (result1.next()) {
			last_ID_s = result1.getInt("max");
			System.out.println("last_ID_s = " + last_ID_s);
			last_ID_s+=1;
		}
		System.out.println("ІД для нового запису заселенного студента: " + last_ID_s);
		//Добавлення нового заселенного студента до бази
		SQLText = "INSERT INTO заселений_студент (№_заселеного_студента,"
												 + " №_залікової_книжки, "
												 + "ід, №_кімнати) "
												 + "VALUES (?, ?, ?, ?)";
		ps = con.prepareStatement(SQLText);
		ps.setInt(1, last_ID_s);
		ps.setString(2, numb);
		ps.setInt(3, current);
		ps.setString(4, room);
		ps.executeUpdate();
		System.out.println("Студента заселенно!");
		isSWP = true;
		return isSWP;
	}
	
	//Змінити кількість вільного місця для кімнати
	static void updateRoom(PreparedStatement ps, Connection con, String room, String st) throws SQLException{
		String SQLText = "SELECT вільного_місця FROM кімната "
				+ "WHERE №_кімнати = ?";
		ps = con.prepareStatement(SQLText);
		ps.setString(1, room);
		ResultSet result1 = ps.executeQuery();
		int free=0;
		if (result1.next()) {
			free = result1.getInt("вільного_місця");
			free-=1;
		}
		SQLText = "UPDATE кімната SET вільного_місця = ?, стать = ? "
				+ "WHERE №_кімнати = ? ";
		ps = con.prepareStatement(SQLText);
		ps.setInt(1, free);
		ps.setString(2, st);
		ps.setString(3, room);
		ps.executeUpdate();
		System.out.println("Вільне місце змінено в кімнаті: " + room);
	}
	
}