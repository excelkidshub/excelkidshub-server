package in.excelkidshub.platform.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a user in API responses. Never exposes the password hash.
 */
@Data
@Builder
public class UserDto {

    private Long    id;
    private String  email;
    private String  firstName;
    private String  lastName;
    private String  name;          // computed: firstName + " " + lastName
    private String  phone;
    private String  role;
    private Boolean emailVerified;
}
