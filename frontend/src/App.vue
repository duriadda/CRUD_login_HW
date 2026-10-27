<script setup>
import { ref, onMounted } from "vue";
import { api, login, getSession, clearSession, canEdit } from "./api";
const posts = ref([]),
  selected = ref(null),
  title = ref(""),
  content = ref(""),
  editing = ref(null),
  confirmingDelete = ref(false);
const email = ref(""),
  password = ref(""),
  user = ref(null),
  busy = ref(false),
  message = ref("리뷰를 불러오는 중입니다."),
  tone = ref("info");

// FE-03: 서버가 보낸 JSON code에 맞춰 안내 문구를 고른다.
const codeMessages = {
  INVALID_CREDENTIALS: "이메일 또는 비밀번호가 올바르지 않습니다.",
  TOKEN_REQUIRED: "로그인이 필요합니다. 로그인한 뒤 다시 시도하세요.",
  INVALID_TOKEN:
    "로그인 정보가 유효하지 않아 로그아웃되었습니다. 다시 로그인하세요.",
  TOKEN_EXPIRED: "로그인 시간이 만료되어 로그아웃되었습니다. 다시 로그인하세요.",
  FORBIDDEN: "본인이 작성한 리뷰만 수정·삭제할 수 있습니다.",
  NOT_FOUND: "해당 리뷰가 없습니다. 이미 삭제되었을 수 있어 목록을 새로 불러왔습니다.",
};
function describe(e) {
  if (e.code === "BAD_INPUT") return `입력을 확인하세요. (${e.message})`;
  return codeMessages[e.code] || `요청을 처리하지 못했습니다. (${e.message})`;
}
function notify(text, kind) {
  message.value = text;
  tone.value = kind;
}

const refresh = async () => {
  posts.value = await api.list();
};
async function run(action, success) {
  busy.value = true;
  try {
    await action();
    if (success) notify(success, "success");
  } catch (e) {
    // 실패한 요청은 성공 문구를 띄우지 않고, 400이면 입력값을 그대로 둔다.
    notify(describe(e), "error");
    if (e.status === 404) {
      selected.value = null;
      reset();
      await refresh().catch(() => {});
    }
  } finally {
    // 401이면 api.js가 세션을 지웠으므로 화면 로그인 표시도 함께 해제된다.
    user.value = getSession().user;
    if (!user.value) reset();
    confirmingDelete.value = false;
    busy.value = false;
  }
}
onMounted(() => run(refresh, "리뷰 게시판이 준비되었습니다."));

