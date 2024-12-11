package quyet.learn.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "role")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany
    Set<Permission> permissions;
}
