package com.studyplanner;

import com.studyplanner.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX 애플리케이션 진입점
 */
public class MainApp extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        try {
            // MainController 초기화
            mainController = new MainController(primaryStage);

            // Stage 설정
            primaryStage.setTitle(AppConfig.APP_TITLE);
            primaryStage.setScene(mainController.getScene());
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            // 종료 시 데이터 저장
            primaryStage.setOnCloseRequest(event -> {
                mainController.onClose();
            });

            // Stage 표시
            primaryStage.show();

            // 스크린샷 자동 생성 (첫 실행 시)
            mainController.captureScreenshots();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("애플리케이션 시작 실패: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
