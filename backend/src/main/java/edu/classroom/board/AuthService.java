package edu.classroom.board;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private static final String EXAMPLE_SECRET = "REPLACE_WITH_RANDOM_64_HEX_CHARACTERS";

  private final Users users;
  private final SecretKey key;
  private final String issuer;
  private final String audience;
  private final long ttl;
  private final JwtParser parser;

  public AuthService(
      Users users,
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.audience}") String audience,
      @Value("${app.jwt.ttl}") long ttl) {
    // 서버 시작 시 키를 검사한다. 빈 값·예시 키·64자리 16진수가 아닌 값이면 서버가 뜨지 않는다.
    if (secret == null || secret.isBlank() || secret.equals(EXAMPLE_SECRET))
      throw new IllegalStateException("JWT_SECRET 환경변수에 새로 생성한 키를 설정하세요.");
    if (!secret.matches("[0-9a-fA-F]{64}"))
      throw new IllegalStateException("JWT_SECRET은 64자리 16진수(32바이트)여야 합니다.");
    if (ttl <= 0) throw new IllegalStateException("TOKEN_TTL은 1 이상의 초 단위 정수여야 합니다.");

    this.users = users;
    this.key = Keys.hmacShaKeyFor(HexFormat.of().parseHex(secret)); // 16진수 → 32바이트 키
    this.issuer = issuer;
    this.audience = audience;
    this.ttl = ttl;
    // 서명 키·발급자·사용 대상을 고정한 검증기. 서명 없는 토큰(alg=none)은 JJWT가 기본으로 거부한다.
    this.parser =
        Jwts.parser().verifyWith(key).requireIssuer(issuer).requireAudience(audience).build();
  }

  // BE-01: 비밀번호 검증을 통과한 사용자에게만 호출된다. 토큰에는 공개 ID만 넣는다.
  public String issueToken(Users.PublicUser user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(user.id()))
        .issuer(issuer)
        .audience()
        .single(audience) // 배열이 아닌 단일 문자열 aud로 발급
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttl)))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  // BE-02: Authorization 헤더 → 서명·클레임 검증 → 실제 계정 확인을 거친 ID만 반환한다.
  public long currentUserId(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.matches("Bearer [^\\s]+"))
      throw new ApiError(401, "TOKEN_REQUIRED", "로그인이 필요합니다.");
    String token = header.substring("Bearer ".length());

    Claims claims;
    try {
      var jws = parser.parseSignedClaims(token);
      // 허용 알고리즘을 HS256 하나로 고정한다.
      if (!"HS256".equals(jws.getHeader().getAlgorithm()))
        throw new ApiError(401, "INVALID_TOKEN", "허용되지 않은 토큰 알고리즘입니다.");
      claims = jws.getPayload();
    } catch (ExpiredJwtException e) {
      // JJWT는 서명을 먼저 검증한 뒤 만료를 검사하므로, 여기 도달하면 서명은 정상이다.
      throw new ApiError(401, "TOKEN_EXPIRED", "로그인 시간이 만료되었습니다. 다시 로그인하세요.");
    } catch (JwtException | IllegalArgumentException e) {
      throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
    }

    // JJWT는 exp가 없어도 통과시키므로 존재 여부를 직접 확인한다.
    if (claims.getExpiration() == null)
      throw new ApiError(401, "INVALID_TOKEN", "만료 시각이 없는 토큰입니다.");
    String sub = claims.getSubject();
    if (sub == null || !sub.matches("[1-9][0-9]{0,15}"))
      throw new ApiError(401, "INVALID_TOKEN", "토큰의 사용자 정보가 올바르지 않습니다.");
    long id = Long.parseLong(sub);
    if (users.find(id) == null)
      throw new ApiError(401, "INVALID_TOKEN", "존재하지 않는 사용자입니다.");
    return id;
  }

  // BE-03: 저장된 글의 작성자와 검증된 현재 사용자가 다르면 변경을 막는다.
  public void assertOwner(long userId, long authorId) {
    if (userId != authorId)
      throw new ApiError(403, "FORBIDDEN", "본인이 작성한 글만 수정·삭제할 수 있습니다.");
  }
}
