package com.studyplanner.controller;

import com.studyplanner.AppConfig;
import com.studyplanner.model.Task;
import com.studyplanner.service.PomodoroService;
import com.studyplanner.service.StorageService;
import com.studyplanner.service.TaskService;
import com.studyplanner.util.FxUtil;
import com.studyplanner.util.TimeUtil;
import com.studyplanner.view.CalendarView;
import com.studyplanner.view.TaskTableView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 메인 컨트롤러
 * UI 레이아웃 구성 및 이벤트 처리
 */
public class MainController {

    private final Stage primaryStage;
    private final Scene scene;

    // 서비스
    private final TaskService taskService;
    private final PomodoroService pomodoroService;

    // UI 컴포넌트
    private BorderPane root;
    private TextField searchField;
    private TaskTableView taskTableView;
    private CalendarView calendarView;
    private TabPane tabPane;

    // 포모도로 UI
    private Label timerLabel;
    private ProgressBar timerProgress;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Label sessionCountLabel;

    // 통계 UI
    private Label totalTasksLabel;
    private Label completedTasksLabel;
    private Label todayTasksLabel;
    private Label overdueTasksLabel;
    private ProgressBar completionProgress;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 서비스 초기화
        StorageService storageService = new StorageService();
        this.taskService = new TaskService(storageService);
        this.pomodoroService = new PomodoroService();

        // UI 구성
        this.root = new BorderPane();
        this.scene = new Scene(root, AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);

        setupUI();
        setupEventHandlers();

