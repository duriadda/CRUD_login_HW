# 테스트 수행 기록 (T01~T17)

- 환경: 백엔드 `java -jar target/jwt-board-1.0.0.jar` (127.0.0.1:8000), 프론트 `npm run preview` (127.0.0.1:4173)
- 계정: Alice(id 1), Bob(id 2), 공통 비밀번호 `Classroom!2026`
- 실제 JWT는 `<TOKEN>`으로 표기하고, 캡처의 토큰은 앞부분만 남기고 가린다.
- 직접 관찰한 값만 적는다. 수행하지 않은 항목은 `미실행`으로 둔다.

| ID | 조건 | 요청 | 예상 HTTP / code | 실제 HTTP / code | 화면 처리 | 데이터 유지 | 증거 |
|---|---|---|---|---|---|---|---|
| T01 | 비로그인, 목록·상세 조회 | GET /api/posts, GET /api/posts/9 | 200 / - | 200, 200 | 목록·상세 표시 (화면 캡처 없음) | - | T01_posts_200-1·2.png, T01_9_200-1·2.png |
| T02 | Alice 로그인 | POST /api/auth/login | 200 / - | 200 (accessToken, expiresIn 90, user id 1) | "Alice님 로그인 중" 표시 | - | T02_login_200.png, T02_login_response.png, T02_screen_logged_in.png |
| T03 | 틀린 비밀번호, 없는 이메일 | POST /api/auth/login | 401 / INVALID_CREDENTIALS | 401 / INVALID_CREDENTIALS (두 경우 동일, 토큰 없음) | 빨간 안내 "이메일 또는 비밀번호가 올바르지 않습니다.", 로그인 카드 유지 | - | T03_wrong_pw_401.png, T03_wrong_pw_response.png, T03_no_email_401.png, T03_no_email_response.png, T03_wrong_pw_screen.png |
| T04 | 토큰 없이 /me, 글 등록 | GET /api/me, POST /api/posts | 401 / TOKEN_REQUIRED | 401 / TOKEN_REQUIRED (둘 다, WWW-Authenticate: Bearer) | 비로그인 시 글쓰기 폼 대신 "리뷰를 작성하려면 먼저 로그인하세요." (서버는 curl로 직접 요청) | 글 개수 3 → 3, 미생성 | T04_curl.txt, T04_screen_login_guide.png |
| T05 | Alice 글 작성 후 /me | POST /api/posts, GET /api/me | 201(authorId 1), 200 | Location: /api/posts/12, authorId 1 / me 200 Alice | "리뷰를 등록했습니다.", 새 글에 "내 리뷰" 표시 | 목록에 반영 | T05_create_201.png, T05_create_201_Authorization.png, T05_create_response.png, T05_me_200.png, T05_screen.png |
| T06 | Alice 본인 글 수정·삭제 | PUT, DELETE /api/posts/12 | 200, 204 | 200 (createdAt 유지, updatedAt 변경), 204 (본문 없음) | "리뷰를 삭제했습니다.", 목록에서 제거 | 수정 반영 후 삭제, 목록 5 → 4개 | T06_update_200.png, T06_update_response.png, T06_delete_204.png, T06_delete_response_empty.png, T06_screen_deleted.png |
| T07 | Bob 토큰으로 Alice 글 수정 | PUT /api/posts/9 | 403 / FORBIDDEN | 403 / FORBIDDEN | Bob 화면에서 Alice 글의 수정·삭제 버튼 숨김 (서버는 curl로 직접 요청) | 요청 전후 원문 동일 | T07_T08_curl.txt, T07_screen_bob_no_buttons.png |
| T08 | Bob 토큰으로 Alice 글 삭제 | DELETE /api/posts/9 | 403 / FORBIDDEN | 403 / FORBIDDEN | 위와 같음 | 요청 후에도 글 존재, 원문 동일 | T07_T08_curl.txt |
| T09 | 서명 첫 글자 변조 토큰 | GET /api/me, POST /api/posts, PUT·DELETE /api/posts/13 | 401 / INVALID_TOKEN | 401 / INVALID_TOKEN (4개 모두, WWW-Authenticate: Bearer), 대조군 정상 토큰 /me 200 | - (curl) | 13번 원문·글 개수 4 → 4 유지 | T09_curl.txt |
| T10 | TTL 3초 토큰 만료 후 | GET /api/me, PUT·DELETE /api/posts/10 | 401 / TOKEN_EXPIRED | 401 / TOKEN_EXPIRED (curl 3개, 화면 /me 1개), 발급 직후 대조군 200 | 빨간 안내 "로그인 시간이 만료되어 로그아웃되었습니다…", 로그인 폼으로 복귀 | 10번 원문 유지 | T10_curl.txt, T10_me_401.png, T10_me_response.png, T10_screen_logged_out.png |
| T11 | 로그아웃 후 /me | GET /api/me | 401 / TOKEN_REQUIRED | 401 / TOKEN_REQUIRED, 요청에 Authorization 없음 | "로그인이 필요합니다. 로그인한 뒤 다시 시도하세요.", 로그인 폼 | - | T11_me_401_no_auth.png, T11_me_response.png, T11_screen.png |
| T12 | Bob 등록에 authorId:1 / userId:1 추가 | POST /api/posts | 400 / BAD_INPUT | 400 / BAD_INPUT (authorId, userId 각각) | - (curl) | 목록 4개 → 4개, 위조 글 미생성 | T12_curl.txt |
| T13 | 빈 제목·공백 본문·길이 초과 | POST /api/posts | 400 / BAD_INPUT | 400 / BAD_INPUT (curl 7종 모두, 화면 1건) | "입력을 확인하세요. (title 입력을 확인하세요.)", 입력값 유지 | 글 개수 4 → 4 | T13_curl.txt, T13_ui_400.png, T13_screen_input_kept.png |
| T14 | 없는 ID 조회·수정·삭제, 잘못된 ID | GET/PUT/DELETE /api/posts/99999, GET /api/posts/abc | 404 / NOT_FOUND, 400 / BAD_INPUT | 404 ×3, 400 ×3(abc·0·-1), 겹침 4종 기대대로 | 404 안내 후 목록 갱신, 로그인 유지 | 13번 원문 유지 | T14_curl.txt, T14_ui_404.png, T14_screen_404.png |
| T15 | 서버 종료·재실행 | GET /api/posts/17 | 200, 글 유지 | 재시작 전후 17번 내용·전체 6개 동일 | 새로고침 후 목록에 표시 | 유지 | T15_restart.txt, T15_screen_after_restart.png |
| T16 | 로그인 후 새로고침 | - | 글 유지, 로그인 해제 | 새로고침 전후 목록 6개 동일, 로그인 해제 | 로그인 폼·로그인 안내로 복귀, "내 리뷰" 표시 사라짐 | 유지 | T16_before_refresh.png, T16_after_refresh.png |
| T17 | 무토큰 수정·삭제, Bob 본인 작성·수정·삭제 | PUT/DELETE /api/posts/10, POST·PUT·DELETE /api/posts/18 | 401, 201/200/204 | 401 TOKEN_REQUIRED ×2 / 201·200·204, 삭제 후 404 | - (curl) | 10번 원문 유지, 18번 생성·수정 후 삭제 | T17_curl.txt |

