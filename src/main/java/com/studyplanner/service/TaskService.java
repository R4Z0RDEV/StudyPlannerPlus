package com.studyplanner.service;

import com.studyplanner.model.Priority;
import com.studyplanner.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 과제 관리 서비스
 * CRUD, 검증, 정렬, 필터 기능 제공
 */
public class TaskService {
    private final ObservableList<Task> tasks;
    private final FilteredList<Task> filteredTasks;
    private final SortedList<Task> sortedTasks;
    private final StorageService storageService;

    public TaskService(StorageService storageService) {
        this.storageService = storageService;
        this.tasks = FXCollections.observableArrayList();
        this.filteredTasks = new FilteredList<>(tasks, p -> true);
        this.sortedTasks = new SortedList<>(filteredTasks);

        // 저장소에서 과제 로드
        loadTasks();
    }

    /**
     * 저장소에서 과제 로드
     */
    public void loadTasks() {
        List<Task> loadedTasks = storageService.loadTasks();
        tasks.setAll(loadedTasks);
    }

    /**
     * 과제 저장
     */
    public void saveTasks() {
        storageService.saveTasks(tasks);
    }

    /**
     * 과제 추가
     */
    public boolean addTask(Task task) {
        // 유효성 검증
        if (!task.isValid()) {
            return false;
        }

        // 과거 마감일 경고 (오늘은 허용)
        if (task.getDueDate().isBefore(LocalDate.now())) {
            System.out.println("경고: 마감일이 과거입니다.");
        }

        tasks.add(task);
        saveTasks();
        return true;
    }

    /**
     * 과제 수정
     */
    public boolean updateTask(Task task) {
        if (!task.isValid()) {
            return false;
        }

        Optional<Task> existing = findTaskById(task.getId());
        if (existing.isPresent()) {
            Task existingTask = existing.get();
            existingTask.setTitle(task.getTitle());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setPriority(task.getPriority());
            existingTask.setNotes(task.getNotes());
            existingTask.setTags(task.getTags());
            existingTask.setCompleted(task.isCompleted());
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * 과제 삭제
     */
    public boolean deleteTask(Task task) {
        boolean removed = tasks.remove(task);
        if (removed) {
            saveTasks();
        }
        return removed;
    }

    /**
     * ID로 과제 찾기
     */
    public Optional<Task> findTaskById(String id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    /**
     * 전체 과제 목록 반환 (ObservableList)
     */
    public ObservableList<Task> getTasks() {
        return tasks;
    }

    /**
     * 필터링된 과제 목록 반환
     */
    public FilteredList<Task> getFilteredTasks() {
        return filteredTasks;
    }

    /**
     * 정렬된 과제 목록 반환
     */
    public SortedList<Task> getSortedTasks() {
        return sortedTasks;
    }

    /**
     * 필터 설정
     */
    public void setFilter(Predicate<Task> predicate) {
        filteredTasks.setPredicate(predicate);
    }

    /**
     * 정렬 기준 설정
     */
    public void setComparator(Comparator<Task> comparator) {
        sortedTasks.setComparator(comparator);
    }

    /**
     * 오늘 마감 과제 필터
     */
    public void filterToday() {
        setFilter(Task::isDueToday);
    }

    /**
     * 이번주 마감 과제 필터
     */
    public void filterThisWeek() {
        setFilter(Task::isDueThisWeek);
    }

    /**
     * 완료되지 않은 과제 필터
     */
    public void filterIncomplete() {
        setFilter(task -> !task.isCompleted());
    }

    /**
     * 전체 과제 표시 (필터 제거)
     */
    public void showAll() {
        setFilter(p -> true);
    }

    /**
     * 제목 또는 태그로 검색
     */
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            showAll();
            return;
        }

        String lowerQuery = query.toLowerCase();
        setFilter(task -> {
            // 제목 검색
            if (task.getTitle().toLowerCase().contains(lowerQuery)) {
                return true;
            }
            // 태그 검색
            for (String tag : task.getTags()) {
                if (tag.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * 특정 날짜의 과제 필터
     */
    public void filterByDate(LocalDate date) {
        setFilter(task -> task.getDueDate().equals(date));
    }

    /**
     * 우선순위 오름차순 정렬
     */
    public void sortByPriorityAsc() {
        setComparator(Comparator.comparing(Task::getPriority));
    }

    /**
     * 우선순위 내림차순 정렬
     */
    public void sortByPriorityDesc() {
        setComparator(Comparator.comparing(Task::getPriority).reversed());
    }

    /**
     * 마감일 오름차순 정렬
     */
    public void sortByDueDateAsc() {
        setComparator(Comparator.comparing(Task::getDueDate));
    }

    /**
     * 마감일 내림차순 정렬
     */
    public void sortByDueDateDesc() {
        setComparator(Comparator.comparing(Task::getDueDate).reversed());
    }

    /**
     * 통계: 전체 과제 수
     */
    public int getTotalTaskCount() {
        return tasks.size();
    }

    /**
     * 통계: 완료된 과제 수
     */
    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }

    /**
     * 통계: 완료율 (0.0 ~ 1.0)
     */
    public double getCompletionRate() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        return (double) getCompletedTaskCount() / getTotalTaskCount();
    }

    /**
     * 통계: 오늘 마감 과제 수
     */
    public int getTodayTaskCount() {
        return (int) tasks.stream().filter(Task::isDueToday).count();
    }

    /**
     * 통계: 연체된 과제 수
     */
    public int getOverdueTaskCount() {
        return (int) tasks.stream().filter(Task::isOverdue).count();
    }
}
