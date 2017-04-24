package main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.collections.*;
import java.util.Locale;
import include.Search_Fields;

public class Search_GUI extends Application{
	boolean res_Name = false, res_Group = false, res_Email = false;
	boolean rC1 = false, rC2 = false, rC3 = false, resEnc = false;
	boolean res4Char = false, res5Char = false, res6Char = false, res7Char = false, res_Last_Char = false;
	boolean rC_name = false, rC_group = false, rC_email = false;
	int bC=0, iP, a;
	char ch[];
	String errStr="";
	
	private Stage st_Search;
	
	public static void main(String[] args){
		launch(args);
	}
	
	public void start(Stage st_Search){
		this.st_Search = st_Search;
		initSearchUI();
		st_Search.show();
	}
	
	private void initSearchUI(){
		Label lab_name;
		TextField tf_name;
		Label lab_group;
		TextField tf_group;
		Label lab_start;
		ComboBox<String> cb_start;	
		Label lab_end;
		ComboBox<String> cb_end;
		Label lab_email;
		TextField tf_email;
		Button btn_search;
		Text action_text;
		//ініціалізація вікна
		st_Search.setTitle("Заселення студента на час сесії");
		Group gr = new Group();
		
		lab_name = new Label("Прізвище Ім'я По-Батькові:");
		lab_name.setLayoutX(60);
		lab_name.setLayoutY(35);
		gr.getChildren().add(lab_name);
		tf_name = new TextField("");
		tf_name.setLayoutX(220);
		tf_name.setLayoutY(30);
		gr.getChildren().add(tf_name);
		lab_group = new Label("Група:");
		lab_group.setLayoutX(60);
		lab_group.setLayoutY(70);
		gr.getChildren().add(lab_group);
		tf_group = new TextField("");
		tf_group.setLayoutX(220);
		tf_group.setLayoutY(65);
		gr.getChildren().add(tf_group);
		lab_start = new Label("Початок сесії:");
		lab_start.setLayoutX(60);
		lab_start.setLayoutY(105);
		gr.getChildren().add(lab_start);
		ObservableList<String> cb_start_list = 
				FXCollections.observableArrayList (
						"Вказати","1.12.2016", "5.12.2016", "10.12.2016");
		cb_start = new ComboBox<String>(cb_start_list);
		cb_start.setValue("Вказати");
		cb_start.setLayoutX(220);
		cb_start.setLayoutY(100);
		cb_start.setPrefSize(148, 25);
		gr.getChildren().add(cb_start);
		lab_end = new Label("Кінець сесії:");
		lab_end.setLayoutX(60);
		lab_end.setLayoutY(140);
		gr.getChildren().add(lab_end);
		ObservableList<String> cb_end_list = 
				FXCollections.observableArrayList (
						"Вказати","15.12.2016", "19.12.2016", "22.12.2016");
		cb_end = new ComboBox<String>(cb_end_list);
		cb_end.setValue("Вказати");
		cb_end.setLayoutX(220);
		cb_end.setLayoutY(135);
		cb_end.setPrefSize(148, 25);
		gr.getChildren().add(cb_end);
		lab_email = new Label("e-mail:");
		lab_email.setLayoutX(60);
		lab_email.setLayoutY(175);
		gr.getChildren().add(lab_email);
		tf_email = new TextField("");
		tf_email.setLayoutX(220);
		tf_email.setLayoutY(170);
		gr.getChildren().add(tf_email);
		btn_search = new Button("Заселити");
		btn_search.setLayoutX(60);
		btn_search.setLayoutY(210);
		btn_search.setPrefSize(309, 25);
		gr.getChildren().add(btn_search);
		action_text = new Text("");
		action_text.setLayoutX(60);
		action_text.setLayoutY(260);
		gr.getChildren().add(action_text);
		Scene sc_Search = new Scene(gr, 450, 300);
		st_Search.setScene(sc_Search);
		
		//створення підказок біля кожного TextField
		tf_name.setTooltip(new Tooltip("Приклад ім'я студента: Іванов Іван Іванович"));
		tf_group.setTooltip(new Tooltip("Приклад групи: ІО-з51с"));
		tf_email.setTooltip(new Tooltip("Приклад e-mail: www.example@mail.com"));
		
		//дія кнопки
		btn_search.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent ae){
				errStr = "";
				if((tf_name.getText().isEmpty()) | (tf_group.getText().isEmpty()) | 
				   (cb_start.getValue() == "Вказати") | (cb_end.getValue() == "Вказати") |
				   (tf_email.getText().isEmpty())){
					action_text.setFill(Color.FIREBRICK);
					action_text.setText("Заповніть усі поля!");
				} else {
					
					res_Name = currectName(tf_name.getText());
					if (!res_Name) {
						errStr +="Недопустиме ім'я ";
					}
					bC = 0;
					res_Group = correctGroup(tf_group.getText());
					if (!res_Group) {
						errStr +="Недопустима група ";
					}
					res_Email = correctEmail(tf_email.getText());
					if (!res_Email) {
						errStr +="Недопустимий e-mail ";
					}
					
					if(res_Name & res_Group & res_Email){
												
						//заповнення дати початку і кінця сесії
						//java.util.Date sd_start = null;
						java.util.Date d_start = null;
						java.util.Date d_end = null;
						try{
							SimpleDateFormat d_format = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
							d_start = d_format.parse(cb_start.getValue());
						} catch (ParseException e){
							System.out.println("Error: " +e);
						}
						try{
							SimpleDateFormat d_format = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
							d_end = d_format.parse(cb_end.getValue());
						} catch(ParseException e){
							System.out.println("Error: " +e);
						}
						
						try (Socket toServer = new Socket("192.168.35.20", 1337)){
							ObjectOutputStream serializer = new ObjectOutputStream(
									toServer.getOutputStream());
							Search_Fields s_f_s = new Search_Fields(tf_name.getText(), tf_group.getText(), 
									d_start, d_end, tf_email.getText());
							serializer.writeObject(s_f_s);
							serializer.flush();
						} catch (UnknownHostException e){
							System.out.println("Error: " +e);
						} catch (IOException e){
							System.out.println("Error: " +e);
						}
															
						
						action_text.setFill(Color.GREEN);
						action_text.setText("Усі поля заповнені!");
					} else {
						action_text.setFill(Color.FIREBRICK);
						action_text.setText(errStr);
					}
				}
			}
		});
	}
	
	//ім'я студента
	boolean currectName(String s_name){
		s_name=s_name.trim();
		if (s_name.length()>20 & s_name.length()<=50)
		{
			ch = new char [1];
			//перший символ прізвища
			ch[0] = s_name.charAt(0);
			if(Character.isUpperCase(ch[0]))
			{
				rC1 = true;
			} else
				rC1 = false;
			//перший символ ім'я
			iP = s_name.indexOf(' ');
			iP++;
			ch[0] = s_name.charAt(iP);
			if(Character.isUpperCase(ch[0]))
			{
				rC2 = true;
			} else
				rC2 = false;
			//перший символ По-Батькові
			iP = s_name.lastIndexOf(' ');
			iP++;
			ch[0] = s_name.charAt(iP);
			if(Character.isUpperCase(ch[0]))
			{
				rC3 = true;
			} else
				rC3 = false;
			byte b[] = s_name.getBytes();
			for (int i = 0; i<b.length; i++)
			{
				a=b[i]+256;
				if (a>=178 & a<=180)
				{
					continue;
				} else
					if (a>=191 & a<=217)
					{
						continue;
					} else
						if (a>=222 & a<=249)
						{
							continue;
						} else
							if (a==165 | a==170 | a==175 | a==186
								| a==220  | a==252 | a==254 | a==255
								| a==288 | a==295 | a==301)
							{
								continue;
							} else 
								bC+=1;
			}
			if(bC==0)
			{
				resEnc=true;
			} else
				resEnc=false;
			if(rC1 & rC2 & rC3 & resEnc)
			{
				rC_name = true;
			} else
				rC_name = false;
		}
		return rC_name;
	}
	
	//правильність вказаної групи
	boolean correctGroup(String s_group){
		s_group = s_group.trim();
		if (s_group.length()>=5 & s_group.length()<=7)
		{
			ch = new char [1];
			//перша літера групи
			ch[0] = s_group.charAt(0);
			if(Character.isUpperCase(ch[0]))
			{
				rC1 = true;
			} else
				rC1 = false;
			//друга літера групи
			ch[0] = s_group.charAt(1);
			if(Character.isUpperCase(ch[0]))
			{
				rC2 = true;
			} else
				rC2 = false;
			//тире у назві групи
			iP = s_group.indexOf('-');
			if (iP!=2 | iP == -1)
			{
				rC3 = false;
			}
			else rC3 = true;
			//довжина 5 символів
			if (s_group.length() == 5)
			{
				ch[0] = s_group.charAt(3);
				if (Character.isDigit(ch[0]))
				{
					res4Char = true;
				}
				else res4Char = false;
				ch[0] = s_group.charAt(4);
				if (Character.isDigit(ch[0]))
				{
					res5Char = true;
				}
				else res5Char = false;
				if (res4Char & res5Char)
				{
					res_Last_Char = true;
				}
			}
			//довжина 6 символів
			if (s_group.length() == 6)
			{
				ch[0] = s_group.charAt(4);
				if (Character.isDigit(ch[0]))
				{
					res5Char = true;
				}
				else res5Char = false;
				ch[0] = s_group.charAt(3);
				if (ch[0] =='з' | Character.isDigit(ch[0]))
				{
					res4Char = true;
				}
				else res4Char = false;
				ch[0] = s_group.charAt(5);
				if (ch[0] =='с' | ch[0] =='м' | Character.isDigit(ch[0]))
				{
					res6Char = true;
				}
				else res6Char = false;
				if (res4Char & res5Char & res6Char)
				{
					res_Last_Char = true;
				}
			}
			//довжина 7 символів
			if (s_group.length() == 7)
			{
				ch[0] = s_group.charAt(3);
				if (ch[0] == 'з')
				{
					res4Char = true;
				}
				else res4Char = false;
				ch[0] = s_group.charAt(4);
				if (Character.isDigit(ch[0]))
				{
					res5Char = true;
				}
				else res5Char = false;
				ch[0] = s_group.charAt(5);
				if (Character.isDigit(ch[0]))
				{
					res6Char = true;
				}
				else res6Char = false;
				ch[0] = s_group.charAt(6);
				if (ch[0] == 'с')
				{
					res7Char = true;
				}
				else res7Char = false;
				if (res4Char & res5Char & res6Char & res7Char)
				{
					res_Last_Char = true;
				}
			}
			byte b[] = s_group.getBytes();
			for (int i = 0; i<b.length; i++)
			{
				a=b[i]+256;
				if (a>=304 & a<=313)
				{
					continue;
				} else
					if (a>=178 & a<=180)
					{
						continue;
					} else
						if (a>=191 & a<=217)
						{
							continue;
						} else
							if (a>=222 & a<=249)
							{
								continue;
							} else
								if (a==165 | a==170 | a==175 | a==186
									| a==220  | a==252 | a==254 | a==255
									| a==301)
								{
									continue;
								} else 
									bC+=1;
			}
			if(bC==0)
			{
				resEnc=true;
			} else
				resEnc=false;
			if(rC1 & rC2 & rC3 & resEnc & res_Last_Char)
			{
				rC_group = true;
			} else
				rC_group = false;
		}
		return rC_group;
	}
	
	//допустимість e-mail
	boolean correctEmail(String s_email){
		if(s_email.contains("@") & s_email.contains(".")){
			rC_email = true;
		} else
			rC_email = false;
		return rC_email;
	}
	
}