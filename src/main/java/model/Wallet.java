package model;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Wallet {

    private Map<String, Category> categories = new HashMap<>();
    private List<Operation> incomes = new ArrayList<>();
    private List<Operation> expenses = new ArrayList<>();

    /**
     * Проверка существования категории определенного типа с определенным названием
     *
     * @param categoryName название категории
     * @param type тип категории
     * @return true если категория есть, false если категории нет
     */
    public boolean isCategoryExists(String categoryName, OperationType type) {
        return getCategories(type).contains(categoryName);
    }

    /**
     * Проверка существования категории с определенным названием
     *
     * @param categoryName название категории
     * @return true если категория есть, false если категории нет
     */
    public boolean isCategoryExists(String categoryName) {
        return categories.containsKey(categoryName);
    }

    /**
     * Изменение названия категории
     *
     * @param oldName текущее название
     * @param newName новое название
     */
    public void renameCategory(String oldName, String newName) {
        Category category = categories.remove(oldName);
        if (category == null) return;

        category.setName(newName);
        categories.put(newName, category);

        List<Operation> operations =
                (category.getType() == OperationType.EXPENSE) ? expenses : incomes;
        operations.forEach(
                op -> {
                    if (op.getCategoryName().equals(oldName)) {
                        op.setCategoryName(newName);
                    }
                });
    }

    /**
     * Получение всех категорий заданного типа
     *
     * @param operationType тип категории
     * @return список категорий с указанным типом
     */
    public List<String> getCategories(OperationType operationType) {
        return categories.values().stream()
                .filter(c -> c.getType().equals(operationType))
                .map(Category::getName)
                .toList();
    }

    /**
     * Проверка, что расходы по категории превышают установленный бюджет
     *
     * @param categoryName название категории
     * @return true, если бюджет превышен, false если бюджет не превышен или не установлен
     */
    public boolean isExpensesMoreThanBudget(String categoryName) {
        Double budget = getCategoryBudget(categoryName);
        if (budget == null) {
            return false;
        }
        Double expenses = getCategoryExpensesSum(categoryName);
        return expenses > budget;
    }

    /**
     * Получению бюджета по категории
     *
     * @param categoryName название категории
     * @return заданный бюджет по категории (null если бюджет не задан)
     */
    public Double getCategoryBudget(String categoryName) {
        Category category = getCategories().get(categoryName);
        return (category != null) ? category.getBudget() : null;
    }

    /**
     * Получение суммы всех расходов по заданной категории
     *
     * @param categoryName название категории
     * @return сумма расходов
     */
    public double getCategoryExpensesSum(String categoryName) {
        return expenses.stream()
                .filter(x -> x.getCategoryName().equals(categoryName))
                .mapToDouble(Operation::getAmount)
                .sum();
    }

    /**
     * Получение всех расходов
     *
     * @return расходы
     */
    public double getAllExpensesSum() {
        return expenses.stream().mapToDouble(Operation::getAmount).sum();
    }

    /**
     * Получение суммы всех доходов
     *
     * @return доход
     */
    public double getAllIncomesSum() {
        return incomes.stream().mapToDouble(Operation::getAmount).sum();
    }

    /**
     * Получение списка всех доходов, сгруппированных по категориям
     *
     * @return список доходов
     */
    public Map<String, Double> getIncomeSumsByCategories() {
        return getSumsByCategories(incomes);
    }

    /**
     * Получение информации о бюджете. Включает в себя: 1.Категорию 2.Установленный бюджет 3.Остаток
     *
     * @return инфо о бюджете
     */
    public Map<String, Double[]> getBudgetsWithSpent() {
        Map<String, Double> spentSums = getSumsByCategories(expenses);
        Map<String, Double[]> result = new TreeMap<>();

        for (Category category : categories.values()) {
            if (category.getType() == OperationType.EXPENSE) {
                String name = category.getName();
                Double budget = category.getBudget();
                Double spent = spentSums.getOrDefault(name, 0.0);
                Double remaining = (budget != null) ? (budget - spent) : null;
                result.put(name, new Double[] {budget, remaining});
            }
        }
        return result;
    }

    /**
     * Проверка, что расходы превышают доходы
     *
     * @return true если расходы превысили доходы, иначе false
     */
    public boolean isExpensesMoreThanIncomes() {
        return getBalance() < 0;
    }

    /**
     * Получение баланса (разница между всеми доходами и расходами)
     *
     * @return баланс
     */
    public double getBalance() {
        return getAllIncomesSum() - getAllExpensesSum();
    }

    /**
     * Получение суммы расходов или доходов по заданным категориям
     *
     * @param categoryNames названия категорий
     * @param type тип операции (доходы/расходы)
     * @return сумма по заданным критериям
     */
    public double getSumByCategories(List<String> categoryNames, OperationType type) {
        List<Operation> operations = (type == OperationType.INCOME) ? incomes : expenses;
        return operations.stream()
                .filter(op -> categoryNames.contains(op.getCategoryName()))
                .mapToDouble(Operation::getAmount)
                .sum();
    }

    /**
     * Изменение бюджета по категории
     *
     * @param categoryName название категории
     * @param newBudget новый бюджет
     */
    public void changeCategoryBudget(String categoryName, Double newBudget) {
        Category category = categories.get(categoryName);
        if (category != null) {
            category.setBudget(newBudget);
        }
    }

    /**
     * Проверяет, достигли ли расходы 80% от установленного бюджета (но еще не превысили его)
     *
     * @param categoryName название категории
     * @return true, если израсходовано от 80% до 100% бюджета
     */
    public boolean isBudgetAlmostExceeded(String categoryName) {
        Double budget = getCategoryBudget(categoryName);
        if (budget == null || budget == 0) {
            return false;
        }
        double expenses = getCategoryExpensesSum(categoryName);
        return expenses >= (budget * 0.8) && expenses <= budget;
    }

    private Map<String, Double> getSumsByCategories(List<Operation> operations) {
        return operations.stream()
                .collect(
                        Collectors.groupingBy(
                                Operation::getCategoryName,
                                TreeMap::new,
                                Collectors.summingDouble(Operation::getAmount)));
    }
}