## 상세 기록

### T01
- 조건: 새로고침 직후 비로그인 상태, 4173 빌드본에서 Network 탭으로 확인 (2026-10-26)
- 요청: GET http://127.0.0.1:4173/api/posts, GET http://127.0.0.1:4173/api/posts/9 (Alice 글)
- 실제: 두 요청 모두 `200 OK`, `Content-Type: application/json`. Request Headers에 `Authorization` 없음 → 토큰 없이 공개 조회됨
- 화면: 목록과 상세가 표시됨. 로그아웃 상태의 로그인 카드는 별도 캡처하지 않음
- 증거: `evidence/T01_posts_200-1.png`, `T01_posts_200-2.png`, `T01_9_200-1.png`, `T01_9_200-2.png`

### T02
- 조건: 비로그인 상태에서 화면의 "Alice로 채우기" 후 로그인, 4173 빌드본 (2026-10-26)
- 요청: POST http://127.0.0.1:4173/api/auth/login, 본문 email·password JSON
- 실제: `200 OK`. 응답 본문은 `accessToken`(가림), `expiresIn: 90`, `user: {id: 1, name: "Alice", email: "alice@class.test"}`. 비밀번호·해시 필드 없음
- 화면: 상단 메시지 "Alice님, 환영합니다!", 로그인 카드 "Alice님 로그인 중"과 내 정보 조회·로그아웃 버튼 표시
- 증거: `evidence/T02_login_200.png`, `T02_login_response.png`, `T02_screen_logged_in.png`

