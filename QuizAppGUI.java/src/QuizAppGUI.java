import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class QuizAppGUI extends JFrame {
    // Цветовая палитра
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color CARD_BG = new Color(30, 30, 30);
    private final Color ACCENT_COLOR = new Color(110, 86, 232);
    private final Color ACCENT_HOVER = new Color(130, 106, 255);
    private final Color SECONDARY_BTN = new Color(45, 45, 45);
    private final Color SECONDARY_HOVER = new Color(60, 60, 60);
    private final Color TEXT_COLOR = new Color(245, 245, 245);
    private final Color SECONDARY_TEXT = new Color(170, 170, 170);
    private final Color DANGER_COLOR = new Color(220, 53, 69);
    private final Color DANGER_HOVER = new Color(250, 70, 85);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69); // Зеленый для успеха
    private final Color WARNING_COLOR = new Color(255, 193, 7); // Желтый для средней оценки

    private QuizManager quizManager = new QuizManager();
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private List<Question> currentQuiz;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int[] userAnswers; // Массив для хранения ответов пользователя

    private JLabel questionLabel;
    private JPanel optionsPanel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionsGroup;

    private ModernButton btnCreate;

    // --- Переменные редактора ---
    private DefaultListModel<Question> listModel;
    private JList<Question> questionList;
    private JTextField tfEditorQuestion;
    private JTextField[] tfEditorOptions;
    private JComboBox<Integer> cbCorrectAnswer;
    private int currentEditingId = -1;

    // --- Переменные экрана результатов ---
    private JLabel scoreLabel;
    private JLabel resultMessageLabel;
    private JPanel breakdownPanel; // Панель для подробного разбора вопросов

    public QuizAppGUI() {
        setTitle("Quiz Builder Ultra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        mainPanel.setBackground(BG_COLOR);

        mainPanel.add(createMainMenuPanel(), "MainMenu");
        mainPanel.add(createRolePanel(), "RoleScreen");
        mainPanel.add(createEditorPanel(), "EditorScreen");
        mainPanel.add(createTakeQuizPanel(), "TakeQuiz");
        mainPanel.add(createResultPanel(), "ResultScreen");

        add(mainPanel);
        cardLayout.show(mainPanel, "RoleScreen");
    }

    class ModernButton extends JButton {
        private Color bgColor;
        private Color hoverColor;

        public ModernButton(String text, Color bg, Color hover) {
            super(text);
            this.bgColor = bg;
            this.hoverColor = hover;

            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(300, 50));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { repaint(); }
                public void mouseExited(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) g2.setColor(hoverColor);
            else g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel createRolePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(15, 0, 15, 0);

        JLabel title = new JLabel("ВЫБЕРИТЕ РОЛЬ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 56));
        title.setForeground(ACCENT_COLOR);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 60, 0);
        panel.add(title, gbc);

        gbc.insets = new Insets(10, 0, 10, 0);

        ModernButton btnTeacher = new ModernButton("Я - ПРЕПОДАВАТЕЛЬ", ACCENT_COLOR, ACCENT_HOVER);
        btnTeacher.addActionListener(e -> {
            btnCreate.setVisible(true);
            cardLayout.show(mainPanel, "MainMenu");
        });
        gbc.gridy = 1; panel.add(btnTeacher, gbc);

        ModernButton btnStudent = new ModernButton("Я - СТУДЕНТ", SECONDARY_BTN, SECONDARY_HOVER);
        btnStudent.addActionListener(e -> {
            btnCreate.setVisible(false);
            cardLayout.show(mainPanel, "MainMenu");
        });
        gbc.gridy = 2; panel.add(btnStudent, gbc);

        return panel;
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("QUIZ PRO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 72));
        title.setForeground(ACCENT_COLOR);
        gbc.gridy = 0; panel.add(title, gbc);

        JLabel subtitle = new JLabel("Создавайте и проходите тесты в одно касание");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(SECONDARY_TEXT);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(subtitle, gbc);

        gbc.insets = new Insets(10, 0, 10, 0);

        ModernButton btnTake = new ModernButton("НАЧАТЬ ТЕСТИРОВАНИЕ", ACCENT_COLOR, ACCENT_HOVER);
        btnTake.addActionListener(e -> startQuiz());
        gbc.gridy = 2; panel.add(btnTake, gbc);

        btnCreate = new ModernButton("УПРАВЛЕНИЕ ВОПРОСАМИ", SECONDARY_BTN, SECONDARY_HOVER);
        btnCreate.addActionListener(e -> {
            refreshEditorList();
            cardLayout.show(mainPanel, "EditorScreen");
        });
        gbc.gridy = 3; panel.add(btnCreate, gbc);

        ModernButton btnChangeRole = new ModernButton("СМЕНИТЬ РОЛЬ", SECONDARY_BTN, SECONDARY_HOVER);
        btnChangeRole.addActionListener(e -> cardLayout.show(mainPanel, "RoleScreen"));
        gbc.gridy = 4; panel.add(btnChangeRole, gbc);

        ModernButton btnExit = new ModernButton("ВЫХОД", SECONDARY_BTN, SECONDARY_HOVER);
        btnExit.addActionListener(e -> System.exit(0));
        gbc.gridy = 5; panel.add(btnExit, gbc);

        return panel;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(40, 0));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setBackground(BG_COLOR);
        leftPanel.setPreferredSize(new Dimension(350, 0));

        JLabel listTitle = new JLabel("СПИСОК ВОПРОСОВ");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listTitle.setForeground(SECONDARY_TEXT);
        leftPanel.add(listTitle, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        questionList = new JList<>(listModel);
        questionList.setBackground(CARD_BG);
        questionList.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        questionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(15, 20, 15, 20));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(CARD_BG);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        });

        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Question selected = questionList.getSelectedValue();
                if (selected != null) {
                    currentEditingId = selected.getId();
                    tfEditorQuestion.setText(selected.getText());
                    for (int i = 0; i < 4; i++) {
                        tfEditorOptions[i].setText(selected.getOptions().get(i));
                    }
                    cbCorrectAnswer.setSelectedIndex(selected.getCorrectAnswer());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(questionList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.getVerticalScrollBar().setBackground(CARD_BG);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_COLOR);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(BG_COLOR);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_COLOR);
        form.setBorder(new EmptyBorder(0, 10, 0, 10));

        JLabel formTitle = new JLabel("РЕДАКТОР");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        formTitle.setForeground(TEXT_COLOR);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(formTitle);
        form.add(Box.createVerticalStrut(30));

        JLabel lblQ = new JLabel("ТЕКСТ ВОПРОСА:");
        lblQ.setForeground(ACCENT_COLOR);
        lblQ.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQ.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblQ);
        form.add(Box.createVerticalStrut(8));

        tfEditorQuestion = createStyledField("Введите текст вопроса...");
        tfEditorQuestion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        tfEditorQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tfEditorQuestion);
        form.add(Box.createVerticalStrut(25));

        JLabel lblOpts = new JLabel("ВАРИАНТЫ ОТВЕТОВ:");
        lblOpts.setForeground(ACCENT_COLOR);
        lblOpts.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblOpts.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblOpts);
        form.add(Box.createVerticalStrut(8));

        tfEditorOptions = new JTextField[4];
        for(int i = 0; i < 4; i++) {
            JPanel optionRow = new JPanel(new BorderLayout(15, 0));
            optionRow.setBackground(BG_COLOR);
            optionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel numLabel = new JLabel(String.valueOf(i + 1));
            numLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            numLabel.setForeground(ACCENT_COLOR);
            numLabel.setPreferredSize(new Dimension(25, 45));
            numLabel.setHorizontalAlignment(SwingConstants.CENTER);

            tfEditorOptions[i] = createStyledField("Вариант " + (i+1));

            optionRow.add(numLabel, BorderLayout.WEST);
            optionRow.add(tfEditorOptions[i], BorderLayout.CENTER);

            form.add(optionRow);
            form.add(Box.createVerticalStrut(12));
        }
        form.add(Box.createVerticalStrut(15));

        JLabel lblCorrect = new JLabel("ПРАВИЛЬНЫЙ ОТВЕТ (НОМЕР):");
        lblCorrect.setForeground(ACCENT_COLOR);
        lblCorrect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCorrect.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblCorrect);
        form.add(Box.createVerticalStrut(8));

        cbCorrectAnswer = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cbCorrectAnswer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        cbCorrectAnswer.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbCorrectAnswer.setBackground(CARD_BG);
        cbCorrectAnswer.setForeground(TEXT_COLOR);
        cbCorrectAnswer.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cbCorrectAnswer);
        form.add(Box.createVerticalStrut(30));

        formWrapper.add(form, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        btnPanel.setBackground(BG_COLOR);
        btnPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        ModernButton btnClear = new ModernButton("СОЗДАТЬ НОВЫЙ", SECONDARY_BTN, SECONDARY_HOVER);
        btnClear.addActionListener(e -> clearEditorForm());

        ModernButton btnSave = new ModernButton("СОХРАНИТЬ", ACCENT_COLOR, ACCENT_HOVER);
        btnSave.addActionListener(e -> {
            if (tfEditorQuestion.getText().trim().isEmpty()) return;
            List<String> opts = new ArrayList<>();
            for (JTextField f : tfEditorOptions) opts.add(f.getText().trim());

            int correctIndex = cbCorrectAnswer.getSelectedIndex();

            if (currentEditingId == -1) {
                quizManager.addQuestion(new Question(tfEditorQuestion.getText().trim(), opts, correctIndex));
            } else {
                quizManager.updateQuestion(new Question(currentEditingId, tfEditorQuestion.getText().trim(), opts, correctIndex));
            }
            refreshEditorList();
        });

        ModernButton btnDelete = new ModernButton("УДАЛИТЬ", DANGER_COLOR, DANGER_HOVER);
        btnDelete.addActionListener(e -> {
            if (currentEditingId != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Точно удалить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    quizManager.deleteQuestion(currentEditingId);
                    refreshEditorList();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Выберите вопрос из списка слева!");
            }
        });

        ModernButton btnBack = new ModernButton("В ГЛАВНОЕ МЕНЮ", SECONDARY_BTN, SECONDARY_HOVER);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        btnPanel.add(btnClear);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        btnPanel.add(btnBack);

        rightPanel.add(formWrapper, BorderLayout.CENTER);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    private void refreshEditorList() {
        List<Question> questions = quizManager.loadQuiz();
        listModel.clear();
        for (Question q : questions) {
            listModel.addElement(q);
        }
        clearEditorForm();
    }

    private void clearEditorForm() {
        questionList.clearSelection();
        currentEditingId = -1;
        tfEditorQuestion.setText("");
        for (JTextField f : tfEditorOptions) f.setText("");
        if (cbCorrectAnswer != null) cbCorrectAnswer.setSelectedIndex(0);
    }

    private JTextField createStyledField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(CARD_BG);
        f.setForeground(TEXT_COLOR);
        f.setCaretColor(TEXT_COLOR);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return f;
    }

    private JPanel createTakeQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(100, 250, 100, 250));

        questionLabel = new JLabel("Вопрос", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        questionLabel.setForeground(TEXT_COLOR);
        panel.add(questionLabel, BorderLayout.NORTH);

        optionsPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        optionsPanel.setBackground(BG_COLOR);
        optionsPanel.setBorder(new EmptyBorder(50, 0, 50, 0));

        optionButtons = new JRadioButton[4];
        optionsGroup = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 20));
            optionButtons[i].setForeground(TEXT_COLOR);
            optionButtons[i].setBackground(CARD_BG);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            optionButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            optionsGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
        }

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(BG_COLOR);
        ModernButton btnNext = new ModernButton("ПОДТВЕРДИТЬ ОТВЕТ", ACCENT_COLOR, ACCENT_HOVER);
        btnNext.addActionListener(e -> processAnswer());
        btnPanel.add(btnNext);

        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // --- ПОЛНОСТЬЮ ПЕРЕДЕЛАННЫЙ ЭКРАН РЕЗУЛЬТАТОВ ---
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(50, 150, 50, 150));

        // ВЕРХНЯЯ ЧАСТЬ: Оценка и заголовок
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        topPanel.setBackground(BG_COLOR);

        JLabel title = new JLabel("ТЕСТ ЗАВЕРШЕН!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(TEXT_COLOR);
        topPanel.add(title);

        scoreLabel = new JLabel("A (0/0)", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        scoreLabel.setForeground(ACCENT_COLOR);
        topPanel.add(scoreLabel);

        resultMessageLabel = new JLabel("Ваш результат", SwingConstants.CENTER);
        resultMessageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        resultMessageLabel.setForeground(SECONDARY_TEXT);
        topPanel.add(resultMessageLabel);

        panel.add(topPanel, BorderLayout.NORTH);

        // ЦЕНТРАЛЬНАЯ ЧАСТЬ: Разбор ошибок (со скроллом)
        breakdownPanel = new JPanel();
        breakdownPanel.setLayout(new BoxLayout(breakdownPanel, BoxLayout.Y_AXIS));
        breakdownPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(breakdownPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.getVerticalScrollBar().setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Чтобы колесико мыши крутило быстрее
        panel.add(scrollPane, BorderLayout.CENTER);

        // НИЖНЯЯ ЧАСТЬ: Кнопки
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(BG_COLOR);

        ModernButton btnRestart = new ModernButton("ПРОЙТИ ЗАНОВО", ACCENT_COLOR, ACCENT_HOVER);
        btnRestart.addActionListener(e -> startQuiz());
        btnPanel.add(btnRestart);

        ModernButton btnHome = new ModernButton("В ГЛАВНОЕ МЕНЮ", SECONDARY_BTN, SECONDARY_HOVER);
        btnHome.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        btnPanel.add(btnHome);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startQuiz() {
        currentQuiz = quizManager.loadQuiz();
        if (currentQuiz.isEmpty()) {
            JOptionPane.showMessageDialog(this, "В базе данных пока нет вопросов!");
            return;
        }
        currentQuestionIndex = 0;
        score = 0;
        userAnswers = new int[currentQuiz.size()]; // Инициализируем массив ответов
        showQuestion();
        cardLayout.show(mainPanel, "TakeQuiz");
    }

    private void showQuestion() {
        Question q = currentQuiz.get(currentQuestionIndex);
        questionLabel.setText("<html><body style='text-align: center'>" + q.getText() + "</body></html>");
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(q.getOptions().get(i));
            optionButtons[i].setSelected(false);
        }
        optionsGroup.clearSelection();
    }

    private void processAnswer() {
        int selected = -1;
        for(int i=0; i<4; i++) if(optionButtons[i].isSelected()) selected = i;

        if(selected == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите вариант ответа!");
            return;
        }

        userAnswers[currentQuestionIndex] = selected; // Сохраняем ответ пользователя

        if(selected == currentQuiz.get(currentQuestionIndex).getCorrectAnswer()) {
            score++;
        }

        currentQuestionIndex++;
        if(currentQuestionIndex < currentQuiz.size()) showQuestion();
        else {
            showResults();
        }
    }

    // --- ЛОГИКА ОТОБРАЖЕНИЯ РЕЗУЛЬТАТОВ И ОЦЕНОК ---
    private void showResults() {
        int total = currentQuiz.size();
        double percentage = (double) score / total;

        String grade;
        Color gradeColor;

        // Американская система оценок
        if (percentage >= 0.9) {
            grade = "A";
            gradeColor = SUCCESS_COLOR;
            resultMessageLabel.setText("Превосходно! Вы отлично усвоили материал.");
        } else if (percentage >= 0.8) {
            grade = "B";
            gradeColor = new Color(136, 196, 64); // Светло-зеленый
            resultMessageLabel.setText("Хорошая работа. Твердые знания.");
        } else if (percentage >= 0.7) {
            grade = "C";
            gradeColor = WARNING_COLOR;
            resultMessageLabel.setText("Удовлетворительно. Можно было и лучше.");
        } else if (percentage >= 0.6) {
            grade = "D";
            gradeColor = new Color(253, 126, 20); // Оранжевый
            resultMessageLabel.setText("Слабовато. Стоит повторить пройденное.");
        } else {
            grade = "F";
            gradeColor = DANGER_COLOR;
            resultMessageLabel.setText("Тест провален. Обязательно подучите материал!");
        }

        scoreLabel.setText(grade + " (" + score + " / " + total + ")");
        scoreLabel.setForeground(gradeColor);

        // Очищаем панель разбора перед заполнением
        breakdownPanel.removeAll();

        // Формируем список ответов
        for (int i = 0; i < total; i++) {
            Question q = currentQuiz.get(i);
            int userAns = userAnswers[i];
            int correctAns = q.getCorrectAnswer();
            boolean isCorrect = (userAns == correctAns);

            // Карточка для одного вопроса
            JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
            itemPanel.setBackground(CARD_BG);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                    new EmptyBorder(15, 20, 15, 20)
            ));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Ограничиваем высоту

            // Текст вопроса
            JLabel qLabel = new JLabel("<html><b>" + (i + 1) + ". " + q.getText() + "</b></html>");
            qLabel.setForeground(TEXT_COLOR);
            qLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            itemPanel.add(qLabel, BorderLayout.NORTH);

            // Текст ответа
            String hexSuccess = "#28a745"; // Зеленый HTML цвет
            String hexDanger = "#dc3545";  // Красный HTML цвет

            String ansText = "<html>Ваш ответ: <font color='" + (isCorrect ? hexSuccess : hexDanger) + "'>"
                    + q.getOptions().get(userAns) + "</font>";

            // Если ответил неверно, показываем правильный ответ
            if (!isCorrect) {
                ansText += "&nbsp;&nbsp;|&nbsp;&nbsp;Правильный ответ: <font color='" + hexSuccess + "'>"
                        + q.getOptions().get(correctAns) + "</font>";
            }
            ansText += "</html>";

            JLabel aLabel = new JLabel(ansText);
            aLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            itemPanel.add(aLabel, BorderLayout.CENTER);

            breakdownPanel.add(itemPanel);
            breakdownPanel.add(Box.createVerticalStrut(10)); // Отступ между карточками
        }

        // Обновляем UI
        breakdownPanel.revalidate();
        breakdownPanel.repaint();

        cardLayout.show(mainPanel, "ResultScreen");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizAppGUI().setVisible(true));
    }
}