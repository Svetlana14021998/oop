import controller.AppRunner;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Scanner;
import model.User;
import service.AuthService;
import service.FileService;
import ui.ConsoleUI;

public class Main {

    private static final String CANT_RUN_APP =
            "Приложение не может быть запущено в данный момент. Свяжитесь с поддержкой";

    public static void main(String[] args) {
        Map<String, User> users;
        FileService fileService = FileService.getInstance();
        try {
            users = fileService.loadUsers();
        } catch (UncheckedIOException | IllegalStateException e) {
            System.out.println(CANT_RUN_APP);
            return;
        }
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleUI ui = new ConsoleUI(scanner);
            AppRunner runner = new AppRunner(fileService, AuthService.getInstance(), ui, users);

            runner.run();
        }
    }
}
