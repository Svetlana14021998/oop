package controller;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import model.*;
import service.AuthService;
import service.FileService;
import ui.ConsoleUI;
import utils.StringUtils;

/** Выполнение основной логики приложения */
public class AppRunner {

    /** Пользовательские команды */
    private static final String LOGIN = "login";

    private static final String REGISTRATION = "reg";
    private static final String HELP = "help";
    private static final String EXIT = "exit";
    private static final String SHOW_CATEGORIES = "show_cat";
    private static final String ADD_CATEGORY = "add_cat";
    private static final String CHANGE_CATEGORY_NAME = "change_cat_name";
    private static final String CHANGE_CATEGORY_BUDGET = "change_cat_budget";
    private static final String ADD_INCOME = "add_in";
    private static final String ADD_EXPENSE = "add_out";
    private static final String SHOW_BUDGET_INFO = "show_budget";
    private static final String SUM_CATEGORIES = "sum_cat";
    private static final String TRANSFER_MONEY = "transfer";

    /** Сообщения для пользователя */
    private static final String WELCOME_MSG =
            "Добро пожаловать в приложение по управлению финансами! Для использования необходимо войти или зарегистрироваться.";

    private static final String LOGIN_MSG =
            "Для регистрации введите %s, для входа введите %s, для выхода из программы введите %s: "
                    .formatted(REGISTRATION, LOGIN, EXIT);
    private static final String GOODBYE_MSG = "Завершение работы приложения. До свидания!";
    private static final String LOGIN_CANCELED = "Вход отменен. Возврат в главное меню...";
    private static final String REGISTER_CANCELED =
            "Регистрация отменена. Возврат в главное меню...";
    private static final String REGISTER_SUCCESS =
            "Регистрация успешна! Можете войти с использованием заданных данных";
    private static final String NOT_EXIST_USERS_YES =
            "Пользователей пока нет. Сначала зарегистрируйтесь (команда %s)."
                    .formatted(REGISTRATION);
    private static final String CHOOSE_COMMAND =
            "Введите команду. Для получения списка всех команд введите %s".formatted(HELP);
    private static final String CHOOSE_AVAILABLE_COMMAND =
            "Введена некорректная команда. Для получения списка всех команд введите %s"
                    .formatted(HELP);
    private static final String CANT_LOAD_DATA =
            "Возникла ошибка при загрузке ваших данных. Свяжитесь с поддержкой";
    private static final String SAVE_DATA_ERROR =
            "Возникла ошибка при сохранении данных. Свяжитесь с поддержкой.Приложение останавливает свою работу";

    /** Список команд, доступных в главном меню */
    private static final List<String> AVAILABLE_MAIN_COMMAND = List.of(EXIT, LOGIN, REGISTRATION);

    /** Список команд, доступных вошедшему пользователю */
    private static final List<String> AVAILABLE_USER_COMMANDS =
            List.of(
                    HELP,
                    EXIT,
                    ADD_CATEGORY,
                    SHOW_CATEGORIES,
                    ADD_INCOME,
                    ADD_EXPENSE,
                    SHOW_BUDGET_INFO,
                    SUM_CATEGORIES,
                    TRANSFER_MONEY,
                    CHANGE_CATEGORY_NAME,
                    CHANGE_CATEGORY_BUDGET);

    private final FileService fileService;
    private final AuthService authService;
    private final ConsoleUI ui;
    private final Map<String, User> users;

    public AppRunner(
            FileService fileService,
            AuthService authService,
            ConsoleUI ui,
            Map<String, User> users) {
        this.fileService = fileService;
        this.authService = authService;
        this.ui = ui;
        this.users = users;
    }