### T03
- 조건: 로그아웃 상태, 4173 빌드본 (2026-10-26)
- 요청: POST http://127.0.0.1:4173/api/auth/login — ① `alice@class.test` + 틀린 비밀번호, ② 없는 이메일 `nobody@class.test` + 정상 비밀번호
- 실제: 두 경우 모두 `401 Unauthorized`, `WWW-Authenticate: Bearer`, 본문 `{"code":"INVALID_CREDENTIALS","message":"이메일 또는 비밀번호를 확인하세요."}`. `accessToken` 없음
- 의미: 계정 없음과 비밀번호 틀림의 응답이 같아 응답만으로 가입된 이메일을 알아낼 수 없음
- 화면: 빨간 안내 "이메일 또는 비밀번호가 올바르지 않습니다."가 표시되고 로그인 카드는 로그인 폼 상태로 남음 (2026-10-27 추가 캡처, 틀린 비밀번호 경우)
- 증거: `evidence/T03_wrong_pw_401.png`, `T03_wrong_pw_response.png`, `T03_no_email_401.png`, `T03_no_email_response.png`, `T03_wrong_pw_screen.png`

### T04
- 조건: Authorization 헤더 없이 curl로 백엔드(8000)에 직접 요청 (2026-10-27). 화면에서는 비로그인 시 "내 정보 조회" 버튼과 글쓰기 폼이 숨겨지므로 서버 검사를 따로 확인함
- 요청: GET http://127.0.0.1:8000/api/me, POST http://127.0.0.1:8000/api/posts (title·content JSON)
- 실제: 두 요청 모두 `HTTP/1.1 401`, `WWW-Authenticate: Bearer`, 본문 `{"message":"로그인이 필요합니다.","code":"TOKEN_REQUIRED"}`
- 데이터: 요청 전 글 3개 → 요청 후 3개. 토큰 없는 등록으로 글이 생성되지 않음
- 화면: 로그아웃 상태에서 리뷰 쓰기 카드에 폼 대신 "리뷰를 작성하려면 먼저 로그인하세요." 표시 (FE-04 비로그인 안내)
- 증거: `evidence/T04_curl.txt`, `T04_screen_login_guide.png`

### T05
- 조건: Alice 로그인 후 화면에서 리뷰 등록, 이어서 "내 정보 조회" (2026-10-27, 4173 빌드본)
- 요청: POST http://127.0.0.1:4173/api/posts (본문은 title·content만), GET http://127.0.0.1:4173/api/me
- 실제: 등록 응답 헤더 `Location: /api/posts/12`, 요청 헤더 `Authorization: Bearer <TOKEN>`(가림). 응답 본문 `id 12, authorId 1, authorName "Alice"`, createdAt = updatedAt. `/me` 응답 `{"id":1,"name":"Alice","email":"alice@class.test"}`
- 의미: 요청 본문에 작성자 필드가 없는데도 서버가 검증한 토큰의 사용자(1)로 authorId를 정함
- 화면: "리뷰를 등록했습니다." 안내, 목록 맨 위 새 글에 "내 리뷰" 표시, "Alice님 로그인 중" 유지
- 참고: 캡처의 Status Code(201) 줄은 잘려 있음 (Location 헤더와 응답으로 생성 확인). 화면 캡처는 이후 한 번 더 등록한 13번 글 "T05 테스트" 기준
- 증거: `evidence/T05_create_201.png`, `T05_create_201_Authorization.png`, `T05_create_response.png`, `T05_me_200.png`, `T05_screen.png`

