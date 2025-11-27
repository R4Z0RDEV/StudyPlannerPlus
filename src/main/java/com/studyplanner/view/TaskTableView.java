package com.studyplanner.view;

import com.studyplanner.model.Priority;
import com.studyplanner.model.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

/**
 * 과제 목록 TableView 컴포넌트
 */
public class TaskTableView extends TableView<Task> {

    public TaskTableView() {
        super();
        setupColumns();
        setupContextMenu();

        // 더블클릭 이벤트는 MainController에서 설정
    }

    /**
     * 테이블 컬럼 설정
     */
    private void setupColumns() {
        // 완료 체크박스 컬럼
        TableColumn<Task, Boolean> completedCol = new TableColumn<>("완료");
        completedCol.setCellValueFactory(new PropertyValueFactory<>("completed"));
        completedCol.setCellFactory(CheckBoxTableCell.forTableColumn(completedCol));
        completedCol.setEditable(true);
        completedCol.setPrefWidth(50);

        // 제목 컬럼
        TableColumn<Task, String> titleCol = new TableColumn<>("제목");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        // 마감일 컬럼
        TableColumn<Task, LocalDate> dueDateCol = new TableColumn<>("마감일");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setPrefWidth(120);

        // 우선순위 컬럼
        TableColumn<Task, Priority> priorityCol = new TableColumn<>("우선순위");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);

        // 우선순위 셀 스타일 적용
        priorityCol.setCellFactory(column -> new TableCell<Task, Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getDisplayName());

                    // 우선순위별 스타일 클래스 적용
                    getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
                    switch (item) {
                        case HIGH:
                            getStyleClass().add("priority-high");
                            break;
                        case MEDIUM:
                            getStyleClass().add("priority-medium");
                            break;
                        case LOW:
                            getStyleClass().add("priority-low");
                            break;
                    }
                }
            }
        });

        // 태그 컬럼
        TableColumn<Task, String> tagsCol = new TableColumn<>("태그");
        tagsCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTagsAsString()));
        tagsCol.setPrefWidth(180);

        // 컬럼 추가
        getColumns().addAll(completedCol, titleCol, dueDateCol, priorityCol, tagsCol);

        // 테이블 편집 가능 설정
        setEditable(true);

        // 플레이스홀더 설정
        setPlaceholder(new Label("등록된 과제가 없습니다."));
    }

    /**
     * 우클릭 컨텍스트 메뉴 설정
     */
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("수정");
        MenuItem deleteItem = new MenuItem("삭제");

        contextMenu.getItems().addAll(editItem, deleteItem);

        setContextMenu(contextMenu);

        // 메뉴 동작은 MainController에서 설정
    }

    /**
     * 선택된 과제 반환
     */
    public Task getSelectedTask() {
        return getSelectionModel().getSelectedItem();
    }
}
