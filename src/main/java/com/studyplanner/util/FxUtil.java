package com.studyplanner.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * JavaFX 헬퍼 유틸리티
 */
public class FxUtil {

    /**
     * 정보 알림 표시
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 경고 알림 표시
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 에러 알림 표시
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 스크린샷을 파일로 저장
     */
    public static void saveSnapshot(WritableImage image, String filePath) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            System.out.println("스크린샷 저장: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("스크린샷 저장 실패: " + e.getMessage());
        }
    }
}
