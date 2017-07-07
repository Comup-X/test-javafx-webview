package com.comup;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SpringBootApplication
public class TestJavafxWebviewApplication extends Application {

    private static String[] args;

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        TestJavafxWebviewApplication.args = args;
        Application.launch(TestJavafxWebviewApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.setOnAlert(event -> showAlert(event.getData()));
        engine.setConfirmHandler(message -> showConfirm(message));

        String content =
                "<html>"
                        + "<head>"
                        + "<script language='javascript'>"
                        + "function doConfirm() {"
                        +"test();"
                        + "    var accepted = confirm('Are you sure?');"
                        + "    if (accepted) {"
                        + "       document.getElementById('result').innerHTML = 'Accepted';"
                        + "    } else {"
                        + "       document.getElementById('result').innerHTML = 'Declined';"
                        + "    }"
                        + "}"
                        + "</script>"
                        + "<body>"
                        + "<div><button onclick='alert(\"This is an alert!\")'>Show alert</button>"
                        + "<button onclick='doConfirm()'>Confirm</button>"
                        + "</div>"
                        + "<div id='result'/>"
                        + "</body>"
                        + "</html>";

        engine.loadContent(content);
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue== Worker.State.SUCCEEDED){
                Element script = engine.getDocument().createElement("script");
                script.setTextContent("function test(){alert('我成功了');}");
                NodeList html = engine.getDocument().getElementsByTagName("html");
                html.item(0).appendChild(script);
                engine.executeScript("test();");
            }
        });
        primaryStage.setScene(new Scene(new BorderPane(webView)));
        primaryStage.show();
    }

    private void showAlert(String message) {
        Dialog<Void> alert = new Dialog<>();
        alert.getDialogPane().setContentText(message);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        alert.showAndWait();
    }

    private boolean showConfirm(String message) {
        Dialog<ButtonType> confirm = new Dialog<>();
        confirm.getDialogPane().setContentText(message);
        confirm.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        boolean result = confirm.showAndWait().filter(ButtonType.YES::equals).isPresent();

        // for debugging:
        System.out.println(result);

        return result;
    }

    @Override
    public void init() throws Exception {
        super.init();
        context = SpringApplication.run(TestJavafxWebviewApplication.class, args);
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        context.close();
    }
}
