# 프론트엔드 (Vue 3 + Vite)

설치·빌드·실행 방법은 저장소 루트의 [README.md](../README.md) "3 프론트 빌드와 로컬 실행"을 따릅니다. 백엔드(127.0.0.1:8000)를 먼저 실행해 두세요.

- 로컬 빌드 확인: `npm ci` → `npm run build` → `npm run preview` → http://127.0.0.1:4173
- 개발 실행: `npm run dev` → http://127.0.0.1:5173
- `/api` 요청은 `vite.config.js`의 프록시가 8000번 백엔드로 전달합니다.
- 로그인 토큰 보관·전송과 응답 코드 처리는 `src/api.js`, 화면 상태는 `src/App.vue`에 있습니다.
