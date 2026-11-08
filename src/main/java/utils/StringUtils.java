package utils;

/** Класс для различных методов для работы со строками */
public class StringUtils {

    /**
     * Преобразует исходную строку: слово начинается с большой буквы
     *
     * @param str строка
     * @return преобразованная строка
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