function signIn() {
  run(async () => {
    const u = await login(email.value, password.value);
    password.value = "";
    notify(`${u.name}님, 환영합니다!`, "success");
  });
}
function fill(name) {
  email.value = `${name}@class.test`;
  password.value = "Classroom!2026";
}
function reset() {
  title.value = "";
  content.value = "";
  editing.value = null;
}
function save() {
  const wasEditing = editing.value;
  run(
    async () => {
      const input = { title: title.value, content: content.value };
      selected.value = wasEditing
        ? await api.update(wasEditing, input)
        : await api.create(input);
      reset();
      await refresh();
    },
    wasEditing ? "리뷰를 수정했습니다." : "리뷰를 등록했습니다.",
  );
}
function open(id) {
  confirmingDelete.value = false;
  run(async () => (selected.value = await api.detail(id)));
}
function edit() {
  editing.value = selected.value.id;
  title.value = selected.value.title;
  content.value = selected.value.content;
}
function remove() {
  run(async () => {
    await api.remove(selected.value.id);
    selected.value = null;
    reset();
    await refresh();
  }, "리뷰를 삭제했습니다.");
}
function logout() {
  clearSession();
  user.value = null;
  reset();
  notify("이 탭의 로그인 상태를 해제했습니다.", "info");
}
function me() {
  run(async () => {
    const u = await api.me();
    notify(`내 정보: ${u.name} (${u.email}) · 회원 번호 ${u.id}`, "success");
  });
}
const when = (iso) => new Date(iso).toLocaleString();
</script>
<template>
  <main>
    <header>
      <span class="eyebrow">MOVIE REVIEW BOARD</span>
      <h1>치이카와 인어섬의 비밀</h1>
      <p class="muted">극장판을 보고 난 감상을 자유롭게 나눠 주세요.</p>
    </header>
    <p class="banner">
      리뷰는 로그인 없이 누구나 읽을 수 있습니다. 리뷰 작성은 로그인한 뒤에
      가능하며, 수정·삭제는 본인이 쓴 리뷰만 할 수 있습니다. 로그인은 이 탭에서만
      유지되며 새로고침하면 해제됩니다.
    </p>
    <div role="status" class="message" :class="tone">{{ message }}</div>
    <div class="grid">
      <aside>
        <section class="card">
          <h2>로그인</h2>
          <template v-if="user">
            <p class="who">
              <strong>{{ user.name }}</strong>님 로그인 중<br /><small>{{
                user.email
              }}</small>
            </p>
            <div class="actions">
              <button class="secondary" :disabled="busy" @click="me">
                내 정보 조회
              </button>
              <button class="secondary" @click="logout">로그아웃</button>
            </div>
          </template>
          <form v-else @submit.prevent="signIn">
            <label
              >이메일<input
                type="email"
                v-model="email"
                placeholder="alice@class.test"
                required
            /></label>
            <label
              >비밀번호<input type="password" v-model="password" required
            /></label>
            <div class="actions">
              <button :disabled="busy">로그인</button
              ><button
                type="button"
                class="secondary"
                :disabled="busy"
                @click="me"
              >
                내 정보 조회
              </button>
            </div>
            <p>
              <small>테스트 계정 (공통 비밀번호: Classroom!2026)</small><br />
              <button type="button" class="chip" @click="fill('alice')">
                Alice로 채우기</button
              ><button type="button" class="chip" @click="fill('bob')">
                Bob으로 채우기
              </button>
            </p>
          </form>
        </section>
        <section class="card">
          <h2>{{ editing ? "리뷰 수정" : "리뷰 쓰기" }}</h2>
          <form v-if="user" @submit.prevent="save">
            <label
              >한줄평<input
                maxlength="100"
                v-model="title"
                placeholder="예) 치이카와가 용감해졌어요"
                required
              /><small class="count">{{ title.length }} / 100</small></label
            ><label
              >리뷰 내용<textarea maxlength="5000" v-model="content" required />
              <small class="count">{{ content.length }} / 5000</small>
            </label>
            <div class="actions">
              <button :disabled="busy">
                {{ editing ? "수정 저장" : "등록" }}</button
              ><button type="button" class="secondary" @click="reset">
                {{ editing ? "수정 취소" : "입력 초기화" }}
              </button>
            </div>
          </form>
          <p v-else class="login-guide">
            리뷰를 작성하려면 먼저 로그인하세요.
          </p>
        </section>
      </aside>
      <div>
        <section class="card">
          <h2>
            관람 리뷰 <small>{{ posts.length }}개</small>
          </h2>
          <p v-if="!posts.length">아직 리뷰가 없어요. 첫 리뷰를 남겨 보세요.</p>
          <article
            class="post"
            :class="{ active: selected?.id === p.id }"
            v-for="p in posts"
            :key="p.id"
          >
            <button class="post-title" :disabled="busy" @click="open(p.id)">
              {{ p.title }}
            </button>
            <p>
              <small
                >{{ p.authorName }} · {{ when(p.createdAt) }}
                <span v-if="user && user.id === p.authorId" class="mine"
                  >내 리뷰</span
                ></small
              >
            </p>
          </article>
        </section>
        <section class="card">
          <h2>리뷰 상세</h2>
          <template v-if="selected"
            ><h3>{{ selected.title }}</h3>
            <p class="content">{{ selected.content }}</p>
            <p>
              <small
                >작성자 {{ selected.authorName }} · 작성
                {{ when(selected.createdAt) }}
                <template v-if="selected.updatedAt !== selected.createdAt">
                  · 수정 {{ when(selected.updatedAt) }}</template
                ></small
              >
            </p>
            <div class="actions" v-if="canEdit(selected)">
              <template v-if="!confirmingDelete">
                <button class="secondary" :disabled="busy" @click="edit">
                  수정</button
                ><button
                  class="danger"
                  :disabled="busy"
                  @click="confirmingDelete = true"
                >
                  삭제
                </button>
              </template>
              <template v-else>
                <span class="confirm">이 리뷰를 삭제할까요?</span>
                <button class="danger" :disabled="busy" @click="remove">
                  삭제 확인</button
                ><button class="secondary" @click="confirmingDelete = false">
                  취소
                </button>
              </template>
            </div></template
          >
          <p class="muted" v-else>목록에서 리뷰를 선택하세요.</p>
        </section>
      </div>
    </div>
  </main>
</template>
