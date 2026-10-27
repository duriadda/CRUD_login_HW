# 백엔드 (Spring Boot)

설치·환경설정·실행 방법은 저장소 루트의 [README.md](../README.md) "2 백엔드 준비와 실행"을 따릅니다.

- 이 폴더(`backend`)에서 실행해야 `.env`와 `data/board.sqlite`를 찾습니다.
- `.env`는 `env.example.txt`를 복사해 만들고, `JWT_SECRET`은 새로 생성한 64자리 16진수로 바꿉니다. 실제 `.env`는 커밋하지 않습니다.
- JWT 발급·검증·작성자 권한은 `src/main/java/edu/classroom/board/AuthService.java`에 구현했습니다.
- 그 밖의 파일 역할: `BoardController`(라우트·입력 검증), `Errors`(공통 JSON 오류), `Users`(테스트 계정·PBKDF2 비밀번호 검증), `PostStore`(SQLite 쿼리), `DatabaseConfig`(DB 폴더 생성·연결)