    /** Главное меню */
    public void run() {
        System.out.println(WELCOME_MSG);

        while (true) {
            String command = ui.readCommand(LOGIN_MSG, AVAILABLE_MAIN_COMMAND);
            switch (command) {
                case REGISTRATION:
                    User user = authService.registerUser(users, ui.getScanner());
                    if (user != null) {
                        try {
                            fileService.saveUsers(users);
                            System.out.println(REGISTER_SUCCESS);
                        } catch (UncheckedIOException e) {
                            System.out.println(SAVE_DATA_ERROR);
                            System.exit(1);
                        }

                    } else {
                        System.out.println(REGISTER_CANCELED);
                        continue;
                    }
                    break;

                case LOGIN:
                    if (users.isEmpty()) {
                        System.out.println(NOT_EXIST_USERS_YES);
                        continue;
                    }
                    user = authService.login(users, ui.getScanner());
                    if (user == null) {
                        System.out.println(LOGIN_CANCELED);
                        continue;
                    }
                    try {
                        Wallet wallet = fileService.loadWallet(user.getLogin());
                        user.setWallet(wallet);
                        runUserMenu(user);
                    } catch (UncheckedIOException | IllegalStateException e) {
                        System.out.println(CANT_LOAD_DATA);
                        return;
                    }
                    break;

                case EXIT:
                    System.out.println(GOODBYE_MSG);
                    return;
            }
        }
    }

    /**
     * Меню вошедшего пользователя
     *
     * @param user текущий пользователь
     */
    private void runUserMenu(User user) {
        while (true) {
            String command = ui.readCommand(CHOOSE_COMMAND, AVAILABLE_USER_COMMANDS);
            switch (command) {
                case HELP:
                    printHelpInfo();
                    break;
                case EXIT:
                    try {
                        fileService.saveWallet(user.getLogin(), user.getWallet());
                    } catch (UncheckedIOException e) {
                        System.out.println(SAVE_DATA_ERROR);
                        System.exit(1);
                    }

                    return;
                case SHOW_CATEGORIES:
                    showMyCategories(user);
                    break;
                case ADD_CATEGORY:
                    addCategory(user);
                    break;
                case CHANGE_CATEGORY_NAME:
                    changeCategoryName(user);
                    break;
                case CHANGE_CATEGORY_BUDGET:
                    changeCategoryBudget(user);
                    break;
                case ADD_INCOME:
                    addIncome(user);
                    break;
                case ADD_EXPENSE:
                    addExpense(user);
                    break;
                case SHOW_BUDGET_INFO:
                    showBudgetInfo(user);
                    break;
                case SUM_CATEGORIES:
                    showSumCategoriesInfo(user);
                    break;
                case TRANSFER_MONEY:
                    transfer(user);
                    break;
                default:
                    System.out.println(CHOOSE_AVAILABLE_COMMAND);
            }
        }
    }

    /**
     * Изменение бюджета категории.Сохраняет кошелек в файл после добавления.
     *
     * @param user текущий пользователь
     */
    private void changeCategoryBudget(User user) {
        String categoryName = ui.readString("Введите название категории");
        categoryName = StringUtils.capitalize(categoryName);
        Wallet wallet = user.getWallet();
        if (!wallet.isCategoryExists(categoryName, OperationType.EXPENSE)) {
            System.out.printf(
                    "Введенная вами категория расходов отсутствует. Ознакомьтесь со списком ваших категорий с помощью команды %s%n",
                    SHOW_CATEGORIES);
            return;
        }
        System.out.println("Задайте новый бюджет. Если ограничение не нужно, введите Enter");
        Double newBudget = ui.readDoubleOrEmpty();
        wallet.changeCategoryBudget(categoryName, newBudget);
        try {
            fileService.saveWallet(user.getLogin(), wallet);
            System.out.println("Бюджет категории успешно изменен!");
            if (newBudget == null) {
                System.out.println("Ограничений по бюджету нет");
                return;
            }
            System.out.println("===Анализ бюджета по категории===");
            if (wallet.isExpensesMoreThanBudget(categoryName)) {
                System.out.println(
                        "Обратите внимание. Ваши расходы по данной категории превышают установленный бюджет");
            } else {
                System.out.println(
                        "Ваши расходы по данной категории не превышают установленный бюджет");
            }
        } catch (UncheckedIOException e) {
            System.out.println(SAVE_DATA_ERROR);
            System.exit(1);
        }
    }

