# 치이카와 인어섬의 비밀 — 관람 리뷰 게시판

웹서비스설계 JWT 게시판 과제입니다. 누구나 리뷰를 읽을 수 있고, 로그인한 사용자만 리뷰를 쓸 수 있으며, 자신이 쓴 리뷰만 수정·삭제할 수 있습니다.

## 작성자와 서비스 소개

- 학번 / 이름: 202210641 / 장현욱
- 서비스 목적과 대상 사용자: 극장판 「치이카와 인어섬의 비밀」을 감명 깊게 본 관객들이 감상을 나누는 리뷰 게시판입니다. 장면마다 숨은 의미를 함께 찾아보고 서로의 해석을 이야기하며 영화에 대한 정보를 공유하는 것을 목적으로 합니다. 영화를 이미 본 관객이 주된 사용자이며, 로그인 없이 다른 사람의 리뷰를 읽어 볼 수도 있습니다.
- 선택한 기술: Vue + Spring Boot
- 주요 기능: 리뷰 목록·상세 공개 조회, 로그인(JWT 발급)과 내 정보 조회, 로그인 사용자의 리뷰 작성, 작성자 본인만 수정·삭제, 토큰 없음·변조·만료 시 재로그인 안내, 로그아웃

## 보고서와 테스트 증거

- [구현 보고서 PDF](docs/report.pdf) — 작성 중
- [테스트 수행 기록 T01~T17](docs/test-results.md)
- [테스트 증거 (캡처·curl 로그)](docs/evidence/)
- [AI 활용 기록](docs/ai-usage-log.md)

## 프로젝트 구조

```text
.
├── README.md
├── .gitignore
├── frontend/              # Vue 3 + Vite
│   ├── src/api.js         # 토큰 메모리 보관, Bearer 헤더, 응답 코드 처리 (FE-01~04)
│   ├── src/App.vue        # 화면 상태, 로그인 표시, 오류 안내, 작성자 버튼
│   └── vite.config.js     # /api → 127.0.0.1:8000 프록시
├── backend/               # Spring Boot 3.5
│   ├── env.example.txt    # 환경설정 예시 (.env로 복사해서 사용)
│   └── src/main/java/edu/classroom/board/
│       ├── AuthService.java      # JWT 발급·검증·작성자 권한 (BE-01~03)
│       ├── BoardController.java  # API 라우트, 입력 검증
│       ├── Errors.java           # 공통 JSON 오류, 401 WWW-Authenticate
│       ├── Users.java            # 테스트 계정, PBKDF2 비밀번호 검증
│       └── PostStore.java        # SQLite 게시글 저장소
├── tools/check_api.py     # 제공된 자동 API 검사 도구
└── docs/
    ├── report.pdf         # 구현 보고서 (작성 중)
    ├── test-results.md    # T01~T17 수행 기록
    ├── evidence/          # 테스트 캡처와 curl 로그
    ├── ai-usage-log.md    # AI 활용 기록
    └── 02_API명세.md      # 제공된 공통 API 명세
```

제공 템플릿의 `frontend-vue/`를 `frontend/`로, `backend-spring/`을 `backend/`로 이름을 바꿔 사용했습니다. 선택하지 않은 템플릿(React, Express, FastAPI)은 포함하지 않았습니다.

## 실행 환경

| 항목 | 사용 버전 |
|---|---|
| 운영체제 | macOS 27.2 |
| 프론트 런타임 Node.js | v26.5.0 (npm 11.17.0), 요구 사항 20.19 이상 |
| 백엔드 런타임 | JDK 21.0.12.1, 요구 사항 17 이상 |
| 프레임워크 | Vue 3.5.43, Vite 8.3.4 / Spring Boot 3.5.16, JJWT 0.13.0, Maven 3.9.11(Wrapper) |
| 데이터 저장 | SQLite (sqlite-jdbc 3.51.3.0) |

## 설치와 실행

백엔드와 프론트를 각각 다른 터미널에서 실행합니다. 의존성 설치(`./mvnw package`, `npm ci`)에는 인터넷이 필요하고, 그 뒤 실행에는 필요하지 않습니다.