### T06
- 조건: Alice 로그인 상태에서 본인 글(12번 "테스트용")을 화면으로 수정 후 삭제 (2026-10-27, 4173 빌드본)
- 요청: PUT http://127.0.0.1:4173/api/posts/12, DELETE http://127.0.0.1:4173/api/posts/12
- 실제(수정): `200 OK`. content가 "테스트입니다 수정본"으로 바뀜. authorId 1·authorName Alice 유지, `createdAt` `2026-10-27T07:48:51.930284Z`는 T05 등록 때와 같고 `updatedAt`만 `07:56:08.856994Z`로 변경
- 실제(삭제): `204 No Content`. Response 탭에 본문 없음(Chrome 표시 "Failed to load response data")
- 화면: 삭제 후 "리뷰를 삭제했습니다." 성공 안내, 목록에서 12번이 사라져 4개 표시. 204 응답에서 JSON 파싱 오류 없이 화면 갱신됨
- 증거: `evidence/T06_update_200.png`, `T06_update_response.png`, `T06_delete_204.png`, `T06_delete_response_empty.png`, `T06_screen_deleted.png`

### T07 · T08
- 조건: Bob 계정으로 로그인해 받은 정상 토큰(`/me` → id 2 Bob 확인)으로 Alice의 9번 글 "잔혹동화"에 curl로 직접 요청 (2026-10-27). 화면에서는 버튼이 숨겨져 있어 누를 수 없음
- 요청: PUT http://127.0.0.1:8000/api/posts/9 (title·content JSON), DELETE http://127.0.0.1:8000/api/posts/9, 둘 다 `Authorization: Bearer <TOKEN>`
- 실제: 두 요청 모두 `HTTP/1.1 403`, 본문 `{"message":"본인이 작성한 글만 수정·삭제할 수 있습니다.","code":"FORBIDDEN"}`. 401이 아니므로 `WWW-Authenticate` 헤더 없음
- 데이터: 요청 전후 GET 결과가 title·content·authorId·createdAt·updatedAt까지 완전히 같음 → 수정·삭제 모두 DB에 반영되지 않음
- 화면: Bob 로그인 상태에서 "잔혹동화" 상세에 수정·삭제 버튼이 표시되지 않음. Bob 본인 글 "재밌습니다"에만 "내 리뷰" 표시
- 의미: 화면은 버튼만 숨기고, 실제 차단은 서버의 `assertOwner`가 수행함
- 증거: `evidence/T07_T08_curl.txt`, `T07_screen_bob_no_buttons.png`

### T09
- 조건: Alice 정상 토큰의 서명(세 번째 부분) 첫 글자를 `T` → `A`로 바꾼 토큰(길이 183 동일)으로 curl 직접 요청 (2026-10-27). 대상은 Alice 본인 글 13번이므로 정상 토큰이었다면 성공해야 하는 요청
- 요청: GET /api/me, POST /api/posts, PUT /api/posts/13, DELETE /api/posts/13 (모두 8000 직접, `Authorization: Bearer <TAMPERED>`)
- 실제: 대조군(같은 토큰의 변조 전) `/me` → `HTTP 200`. 변조 토큰 4개 요청 모두 `HTTP/1.1 401`, `WWW-Authenticate: Bearer`, `{"message":"유효하지 않은 토큰입니다.","code":"INVALID_TOKEN"}`
- 데이터: 13번 원문(updatedAt 포함)과 글 개수 4개가 요청 전후 동일 → 생성·수정·삭제 모두 반영 안 됨
- 참고: 첫 시도는 AI가 준 변조 명령의 문법 오류로 변조 토큰이 빈 값이 되어 4개 요청 모두 `TOKEN_REQUIRED`가 나옴(서명 변조 미시험). 명령을 고쳐 재실행한 결과로 기록을 대체함 (`ai-usage-log.md` 참고)
- 증거: `evidence/T09_curl.txt`