    /**
     * Изменение названия категории.Сохраняет кошелек в файл после добавления.
     *
     * @param user текущий пользователь
     */
    private void changeCategoryName(User user) {
        String categoryName = ui.readString("Введите название категории");
        categoryName = StringUtils.capitalize(categoryName);
        Wallet wallet = user.getWallet();
        if (!wallet.isCategoryExists(categoryName)) {
            System.out.printf(
                    "Введенная вами категория отсутствует. Ознакомьтесь со списком ваших категорий с помощью команды %s%n",
                    SHOW_CATEGORIES);
            return;
        }
        String newCategoryName;
        while (true) {
            newCategoryName = ui.readString("Введите новое название категории");
            newCategoryName = StringUtils.capitalize(newCategoryName);
            if (!wallet.isCategoryExists(newCategoryName)) {
                break;
            }
            System.out.println("Введенная вами категория уже существует. Выберите другое название");
        }
        wallet.renameCategory(categoryName, newCategoryName);

        try {
            fileService.saveWallet(user.getLogin(), wallet);
            System.out.println("Название категории успешно изменено!");
        } catch (UncheckedIOException e) {
            System.out.println(SAVE_DATA_ERROR);
            System.exit(1);
        }
    }

    /**
     * Метод для перевода денег со счета одного пользователя на счет другого.Сохраняет кошелек в
     * файл после добавления.
     *
     * @param user текущий пользователь
     */
    private void transfer(User user) {
        String beneficiaryLogin = ui.readString("Введите логин получателя");
        if (!users.containsKey(beneficiaryLogin)) {
            System.out.println("Пользователь с данным логином не найден");
            return;
        }
        if (beneficiaryLogin.equals(user.getLogin())) {
            System.out.println("Нельзя перевести деньги самому себе.");
            return;
        }

        Wallet userWallet = user.getWallet();
        double balance = userWallet.getBalance();
        if (balance <= 0) {
            System.out.println("На вашем счете нет средств для перевода");
            return;
        }
        double sum;
        while (true) {
            sum = ui.readDouble("Введите сумму перевода");

            if (sum > balance) {
                System.out.printf(
                        "Введенная сумма превышает сумму вашего баланса. Введите сумму не превышающую %.2f%n",
                        balance);
            } else {
                break;
            }
        }
        try {
            Wallet beneficiaryWallet = fileService.loadWallet(beneficiaryLogin);
            String expensePayment = "Исходящий перевод";
            if (!userWallet.getCategories().containsKey(expensePayment)) {
                userWallet
                        .getCategories()
                        .put(
                                expensePayment,
                                new Category(expensePayment, OperationType.EXPENSE, null));
            }
            userWallet.getExpenses().add(new Operation(expensePayment, sum));
            String incomePayment = "Входящий перевод";
            if (!beneficiaryWallet.getCategories().containsKey(incomePayment)) {
                beneficiaryWallet
                        .getCategories()
                        .put(
                                incomePayment,
                                new Category(incomePayment, OperationType.INCOME, null));
            }
            beneficiaryWallet.getIncomes().add(new Operation(incomePayment, sum));
            fileService.saveWallet(beneficiaryLogin, beneficiaryWallet);
            fileService.saveWallet(user.getLogin(), userWallet);
            System.out.println("Перевод успешно осуществлен");

        } catch (UncheckedIOException | IllegalStateException e) {
            System.out.println(SAVE_DATA_ERROR);
            System.exit(1);
        }
    }

    /**
     * Метод для отображения расходов или доходов по заданным пользователем категориям
     *
     * @param user текущий пользователь
     */
    private void showSumCategoriesInfo(User user) {
        Wallet wallet = user.getWallet();
        String command =
                ui.readCommand(
                        "Выберите категорию in - доходы, out - расходы", List.of("in", "out"));
        OperationType type = command.equals("in") ? OperationType.INCOME : OperationType.EXPENSE;

        List<String> categoryNames;
        while (true) {
            String input = ui.readString("Введите категории через запятую (например: Еда, Такси)");
            categoryNames = parseCategories(input);
            if (!categoryNames.isEmpty()) {
                break;
            }
            System.out.println("Ошибка: вы не ввели ни одной категории. Попробуйте снова.");
        }
        List<String> existing = wallet.getCategories(type);
        List<String> missing =
                categoryNames.stream().filter(name -> !existing.contains(name)).toList();
        String msg = type == OperationType.INCOME ? "доходов" : "расходов";
        if (!missing.isEmpty()) {
            System.out.printf("Данные категории %s не были найдены:%n", msg);
            System.out.println(String.join("\n", missing));
        } else {
            double sum = wallet.getSumByCategories(categoryNames, type);
            System.out.printf(
                    "Сумма %s по категориям (%s): %.2f%n",
                    msg, String.join(", ", categoryNames), sum);
        }
    }