### 1 저장소 복제

```sh
git clone https://github.com/duriadda/CRUD_login_HW.git
cd CRUD_login_HW
```

### 2 백엔드 준비와 실행 (터미널 ①)

```sh
cd backend
# 1) 환경설정 파일 만들기: 예시를 복사하면서 JWT_SECRET을 새 난수 키로 바꾼다
sed "s/^JWT_SECRET=.*/JWT_SECRET=$(openssl rand -hex 32)/" env.example.txt > .env
# 2) 빌드 (처음 한 번은 Maven과 의존성을 내려받음)
./mvnw -q -DskipTests package
# 3) 실행
java -jar target/jwt-board-1.0.0.jar
```

Windows PowerShell에서는 다음과 같이 실행합니다.

```powershell
cd backend
$key = node -e "console.log(require('node:crypto').randomBytes(32).toString('hex'))"
(Get-Content env.example.txt) -replace '^JWT_SECRET=.*', "JWT_SECRET=$key" | Set-Content .env
.\mvnw.cmd -q -DskipTests package
java -jar target/jwt-board-1.0.0.jar
```

- 환경설정 예시 파일: `backend/env.example.txt` → `backend/.env`로 복사해 사용합니다. `.env`는 git에 올리지 않습니다.
- JWT_SECRET 설정 방법: 64자리 16진수(32바이트) 난수를 새로 만들어 `.env`에 넣습니다. Spring은 원래 `.env`를 읽지 않으므로 `application.properties`에 `spring.config.import=optional:file:.env[.properties]`를 추가해, **`backend` 폴더에서 실행하면** `.env`를 읽도록 했습니다. 같은 이름의 OS 환경변수를 주면 `.env` 값보다 우선합니다. 키가 비었거나 예시 값 그대로이거나 64자리 16진수가 아니면 서버가 시작되지 않습니다.
- TOKEN_TTL: 기본 90초입니다. 만료 테스트를 하려면 서버를 `TOKEN_TTL=3 java -jar target/jwt-board-1.0.0.jar`로 실행합니다. PowerShell에서는 `$env:TOKEN_TTL='3'`을 설정한 뒤 실행합니다.
- 백엔드 주소: http://127.0.0.1:8000
- 정상 실행 확인: 다른 터미널에서 `curl http://127.0.0.1:8000/api/health`를 실행해 `{"status":"ok",...}`가 나오면 됩니다.

### 3 프론트 빌드와 로컬 실행 (터미널 ②)

백엔드를 켜 둔 채 저장소 루트에서 실행합니다.

```sh
cd frontend
npm ci
npm run build
npm run preview
```

- 최종 확인 주소: http://127.0.0.1:4173
- 개발 중 실행: `npm run dev`, http://127.0.0.1:5173
- API 연결 방식: 브라우저는 같은 주소의 `/api`로 요청하고, Vite preview(4173)와 dev 서버(5173)가 이를 `http://127.0.0.1:8000`의 백엔드로 전달합니다(`vite.config.js`의 `preview.proxy`, `server.proxy`). 백엔드 포트를 바꾸면 이 두 곳도 함께 수정합니다.
- 종료: 프론트와 백엔드를 실행한 각 터미널에서 Ctrl+C
- 코드 변경 후 재실행: 프론트는 `npm run build` 후 `npm run preview`를 다시 실행하고, 백엔드는 Ctrl+C로 종료한 뒤 `./mvnw -q -DskipTests package` → `java -jar target/jwt-board-1.0.0.jar`로 다시 실행합니다.

### 4 테스트 계정과 DB

| 사용자 | 이메일 | 실습용 비밀번호 |
|---|---|---|
| Alice (id 1) | alice@class.test | Classroom!2026 |
| Bob (id 2) | bob@class.test | Classroom!2026 |

