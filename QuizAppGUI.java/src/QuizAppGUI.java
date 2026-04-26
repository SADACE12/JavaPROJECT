import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class QuizAppGUI extends JFrame {
    private final QuizManager quizManager = new QuizManager();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // Переменные для прохождения теста
    private List<Question> currentQuiz;
    private int currentQuestionIndex = 0;
    private int score = 0;

    // Элементы UI для теста
    private JLabel questionLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionsGroup;

    public QuizAppGUI() {
        setTitle("Система создания и прохождения тестов (Quiz Builder)");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // По центру экрана

        // Создаем экраны
        mainPanel.add(createMainMenuPanel(), "MainMenu");
        mainPanel.add(createAddQuestionPanel(), "AddQuestion");
        mainPanel.add(createTakeQuizPanel(), "TakeQuiz");

        add(mainPanel);
        cardLayout.show(mainPanel, "MainMenu"); // Показываем главное меню при старте
    }

    // --- Экран 1: Главное меню ---
    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Главное меню", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton btnCreate = new JButton("Создать вопрос (Администратор)");
        JButton btnTake = new JButton("Пройти тестирование (Студент)");
        JButton btnExit = new JButton("Выход");

        btnCreate.addActionListener(e -> cardLayout.show(mainPanel, "AddQuestion"));
        btnTake.addActionListener(e -> startQuiz());
        btnExit.addActionListener(e -> System.exit(0));

        panel.add(titleLabel);
        panel.add(btnCreate);
        panel.add(btnTake);
        panel.add(btnExit);

        return panel;
    }

    // --- Экран 2: Создание вопроса ---
    private JPanel createAddQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 10));

        JTextField tfQuestion = new JTextField();
        JTextField[] tfOptions = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            tfOptions[i] = new JTextField();
        }
        Integer[] correctIndices = {1, 2, 3, 4};
        JComboBox<Integer> cbCorrect = new JComboBox<>(correctIndices);

        formPanel.add(new JLabel("Текст вопроса:"));
        formPanel.add(tfQuestion);
        for (int i = 0; i < 4; i++) {
            formPanel.add(new JLabel("Вариант " + (i + 1) + ":"));
            formPanel.add(tfOptions[i]);
        }
        formPanel.add(new JLabel("Номер правильного ответа:"));
        formPanel.add(cbCorrect);

        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Сохранить");
        JButton btnBack = new JButton("Назад");

        btnSave.addActionListener(e -> {
            String qText = tfQuestion.getText().trim();
            if (qText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите текст вопроса!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> options = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String optText = tfOptions[i].getText().trim();
                if (optText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Заполните все варианты ответов!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                options.add(optText);
            }

            int correctIdx = (int) cbCorrect.getSelectedItem();

            // Загружаем старые, добавляем новый, сохраняем
            List<Question> questions = quizManager.loadQuiz();
            questions.add(new Question(qText, options, correctIdx));
            quizManager.saveQuiz(questions);

            JOptionPane.showMessageDialog(this, "Вопрос успешно добавлен!", "Успех", JOptionPane.INFORMATION_MESSAGE);

            // Очищаем поля
            tfQuestion.setText("");
            for (JTextField tf : tfOptions) tf.setText("");
            cbCorrect.setSelectedIndex(0);
        });

        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        btnPanel.add(btnSave);
        btnPanel.add(btnBack);

        panel.add(new JLabel("Создание нового вопроса", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Экран 3: Прохождение теста ---
    private JPanel createTakeQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        questionLabel = new JLabel("Текст вопроса здесь");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        optionButtons = new JRadioButton[4];
        optionsGroup = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton("Вариант " + (i + 1));
            optionsGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
        }

        JPanel btnPanel = new JPanel();
        JButton btnNext = new JButton("Ответить / Дальше");
        JButton btnCancel = new JButton("Прервать тест");

        btnNext.addActionListener(e -> processAnswer());
        btnCancel.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        btnPanel.add(btnNext);
        btnPanel.add(btnCancel);

        panel.add(questionLabel, BorderLayout.NORTH);
        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Логика запуска теста
    private void startQuiz() {
        currentQuiz = quizManager.loadQuiz();
        if (currentQuiz == null || currentQuiz.isEmpty()) {
            JOptionPane.showMessageDialog(this, "База вопросов пуста. Сначала добавьте вопросы!", "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentQuestionIndex = 0;
        score = 0;
        showQuestion();
        cardLayout.show(mainPanel, "TakeQuiz");
    }

    // Отображение текущего вопроса
    private void showQuestion() {
        optionsGroup.clearSelection(); // Сбрасываем выбор
        Question q = currentQuiz.get(currentQuestionIndex);

        questionLabel.setText("Вопрос " + (currentQuestionIndex + 1) + " из " + currentQuiz.size() + ": " + q.getText());
        List<String> options = q.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i));
        }
    }

    // Обработка ответа
    private void processAnswer() {
        int selectedIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                selectedIndex = i + 1; // Индексы правильных ответов у нас от 1 до 4
                break;
            }
        }

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите вариант ответа!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Question q = currentQuiz.get(currentQuestionIndex);
        if (selectedIndex == q.getCorrectAnswer()) {
            score++;
        }

        currentQuestionIndex++;

        if (currentQuestionIndex < currentQuiz.size()) {
            showQuestion();
        } else {
            showResults();
        }
    }

    // Вывод результатов
    private void showResults() {
        double percentage = ((double) score / currentQuiz.size()) * 100;
        String message = String.format("Тест завершен!\n\nПравильных ответов: %d из %d\nПроцент верных ответов: %.1f%%",
                score, currentQuiz.size(), percentage);

        JOptionPane.showMessageDialog(this, message, "Итоги тестирования", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "MainMenu"); // Возврат в главное меню
    }

    // Точка входа в программу
    public static void main(String[] args) {
        // Устанавливаем системный дизайн окон (чтобы выглядело как родное приложение Windows/macOS)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Запуск окна в отдельном потоке
        SwingUtilities.invokeLater(() -> {
            new QuizAppGUI().setVisible(true);
        });
    }
}