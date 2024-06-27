package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.Parent;


public class Main extends Application {
	
    
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//Stage stage = new Stage(); 
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        loader.setController(new MainController());
        Parent root = loader.load();
		Scene scene = new Scene(root,Color.DARKGREEN);
		
		//Add icon
		//Image icon= new Image("imagename");
		//primaryStage.getIcons().add(icon);
		
		
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setResizable(false);
	}
}
