# 진행 상황 (이어서 작업하기용)

마지막 저장: 2026-10-26 (월) 18시 전

## 완료

- [x] 조합 선택: Vue + Spring Boot, 주제 '치이카와 인어섬의 비밀' 관람 리뷰 게시판
- [x] 시작 템플릿을 이 폴더로 복사, `git init`, 기준 커밋 `4e2dfc8`
- [x] 백엔드 BE-01~03 (`AuthService.java`), `.env` 로딩 설정(`application.properties`), 실제 키는 `backend/.env`(git 제외)
- [x] 프론트 FE-01~04 (`api.js`, `App.vue`, `style.css`, `index.html`)
- [x] 자동 검사: final 40/40, `TOKEN_TTL=3` + `--expiry` 48/48, 클레임 조작 15/15, 서버 재시작 후 글 유지 확인 (자세한 내용은 `ai-usage-log.md`)
- [x] 수동 테스트 T01~T17 전부 기록 완료 → `test-results.md` (2026-10-27)
- [x] T11용으로 로그아웃 상태에도 "내 정보 조회" 버튼 표시하도록 `App.vue` 수정·재빌드
- [x] 보고서용 설명 포인트 정리 → `report-notes.md`
- [x] FE-02 Authorization 헤더 캡처 (`evidence/FE02_*`)

## 다음에 할 일

1. (완료) 수동 테스트 T01~T17
2. 루트 `README.md` 작성 (`JWT-Assignment/docs/06_제출용_README.md` 양식 기반)
3. 보고서 (`03_보고서작성양식.docx`) → `docs/report.pdf`
4. 지금까지의 변경 커밋 (아직 미커밋) → GitHub 저장소 생성·push → LMS에 URL·커밋 SHA 제출

## 다시 시작할 때 서버 켜는 법

터미널 1 (백엔드):
```sh
cd "/Users/janghyeon-ug/Desktop/과제/3학년/웹서비스설계/CRUD 및 로그인 구현/chiikawa-review-board/backend"
java -jar target/jwt-board-1.0.0.jar
```

터미널 2 (프론트 빌드본):
```sh
cd "/Users/janghyeon-ug/Desktop/과제/3학년/웹서비스설계/CRUD 및 로그인 구현/chiikawa-review-board/frontend"
npm run preview
```

브라우저: http://127.0.0.1:4173 (코드를 고쳤다면 `npm run build` 후 preview, 백엔드는 `./mvnw -q -DskipTests package` 후 실행)

참고: 빌드·실행은 인터넷 없이 가능합니다. `npm ci`나 Maven 의존성 새 설치만 인터넷이 필요합니다.

## 현재 DB에 있는 글 (테스트 참고)

- 8번 Alice "재시작 유지 확인용" (T15 사전 확인용으로 만든 글, 지워도 됨)
- 9번 Alice "잔혹동화"
- 10번 Bob "재밌습니다"
