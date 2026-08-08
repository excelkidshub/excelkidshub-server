package in.excelkidshub.platform.user.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Role entity representing user roles in the system.
 * Examples: ADMIN, TEACHER, PARENT, STUDENT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_roles_name", columnList = "name"),
    @Index(name = "idx_roles_active", columnList = "active")
})
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;
}