        // 스타일시트 로드
        String css = getClass().getResource(AppConfig.CSS_FILE).toExternalForm();
        scene.getStylesheets().add(css);
    }

    /**
     * UI 초기화
     */
    private void setupUI() {
        // 상단 툴바
        root.setTop(createToolBar());

        // 좌측 탭 패널
        root.setLeft(createTabPane());

        // 중앙 컨텐츠 영역 (초기에는 오늘 과제)
        root.setCenter(createTodayView());

        // 우측 포모도로 타이머
        root.setRight(createPomodoroPanel());
    }

    /**
     * 상단 툴바 생성
     */
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();

        // 검색 필드
        searchField = new TextField();
        searchField.setPromptText("과제 검색 (제목, 태그)");
        searchField.setPrefWidth(250);

        // 과제 추가 버튼
        Button addButton = new Button("+ 과제 추가");
        addButton.setOnAction(e -> handleAddTask());

        // 프로필 메뉴 (더미)
        MenuButton profileMenu = new MenuButton("프로필");
        MenuItem aboutItem = new MenuItem("정보");
        aboutItem.setOnAction(e -> FxUtil.showInfo("StudyPlanner+",
                "버전: " + AppConfig.APP_VERSION + "\n학습 일정/과제 관리 애플리케이션"));
        MenuItem exitItem = new MenuItem("종료");
        exitItem.setOnAction(e -> Platform.exit());
        profileMenu.getItems().addAll(aboutItem, new SeparatorMenuItem(), exitItem);

        // 공간 분할
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolBar.getItems().addAll(searchField, addButton, spacer, profileMenu);

        return toolBar;
    }

    /**
     * 좌측 탭 패널 생성
     */
    private TabPane createTabPane() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefWidth(200);

        // 탭 생성
        Tab todayTab = new Tab("오늘");
        todayTab.setContent(new Label("")); // 컨텐츠는 중앙에 표시

        Tab allTab = new Tab("할일");
        allTab.setContent(new Label(""));

        Tab calendarTab = new Tab("캘린더");
        calendarTab.setContent(new Label(""));

        Tab statsTab = new Tab("통계");
        statsTab.setContent(new Label(""));

        Tab settingsTab = new Tab("설정");
        settingsTab.setContent(new Label(""));

        tabPane.getTabs().addAll(todayTab, allTab, calendarTab, statsTab, settingsTab);

        // 탭 선택 리스너
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            handleTabChange(newTab.getText());
        });

        return tabPane;
    }

    /**
     * 오늘 과제 뷰 생성
     */
    private VBox createTodayView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label header = new Label("오늘 마감 과제");
        header.setFont(Font.font("System", FontWeight.BOLD, 24));

        taskTableView = new TaskTableView();
        taskTableView.setItems(taskService.getSortedTasks());

        // 오늘 필터 적용
        taskService.filterToday();

        vbox.getChildren().addAll(header, taskTableView);
        VBox.setVgrow(taskTableView, Priority.ALWAYS);

        return vbox;
    }

    /**
     * 전체 과제 뷰 생성
     */
    private VBox createAllTasksView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label header = new Label("전체 과제 목록");
        header.setFont(Font.font("System", FontWeight.BOLD, 24));

        taskTableView = new TaskTableView();
        taskTableView.setItems(taskService.getSortedTasks());

        // 필터 제거 (전체 표시)
        taskService.showAll();

        // 정렬: 마감일 오름차순
        taskService.sortByDueDateAsc();

        vbox.getChildren().addAll(header, taskTableView);
        VBox.setVgrow(taskTableView, Priority.ALWAYS);

        return vbox;
    }

    /**
     * 캘린더 뷰 생성
     */
    private VBox createCalendarViewPane() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label header = new Label("캘린더");
        header.setFont(Font.font("System", FontWeight.BOLD, 24));

        calendarView = new CalendarView();

        // 과제가 있는 날짜 하이라이트
        List<LocalDate> taskDates = taskService.getTasks().stream()
                .map(Task::getDueDate)
                .distinct()
                .collect(Collectors.toList());
        calendarView.setHighlightedDates(taskDates);

        // 날짜 클릭 시 해당 날짜 과제 필터
        calendarView.setOnDateClick(date -> {
            taskService.filterByDate(date);
            FxUtil.showInfo("캘린더", date + "의 과제를 표시합니다.");
        });

        taskTableView = new TaskTableView();
        taskTableView.setItems(taskService.getSortedTasks());

        vbox.getChildren().addAll(header, calendarView, taskTableView);
        VBox.setVgrow(taskTableView, Priority.ALWAYS);

        return vbox;
    }

    /**
     * 통계 뷰 생성
     */
    private VBox createStatsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label header = new Label("학습 통계");
        header.setFont(Font.font("System", FontWeight.BOLD, 24));

        // 통계 그리드
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10));

        // 전체 과제
        totalTasksLabel = new Label("0");
        totalTasksLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        VBox totalBox = createStatBox("전체 과제", totalTasksLabel);

        // 완료 과제
        completedTasksLabel = new Label("0");
        completedTasksLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        VBox completedBox = createStatBox("완료 과제", completedTasksLabel);

        // 오늘 과제
        todayTasksLabel = new Label("0");
        todayTasksLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        VBox todayBox = createStatBox("오늘 마감", todayTasksLabel);

        // 연체 과제
        overdueTasksLabel = new Label("0");
        overdueTasksLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        VBox overdueBox = createStatBox("연체 과제", overdueTasksLabel);

        statsGrid.add(totalBox, 0, 0);
        statsGrid.add(completedBox, 1, 0);
        statsGrid.add(todayBox, 0, 1);
        statsGrid.add(overdueBox, 1, 1);

        // 완료율 진행바
        Label progressLabel = new Label("완료율");
        progressLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        completionProgress = new ProgressBar(0);
        completionProgress.setPrefWidth(400);
        completionProgress.setPrefHeight(30);

        Label percentLabel = new Label("0%");
        percentLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // 포모도로 세션 수
        Label sessionLabel = new Label("포모도로 세션: " + pomodoroService.getSessionCount());
        sessionLabel.setFont(Font.font("System", 14));

        // 통계 업데이트
        updateStatistics();

        vbox.getChildren().addAll(header, statsGrid, progressLabel, completionProgress, percentLabel, sessionLabel);

        // 완료율 바인딩
        completionProgress.progressProperty().addListener((obs, oldVal, newVal) -> {
            percentLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });

        return vbox;
    }

    /**
     * 통계 박스 생성 헬퍼
     */
    private VBox createStatBox(String title, Label valueLabel) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("stat-box");
        box.setMinSize(150, 100);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", 14));

        box.getChildren().addAll(titleLabel, valueLabel);

        return box;
    }

    /**
     * 통계 업데이트
     */
    private void updateStatistics() {
        totalTasksLabel.setText(String.valueOf(taskService.getTotalTaskCount()));
        completedTasksLabel.setText(String.valueOf(taskService.getCompletedTaskCount()));
        todayTasksLabel.setText(String.valueOf(taskService.getTodayTaskCount()));
        overdueTasksLabel.setText(String.valueOf(taskService.getOverdueTaskCount()));
        completionProgress.setProgress(taskService.getCompletionRate());
    }

    /**
     * 설정 뷰 생성
     */
    private VBox createSettingsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label header = new Label("설정");
        header.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label info = new Label("StudyPlanner+ v" + AppConfig.APP_VERSION);
        info.setFont(Font.font("System", 16));

        Button clearDataButton = new Button("모든 데이터 삭제");
        clearDataButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("확인");
            confirm.setHeaderText("모든 과제를 삭제하시겠습니까?");
            confirm.setContentText("이 작업은 되돌릴 수 없습니다.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                taskService.getTasks().clear();
                taskService.saveTasks();
                FxUtil.showInfo("완료", "모든 과제가 삭제되었습니다.");
            }
        });

        vbox.getChildren().addAll(header, info, clearDataButton);

        return vbox;
    }

    /**
     * 포모도로 타이머 패널 생성
     */
    private VBox createPomodoroPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setPrefWidth(250);
        vbox.getStyleClass().add("pomodoro-panel");

        Label header = new Label("포모도로 타이머");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        // 타이머 표시
        timerLabel = new Label("25:00");
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        timerLabel.setAlignment(Pos.CENTER);
        timerLabel.setMaxWidth(Double.MAX_VALUE);

        // 진행바
        timerProgress = new ProgressBar(0);
        timerProgress.setPrefWidth(220);
        timerProgress.setPrefHeight(20);

        // 컨트롤 버튼
        startButton = new Button("시작");
        pauseButton = new Button("일시정지");
        resetButton = new Button("리셋");

        pauseButton.setDisable(true);

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, pauseButton, resetButton);

        // 세션 카운트
        sessionCountLabel = new Label("세션: 0");
        sessionCountLabel.setFont(Font.font("System", 14));

        vbox.getChildren().addAll(header, timerLabel, timerProgress, buttonBox, sessionCountLabel);

        return vbox;
    }

    /**
     * 이벤트 핸들러 설정
     */
    private void setupEventHandlers() {
        // 검색 필드
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            taskService.search(newVal);
        });

        // 테이블 더블클릭
        if (taskTableView != null) {
            taskTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Task selected = taskTableView.getSelectedTask();
                    if (selected != null) {
                        handleEditTask(selected);
                    }
                }
            });

            // 컨텍스트 메뉴
            ContextMenu contextMenu = taskTableView.getContextMenu();
            if (contextMenu != null) {
                contextMenu.getItems().get(0).setOnAction(e -> {
                    Task selected = taskTableView.getSelectedTask();
                    if (selected != null)
                        handleEditTask(selected);
                });
                contextMenu.getItems().get(1).setOnAction(e -> {
                    Task selected = taskTableView.getSelectedTask();
                    if (selected != null)
                        handleDeleteTask(selected);
                });
            }
        }

        // 포모도로 타이머
        setupPomodoroHandlers();

        // 과제 목록 변경 리스너 (캘린더 업데이트용)
        taskService.getTasks().addListener((javafx.collections.ListChangeListener.Change<? extends Task> c) -> {
            if (calendarView != null) {
                List<LocalDate> taskDates = taskService.getTasks().stream()
                        .map(Task::getDueDate)
                        .distinct()
                        .collect(Collectors.toList());
                calendarView.setHighlightedDates(taskDates);
            }
            updateStatistics();
        });
    }

    /**
     * 포모도로 타이머 핸들러 설정
     */
    private void setupPomodoroHandlers() {
        // 타이머 바인딩
        pomodoroService.remainingSecondsProperty().addListener((obs, oldVal, newVal) -> {
            timerLabel.setText(TimeUtil.formatTime(newVal.intValue()));
            timerProgress.setProgress(pomodoroService.getProgress());
        });

        // 상태 변경 리스너
        pomodoroService.stateProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal) {
                case READY:
                    startButton.setDisable(false);
                    pauseButton.setDisable(true);
                    resetButton.setDisable(false);
                    break;
                case RUNNING:
                    startButton.setDisable(true);
                    pauseButton.setDisable(false);
                    resetButton.setDisable(false);
                    break;
                case PAUSED:
                    startButton.setDisable(false);
                    startButton.setText("재개");
                    pauseButton.setDisable(true);
                    resetButton.setDisable(false);
                    break;
                case FINISHED:
                    startButton.setDisable(false);
                    startButton.setText("시작");
                    pauseButton.setDisable(true);
                    resetButton.setDisable(false);
                    break;
            }
        });

        // 세션 카운트 바인딩
        pomodoroService.sessionCountProperty().addListener((obs, oldVal, newVal) -> {
            sessionCountLabel.setText("세션: " + newVal);
        });

        // 완료 콜백
        pomodoroService.setOnFinish(() -> {
            Platform.runLater(() -> {
                FxUtil.showInfo("포모도로 완료!", "25분 집중 시간이 완료되었습니다.\n잠시 휴식하세요!");
                updateStatistics();
            });
        });

        // 버튼 이벤트
        startButton.setOnAction(e -> pomodoroService.start());
        pauseButton.setOnAction(e -> pomodoroService.pause());
        resetButton.setOnAction(e -> {
            pomodoroService.reset();
            startButton.setText("시작");
        });
    }

    /**
     * 탭 변경 처리
     */
    private void handleTabChange(String tabName) {
        switch (tabName) {
            case "오늘":
                root.setCenter(createTodayView());
                setupEventHandlers();
                break;
            case "할일":
                root.setCenter(createAllTasksView());
                setupEventHandlers();
                break;
            case "캘린더":
                root.setCenter(createCalendarViewPane());
                setupEventHandlers();
                break;
            case "통계":
                root.setCenter(createStatsView());
                break;
            case "설정":
                root.setCenter(createSettingsView());
                break;
        }
    }

    /**
     * 과제 추가 처리
     */
    private void handleAddTask() {
        Optional<Task> result = DialogController.showAddDialog(primaryStage);
        result.ifPresent(task -> {
            if (taskService.addTask(task)) {
                FxUtil.showInfo("완료", "과제가 추가되었습니다.");
                updateStatistics();
            } else {
                FxUtil.showError("오류", "과제 추가에 실패했습니다.");
            }
        });
    }

    /**
     * 과제 수정 처리
     */
    private void handleEditTask(Task task) {
        Optional<Task> result = DialogController.showEditDialog(primaryStage, task);
        result.ifPresent(editedTask -> {
            if (taskService.updateTask(editedTask)) {
                FxUtil.showInfo("완료", "과제가 수정되었습니다.");
                updateStatistics();
            } else {
                FxUtil.showError("오류", "과제 수정에 실패했습니다.");
            }
        });
    }

    /**
     * 과제 삭제 처리
     */
    private void handleDeleteTask(Task task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("삭제 확인");
        confirm.setHeaderText("'" + task.getTitle() + "' 과제를 삭제하시겠습니까?");
        confirm.setContentText("이 작업은 되돌릴 수 없습니다.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (taskService.deleteTask(task)) {
                FxUtil.showInfo("완료", "과제가 삭제되었습니다.");
                updateStatistics();
            } else {
                FxUtil.showError("오류", "과제 삭제에 실패했습니다.");
            }
        }
    }

    /**
     * Scene 반환
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * 자동 스크린샷 생성
     */
    public void captureScreenshots() {
        // 메인 화면 스크린샷
        Platform.runLater(() -> {
            WritableImage mainImage = scene.snapshot(null);
            FxUtil.saveSnapshot(mainImage, AppConfig.SCREENSHOT_MAIN);

            // 다이얼로그 스크린샷
            captureDialogScreenshot();

            // 타이머 스크린샷 (타이머 시작 후)
            pomodoroService.start();
            Platform.runLater(() -> {
                WritableImage timerImage = scene.snapshot(null);
                FxUtil.saveSnapshot(timerImage, AppConfig.SCREENSHOT_TIMER);
                pomodoroService.reset();
            });
        });
    }

    /**
     * 다이얼로그 스크린샷
     */
    private void captureDialogScreenshot() {
        // 다이얼로그 열기
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("과제 추가");
        dialog.setHeaderText("새로운 과제를 추가하세요.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 간단한 폼 추가
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField("샘플 과제");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("높음", "보통", "낮음");
        priorityCombo.setValue("보통");

        grid.add(new Label("제목:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("마감일:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("우선순위:"), 0, 2);
        grid.add(priorityCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 다이얼로그 표시 후 스크린샷
        dialog.show();
        Platform.runLater(() -> {
            WritableImage dialogImage = dialog.getDialogPane().getScene().snapshot(null);
            FxUtil.saveSnapshot(dialogImage, AppConfig.SCREENSHOT_DIALOG);
            dialog.close();
        });
    }

    /**
     * 종료 시 데이터 저장
     */
    public void onClose() {
        taskService.saveTasks();
    }
}
