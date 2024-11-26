package boostech.code.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Long expirationTime;
    private String status = "success";


    public JwtResponse(String token, Long id, String username, String email, List<String> roles, Long expirationTime) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.expirationTime = expirationTime;
    }

    // Constructor r?ng
    public JwtResponse() {
    }
}
