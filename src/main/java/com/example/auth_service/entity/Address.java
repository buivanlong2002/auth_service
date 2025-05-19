package com.example.auth_service.entity;


import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "User không được để trống")
    private User user;

    @Column(name = "recipient_name", nullable = false)
    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String recipientName;

    @Column(name = "phone_number", nullable = false)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phoneNumber;

    @Column(name = "street", nullable = false)
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String street;

    @Column(name = "ward")
    @Size(max = 100, message = "Phường tối đa 100 ký tự")
    private String ward;

    @Column(name = "district")
    @Size(max = 100, message = "Quận tối đa 100 ký tự")
    private String district;

    @Column(name = "city")
    @Size(max = 100, message = "Thành phố tối đa 100 ký tự")
    private String city;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

}
