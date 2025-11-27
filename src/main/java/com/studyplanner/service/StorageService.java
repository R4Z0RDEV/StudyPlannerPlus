package com.studyplanner.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.studyplanner.model.Priority;
import com.studyplanner.model.Task;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 기반 데이터 저장소 서비스
 */
public class StorageService {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/tasks.json";
    private static final String BACKUP_FILE = DATA_DIR + "/tasks.json.bak";

    private final Gson gson;

    public StorageService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        // 데이터 디렉토리 생성
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * 과제 목록 로드
     */
    public List<Task> loadTasks() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            System.out.println("데이터 파일이 없습니다. 샘플 데이터를 생성합니다.");
            return createSampleData();
        }

        try (Reader reader = new FileReader(file)) {
            Type taskListType = new TypeToken<List<TaskData>>() {
            }.getType();
            List<TaskData> taskDataList = gson.fromJson(reader, taskListType);

            if (taskDataList == null) {
                return new ArrayList<>();
            }

            // TaskData를 Task로 변환
            List<Task> tasks = new ArrayList<>();
            for (TaskData data : taskDataList) {
                Task task = new Task(
                        data.id,
                        data.title,
                        data.dueDate,
                        data.priority,
                        data.notes,
                        data.tags,
                        data.completed);
                tasks.add(task);
            }

            System.out.println(tasks.size() + "개의 과제를 로드했습니다.");
            return tasks;

        } catch (IOException e) {
            System.err.println("과제 로드 실패: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 과제 목록 저장
     */
    public void saveTasks(List<Task> tasks) {
        try {
            // 기존 파일 백업
            File file = new File(DATA_FILE);
            if (file.exists()) {
                Files.copy(file.toPath(), new File(BACKUP_FILE).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            // Task를 TaskData로 변환
            List<TaskData> taskDataList = new ArrayList<>();
            for (Task task : tasks) {
                TaskData data = new TaskData();
                data.id = task.getId();
                data.title = task.getTitle();
                data.dueDate = task.getDueDate();
                data.priority = task.getPriority();
                data.notes = task.getNotes();
                data.tags = task.getTags();
                data.completed = task.isCompleted();
                taskDataList.add(data);
            }

            // JSON으로 저장
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(taskDataList, writer);
            }

            System.out.println(tasks.size() + "개의 과제를 저장했습니다.");

        } catch (IOException e) {
            System.err.println("과제 저장 실패: " + e.getMessage());
            throw new RuntimeException("과제를 저장할 수 없습니다.", e);
        }
    }

    /**
     * 샘플 데이터 생성
     */
    private List<Task> createSampleData() {
        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task("자바 과제 제출", LocalDate.now(), Priority.HIGH);
        task1.setNotes("Chapter 5-7 연습문제 풀이");
        task1.setTagsFromString("프로그래밍, 과제");
        tasks.add(task1);

        Task task2 = new Task("수학 시험 공부", LocalDate.now().plusDays(3), Priority.MEDIUM);
        task2.setNotes("2차 방정식 복습");
        task2.setTagsFromString("수학, 시험");
        tasks.add(task2);

        Task task3 = new Task("영어 단어 암기", LocalDate.now().plusDays(7), Priority.LOW);
        task3.setNotes("Unit 10 단어 50개");
        task3.setTagsFromString("영어, 암기");
        tasks.add(task3);

        Task task4 = new Task("프로젝트 기획서 작성", LocalDate.now().plusDays(5), Priority.HIGH);
        task4.setNotes("팀 프로젝트 기획안 초안");
        task4.setTagsFromString("프로젝트, 팀플");
        tasks.add(task4);

        // 샘플 데이터 저장
        saveTasks(tasks);

        return tasks;
    }

    /**
     * JSON 직렬화를 위한 데이터 클래스
     */
    private static class TaskData {
        String id;
        String title;
        LocalDate dueDate;
        Priority priority;
        String notes;
        List<String> tags;
        boolean completed;
    }

    /**
     * LocalDate JSON 어댑터
     */
    private static class LocalDateAdapter extends com.google.gson.TypeAdapter<LocalDate> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDate read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
}
