package com.studyplanner.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 과제 데이터 모델
 * JavaFX Property를 사용하여 UI 바인딩 지원
 */
public class Task {
    private final StringProperty id;
    private final StringProperty title;
    private final ObjectProperty<LocalDate> dueDate;
    private final ObjectProperty<Priority> priority;
    private final StringProperty notes;
    private final List<String> tags;
    private final BooleanProperty completed;

    /**
     * 새 과제 생성 (UUID 자동 생성)
     */
    public Task(String title, LocalDate dueDate, Priority priority) {
        this(UUID.randomUUID().toString(), title, dueDate, priority, "", new ArrayList<>(), false);
    }

    /**
     * 완전한 과제 생성 (저장소에서 로드할 때 사용)
     */
    public Task(String id, String title, LocalDate dueDate, Priority priority,
            String notes, List<String> tags, boolean completed) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.priority = new SimpleObjectProperty<>(priority);
        this.notes = new SimpleStringProperty(notes);
        this.tags = new ArrayList<>(tags);
        this.completed = new SimpleBooleanProperty(completed);
    }

    // ID
    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    // Title
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String value) {
        title.set(value);
    }

    public StringProperty titleProperty() {
        return title;
    }

    // Due Date
    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(LocalDate value) {
        dueDate.set(value);
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDate;
    }

    // Priority
    public Priority getPriority() {
        return priority.get();
    }

    public void setPriority(Priority value) {
        priority.set(value);
    }

    public ObjectProperty<Priority> priorityProperty() {
        return priority;
    }

    // Notes
    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String value) {
        notes.set(value);
    }

    public StringProperty notesProperty() {
        return notes;
    }

    // Tags
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void setTags(List<String> value) {
        tags.clear();
        tags.addAll(value);
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    /**
     * 태그를 쉼표로 구분된 문자열로 반환
     */
    public String getTagsAsString() {
        return String.join(", ", tags);
    }

    /**
     * 쉼표로 구분된 문자열에서 태그 파싱
     */
    public void setTagsFromString(String tagsString) {
        tags.clear();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            String[] tagArray = tagsString.split(",");
            for (String tag : tagArray) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
        }
    }

    // Completed
    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean value) {
        completed.set(value);
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    /**
     * 과제 유효성 검증
     */
    public boolean isValid() {
        return title.get() != null && !title.get().trim().isEmpty()
                && dueDate.get() != null
                && priority.get() != null;
    }

    /**
     * 마감일이 오늘인지 확인
     */
    public boolean isDueToday() {
        return dueDate.get() != null && dueDate.get().equals(LocalDate.now());
    }

    /**
     * 마감일이 과거인지 확인
     */
    public boolean isOverdue() {
        return dueDate.get() != null && dueDate.get().isBefore(LocalDate.now()) && !isCompleted();
    }

    /**
     * 마감일이 이번 주인지 확인
     */
    public boolean isDueThisWeek() {
        if (dueDate.get() == null)
            return false;
        LocalDate now = LocalDate.now();
        LocalDate weekEnd = now.plusDays(7);
        return !dueDate.get().isBefore(now) && !dueDate.get().isAfter(weekEnd);
    }

    @Override
    public String toString() {
        return String.format("Task[%s: %s, due=%s, priority=%s, completed=%s]",
                id.get(), title.get(), dueDate.get(), priority.get(), completed.get());
    }
}
