package service;

import java.util.Map;
import java.util.Scanner;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

/** Класс для создания пользователя и аутентификации */
public class AuthService {

    private static final String EXIT_COMMAND = "exit";
    private static final int BCRYPT_LOG_ROUNDS = 12;
    private static final String INCORRECT_DATA =
            "Неверный логин или пароль. Если хотите выйти, введите команду %s%n";
    private static final String INPUT_LOGIN =
            "Введите логин или команду %s для выхода в главное меню%n";
    private static final String INPUT_PASSWORD =
            "Введите пароль или команду %s для выхода в главное меню%n";
    private static final String NOT_NULL_VALUE = "%s не может быть пустым%n";
    private static final String LOGIN_ALREADY_EXISTS =
            "Логин занят. Введите другой или %s для выхода в главное меню%n";
    private static final String AUTH_SUCCESS = "Вы успешно вошли в систему, %s%n";

    private static final AuthService INSTANCE = new AuthService();

    private AuthService() {}

    public static AuthService getInstance() {
        return INSTANCE;
    }

    /**
     * Регистрация пользователя: 1.Вводится логин, проверяется, что он не занят. 2.Вводится пароль,
     * хешируется, после чего создается пользователь и сохраняется в Map. Есть возможность прервать
     * регистрацию и вернуться в главное меню с помощью команды {@link #EXIT_COMMAND}
     *
     * @param users список загруженных из файла пользователей
     * @param scanner для чтения из консоли
     * @return созданный пользователь
     */
    public User registerUser(Map<String, User> users, Scanner scanner) {
        System.out.printf(INPUT_LOGIN, EXIT_COMMAND);
        String login = scanner.nextLine();
        while (login.equalsIgnoreCase(EXIT_COMMAND)
                || users.containsKey(login)
                || login.trim().isEmpty()) {
            if (login.equalsIgnoreCase(EXIT_COMMAND)) {
                return null;
            }
            if (login.trim().isEmpty()) {
                System.out.printf(NOT_NULL_VALUE, "Логин");
            } else {
                System.out.printf(LOGIN_ALREADY_EXISTS, EXIT_COMMAND);
            }

            login = scanner.nextLine();
        }
        System.out.printf(INPUT_PASSWORD, EXIT_COMMAND);
        String password = scanner.nextLine();
        if (password.equalsIgnoreCase(EXIT_COMMAND)) {
            return null;
        }
        while (password.trim().isEmpty()) {
            System.out.printf(NOT_NULL_VALUE, "Пароль");
            password = scanner.nextLine();
            if (password.equalsIgnoreCase(EXIT_COMMAND)) {
                return null;
            }
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
        User newUser = new User(login, hashedPassword);
        users.put(login, newUser);
        return newUser;
    }

    /**
     * Аутентификация пользователя: 1.Вводится логин и проверяется, что он есть в списке
     * пользователей. 2.Вводится пароль и проверяется с учетом хеширования Есть возможность прервать
     * вход и вернуться в главное меню с помощью команды {@link #EXIT_COMMAND}
     *
     * @param users список загруженных из файла пользователей
     * @param scanner для чтения из консоли
     * @return аутентифицированный пользователь
     */
    public User login(Map<String, User> users, Scanner scanner) {
        System.out.printf(INPUT_LOGIN, EXIT_COMMAND);
        String login = scanner.nextLine();
        while (!users.containsKey(login)) {
            if (login.equalsIgnoreCase(EXIT_COMMAND)) {
                return null;
            }
            System.out.printf(INCORRECT_DATA, EXIT_COMMAND);
            login = scanner.nextLine();
        }

        System.out.printf(INPUT_PASSWORD, EXIT_COMMAND);
        String enteredPassword = scanner.nextLine();
        User user = users.get(login);
        String password = user.getPassword();
        while (!matches(enteredPassword, password)) {
            if (enteredPassword.equalsIgnoreCase(EXIT_COMMAND)) {
                return null;
            }
            System.out.printf(INCORRECT_DATA, EXIT_COMMAND);
            enteredPassword = scanner.nextLine();
        }
        System.out.printf(AUTH_SUCCESS, login);
        return user;
    }

    /**
     * Проверяет, совпадает ли введённый пароль с сохранённым хешем. Повреждённый или некорректный
     * хеш считается несовпадением.
     */
    private boolean matches(String enteredPassword, String storedHash) {
        if (storedHash == null || enteredPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(enteredPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
