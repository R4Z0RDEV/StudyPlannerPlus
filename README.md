# StudyPlanner+

학습 일정/과제 관리 + 포모도로 타이머 + 통계 기능을 제공하는 JavaFX 데스크톱 애플리케이션입니다.

## 주요 기능

- ✅ **과제 관리**: CRUD 작업으로 과제 추가, 수정, 삭제
- ✅ **마감일 관리**: 날짜별 과제 필터링 및 캘린더 뷰
- ✅ **우선순위 설정**: 높음/보통/낮음 우선순위 지정
- ✅ **태그 시스템**: 과제를 태그로 분류하고 검색
- ✅ **포모도로 타이머**: 25분 집중 타이머
- ✅ **학습 통계**: 완료율, 세션 수, 연체 과제 등 시각화
- ✅ **캘린더 뷰**: 월간 캘린더로 과제 일정 확인

## 기술 스택

- **Language**: Java 17
- **UI Framework**: JavaFX 17+
- **Build Tool**: Gradle 8.x
- **Dependencies**:
  - Gson (JSON 직렬화)
  - JUnit 5 (테스트)

## 요구사항

- **JDK 17** 이상
- Gradle (wrapper 포함)

## 빌드 및 실행

### 1. 프로젝트 클론/다운로드

```bash
cd /Users/erickong08/StudyPlanner+
```

### 2. 빌드

```bash
./gradlew build
```

### 3. 실행

```bash
./gradlew run
```

또는 JAR 파일로 실행:

```bash
./gradlew jar
java -jar build/libs/StudyPlanner+-1.0.0.jar
```

### 4. 테스트 실행

```bash
./gradlew test
```

## 프로젝트 구조

```
StudyPlanner+/
├── build.gradle                    # Gradle 빌드 설정
├── settings.gradle                 # 프로젝트 설정
├── README.md                       # 이 파일
├── src/
│   ├── main/
│   │   ├── java/com/studyplanner/
│   │   │   ├── MainApp.java       # 애플리케이션 진입점
│   │   │   ├── AppConfig.java     # 설정
│   │   │   ├── model/             # 데이터 모델
│   │   │   ├── service/           # 비즈니스 로직
│   │   │   ├── controller/        # UI 컨트롤러
│   │   │   ├── view/              # UI 컴포넌트
│   │   │   └── util/              # 유틸리티
│   │   └── resources/
│   │       └── css/app.css        # 스타일시트
│   └── test/
│       └── java/com/studyplanner/ # 단위 테스트
├── data/
│   └── tasks.json                 # 과제 데이터 (자동 생성)
└── docs/
    ├── screen_main.png            # 메인 화면 스크린샷
    ├── screen_add_dialog.png      # 추가 다이얼로그 스크린샷
    ├── screen_timer.png           # 타이머 화면 스크린샷
    └── PROJECT_REPORT.md          # 상세 기술 보고서
```

## 스크린샷

스크린샷은 애플리케이션 첫 실행 시 `docs/` 디렉토리에 자동으로 생성됩니다:

- `docs/screen_main.png` - 메인 화면
- `docs/screen_add_dialog.png` - 과제 추가 다이얼로그
- `docs/screen_timer.png` - 포모도로 타이머 실행 중

## 사용 방법

### 과제 추가

1. 상단 툴바에서 **"+ 과제 추가"** 버튼 클릭
2. 제목, 마감일, 우선순위, 메모, 태그 입력
3. **저장** 버튼 클릭

### 과제 수정/삭제

- 과제 더블클릭 또는 우클릭 메뉴에서 **수정**
- 우클릭 메뉴에서 **삭제**

### 검색

상단 검색 필드에 키워드 입력 (제목, 태그 검색)

### 캘린더 사용

1. 좌측 탭에서 **캘린더** 선택
2. 날짜 클릭 시 해당 날짜의 과제 목록 표시
3. 과제가 있는 날짜는 주황색 테두리로 표시

### 포모도로 타이머

1. 우측 패널에서 **시작** 버튼 클릭
2. 25분 카운트다운 시작
3. 일시정지/리셋 가능
4. 완료 시 알림 표시

## 데이터 저장

- 과제 데이터는 `data/tasks.json` 파일에 자동 저장
- 저장 실패 시 백업 파일 (`tasks.json.bak`) 생성
- 애플리케이션 종료 시 자동 저장

## 문제 해결

### Q: 애플리케이션이 시작되지 않습니다.

A: JDK 17 이상이 설치되어 있는지 확인하세요:

```bash
java -version
```

### Q: JavaFX 모듈을 찾을 수 없다는 오류

A: Gradle이 자동으로 JavaFX 의존성을 다운로드합니다. `./gradlew clean build`를 실행하세요.

### Q: 데이터가 사라졌습니다.

A: `data/tasks.json.bak` 백업 파일을 `tasks.json`으로 복사하세요.

## 라이선스

이 프로젝트는 교육 목적으로 제작되었습니다.

## 추가 정보

상세한 기술 문서는 [docs/PROJECT_REPORT.md](docs/PROJECT_REPORT.md)를 참조하세요.
# StudyPlannerPlus
