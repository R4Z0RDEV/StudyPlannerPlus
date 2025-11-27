package com.studyplanner;

import com.studyplanner.model.Priority;
import com.studyplanner.model.Task;
import com.studyplanner.service.StorageService;
import com.studyplanner.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TaskService 단위 테스트
 */
class TaskServiceTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        StorageService storageService = new StorageService();
        taskService = new TaskService(storageService);
        taskService.getTasks().clear(); // 테스트 시작 전 초기화
    }

    @Test
    void testAddTask_Success() {
        Task task = new Task("테스트 과제", LocalDate.now(), Priority.HIGH);

        boolean result = taskService.addTask(task);

        assertTrue(result, "과제 추가 성공");
        assertEquals(1, taskService.getTotalTaskCount(), "전체 과제 수 1개");
    }

    @Test
    void testAddTask_EmptyTitle_Fails() {
        Task task = new Task("", LocalDate.now(), Priority.MEDIUM);

        boolean result = taskService.addTask(task);

        assertFalse(result, "제목이 비어있으면 추가 실패");
        assertEquals(0, taskService.getTotalTaskCount(), "과제가 추가되지 않음");
    }

    @Test
    void testDeleteTask_Success() {
        Task task = new Task("삭제 테스트", LocalDate.now(), Priority.LOW);
        taskService.addTask(task);

        boolean result = taskService.deleteTask(task);

        assertTrue(result, "과제 삭제 성공");
        assertEquals(0, taskService.getTotalTaskCount(), "전체 과제 수 0개");
    }

    @Test
    void testUpdateTask_Success() {
        Task task = new Task("원본 제목", LocalDate.now(), Priority.MEDIUM);
        taskService.addTask(task);

        task.setTitle("수정된 제목");
        task.setPriority(Priority.HIGH);
        boolean result = taskService.updateTask(task);

        assertTrue(result, "과제 수정 성공");
        assertEquals("수정된 제목", task.getTitle(), "제목이 수정됨");
        assertEquals(Priority.HIGH, task.getPriority(), "우선순위가 수정됨");
    }

    @Test
    void testFilterToday() {
        Task todayTask = new Task("오늘 과제", LocalDate.now(), Priority.HIGH);
        Task tomorrowTask = new Task("내일 과제", LocalDate.now().plusDays(1), Priority.MEDIUM);

        taskService.addTask(todayTask);
        taskService.addTask(tomorrowTask);
        taskService.filterToday();

        assertEquals(1, taskService.getFilteredTasks().size(), "오늘 과제만 필터링됨");
        assertTrue(taskService.getFilteredTasks().contains(todayTask), "오늘 과제가 포함됨");
    }

    @Test
    void testFilterThisWeek() {
        Task thisWeekTask = new Task("이번주 과제", LocalDate.now().plusDays(3), Priority.HIGH);
        Task nextWeekTask = new Task("다음주 과제", LocalDate.now().plusDays(10), Priority.MEDIUM);

        taskService.addTask(thisWeekTask);
        taskService.addTask(nextWeekTask);
        taskService.filterThisWeek();

        assertEquals(1, taskService.getFilteredTasks().size(), "이번주 과제만 필터링됨");
        assertTrue(taskService.getFilteredTasks().contains(thisWeekTask), "이번주 과제가 포함됨");
    }

    @Test
    void testSearch_ByTitle() {
        Task task1 = new Task("자바 과제", LocalDate.now(), Priority.HIGH);
        Task task2 = new Task("수학 숙제", LocalDate.now(), Priority.MEDIUM);

        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.search("자바");

        assertEquals(1, taskService.getFilteredTasks().size(), "검색 결과 1개");
        assertTrue(taskService.getFilteredTasks().contains(task1), "'자바'가 포함된 과제만 필터링");
    }

    @Test
    void testSearch_ByTag() {
        Task task = new Task("프로그래밍 과제", LocalDate.now(), Priority.HIGH);
        task.setTagsFromString("자바, 프로그래밍");

        taskService.addTask(task);
        taskService.search("프로그래밍");

        assertEquals(1, taskService.getFilteredTasks().size(), "태그로 검색 성공");
        assertTrue(taskService.getFilteredTasks().contains(task), "'프로그래밍' 태그가 있는 과제");
    }

    @Test
    void testSortByPriorityDesc() {
        Task lowTask = new Task("낮음", LocalDate.now(), Priority.LOW);
        Task highTask = new Task("높음", LocalDate.now(), Priority.HIGH);
        Task mediumTask = new Task("보통", LocalDate.now(), Priority.MEDIUM);

        taskService.addTask(lowTask);
        taskService.addTask(highTask);
        taskService.addTask(mediumTask);
        taskService.sortByPriorityDesc();

        assertEquals(Priority.HIGH, taskService.getSortedTasks().get(0).getPriority(),
                "우선순위 내림차순 정렬: 첫 번째는 HIGH");
        assertEquals(Priority.LOW, taskService.getSortedTasks().get(2).getPriority(),
                "우선순위 내림차순 정렬: 마지막은 LOW");
    }

    @Test
    void testSortByDueDateAsc() {
        Task farTask = new Task("먼 과제", LocalDate.now().plusDays(10), Priority.MEDIUM);
        Task nearTask = new Task("가까운 과제", LocalDate.now().plusDays(1), Priority.MEDIUM);

        taskService.addTask(farTask);
        taskService.addTask(nearTask);
        taskService.sortByDueDateAsc();

        assertEquals(nearTask, taskService.getSortedTasks().get(0),
                "마감일 오름차순 정렬: 가까운 과제가 먼저");
    }

    @Test
    void testCompletionRate() {
        Task task1 = new Task("과제1", LocalDate.now(), Priority.MEDIUM);
        Task task2 = new Task("과제2", LocalDate.now(), Priority.MEDIUM);
        task1.setCompleted(true);

        taskService.addTask(task1);
        taskService.addTask(task2);

        assertEquals(0.5, taskService.getCompletionRate(), 0.01, "완료율 50%");
    }

    @Test
    void testGetOverdueTaskCount() {
        Task overdueTask = new Task("연체 과제", LocalDate.now().minusDays(1), Priority.HIGH);
        Task currentTask = new Task("현재 과제", LocalDate.now(), Priority.MEDIUM);

        taskService.addTask(overdueTask);
        taskService.addTask(currentTask);

        assertEquals(1, taskService.getOverdueTaskCount(), "연체 과제 1개");
    }

    @Test
    void testGetCompletedTaskCount() {
        Task completedTask = new Task("완료 과제", LocalDate.now(), Priority.MEDIUM);
        completedTask.setCompleted(true);
        Task incompleteTask = new Task("미완료 과제", LocalDate.now(), Priority.MEDIUM);

        taskService.addTask(completedTask);
        taskService.addTask(incompleteTask);

        assertEquals(1, taskService.getCompletedTaskCount(), "완료 과제 1개");
    }
}
