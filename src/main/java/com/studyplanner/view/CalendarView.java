package com.studyplanner.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 월간 캘린더 뷰 컴포넌트
 */
public class CalendarView extends VBox {

    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthLabel;
    private Consumer<LocalDate> onDateClickCallback;
    private List<LocalDate> highlightedDates;

    public CalendarView() {
        super(10);
        this.currentYearMonth = YearMonth.now();
        this.highlightedDates = new ArrayList<>();

        setupUI();
        updateCalendar();
    }

    /**
     * UI 초기화
     */
    private void setupUI() {
        // 상단 헤더 (이전/다음 달 네비게이션)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);

        Button prevButton = new Button("◀");
        prevButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        monthLabel = new Label();
        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        monthLabel.setMinWidth(150);
        monthLabel.setAlignment(Pos.CENTER);

        Button nextButton = new Button("▶");
        nextButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        header.getChildren().addAll(prevButton, monthLabel, nextButton);

        // 요일 헤더
        GridPane weekdayHeader = new GridPane();
        weekdayHeader.setHgap(5);
        String[] weekdays = { "일", "월", "화", "수", "목", "금", "토" };
        for (int i = 0; i < weekdays.length; i++) {
            Label label = new Label(weekdays[i]);
            label.setFont(Font.font("System", FontWeight.BOLD, 14));
            label.setMinWidth(80);
            label.setAlignment(Pos.CENTER);

            if (i == 0) {
                label.getStyleClass().add("calendar-sunday");
            } else if (i == 6) {
                label.getStyleClass().add("calendar-saturday");
            }

            weekdayHeader.add(label, i, 0);
        }

        // 캘린더 그리드
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);

        getChildren().addAll(header, weekdayHeader, calendarGrid);
        setPadding(new Insets(10));
    }

    /**
     * 캘린더 업데이트
     */
    private void updateCalendar() {
        // 월 레이블 업데이트
        monthLabel.setText(String.format("%d년 %d월",
                currentYearMonth.getYear(), currentYearMonth.getMonthValue()));

        // 그리드 클리어
        calendarGrid.getChildren().clear();

        // 해당 월의 1일
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // 1일이 시작하는 요일 (일요일=0, 월요일=1, ...)
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        // 캘린더 그리드 채우기
        int dayCounter = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if ((row == 0 && col < startDayOfWeek) || dayCounter > daysInMonth) {
                    // 빈 셀
                    Label emptyLabel = new Label("");
                    emptyLabel.setMinSize(80, 60);
                    calendarGrid.add(emptyLabel, col, row);
                } else {
                    // 날짜 버튼
                    int day = dayCounter;
                    LocalDate date = currentYearMonth.atDay(day);

                    Button dayButton = new Button(String.valueOf(day));
                    dayButton.setMinSize(80, 60);
                    dayButton.getStyleClass().add("calendar-day");

                    // 오늘 날짜 강조
                    if (date.equals(LocalDate.now())) {
                        dayButton.getStyleClass().add("calendar-today");
                    }

                    // 과제가 있는 날짜 표시
                    if (highlightedDates.contains(date)) {
                        dayButton.getStyleClass().add("calendar-has-task");
                    }

                    // 주말 스타일
                    if (col == 0) {
                        dayButton.getStyleClass().add("calendar-sunday");
                    } else if (col == 6) {
                        dayButton.getStyleClass().add("calendar-saturday");
                    }

                    // 클릭 이벤트
                    dayButton.setOnAction(e -> {
                        if (onDateClickCallback != null) {
                            onDateClickCallback.accept(date);
                        }
                    });

                    calendarGrid.add(dayButton, col, row);
                    dayCounter++;
                }
            }

            if (dayCounter > daysInMonth) {
                break;
            }
        }
    }

    /**
     * 날짜 클릭 콜백 설정
     */
    public void setOnDateClick(Consumer<LocalDate> callback) {
        this.onDateClickCallback = callback;
    }

    /**
     * 과제가 있는 날짜 목록 설정
     */
    public void setHighlightedDates(List<LocalDate> dates) {
        this.highlightedDates = new ArrayList<>(dates);
        updateCalendar();
    }

    /**
     * 현재 표시 중인 년월 반환
     */
    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }

    /**
     * 특정 년월로 이동
     */
    public void setYearMonth(YearMonth yearMonth) {
        this.currentYearMonth = yearMonth;
        updateCalendar();
    }
}
