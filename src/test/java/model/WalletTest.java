package model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Тестирование методов Wallet")
class WalletTest {
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.getCategories().put("Еда", new Category("Еда", OperationType.EXPENSE, 1000.0));
        wallet.getCategories()
                .put("Зарплата", new Category("Зарплата", OperationType.INCOME, null));
    }

    @Test
    @DisplayName("Проверка баланса после зачисления и списания")
    void calculateBalanceTest() {
        // given
        // when
        wallet.getIncomes().add(new Operation("Зарплата", 5000));
        wallet.getExpenses().add(new Operation("Еда", 2000));

        // then
        assertEquals(3000.0, wallet.getBalance(), "Баланс должен быть 3000");
    }

    @Test
    @DisplayName("Расходы превышают доходы")
    void checkExpensesMoreThanIncomesTest() {
        // given
        // when
        wallet.getIncomes().add(new Operation("Зарплата", 100));
        wallet.getExpenses().add(new Operation("Еда", 500));

        // then
        assertTrue(
                wallet.isExpensesMoreThanIncomes(),
                "Метод должен вернуть true, так как расходы больше доходов");
    }

    @Test
    @DisplayName("Расходы не превышают доходы")
    void checkExpensesIsNotMoreThanIncomesTest() {
        // given
        // when
        wallet.getIncomes().add(new Operation("Зарплата", 1000));
        wallet.getExpenses().add(new Operation("Еда", 500));

        // then
        assertFalse(
                wallet.isExpensesMoreThanIncomes(),
                "Метод должен вернуть false, так как расходы не больше доходов");
    }

    @Test
    @DisplayName("Превышен бюджет по категории")
    void checkExpensesMoreThanBudgetTest() {
        // given
        // when
        wallet.getExpenses().add(new Operation("Еда", 1200));

        // then
        assertTrue(
                wallet.isExpensesMoreThanBudget("Еда"),
                "Расходы превысили бюджет, должен вернуть true");
    }

    @Test
    @DisplayName("Не превышен бюджет по категории")
    void checkExpensesIsNotMoreThanBudgetTest() {
        // given
        // when
        wallet.getExpenses().add(new Operation("Еда", 100));

        // then
        assertFalse(
                wallet.isExpensesMoreThanBudget("Еда"),
                "Расходы не превысили бюджет, должен вернуть false");
    }

    @Test
    @DisplayName("Доход не задан. Проверка превышения расходов по категории")
    void checkExpensesIsNotMoreThanBudgetWhenBudgetIsNullTest() {
        // given
        wallet.getCategories().get("Еда").setBudget(null);

        // when
        wallet.getExpenses().add(new Operation("Еда", 100));

        // then
        assertFalse(
                wallet.isExpensesMoreThanBudget("Еда"), "Бюджет не задан, должен вернуть false");
    }

    @Test
    @DisplayName("Бюджет достиг порога 80%, но не превысил 100%")
    void checkBudgetAlmostExceededWhenItIs80PerCentTest() {
        // given
        // when
        wallet.getExpenses().add(new Operation("Еда", 800));

        // then
        assertTrue(
                wallet.isBudgetAlmostExceeded("Еда"), "Достигнут порог 80%, должен вернуть true");
    }

    @Test
    @DisplayName("Бюджет не задан. Проверка метода определения порога 80%")
    void checkBudgetAlmostExceededWhenBudgetIsNullTest() {
        // given
        wallet.getCategories()
                .put("Развлечения", new Category("Развлечения", OperationType.EXPENSE, null));

        // when
        wallet.getExpenses().add(new Operation("Развлечения", 800));

        // then
        assertFalse(
                wallet.isBudgetAlmostExceeded("Развлечения"),
                "Бюджет не задан, должен вернуть false");
    }

    @Test
    @DisplayName("Проверка изменения названия категории, в том числе и в операции")
    void checkRenameCategoryUpdatesOperationsTest() {
        // given
        wallet.getExpenses().add(new Operation("Еда", 500));

        // when
        wallet.renameCategory("Еда", "Продукты");

        // then
        assertFalse(wallet.isCategoryExists("Еда"));
        assertTrue(wallet.isCategoryExists("Продукты"));
        assertEquals(
                "Продукты",
                wallet.getExpenses().get(0).getCategoryName(),
                "Операция должна ссылаться на новое имя");
    }

    @Test
    @DisplayName("Получение категорий по типу: только доходы")
    void checkGetCategoriesReturnsOnlyIncomesTest() {
        // given
        // when
        List<String> incomes = wallet.getCategories(OperationType.INCOME);

        // then
        assertEquals(1, incomes.size());
        assertTrue(incomes.contains("Зарплата"));
        assertFalse(incomes.contains("Еда"));
    }

    @Test
    @DisplayName("Получение категорий по типу: только расходы")
    void checkGetCategoriesReturnsOnlyExpensesTest() {
        // given
        // when
        List<String> expenses = wallet.getCategories(OperationType.EXPENSE);

        // then
        assertEquals(1, expenses.size());
        assertTrue(expenses.contains("Еда"));
        assertFalse(expenses.contains("Зарплата"));
    }

    @Test
    @DisplayName("Группировка и суммирование доходов по категориям")
    void checkGetIncomeSumsByCategoriesTest() {
        // given
        wallet.getIncomes().add(new Operation("Зарплата", 50000));
        wallet.getIncomes().add(new Operation("Зарплата", 20000));

        // when
        Map<String, Double> sums = wallet.getIncomeSumsByCategories();

        // then
        assertEquals(1, sums.size());
        assertEquals(70000.0, sums.get("Зарплата"), "Сумма по ЗП должна быть 70000");
    }

    @Test
    @DisplayName("Группировка доходов: пустой список")
    void checkGetIncomeSumsByCategoriesEmptyTest() {
        // given
        // when
        Map<String, Double> sums = wallet.getIncomeSumsByCategories();

        // then
        assertTrue(sums.isEmpty(), "Мапа должна быть пустой, если доходов нет");
    }

    @Test
    @DisplayName("Получение бюджетов с расходами (остаток положительный)")
    void checkGetBudgetsWithSpentPositiveRemainingTest() {
        // given
        wallet.getExpenses().add(new Operation("Еда", 400));

        // when
        Map<String, Double[]> budgets = wallet.getBudgetsWithSpent();

        // then
        assertTrue(budgets.containsKey("Еда"));
        assertEquals(1000.0, budgets.get("Еда")[0], "Бюджет должен быть 1000");
        assertEquals(600.0, budgets.get("Еда")[1], "Остаток должен быть 600");
    }

    @Test
    @DisplayName("Получение бюджетов с расходами (остаток отрицательный)")
    void checkGetBudgetsWithSpentNegativeRemainingTest() {
        // given
        wallet.getExpenses().add(new Operation("Еда", 1500));

        // when
        Map<String, Double[]> budgets = wallet.getBudgetsWithSpent();

        // then
        assertEquals(1000.0, budgets.get("Еда")[0]);
        assertEquals(-500.0, budgets.get("Еда")[1], "Остаток должен быть отрицательным");
    }

    @Test
    @DisplayName("Получение бюджетов: бюджет не задан")
    void checkGetBudgetsWithSpentNullBudgetTest() {
        // given
        wallet.getCategories()
                .put("Развлечения", new Category("Развлечения", OperationType.EXPENSE, null));
        wallet.getExpenses().add(new Operation("Развлечения", 500));

        // when
        Map<String, Double[]> budgets = wallet.getBudgetsWithSpent();

        // then
        assertTrue(budgets.containsKey("Развлечения"));
        assertNull(budgets.get("Развлечения")[0], "Бюджет должен быть null");
        assertNull(budgets.get("Развлечения")[1], "Остаток должен быть null");
    }

    @Test
    @DisplayName("Подсчет суммы по нескольким категориям")
    void checkGetSumByCategoriesSelectedCategoriesTest() {
        // given
        wallet.getCategories().put("Такси", new Category("Такси", OperationType.EXPENSE, 500.0));

        wallet.getExpenses().add(new Operation("Еда", 300));
        wallet.getExpenses().add(new Operation("Такси", 200));
        wallet.getExpenses().add(new Operation("Зарплата", 1000));

        // when
        double sum = wallet.getSumByCategories(List.of("Еда", "Такси"), OperationType.EXPENSE);

        // then
        assertEquals(500.0, sum, "Сумма должна быть 300 + 200 = 500");
    }

    @Test
    @DisplayName("Подсчет суммы по категориям: совпадений нет")
    void checkGetSumByCategoriesNoMatchesTest() {
        // given
        wallet.getIncomes().add(new Operation("Зарплата", 50000));

        // when
        double sum = wallet.getSumByCategories(List.of("Зарплата"), OperationType.EXPENSE);

        // then
        assertEquals(0.0, sum, "Сумма должна быть 0, так как расходов по этой категории нет");
    }

    @Test
    @DisplayName("Изменение бюджета: успешное изменение и снятие (null)")
    void checkChangeCategoryBudgetSuccessTest() {
        // given
        // when
        wallet.changeCategoryBudget("Еда", 5000.0);

        // then
        assertEquals(5000.0, wallet.getCategoryBudget("Еда"));

        // when
        wallet.changeCategoryBudget("Еда", null);

        // then
        assertNull(wallet.getCategoryBudget("Еда"), "Бюджет должен стать null");
    }

    @Test
    @DisplayName("Изменение бюджета: несуществующая категория")
    void checkChangeCategoryBudgetCategoryNotFoundTest() {
        // given
        // when
        assertDoesNotThrow(() -> wallet.changeCategoryBudget("Несуществующая", 1000.0));

        // then
        assertFalse(wallet.isCategoryExists("Несуществующая"));
    }
}
