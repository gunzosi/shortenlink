package boostech.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse implements Serializable {
    private Long id;
    private String username;
    private String email;

    public UserResponse() {
    }
}