- SQLite 파일 위치: `backend/data/board.sqlite`. `backend` 폴더에서 서버를 실행하면 폴더·파일·`posts` 테이블이 자동으로 생성됩니다. 경로를 바꾸려면 `.env`의 `DB_FILE`을 수정합니다.
- DB 초기화: 서버를 종료한 뒤 `backend/data/board.sqlite`를 삭제하고 다시 실행합니다. 기존 리뷰는 모두 지워집니다.
- 서버 재시작 후 데이터 유지: 리뷰를 등록하고 백엔드를 Ctrl+C로 종료한 뒤 다시 실행했을 때, 같은 리뷰의 내용·작성자·작성 시각이 그대로 조회되는 것을 확인했습니다([T15](docs/test-results.md)).

위 계정은 과제 전용 테스트 계정입니다. 실제 서버 비밀 키나 토큰은 저장소에 기록하지 않았습니다.

## 구현 완료 범위

| 기능 | 완료 여부 | 주요 파일 또는 함수 |
|---|---|---|
| 공개 목록과 상세 | 완료 | `BoardController.list()`, `get()` / `App.vue`의 목록·상세 |
| 로그인과 JWT 발급 | 완료 | `AuthService.issueToken()`, `BoardController.login()` / `api.js`의 `login()` |
| JWT 검증과 내 정보 조회 | 완료 | `AuthService.currentUserId()`, `BoardController.me()` |
| 글 작성과 작성자 저장 | 완료 | `BoardController.create()`: 작성자 ID는 `currentUserId()`로 서버가 결정 |
| 본인 글 수정·삭제 | 완료 | `AuthService.assertOwner()` / `api.js`의 `canEdit()`(화면 표시용) |
| 토큰 만료와 로그아웃 | 완료 | `currentUserId()`의 만료 처리 / `api.js`의 `request()` 401 처리, `clearSession()` |
| 입력 오류와 권한 오류 처리 | 완료 | `BoardController.fields()`, `postId()` / `App.vue`의 code별 안내 |

알려진 문제와 미완성 기능: 없음. 과제 요구사항에 따라 로그인 상태는 탭 메모리에만 보관하므로 새로고침하면 로그인이 해제되며, 로그아웃은 이미 발급된 토큰을 서버에서 무효화하지 않습니다(토큰은 만료 시각까지 유효).

## 테스트 방법과 결과 요약

- 실행한 검사 명령과 실행 위치: 백엔드 실행 후 저장소 루트에서 `python3 tools/check_api.py --mode final` 40/40 통과, `TOKEN_TTL=3`으로 재시작 후 `python3 tools/check_api.py --mode final --expiry` 48/48 통과. 자동 검사와 별도로 T01~T17을 4173 빌드 화면과 curl로 직접 수행했습니다.
- Alice/Bob 권한 구분: Bob의 정상 토큰으로 Alice 글을 수정·삭제하면 둘 다 403 `FORBIDDEN`이고 원문이 그대로입니다(T07·T08). Bob 화면에서는 Alice 글의 수정·삭제 버튼이 보이지 않습니다. Bob 본인 글은 작성 201, 수정 200, 삭제 204로 정상 처리됩니다(T17).
- HTTP 상태와 오류 code: 성공은 200(조회·로그인·수정), 201(등록), 204(삭제)입니다. 실패는 400 `BAD_INPUT`(입력·추가 필드·잘못된 ID), 401(`INVALID_CREDENTIALS`, `TOKEN_REQUIRED`, `INVALID_TOKEN`, `TOKEN_EXPIRED`), 403 `FORBIDDEN`, 404 `NOT_FOUND`를 모두 실제 응답으로 확인했습니다.
- 응답 헤더와 본문: 등록 201에 `Location: /api/posts/{id}`(T05), 보호 API 401에 `WWW-Authenticate: Bearer`(T04·T09·T10·T11), 삭제 204는 빈 본문(T06)입니다.
- 토큰 없음·변조·만료: 토큰 없음은 401 `TOKEN_REQUIRED`(T04·T11·T17), 서명 첫 글자 변조는 401 `INVALID_TOKEN`(T09), 3초 TTL 토큰의 실제 만료는 401 `TOKEN_EXPIRED`(T10)입니다. 실패한 변경 요청 뒤에는 매번 재조회해 데이터가 바뀌지 않았음을 확인했습니다.
- 전체 T01~T17 결과와 증거: [docs/test-results.md](docs/test-results.md), [docs/evidence/](docs/evidence/)

