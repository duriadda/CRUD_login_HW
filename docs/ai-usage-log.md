# AI 활용 기록 (작업 중 기록)

보고서의 "AI 활용 및 출처" 절을 쓰기 위한 원자료입니다. 작업할 때마다 바로 추가합니다.

- 도구: Claude Code (VS Code 확장)
- 모델: Claude Opus 5.5 (`claude-opus-5-5`)

## 2026-10-26 — 1차 구현

| 구분 | 내용 |
|---|---|
| 요청 | 과제 공지 PDF를 읽고 계획 수립, 이후 Vue + Spring Boot 조합과 '치이카와 인어섬의 비밀' 영화 리뷰 주제로 구현 |
| AI가 작성·수정한 파일 | `backend/.../AuthService.java` (issueToken, currentUserId, assertOwner, 생성자의 키 검사), `backend/src/main/resources/application.properties` (`spring.config.import` 한 줄), `backend/env.example.txt`, `frontend/src/api.js` (FE-01~04), `frontend/src/App.vue` (화면 상태·오류 안내·주제 문구), `frontend/src/style.css`, `frontend/index.html` |
| 그대로 사용한 제공 코드 | `BoardController.java`(라우트·입력 검증·처리 순서), `Errors.java`(401 시 WWW-Authenticate), `Users.java`(PBKDF2 비밀번호 검증), `PostStore.java`, `DatabaseConfig.java` |
| AI가 실행한 검증 | ① 시작 코드 `check_api.py --mode starter` 14/14 통과, 로그인 501 확인 ② 예시 키·형식 오류 키(`abc123`)로 서버 시작 시 `IllegalStateException`으로 기동 거부 확인 ③ `--mode final` 40/40 통과 ④ `TOKEN_TTL=3`으로 `--mode final --expiry` 48/48 통과 ⑤ 로컬 키로 조작 JWT 15종(HS512, alg=none, iss/aud 불일치, exp·sub 누락, 없는 사용자, 만료, 헤더 형식 오류 등)을 /api/me에 보내 기대 상태·code 확인 ⑥ 발급 토큰 디코드 결과 header `alg=HS256`, payload `sub/iss/aud/iat/exp`만 있고 비밀번호·해시 없음, `exp-iat=90` ⑦ 4173 preview 프록시 경유 final 40/40 ⑧ 글 생성 → 서버 프로세스 종료·재실행 → 같은 글 조회로 유지 확인 |
| 검증 중 수정한 점 | JJWT 기본 발급 시 `aud`가 배열(`["board-api"]`)로 나와 명세 표기와 맞추려고 `.audience().single(...)`로 변경 후 재검사 |
| 아직 하지 않은 것 | 브라우저 화면 수동 확인(Network 탭 캡처 포함), T01~T17 수동 기록, 보고서·README 작성 |

## 2026-10-27 — 수동 테스트 진행 보조

| 구분 | 내용 |
|---|---|
| 방식 | 학생이 화면·curl 테스트를 직접 실행하고 캡처, AI는 테스트 절차·curl 명령 작성, 캡처·로그를 읽어 `test-results.md` 기록 |
| AI 오류와 수정 | T09용으로 AI가 준 변조 토큰 생성 명령의 Python 코드에 문법 오류(f-string 안 `\"` 이스케이프)가 있어 변조 토큰이 빈 문자열이 됨 → 서버는 `Bearer ` 뒤가 빈 헤더로 받아 401 `TOKEN_REQUIRED` 반환 (명세상 올바른 응답이지만 서명 변조 시험은 안 된 상태). 같은 오류를 재현해 원인을 확인하고 명령을 고친 뒤 재실행 |
| AI 오류와 수정 (2) | T14용 curl 명령의 로그인 함수가 비밀번호 `Classroom!2026`을 큰따옴표로 감싸, 학생 터미널(zsh)이 `!2026`을 history expansion으로 해석해 `zsh: no such event: 2026` 오류 발생. 실행 흔적(T14_curl.txt 미생성, 13번 글 변경 없음) 확인 후 JSON을 작은따옴표로 감싸고 `$1`만 밖으로 빼는 방식으로 고쳐 대화형 zsh에서 검증한 뒤 다시 제공 |
| AI 판단 오류와 정정 | T16 캡처의 새로고침 후 이메일 칸 `alice@class.test`를 AI가 "브라우저 입력값 복원"으로 잘못 기록함. 학생이 placeholder일 수 있다고 지적 → `App.vue`의 `placeholder` 속성과, 캡처를 잘라 글자 색(placeholder 회색 vs T10 실제 입력값 검정)을 비교해 placeholder가 맞음을 확인하고 `test-results.md` 기록 정정 |
| AI 설계 누락과 수정 | 1차 구현의 `App.vue`가 로그아웃 상태에서 "내 정보 조회" 버튼도 숨겨 T11(로그아웃 후 /me → 401 TOKEN_REQUIRED)을 화면에서 수행할 수 없었음 → 로그인 폼 옆에도 "내 정보 조회" 버튼을 두도록 수정하고 `npm run build`로 재빌드. 서버 코드 변경 없음 |

## 2026-10-27 — 제출 문서 보조

| 구분 | 내용 |
|---|---|
| README | 제공 양식(`06_제출용_README.md`) 기준으로 AI가 작성. 서비스 목적은 학생이 준 문장을 다듬고 대상 사용자 문장을 AI가 덧붙임. `.env` 생성 명령은 실제 실행해 64자리 키 생성 확인 |
| 보고서 초안 (`report-draft.md`, 저장소 밖) | AI가 작성: 1·4절의 사실 정리 표, 3절 코드 발췌(커밋 `ba004e6` 줄 번호), 2절 구조도와 흐름 설명 초안, 4.1 로컬 실행 기록. 처음에는 서술 칸을 힌트로만 남겼으나, 2026-10-28 학생 요청으로 1·3·4·5·6절 서술 초안(코드 설명, 401/403 화면 차이, AI 사례의 확인·검증 내용, 이해도 질문 답변)도 AI가 실제 코드와 테스트 기록을 근거로 작성. 본인 생각이 필요한 곳(기술 선택 이유, 느낀 점, 가장 어려웠던 오류 선택 등)은 ⚠️로 표시. 학생이 이를 요약·재작성해 최종 보고서에 옮김. 4.1에 서버 실행 이력(실행 주체·목적) 표 추가 |

> 학생이 직접 확인·수정한 내용은 아래에 이어서 기록하세요. (예: 코드를 읽고 이해한 부분, 직접 바꾼 문구, 직접 수행한 테스트)
