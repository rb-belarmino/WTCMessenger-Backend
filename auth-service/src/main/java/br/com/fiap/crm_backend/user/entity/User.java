package br.com.fiap.crm_backend.user.entity;

import br.com.fiap.crm_backend.user.enums.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @Field("role")
    private UserRole role;

    @Field("full_name")
    private String fullName;

    @Field("is_active")
    @Builder.Default
    private boolean active = true;

    @Field("created_at")
    private LocalDateTime createdAt;
}