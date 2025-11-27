package com.studyplanner.controller;

import com.studyplanner.model.Priority;
import com.studyplanner.model.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 과제 추가/수정 다이얼로그 컨트롤러
 */
public class DialogController {

    /**
     * 새 과제 추가 다이얼로그 표시
     */
    public static Optional<Task> showAddDialog(Stage owner) {
        return showDialog(owner, null);
    }

    /**
     * 과제 수정 다이얼로그 표시
     */
    public static Optional<Task> showEditDialog(Stage owner, Task task) {
        return showDialog(owner, task);
    }

    /**
     * 다이얼로그 표시 (추가/수정 공통)
     */
    private static Optional<Task> showDialog(Stage owner, Task existingTask) {
        boolean isEdit = (existingTask != null);

        // 다이얼로그 생성
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "과제 수정" : "과제 추가");
        dialog.setHeaderText(isEdit ? "과제 정보를 수정하세요." : "새로운 과제를 추가하세요.");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);

        // 버튼 타입 설정
        ButtonType saveButtonType = new ButtonType("저장", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 폼 레이아웃
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // 제목 입력
        TextField titleField = new TextField();
        titleField.setPromptText("과제 제목을 입력하세요");
        if (isEdit)
            titleField.setText(existingTask.getTitle());

        // 마감일 선택
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("마감일 선택");
        if (isEdit) {
            dueDatePicker.setValue(existingTask.getDueDate());
        } else {
            dueDatePicker.setValue(LocalDate.now());
        }

        // 우선순위 선택
        ComboBox<Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Priority.values());
        if (isEdit) {
            priorityCombo.setValue(existingTask.getPriority());
        } else {
            priorityCombo.setValue(Priority.MEDIUM);
        }

        // 메모 입력
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("메모 (선택사항)");
        notesArea.setPrefRowCount(3);
        if (isEdit)
            notesArea.setText(existingTask.getNotes());

        // 태그 입력
        TextField tagsField = new TextField();
        tagsField.setPromptText("태그 (쉼표로 구분)");
        if (isEdit)
            tagsField.setText(existingTask.getTagsAsString());

        // 완료 체크박스 (수정 시에만)
        CheckBox completedCheck = null;
        if (isEdit) {
            completedCheck = new CheckBox("완료됨");
            completedCheck.setSelected(existingTask.isCompleted());
        }

        // 그리드에 컴포넌트 추가
        grid.add(new Label("제목:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("마감일:"), 0, 1);
        grid.add(dueDatePicker, 1, 1);
        grid.add(new Label("우선순위:"), 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(new Label("메모:"), 0, 3);
        grid.add(notesArea, 1, 3);
        grid.add(new Label("태그:"), 0, 4);
        grid.add(tagsField, 1, 4);

        if (isEdit && completedCheck != null) {
            grid.add(completedCheck, 1, 5);
        }

        dialog.getDialogPane().setContent(grid);

        // 초기 포커스 설정
        titleField.requestFocus();

        // 저장 버튼 비활성화 조건
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // 제목과 마감일 입력 시에만 저장 버튼 활성화
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || dueDatePicker.getValue() == null);
        });

        dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(titleField.getText().trim().isEmpty() || newValue == null);
        });

        // 초기 유효성 검사
        if (isEdit) {
            saveButton.setDisable(false);
        }

        // 결과 변환
        CheckBox finalCompletedCheck = completedCheck;
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // 유효성 검증
                String title = titleField.getText().trim();
                LocalDate dueDate = dueDatePicker.getValue();

                if (title.isEmpty()) {
                    showError("제목을 입력해주세요.");
                    return null;
                }

                if (dueDate == null) {
                    showError("마감일을 선택해주세요.");
                    return null;
                }

                // 과거 마감일 경고
                if (dueDate.isBefore(LocalDate.now())) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("경고");
                    alert.setHeaderText("마감일이 과거입니다.");
                    alert.setContentText("계속하시겠습니까?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return null;
                    }
                }

                // Task 객체 생성
                Task task;
                if (isEdit) {
                    task = existingTask;
                    task.setTitle(title);
                    task.setDueDate(dueDate);
                    task.setPriority(priorityCombo.getValue());
                    task.setNotes(notesArea.getText());
                    task.setTagsFromString(tagsField.getText());
                    if (finalCompletedCheck != null) {
                        task.setCompleted(finalCompletedCheck.isSelected());
                    }
                } else {
                    task = new Task(title, dueDate, priorityCombo.getValue());
                    task.setNotes(notesArea.getText());
                    task.setTagsFromString(tagsField.getText());
                }

                return task;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * 에러 메시지 표시
     */
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("입력 오류");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
