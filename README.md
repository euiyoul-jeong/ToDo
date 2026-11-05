# ToDo

홈 화면 위젯이 있는 안드로이드 할 일 앱.

## 기능
- 할 일 추가 · 수정 · 삭제(스와이프, 실행 취소 가능) · 완료 체크
- **마감일/시간**, 지난 마감은 빨간색으로 표시
- **알림**: 마감 시각에 알림, 알림에서 바로 "완료" 처리 (재부팅 후에도 유지)
- **중요도**: 높음 / 보통 / 낮음
- **카테고리**: 기본 "업무", "개인" + 직접 추가·수정·삭제, 목록 상단에서 카테고리별로 필터
- 정렬: 마감일순 / 중요도순
- **홈 화면 위젯**: 남은 할 일 목록, 위젯에서 바로 완료 체크, `+` 버튼으로 추가 화면 열기, 항목을 누르면 수정 화면

## 기술 스택
Kotlin · Jetpack Compose (Material 3) · Room · Jetpack Glance(위젯) · AlarmManager

- minSdk 26 (Android 8.0) / targetSdk 36
- 빌드: AGP 8.13, Gradle 8.14.3, JDK 17 이상

## 빌드
```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # 단위 테스트
./gradlew installDebug         # 연결된 기기에 설치
```
Android Studio에서 폴더를 열어 실행해도 됩니다.

## 구조
```
app/src/main/java/com/euiyoul/todo/
├── data/       Room 엔티티·DAO·DB, 정렬 규칙, TodoRepository(변경 시 알림 재예약 + 위젯 갱신)
├── ui/         MainActivity(네비게이션), list/ 목록, edit/ 편집, category/ 카테고리 관리, theme/
├── reminder/   알람 예약, 알림 표시/완료 처리, 재부팅 시 재예약
├── widget/     Glance 홈 화면 위젯
└── util/       마감일 표시 형식
```