## 핵심 구현 설명

1. **로그인·발급**: `POST /api/auth/login` → 제공된 `Users.verify()`로 비밀번호를 확인한 계정에만 `AuthService.issueToken()`이 HS256 JWT를 발급합니다. 클레임은 `sub`(사용자 ID 문자열), `iss`, `aud`, `iat`, `exp`입니다.
2. **토큰 보관·전송**: `frontend/src/api.js`가 토큰과 사용자를 메모리 변수 `session`에만 저장하고(localStorage 미사용), `authorizationHeaders()`가 토큰이 있을 때만 `Authorization: Bearer <JWT>`를 붙입니다.
3. **서버 검증**: `AuthService.currentUserId()`가 헤더 형식을 확인하고, 서명·HS256·`iss`·`aud`·만료를 라이브러리로 검증합니다. 이어서 `exp` 존재, `sub` 형식, 실제 계정 존재를 직접 확인한 뒤 그 ID만 등록·수정·삭제·`/me`에 넘깁니다.
4. **작성자 권한**: 수정·삭제는 인증 → 글 존재 → `AuthService.assertOwner()`(저장된 authorId와 검증된 ID 비교) → 입력 검증 → DB 변경 순서로 처리합니다. 화면의 `canEdit()`는 버튼 표시만 담당합니다.

자세한 코드 설명은 보고서에 있습니다.

## AI 활용 요약과 참고 자료

- AI 사용 여부: 사용
- 도구와 사용 목적: Claude Code(VS Code 확장, Claude Opus 5.5). 과제 계획 수립, `AuthService.java`·`api.js`·`App.vue` 구현 코드 작성, 자동 검사 실행, 테스트 절차와 curl 명령 작성, 테스트 캡처 검토와 기록 정리에 사용했습니다.
- 반영 위치와 직접 수정·검증한 내용: AI가 작성한 코드는 위 구현 파일에 반영했습니다. T01~T17은 직접 실행하고 캡처했으며, 결과를 AI와 함께 검토했습니다. 검증 과정에서 AI가 준 테스트 명령의 오류 2건(변조 토큰 생성 문법 오류, zsh `!` 따옴표 문제)과 AI의 캡처 해석 오류 1건(placeholder를 입력값으로 오인)을 발견해 바로잡았습니다.
- 상세 활용 기록: [docs/ai-usage-log.md](docs/ai-usage-log.md), 보고서의 AI 활용 절
- 참고한 수업 자료·외부 코드·공식 문서:
  - 수업 제공 시작 템플릿(`frontend-vue`, `backend-spring`), `tools/check_api.py`, 공통 API 명세: 프로젝트 전체의 기반
  - JJWT 사용법 https://github.com/jwtk/jjwt: `AuthService.java`의 발급·검증
  - Spring Boot 외부 설정(config import) 공식 문서 https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html: `application.properties`의 `.env` 로딩
  - Vite 가이드 https://vite.dev/guide/: 빌드·preview·프록시

## 제출 확인

- [x] README의 안내 문구를 실제 내용으로 교체했다.
- [x] 새 폴더에 clone하여 README만으로 설치·환경설정·빌드·로컬 실행했다. ([기록](docs/test-results.md#새-폴더-clone-재현-확인))
- [x] 4173번 빌드 화면에서 API 연결과 전체 필수 기능을 확인했다.
- [ ] GitHub에서 보고서와 증거 링크가 열린다.
- [x] 교수자가 저장소를 열람·복제할 수 있다. (공개 저장소)
- [x] 비밀 키·실제 토큰·DB·node_modules·.venv·target을 커밋하지 않았다.
- [ ] 최종 push 후 저장소 URL과 제출 기준 커밋 SHA를 LMS에 제출했다.
