package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import model.User;
import model.Wallet;

public class FileService {

    private static final String USERS_FILE = "users.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final FileService INSTANCE = new FileService();

    private FileService() {}

    public static FileService getInstance() {
        return INSTANCE;
    }

    /**
     * Загружает всех пользователей из файла. Если файла нет — создает пустой файл и возвращает
     * пустую Map.
     *
     * @return все пользователи
     */
    public Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        Map<String, User> users = new HashMap<>();

        if (!file.exists()) {
            System.out.println("Файл пользователей не найден. Создаем новый...");
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write("{}");
            } catch (IOException e) {
                System.err.println("Ошибка при создании файла: " + e.getMessage());
            }
            return users;
        }

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, User>>() {}.getType();
            users = GSON.fromJson(reader, type);

            if (users == null) {
                users = new HashMap<>();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Файл пользователей недоступен", e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Не удалось разобрать users.json", e);
        }
        return users;
    }

    /**
     * Сохраняет всех пользователей в файл (перезаписывает).
     *
     * @param users список пользователей
     */
    public void saveUsers(Map<String, User> users) {
        try (Writer writer = new FileWriter(USERS_FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(users, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при сохранении пользователя", e);
        }
    }

    /**
     * Загружает кошелек конкретного пользователя. Если файла нет (новый пользователь) — возвращает
     * новый пустой кошелек.
     *
     * @param login логин пользователя
     * @return объект model.Wallet
     */
    public Wallet loadWallet(String login) {
        String fileName = "wallet_" + login + ".json";
        File file = new File(fileName);

        if (!file.exists()) {
            return new Wallet();
        }
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Wallet wallet = GSON.fromJson(reader, Wallet.class);
            if (wallet == null) {
                return new Wallet();
            }
            return wallet;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать файл кошелька для " + login, e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Не удалось разобрать JSON кошелька для " + login, e);
        }
    }

    /**
     * Сохраняет кошелек пользователя в отдельный файл.
     *
     * @param login логин пользователя
     * @param wallet объект кошелька
     */
    public void saveWallet(String login, Wallet wallet) {
        String fileName = "wallet_" + login + ".json";
        try (Writer writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
            GSON.toJson(wallet, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Не удалось сохранить кошелек для пользователя " + login, e);
        }
    }
}