### T10
- 조건: 백엔드를 `TOKEN_TTL=3`으로 재시작 (로그인 응답 `expiresIn: 3` 확인), 2026-10-27
- curl: Bob 로그인 직후 `/me` → `HTTP 200`(대조군). 5초 대기 후 같은 토큰으로 GET /api/me, Bob 본인 글 PUT·DELETE /api/posts/10 → 3개 모두 `HTTP/1.1 401`, `WWW-Authenticate: Bearer`, `{"message":"로그인 시간이 만료되었습니다. 다시 로그인하세요.","code":"TOKEN_EXPIRED"}`. 본인 글인데도 거부되므로 원인은 만료뿐
- 데이터: 10번 원문(updatedAt 포함) 요청 전후 동일
- 화면: Alice 로그인 후 5초 이상 지나 "내 정보 조회" → GET http://127.0.0.1:4173/api/me `401 Unauthorized`, `Www-Authenticate: Bearer`, code `TOKEN_EXPIRED`. 화면에 빨간 안내 "로그인 시간이 만료되어 로그아웃되었습니다. 다시 로그인하세요.", 로그인 카드가 로그인 폼으로 돌아가고 리뷰 쓰기 카드는 로그인 안내로 바뀜 (FE-03 401 처리)
- 증거: `evidence/T10_curl.txt`, `T10_me_401.png`, `T10_me_response.png`, `T10_screen_logged_out.png`

### T11
- 조건: 백엔드 TTL 90 복구 후, 화면에서 Alice 로그인 → 로그아웃 → 로그인 폼 옆 "내 정보 조회" (2026-10-27). 이 버튼은 T11 수행을 위해 로그아웃 상태에도 표시하도록 `App.vue`를 수정·재빌드한 뒤 진행
- 요청: GET http://127.0.0.1:4173/api/me
- 실제: `401 Unauthorized`, `Www-Authenticate: Bearer`, `{"code":"TOKEN_REQUIRED","message":"로그인이 필요합니다."}`. Request Headers(알파벳순)에서 `Accept-Language` 다음이 `Connection`으로, `Authorization` 줄이 없음 → 로그아웃으로 메모리의 토큰이 지워져 헤더가 만들어지지 않음 (`Bearer undefined` 같은 값도 없음)
- 화면: 빨간 안내 "로그인이 필요합니다. 로그인한 뒤 다시 시도하세요.", 로그인 폼 상태, 리뷰 쓰기 카드는 로그인 안내
- 증거: `evidence/T11_me_401_no_auth.png`, `T11_me_response.png`, `T11_screen.png`

### T12
- 조건: Bob 정상 토큰(`/me` → id 2 Bob 확인)으로 글 등록 본문에 작성자 필드를 추가해 curl 직접 요청 (2026-10-27)
- 요청: POST http://127.0.0.1:8000/api/posts ① `{"title":"위조 글 A",...,"authorId":1}` ② `{"title":"위조 글 B",...,"userId":1}`
- 실제: 두 요청 모두 `HTTP/1.1 400`, `{"code":"BAD_INPUT","message":"허용된 JSON 필드만 보내세요."}`
- 데이터: 요청 전후 목록이 같은 4개 `(13 Alice, 10 Bob, 9 Alice, 8 Alice)`. "위조 글 A·B"는 어느 작성자로도 생성되지 않음 → 추가 필드를 무시하고 저장하는 것이 아니라 요청 자체를 거부
- 증거: `evidence/T12_curl.txt`

### T13
- 조건: Alice 정상 토큰으로 글 등록 시도 (2026-10-27). curl은 응답 상태 줄과 본문만 남도록 필터링해 저장
- curl 결과 (8000 직접, 모두 `HTTP/1.1 400`, code `BAD_INPUT`):
  | 입력 | message |
  |---|---|
  | 빈 제목 `""` | title 입력을 확인하세요. |
  | 공백만 있는 제목 `"   "` | title 입력을 확인하세요. |
  | 공백·탭·줄바꿈만 있는 본문 | content 입력을 확인하세요. |
  | 제목 101자 (`"가"`×101) | title 입력을 확인하세요. |
  | 내용 5001자 (`"나"`×5001) | content 입력을 확인하세요. |
  | 제목이 숫자 `123` (추가 확인) | 문자열을 입력하세요. |
  | content 필드 누락 (추가 확인) | 문자열을 입력하세요. |
