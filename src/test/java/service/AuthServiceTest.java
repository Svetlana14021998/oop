package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Тестирование сервиса аутентификации")
class AuthServiceTest {

    private AuthService authService;
    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        authService = AuthService.getInstance();
        users = new HashMap<>();
    }

    @Test
    @DisplayName("Регистрация: успешное создание нового пользователя")
    void checkRegisterUserSuccessTest() {
        // given
        Scanner scanner = new Scanner("ivan\nsecret123\n");

        // when
        User registeredUser = authService.registerUser(users, scanner);

        // then
        assertNotNull(registeredUser, "Пользователь должен быть создан");
        assertEquals("ivan", registeredUser.getLogin());
        assertTrue(users.containsKey("ivan"), "Пользователь должен быть добавлен в Map");
        assertNotEquals(
                "secret123",
                registeredUser.getPassword(),
                "Пароль не должен храниться в открытом виде");
        assertTrue(
                registeredUser.getPassword().startsWith("$2a$"), "Пароль должен быть BCrypt-хешем");
    }

    @Test
    @DisplayName("Регистрация: отмена на этапе ввода логина (команда exit)")
    void checkRegisterUserCancelAtLoginTest() {
        // given
        Scanner scanner = new Scanner("exit\n");

        // when
        User registeredUser = authService.registerUser(users, scanner);

        // then
        assertNull(registeredUser, "Должен вернуться null при отмене");
        assertTrue(users.isEmpty(), "Список пользователей должен остаться пустым");
    }

    @Test
    @DisplayName("Регистрация: отмена на этапе ввода пароля (команда exit)")
    void checkRegisterUserCancelAtPasswordTest() {
        // given
        Scanner scanner = new Scanner("ivan\nexit\n");

        // when
        User registeredUser = authService.registerUser(users, scanner);

        // then
        assertNull(registeredUser, "Должен вернуться null при отмене");
        assertFalse(users.containsKey("ivan"), "Пользователь не должен быть создан");
    }

    @Test
    @DisplayName("Регистрация: логин уже занят (ввод повторяется)")
    void checkRegisterUserLoginAlreadyExistsTest() {
        // given
        users.put("ivan", new User("ivan", "hash"));
        Scanner scanner = new Scanner("ivan\nmaria\nsecret123\n");

        // when
        User registeredUser = authService.registerUser(users, scanner);

        // then
        assertNotNull(registeredUser);
        assertEquals("maria", registeredUser.getLogin(), "Должен зарегистрироваться новый логин");
        assertEquals(2, users.size(), "В Map должно быть 2 пользователя");
    }

    @Test
    @DisplayName("Регистрация: пустой логин (запрос повторяется)")
    void checkRegisterUserEmptyLoginTest() {
        // given
        Scanner scanner = new Scanner("\n   \nivan\nsecret123\n");

        // when
        User registeredUser = authService.registerUser(users, scanner);

        // then
        assertNotNull(registeredUser);
        assertEquals(
                "ivan", registeredUser.getLogin(), "Должен пропустить пустые вводы и создать ivan");
    }

    @Test
    @DisplayName("Вход: успешная авторизация с верным паролем")
    void checkLoginSuccessTest() {
        // given
        Scanner regScanner = new Scanner("ivan\nsecret123\n");
        authService.registerUser(users, regScanner);

        // when
        Scanner loginScanner = new Scanner("ivan\nsecret123\n");
        User loggedInUser = authService.login(users, loginScanner);

        // then
        assertNotNull(loggedInUser, "Вход должен быть успешным");
        assertEquals("ivan", loggedInUser.getLogin());
    }

    @Test
    @DisplayName("Вход: отмена на этапе ввода логина (команда exit)")
    void checkLoginCancelAtLoginTest() {
        // given
        Scanner scanner = new Scanner("exit\n");

        // when
        User loggedInUser = authService.login(users, scanner);

        // then
        assertNull(loggedInUser, "Должен вернуться null при отмене");
    }

    @Test
    @DisplayName("Вход: несуществующий пользователь (запрос повторяется)")
    void checkLoginUserNotFoundTest() {
        // given
        Scanner regScanner = new Scanner("ivan\nsecret123\n");
        authService.registerUser(users, regScanner);

        // when
        Scanner loginScanner = new Scanner("ghost\nivan\nsecret123\n");
        User loggedInUser = authService.login(users, loginScanner);

        // then
        assertNotNull(loggedInUser);
        assertEquals(
                "ivan", loggedInUser.getLogin(), "Должен войти под ivan после неверной попытки");
    }

    @Test
    @DisplayName("Вход: неверный пароль (запрос повторяется, потом успех)")
    void checkLoginWrongPasswordThenSuccessTest() {
        // given
        Scanner regScanner = new Scanner("ivan\nsecret123\n");
        authService.registerUser(users, regScanner);

        // when
        Scanner loginScanner = new Scanner("ivan\nwrong\nsecret123\n");
        User loggedInUser = authService.login(users, loginScanner);

        // then
        assertNotNull(loggedInUser, "После неверного пароля должен пройти со второй попытки");
    }

    @Test
    @DisplayName("Вход: отмена на этапе ввода пароля (команда exit)")
    void checkLoginCancelAtPasswordTest() {
        // given
        Scanner regScanner = new Scanner("ivan\nsecret123\n");
        authService.registerUser(users, regScanner);

        // when
        Scanner loginScanner = new Scanner("ivan\nexit\n");
        User loggedInUser = authService.login(users, loginScanner);

        // then
        assertNull(loggedInUser, "Должен вернуться null при отмене");
    }

    @Test
    @DisplayName("Вход: неверный пароль и отмена (команда exit)")
    void checkLoginWrongPasswordThenCancelTest() {
        // given
        Scanner regScanner = new Scanner("ivan\nsecret123\n");
        authService.registerUser(users, regScanner);

        // when
        Scanner loginScanner = new Scanner("ivan\nwrong\nexit\n");
        User loggedInUser = authService.login(users, loginScanner);

        // then
        assertNull(loggedInUser, "При вводе exit после неверного пароля должен вернуться null");
    }
}
