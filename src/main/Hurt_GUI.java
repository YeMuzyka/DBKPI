package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import include.Input_Log_Pass;
import include.Login_Hurt;
import include.Send_Mess;
import javafx.application.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class Hurt_GUI extends Application{
	private Stage hurt_GUI;
	private GridPane grid;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void start(Stage hurt_GUI){
		this.hurt_GUI = hurt_GUI;
		GridPane gp = new GridPane();
		initHurtUI(gp);
		hurt_GUI.show();
	}
	
	private void initHurtUI(GridPane gp){
		hurt_GUI.setTitle("Вхід в систему гуртожитка");
		this.grid = gp;
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		Text scenetitle = new Text("Вітаю");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);
		 
		Label userName = new Label("User Name:");
		grid.add(userName, 0, 1);
		 
		TextField userTextField = new TextField();
		grid.add(userTextField, 1, 1);
		 
		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);
		 
		PasswordField pwBox = new PasswordField();
		grid.add(pwBox, 1, 2);
		
		Button btn = new Button("Sign in");
		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		grid.add(hbBtn, 1, 4);
		
		final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        Scene scene = new Scene(grid, 300, 275);
        
        btn.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
            	try (Socket toServer = new Socket("192.168.35.20", 1337)){
					ObjectOutputStream ser_hurt = new ObjectOutputStream(
							toServer.getOutputStream());
					Login_Hurt L_H = new Login_Hurt( userTextField.getText(), pwBox.getText());
					ser_hurt.writeObject(L_H);
					ser_hurt.flush();
					
					ObjectInputStream desr_ilp = new ObjectInputStream(toServer.getInputStream());
					Input_Log_Pass i_l_p = (Input_Log_Pass) desr_ilp.readObject();
					System.out.println(i_l_p.getNumber());
					System.out.println(i_l_p.isResult());
					
					if (i_l_p.isResult()){
						System.out.println("Залогінився гуртожиток: " + i_l_p.getNumber());
						//створення меню
						BorderPane bp = new BorderPane();
						MenuBar mb = new MenuBar();
						Menu menu = new Menu("Вихід");
						MenuItem exit = new MenuItem ("Вийти з профілю");
						menu.getItems().addAll(exit);
						mb.getMenus().add(menu);
						bp.setTop(mb);
						TextArea ta = new TextArea();
						ta.setVisible(false);
						bp.setCenter(ta);
						Pane pane = new Pane();
						ToggleButton tb = new ToggleButton("Повідомлення");
						tb.setLayoutY(35.0);
						pane.getChildren().add(tb);
						exit.setOnAction(new EventHandler<ActionEvent>(){
									public void handle(ActionEvent ae){
										try {
											toServer.close();
											hurt_GUI.setTitle("Вхід в систему гуртожитка");
											userTextField.setText("");
											pwBox.setText("");
											actiontarget.setText("");
											hurt_GUI.setScene(scene);
										} catch (IOException e) {
											System.out.println("Error: " + e);
										}
									}
								});
						Send_Mess i_mess = (Send_Mess) desr_ilp.readObject();
						tb.setOnAction(new EventHandler<ActionEvent>(){
									public void handle(ActionEvent ae){
										if(tb.isSelected()) {
											ta.setText(i_mess.getNew_stud());
											ta.setVisible(true);
										}
										else{
											ta.clear();
											ta.setVisible(false);
										}
									}
								});
						bp.setLeft(pane);
						hurt_GUI.setTitle("Гуртожиток " + i_l_p.getNumber());
						Scene BPScene = new Scene (bp, 300, 275);
						hurt_GUI.setScene(BPScene);
						
					} else{
						actiontarget.setFill(Color.FIREBRICK);
		                actiontarget.setText(i_l_p.getNumber());
						System.out.println(i_l_p.getNumber());
					}
				} catch (UnknownHostException uhe){
					System.out.println("Error: " + uhe);
				} catch (IOException ioe){
					System.out.println("Error: " + ioe);
				} catch (ClassNotFoundException e1) {
					System.out.println("Error: " + e1);
				}
            }
        });
		hurt_GUI.setScene(scene);
	}
}
