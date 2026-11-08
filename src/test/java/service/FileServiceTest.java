package service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import model.Category;
import model.Operation;
import model.OperationType;
import model.Wallet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Тестирование сервиса для работы с файлами")
class FileServiceTest {

    private final FileService fileService = FileService.getInstance();
    private final String testLogin = "integration_test_user";
    private final String testFileName = "wallet_" + testLogin + ".json";

    @AfterEach
    void tearDown() {
        File file = new File(testFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Сохранение и загрузка кошелька в JSON")
    void checkSaveAndLoadWalletTest() {
        // given
        Wallet originalWallet = new Wallet();
        originalWallet
                .getCategories()
                .put("Еда", new Category("Еда", OperationType.EXPENSE, 5000.0));
        originalWallet.getIncomes().add(new Operation("Зарплата", 60000));
        originalWallet.getExpenses().add(new Operation("Еда", 300));

        // when
        fileService.saveWallet(testLogin, originalWallet);

        // then
        File file = new File(testFileName);
        assertTrue(file.exists(), "Файл JSON должен быть создан на диске");

        // when
        Wallet loadedWallet = fileService.loadWallet(testLogin);

        // then
        assertNotNull(loadedWallet);
        assertEquals(60000, loadedWallet.getAllIncomesSum(), "Сумма доходов должна совпадать");
        assertEquals(300, loadedWallet.getAllExpensesSum(), "Сумма расходов должна совпадать");
        assertTrue(loadedWallet.isCategoryExists("Еда"), "Категория должна восстановиться");
        assertEquals(5000.0, loadedWallet.getCategoryBudget("Еда"), "Бюджет должен восстановиться");
    }

    @Test
    @DisplayName("Загрузка несуществующего кошелька возвращает пустой объект")
    void checkLoadNonExistentWalletReturnsEmptyTest() {
        // given
        // when
        Wallet loadedWallet = fileService.loadWallet("user_that_does_not_exist");

        // then
        assertNotNull(loadedWallet, "Метод не должен возвращать null");
        assertTrue(loadedWallet.getIncomes().isEmpty(), "Список доходов должен быть пустым");
        assertTrue(loadedWallet.getExpenses().isEmpty(), "Список расходов должен быть пустым");
    }
}
