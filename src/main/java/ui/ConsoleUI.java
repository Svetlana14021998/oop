package ui;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
/** Класс для чтения команд из консоли */
public class ConsoleUI {

    private final Scanner scanner;

    /**
     * Чтение чисел с запятой из консоли с предотвращением некорректного ввода.
     *
     * @param message отображение сообщения
     * @return введенное пользователем значение
     */
    public double readDouble(String message) {
        while (true) {
            System.out.println(message);
            String input = scanner.nextLine();
            Double value = parseDoubleUniversal(input);
            if (value != null && value > 0) {
                return value;
            }
            System.out.println("Ошибка: введите положительное число");
        }
    }

    /**
     * Считывает число с запятой из консоли с возможностью ввода null-значения
     *
     * @return число или null
     */
    public Double readDoubleOrEmpty() {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            Double value = parseDoubleUniversal(input);
            if (value != null && value > 0) {
                return value;
            }
            System.out.println(
                    "Ошибка: введите положительное число или нажмите Enter, если не хотите указывать сумму");
        }
    }

    /**
     * Считывает сообщение из консоли
     *
     * @param message сообщение для вывода пользователю
     * @return строка из консоли
     */
    public String readString(String message) {
        System.out.println(message);
        String str = scanner.nextLine().trim();
        while (str.isEmpty()) {
            System.out.println(message);
            str = scanner.nextLine().trim();
        }
        return str;
    }

    /**
     * Считывает команду из консоли до тех пор, пока не будет введена команда из списка разрешенных
     * команд
     *
     * @param message сообщение пользователю
     * @param availableCommand список доспупных команд
     * @return команда от пользователя
     */
    public String readCommand(String message, List<String> availableCommand) {
        System.out.println(message);
        String str = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        while (!availableCommand.contains(str)) {
            System.out.println(message);
            str = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        }
        return str;
    }

    /**
     * Пытается распарсить строку в число, принимая и точку, и запятую.
     *
     * @param input строка от пользователя
     * @return число или null, если распарсить не удалось
     */
    private Double parseDoubleUniversal(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String normalized = input.trim().replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
