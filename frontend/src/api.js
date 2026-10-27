// 로그인 상태는 이 탭의 메모리에만 둔다. localStorage·sessionStorage는 사용하지 않는다.
let session = { accessToken: "", user: null };
export const getSession = () => session;
export function clearSession() {
  session = { accessToken: "", user: null };
}
// FE-02: 토큰이 있을 때만 Bearer 헤더를 만든다. 없으면 헤더 자체를 보내지 않는다.
function authorizationHeaders() {
  return session.accessToken
    ? { Authorization: `Bearer ${session.accessToken}` }
    : {};
}
export async function request(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...authorizationHeaders(),
      ...options.headers,
    },
  });
  // 204는 본문이 없으므로 파싱하지 않는다. JSON이 아닌 오류 본문(프록시 오류 등)도 null로 둔다.
  const data =
    response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) {
    // FE-03: 401은 토큰이 더 이상 쓸 수 없다는 뜻이므로 메모리의 로그인 상태를 해제한다.
    // 403은 인증은 유효하고 권한만 없는 것이므로 로그인 상태를 유지한다.
    if (response.status === 401) clearSession();
    const error = new Error(
      data?.message || data?.code || `HTTP ${response.status}`,
    );
    error.status = response.status;
    error.code = data?.code;
    throw error;
  }
  return data;
}
// FE-01: 성공 응답의 토큰과 사용자를 메모리에 저장한다. 실패하면 request()가 예외를 던진다.
export async function login(email, password) {
  const data = await request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  session = { accessToken: data.accessToken, user: data.user };
  return data.user;
}
// FE-04: 화면 표시용 판단일 뿐이며, 실제 권한 검사는 서버의 assertOwner가 한다.
export function canEdit(post) {
  return Boolean(session.user && post && session.user.id === post.authorId);
}
export const api = {
  list: () => request("/posts"),
  detail: (id) => request(`/posts/${id}`),
  create: (input) =>
    request("/posts", { method: "POST", body: JSON.stringify(input) }),
  update: (id, input) =>
    request(`/posts/${id}`, { method: "PUT", body: JSON.stringify(input) }),
  remove: (id) => request(`/posts/${id}`, { method: "DELETE" }),
  me: () => request("/me"),
};
