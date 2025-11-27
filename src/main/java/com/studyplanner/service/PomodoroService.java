package com.studyplanner.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.util.Duration;

/**
 * 포모도로 타이머 서비스
 * 25분 카운트다운 타이머 with 상태 관리
 */
public class PomodoroService {

    /**
     * 타이머 상태
     */
    public enum State {
        READY("준비"),
        RUNNING("실행중"),
        PAUSED("일시정지"),
        FINISHED("완료");

        private final String displayName;

        State(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static final int DEFAULT_DURATION = 25 * 60; // 25분 (초 단위)

    private final IntegerProperty remainingSeconds;
    private final ObjectProperty<State> state;
    private final IntegerProperty sessionCount;
    private Timeline timeline;
    private Runnable onFinishCallback;

    public PomodoroService() {
        this.remainingSeconds = new SimpleIntegerProperty(DEFAULT_DURATION);
        this.state = new SimpleObjectProperty<>(State.READY);
        this.sessionCount = new SimpleIntegerProperty(0);
    }

    /**
     * 타이머 시작
     */
    public void start() {
        if (state.get() == State.READY) {
            remainingSeconds.set(DEFAULT_DURATION);
        }

        if (state.get() == State.READY || state.get() == State.PAUSED) {
            state.set(State.RUNNING);
            startTimeline();
        }
    }

    /**
     * 타이머 일시정지
     */
    public void pause() {
        if (state.get() == State.RUNNING) {
            state.set(State.PAUSED);
            stopTimeline();
        }
    }

    /**
     * 타이머 리셋
     */
    public void reset() {
        stopTimeline();
        remainingSeconds.set(DEFAULT_DURATION);
        state.set(State.READY);
    }

    /**
     * Timeline 시작
     */
    private void startTimeline() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int current = remainingSeconds.get();

            if (current > 0) {
                remainingSeconds.set(current - 1);
            } else {
                // 타이머 완료
                finish();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Timeline 정지
     */
    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * 타이머 완료 처리
     */
    private void finish() {
        stopTimeline();
        state.set(State.FINISHED);
        sessionCount.set(sessionCount.get() + 1);

        // 완료 콜백 실행
        if (onFinishCallback != null) {
            onFinishCallback.run();
        }
    }

    /**
     * 완료 콜백 설정
     */
    public void setOnFinish(Runnable callback) {
        this.onFinishCallback = callback;
    }

    // Property getters

    public int getRemainingSeconds() {
        return remainingSeconds.get();
    }

    public IntegerProperty remainingSecondsProperty() {
        return remainingSeconds;
    }

    public State getState() {
        return state.get();
    }

    public ObjectProperty<State> stateProperty() {
        return state;
    }

    public int getSessionCount() {
        return sessionCount.get();
    }

    public IntegerProperty sessionCountProperty() {
        return sessionCount;
    }

    /**
     * 진행률 계산 (0.0 ~ 1.0)
     */
    public double getProgress() {
        return 1.0 - ((double) remainingSeconds.get() / DEFAULT_DURATION);
    }

    /**
     * 남은 시간을 분으로 반환
     */
    public int getRemainingMinutes() {
        return remainingSeconds.get() / 60;
    }

    /**
     * 남은 시간의 초 부분 반환
     */
    public int getRemainingSecondsInMinute() {
        return remainingSeconds.get() % 60;
    }
}