- 데이터: 요청 전후 글 개수 4 → 4
- 화면: 한줄평에 공백 2칸, 리뷰 내용 "아무문장~"으로 등록 → POST http://127.0.0.1:4173/api/posts `400 Bad Request`. 빨간 안내 "입력을 확인하세요. (title 입력을 확인하세요.)", 리뷰 내용 "아무문장~"과 한줄평 공백(카운터 2 / 100)이 그대로 남아 수정 가능. 로그인 상태 유지 (FE-03 400 처리)
- 증거: `evidence/T13_curl.txt`, `T13_ui_400.png`, `T13_screen_input_kept.png`

### T14
- 화면(FE-03 404 처리): Alice가 "404 테스트"(15번) 등록 → 상세를 연 채로 터미널에서 Alice 토큰으로 15번 DELETE(204) → 화면에서 삭제 → 삭제 확인 (2026-10-27)
  - 실제: DELETE http://127.0.0.1:4173/api/posts/15 `404 Not Found`
  - 화면: 빨간 안내 "해당 리뷰가 없습니다. 이미 삭제되었을 수 있어 목록을 새로 불러왔습니다.", 목록이 4개로 갱신되어 15번 없음, 로그인 상태 유지(404는 로그아웃하지 않음)
  - 증거: `evidence/T14_ui_404.png`(Network 헤더), `T14_screen_404.png`(화면)
- curl (8000 직접, Alice·Bob 정상 토큰, 2026-10-27):
  | 요청 | 실제 HTTP / code |
  |---|---|
  | GET /api/posts/99999 (토큰 없음) | 404 / NOT_FOUND |
  | PUT /api/posts/99999 (Alice) | 404 / NOT_FOUND |
  | DELETE /api/posts/99999 (Alice) | 404 / NOT_FOUND |
  | GET /api/posts/abc | 400 / BAD_INPUT ("ID는 양의 정수여야 합니다.") |
  | GET /api/posts/0 | 400 / BAD_INPUT |
  | GET /api/posts/-1 | 400 / BAD_INPUT |
  | 겹침: 토큰 없이 없는 글 수정 | 401 / TOKEN_REQUIRED |
  | 겹침: 정상 토큰으로 같은 없는 글 수정 | 404 / NOT_FOUND |
  | 겹침: Bob이 Alice 글(13)에 공백 제목 | 403 / FORBIDDEN |
  | 겹침: Alice가 자기 글(13)에 공백 제목 | 400 / BAD_INPUT |
  - 의미: 같은 요청이라도 인증 → 글 존재 → 작성자 권한 → 입력 검증 순서로 먼저 걸리는 단계의 응답이 나옴
  - 데이터: 13번 원문(updatedAt 포함) 요청 전후 동일
  - 증거: `evidence/T14_curl.txt`
  - 참고(T14): 첫 실행은 AI가 준 명령의 비밀번호 따옴표 문제로 zsh `no such event: 2026` 오류가 나 실행되지 않음(흔적 없음 확인). 수정한 명령으로 재실행한 결과

### T15
- 조건: 학생이 터미널 ①에서 백엔드를 직접 실행(`java -jar target/jwt-board-1.0.0.jar`), 터미널 ②에서 curl로 기록 (2026-10-27)
- 절차: ① Alice 토큰으로 글 등록 → ② 터미널 ①에서 Ctrl+C로 서버 종료 → ③ 같은 명령으로 재실행 → ④ 같은 ID 재조회
- 실제:
  - [재시작 전 18:57:39] 17번 "재시작 전에 쓴 리뷰" 등록, authorId 1, createdAt `2026-10-27T09:57:39.225348Z`, 전체 6개
  - [재시작 후 18:57:59] GET /api/posts/17 → 재시작 전과 id·title·content·authorId·authorName·createdAt·updatedAt 모두 동일, 전체 6개
  - 재시작 확인: 실행 중인 백엔드 프로세스(PID 41334)의 시작 시각이 18:57:52로, 등록(18:57:39)과 재조회(18:57:59) 사이에 새로 시작된 프로세스임 (AI가 `ps -o lstart`로 확인)
- 화면: http://127.0.0.1:4173 새로고침 후 목록 6개에 "재시작 전에 쓴 리뷰"(오후 6:57:39) 표시
- 참고: 3단계의 "서버 꺼짐 확인" 줄은 `T15_restart.txt`에 기록되지 않음(실행 생략 또는 미기록). 16번 "재시작 전에 쓴 리뷰"(18:56:37)는 2단계를 한 번 더 실행해 생긴 같은 제목의 글
- 증거: `evidence/T15_restart.txt`, `T15_screen_after_restart.png`