    /** Вывод списка доступных команд */
    private void printHelpInfo() {
        String helpInfo =
                """
                Список доступных команд:
                exit - выйти из профиля
                show_cat - показать список категорий доходов и расходов (с заданным бюджетом)
                add_cat - добавить категорию
                change_cat_name - изменить название категории
                change_cat_budget - изменить бюджет для категории расходов
                add_in - добавить доход
                add_out - добавить расход
                show_budget - показать всю информацию о доходах и расходах
                sum_cat - показывает сумму расходов или доходов по нескольким выбранным категориям
                transfer - перевести деньги другому пользователю
                """;
        System.out.println(helpInfo);
    }

    /**
     * Вывод информации о бюджете: 1.Общий доход 2.Доходы по категориям 3.Общие расходы 4.Бюджет по
     * категориям 5.Текущий баланс
     *
     * @param user текущий пользователь
     */
    private void showBudgetInfo(User user) {
        Wallet wallet = user.getWallet();
        double incomes = wallet.getAllIncomesSum();
        Map<String, Double> incomeSumsByCategories = wallet.getIncomeSumsByCategories();
        System.out.printf("\n===Общий доход: %.2f===%n", incomes);
        if (!incomeSumsByCategories.isEmpty()) {
            System.out.println("\n===Доходы по категориям===");
            incomeSumsByCategories.forEach(
                    (categoryName, total) -> {
                        System.out.printf("  %s: %.2f%n", categoryName, total);
                    });
        }
        double expenses = wallet.getAllExpensesSum();
        Map<String, Double[]> budgetsWithSpent = wallet.getBudgetsWithSpent();
        System.out.printf("\n===Общие расходы: %.2f===%n", expenses);
        if (!budgetsWithSpent.isEmpty()) {
            System.out.println("\n===Бюджет по категориям===");
            budgetsWithSpent.forEach(
                    (name, values) -> {
                        Double budget = values[0];
                        Double remaining = values[1];

                        if (remaining == null) {
                            System.out.printf("  %s: Бюджет не задан%n", name);
                        } else {
                            System.out.printf(
                                    "  %s: %.2f, Оставшийся бюджет: %.2f%n",
                                    name, budget, remaining);
                        }
                    });
        }
        double budget = incomes - expenses;
        System.out.printf("=== Текущий баланс: %.2f===%n", budget);
    }

    /**
     * Добавление дохода.Сохраняет кошелек в файл после добавления.
     *
     * @param user текущий пользователь
     */
    private void addIncome(User user) {
        Wallet wallet = user.getWallet();
        String categoryName = ui.readString("Укажите категорию доходов");
        categoryName = StringUtils.capitalize(categoryName);
        List<String> userCategories = wallet.getCategories(OperationType.INCOME);
        if (!userCategories.contains(categoryName)) {
            System.out.printf(
                    "В списке ваших категорий доходов нет указанной категории. Необходимо сначала добавить данную категорию с помощью команды %s%n",
                    ADD_CATEGORY);
        } else {
            double sum = ui.readDouble("Укажите сумму");
            Operation operation = new Operation(categoryName, sum);
            wallet.getIncomes().add(operation);
            try {
                fileService.saveWallet(user.getLogin(), wallet);
            } catch (UncheckedIOException e) {
                System.out.println(SAVE_DATA_ERROR);
                System.exit(1);
            }

            System.out.println("Доход успешно добавлен");
        }
    }

