package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
       /* Parent root = FXMLLoader.load(getClass().getResource("GDBView.fxml"));
        primaryStage.setTitle("GDB UI");
        primaryStage.setScene(new Scene(root, 560, 700));
        primaryStage.setResizable(false);
        primaryStage.show();*/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GDBView.fxml"));
        Parent root = loader.load();
        GDBController controller = loader.getController();
        primaryStage.setTitle("GDB UI");
        primaryStage.setScene(new Scene(root, 608, 700));
        primaryStage.setResizable(false);
        primaryStage.setOnHidden(e -> controller.shutdown());
        primaryStage.show();


    }


    public static void main(String[] args) {
        launch(args);
        //GDBModel m = new GDBModel();
        //m.GDBStart();

        //GDBView v = new GDBView();
        //GDBController c = new GDBController();
        //c.initController();
    }
}
