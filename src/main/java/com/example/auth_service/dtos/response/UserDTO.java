package com.example.auth_service.dtos.response;

import com.example.auth_service.model.Address;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private List<Address> addressList;
    private String phone;
    private long role;
}
