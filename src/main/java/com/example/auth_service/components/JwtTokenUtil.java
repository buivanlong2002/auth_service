package com.example.auth_service.components;


import com.example.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
@Component
@RequiredArgsConstructor // Tự động tạo constructor cho các field final hoặc @NonNull
public class JwtTokenUtil {

    @Value("${jwt.expiration:3600}") // Lấy giá trị thời gian hết hạn token từ file cấu hình, mặc định 3600 giây
    private int expiration;

    @Value("${jwt.secretKey}") // Lấy secret key từ file cấu hình
    private String secretKey;

    /**
     * Tạo JWT token dựa trên thông tin người dùng.
     *
     * @param user Đối tượng người dùng
     * @return Chuỗi JWT token
     * @throws Exception Nếu có lỗi khi tạo token
     */
    public String generateToken(User user) throws Exception {
        Map<String, Object> claims = new HashMap<>(); // Khởi tạo claims để lưu thông tin bổ sung
        claims.put("Email", user.getEmail()); // Thêm thông tin số điện thoại vào claims

        try {
            return Jwts.builder()
                    .setClaims(claims) // Gán claims vào token
                    .setSubject(user.getEmail()) // Thiết lập subject (ở đây là số điện thoại)
                    .setIssuedAt(new Date()) // Thời gian phát hành token
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L)) // Thiết lập thời gian hết hạn
                    .signWith(getPrivateKey(), SignatureAlgorithm.HS256) // Ký token bằng private key với thuật toán HS256
                    .compact(); // Tạo token
        } catch (Exception e) {
            throw new Exception("Error generating token: " + e.getMessage(), e); // Ném ngoại lệ nếu có lỗi
        }
    }

    /**
     * Lấy private key từ secret key đã được mã hóa base64.
     *
     * @return Đối tượng Key để ký hoặc xác thực JWT
     */
    private Key getPrivateKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Giải mã secret key từ base64
        return Keys.hmacShaKeyFor(keyBytes); // Tạo HMAC key từ mảng byte
    }

    /**
     * Trích xuất claims (payload) từ JWT token.
     *
     * @param token Chuỗi JWT token
     * @return Đối tượng Claims chứa thông tin payload
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPrivateKey()) // Sử dụng private key để xác thực token
                .build()
                .parseClaimsJws(token) // Phân tích và xác thực token
                .getBody(); // Lấy phần payload (claims)
    }

    /**
     * Trích xuất một giá trị cụ thể từ claims của token.
     *
     * @param token          JWT token
     * @param claimsResolver Hàm lấy giá trị cụ thể từ claims
     * @param <T>            Kiểu dữ liệu của giá trị cần trích xuất
     * @return Giá trị đã trích xuất
     */
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token); // Lấy toàn bộ claims
        return claimsResolver.apply(claims); // Lấy giá trị cụ thể từ claims
    }

    /**
     * Kiểm tra xem token có bị hết hạn hay không.
     *
     * @param token JWT token
     * @return true nếu token đã hết hạn, ngược lại false
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token, Claims::getExpiration); // Lấy thời gian hết hạn từ claims
        return expiration.before(new Date()); // So sánh thời gian hết hạn với thời gian hiện tại
    }

    /**
     * Trích xuất username (ở đây là số điện thoại) từ token.
     *
     * @param token JWT token
     * @return Username (số điện thoại)
     */
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject); // Lấy subject từ claims
    }

    /**
     * Xác thực token với thông tin người dùng.
     *
     * @param token       JWT token
     * @param userDetails Thông tin chi tiết người dùng
     * @return true nếu token hợp lệ, ngược lại false
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String phoneNumber = extractUsername(token); // Lấy username (số điện thoại) từ token
        return phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired(token); // Kiểm tra username và thời hạn token
    }

    /**
     * Tạo một secret key ngẫu nhiên để sử dụng nếu cần.
     *
     * @return Chuỗi secret key đã được mã hóa base64
     */
    private String generateSecretKey() {
        SecureRandom random = new SecureRandom(); // Tạo đối tượng random an toàn
        byte[] keyBytes = new byte[32]; // Tạo mảng byte 32 phần tử (256 bit)
        random.nextBytes(keyBytes); // Sinh ngẫu nhiên các byte
        return Encoders.BASE64.encode(keyBytes); // Mã hóa base64 và trả về
    }
}
