package com.example.auth_service.controller;

import com.example.auth_service.dtos.request.UserAddRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.dtos.response.UserResponse;
import com.example.auth_service.entity.User;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/users") // Đường dẫn chính
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private final Path imageLocation = Paths.get("uploads/");


    @GetMapping // GET /api/users
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody UserAddRequest userAddRequest) {
        return ResponseEntity.ok(userService.addUser(userAddRequest));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserAddRequest userAddRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userAddRequest));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserAvatar(
            @PathVariable("id") String userId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }


            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Max 10MB");
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("Only image files are allowed");
            }

            // Lưu file vào thư mục uploads/
            String fileName = userService.storeFile(file);

            // Tìm và cập nhật user
            User user = userRepository.getUserById(userId); // hàm lấy user theo id
            user.setAvatarUrl(fileName); // Cập nhật avatar URL
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user); // Lưu user

            return ResponseEntity.ok("Avatar updated successfully: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Lấy ảnh avatar người dùng
    @GetMapping("/avatar-file/{fileName}")
    public ResponseEntity<?> getAvatarByFileName(@PathVariable String fileName) {
        try {
            // Đường dẫn tới file ảnh
            Path imagePath = imageLocation.resolve(fileName);
            Resource imageResource = new UrlResource(imagePath.toUri());

            // Kiểm tra ảnh tồn tại
            if (!imageResource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
            }

            // Xác định loại MIME tự động
            String contentType = java.nio.file.Files.probeContentType(imagePath);
            MediaType mediaType = MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream");

            // Trả ảnh
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageResource.getFilename() + "\"")
                    .body(imageResource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.deleteUserById(id));
    }
}
