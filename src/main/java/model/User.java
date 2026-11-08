package model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class User {
    private String login;
    private String password;
    @Setter private transient Wallet wallet;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
