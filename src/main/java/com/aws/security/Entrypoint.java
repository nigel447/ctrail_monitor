package com.aws.security;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Entrypoint extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CTrailApp app = new CTrailApp();
        app.start(primaryStage);
    }

    @Override
    public void stop( ) throws Exception {
        super.stop();
        Platform.exit();
    }

}