    /**
     * Добавление расхода.Сохраняет кошелек в файл после добавления.
     *
     * @param user текущий пользователь
     */
    private void addExpense(User user) {
        Wallet wallet = user.getWallet();
        String categoryName = ui.readString("Укажите категорию расходов");
        categoryName = StringUtils.capitalize(categoryName);
        List<String> userCategories = wallet.getCategories(OperationType.EXPENSE);
        if (!userCategories.contains(categoryName)) {
            System.out.printf(
                    "В списке ваших категорий расходов нет указанной категории. Необходимо сначала добавить данную категорию с помощью команды %s%n",
                    ADD_CATEGORY);
        } else {
            double sum = ui.readDouble("Укажите сумму");
            Operation operation = new Operation(categoryName, sum);
            wallet.getExpenses().add(operation);
            try {
                fileService.saveWallet(user.getLogin(), wallet);
            } catch (UncheckedIOException e) {
                System.out.println(SAVE_DATA_ERROR);
                System.exit(1);
            }

            System.out.println("Расход успешно добавлен");

            if (wallet.isExpensesMoreThanBudget(categoryName)) {
                System.out.printf(
                        "Обратите внимание, что вы превысили расходы по категории %s%n",
                        categoryName);
            } else if (wallet.isBudgetAlmostExceeded(categoryName)) {
                System.out.printf(
                        "Обратите внимание, что ваши расходы по категории %s достигли 80%% %n",
                        categoryName);
            }
            if (wallet.isExpensesMoreThanIncomes()) {
                System.out.println("Обратите внимание, что ваши расходы превысили ваши доходы");
            }
        }
    }

    /**
     * Краткая информация о категориях доходов и расходов (с указанием заданного бюджета)
     *
     * @param user текущий пользователь
     */
    private void showMyCategories(User user) {
        Wallet wallet = user.getWallet();
        System.out.println("\n=== Категории доходов ===");
        List<Category> incomeCategories =
                wallet.getCategories().values().stream()
                        .filter(c -> c.getType() == OperationType.INCOME)
                        .toList();
        if (incomeCategories.isEmpty()) {
            System.out.println("(нет категорий доходов)");
        } else {
            incomeCategories.forEach(c -> System.out.println("- " + c.getName()));
        }
        System.out.println("\n=== Категории расходов ===");
        List<Category> expenseCategories =
                wallet.getCategories().values().stream()
                        .filter(c -> c.getType() == OperationType.EXPENSE)
                        .toList();

        if (expenseCategories.isEmpty()) {
            System.out.println("(нет категорий расходов)");
        } else {
            expenseCategories.forEach(
                    c -> {
                        String budgetInfo =
                                (c.getBudget() != null)
                                        ? " (бюджет: " + c.getBudget() + ")"
                                        : " (бюджет не установлен)";
                        System.out.println("- " + c.getName() + budgetInfo);
                    });
        }
    }

    /**
     * Добавление новой категории. Сохраняет кошелек в файл после добавления.
     *
     * @param user текущий пользователь
     */
    private void addCategory(User user) {
        String categoryType =
                ui.readCommand(
                        "Введите тип категории: out - расходы, in - доходы", List.of("out", "in"));
        OperationType type =
                categoryType.equals("out") ? OperationType.EXPENSE : OperationType.INCOME;
        String categoryName = ui.readString("Введите название категории (не может быть пустой)");
        categoryName = StringUtils.capitalize(categoryName);
        if (user.getWallet().getCategories().containsKey(categoryName)) {
            System.out.println("Данная категория уже существует");
            return;
        }
        Double budget = null;
        if (type.equals(OperationType.EXPENSE)) {
            System.out.println("Если хотите, можете установить бюджет, иначе нажмите Enter");
            budget = ui.readDoubleOrEmpty();
        }
        Category category = new Category(categoryName, type, budget);
        user.getWallet().getCategories().put(categoryName, category);
        System.out.println("Категория успешно добавлена");
        try {
            fileService.saveWallet(user.getLogin(), user.getWallet());
        } catch (UncheckedIOException e) {
            System.out.println(SAVE_DATA_ERROR);
            System.exit(1);
        }
    }

    /**
     * Метод для парсинга строки с указанными через ',' категориями в список этих категорий
     *
     * @param input входная строка
     * @return список категорий
     */
    private List<String> parseCategories(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(StringUtils::capitalize)
                .distinct()
                .toList();
    }
}