### T16
- 조건: Alice 로그인 상태에서 ⌘R 새로고침 (2026-10-27, 4173 빌드본)
- 새로고침 전: "Alice님 로그인 중", 리뷰 쓰기 폼 표시, 목록 6개 중 Alice 글 5개에 "내 리뷰" 표시
- 새로고침 후: 로그인 카드가 로그인 폼으로, 리뷰 쓰기는 "리뷰를 작성하려면 먼저 로그인하세요."로 바뀜, "내 리뷰" 표시 사라짐. 목록은 같은 6개 그대로
- 의미: 게시글은 SQLite 파일 DB에 있어 유지되고, 토큰은 탭 메모리(`api.js`의 `session`)에만 있어 새로고침 시 사라짐
- 참고: 새로고침 후 이메일 칸에 보이는 `alice@class.test`는 입력값이 아니라 placeholder(예시 문구, `App.vue`의 `placeholder="alice@class.test"`). 같은 캡처의 placeholder "예) 치이카와가 용감해졌어요"와 같은 회색이고, 실제 값이 남아 있던 T10 캡처의 이메일(검정)과 색이 다름 → 이메일·비밀번호 입력칸 모두 비어 있는 초기 상태
- 증거: `evidence/T16_before_refresh.png`, `T16_after_refresh.png`

### T17
- 조건: 백엔드는 학생 터미널에서 실행 중, curl 직접 요청 (2026-10-27)
- 토큰 없는 변경: PUT /api/posts/10, DELETE /api/posts/10 (Bob 글, Authorization 없음) → 둘 다 `HTTP/1.1 401`, `WWW-Authenticate: Bearer`, `TOKEN_REQUIRED`. 10번 원문 재조회 결과 변경 없음
- Bob 본인 글 (Bob 정상 토큰):
  - POST /api/posts → `201`, `Location: /api/posts/18`, authorId 2, authorName Bob
  - PUT /api/posts/18 → `200`, title·content 변경, authorId 2와 createdAt `10:04:15.100217Z` 유지, updatedAt만 `10:04:15.132722Z`로 변경
  - DELETE /api/posts/18 → `204`, 본문 없음
  - GET /api/posts/18 → `404 NOT_FOUND` (실제 삭제 확인)
- 의미: 권한 검사가 특정 사용자(Alice)만 통과시키는 것이 아니라 "작성자 본인"이면 누구든 통과시킴
- 증거: `evidence/T17_curl.txt`

## 새 폴더 clone 재현 확인

- 조건: GitHub 공개 저장소를 새 폴더 `~/Desktop/clone-test`에 clone하고 루트 README의 명령만 따라 실행 (2026-10-27)
- 백엔드: `env.example.txt`에서 새 키로 `.env` 생성 → `./mvnw -q -DskipTests package` → `java -jar target/jwt-board-1.0.0.jar`. clone 폴더 경로에서 `Tomcat started on port 8000`, `Started BoardApplication` 확인. `data/board.sqlite`가 자동 생성됨
- 프론트: `npm ci` → `npm run build` → `npm run preview`. `✓ built in 99ms`, `Local: http://127.0.0.1:4173/` 확인
- 화면: 새 DB라 빈 목록에서 시작 → Alice 로그인 → 리뷰 "클론 테스트에용" 등록 → POST http://127.0.0.1:4173/api/posts `201 Created`, `Location: /api/posts/1`(새 DB의 첫 글), 요청에 `Authorization: Bearer <TOKEN>`(가림). 목록 1개, "내 리뷰"와 수정·삭제 버튼 표시
- 참고: 터미널 캡처 2장은 화면 확인(19:44) 뒤 같은 clone 폴더에서 다시 실행(19:47)한 화면이라 첫 실행의 `npm ci` 출력은 포함되지 않음
- 증거: `evidence/CLONE_backend_started.png`, `CLONE_frontend_build_preview.png`, `CLONE_screen_working.png`
