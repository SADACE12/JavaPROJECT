import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class QuizAppGUI extends JFrame {

    // ===================== ЦВЕТА =====================
    private final Color BG_COLOR       = new Color(18, 18, 18);
    private final Color CARD_BG        = new Color(30, 30, 30);
    private final Color ACCENT_COLOR   = new Color(110, 86, 232);
    private final Color ACCENT_HOVER   = new Color(130, 106, 255);
    private final Color SECONDARY_BTN  = new Color(45, 45, 45);
    private final Color SECONDARY_HOVER= new Color(60, 60, 60);
    private final Color TEXT_COLOR     = new Color(245, 245, 245);
    private final Color SECONDARY_TEXT = new Color(170, 170, 170);
    private final Color DANGER_COLOR   = new Color(220, 53, 69);
    private final Color DANGER_HOVER   = new Color(250, 70, 85);
    private final Color SUCCESS_COLOR  = new Color(40, 167, 69);
    private final Color WARNING_COLOR  = new Color(255, 193, 7);

    // ===================== СОСТОЯНИЕ =====================
    private QuizManager quizManager = new QuizManager();
    private CardLayout cardLayout    = new CardLayout();
    private JPanel mainPanel         = new JPanel(cardLayout);

    // Роль текущего пользователя
    private boolean isTeacher = false;
    private String teacherName = "Преподаватель"; // можно расширить до логина

    // Текущая активная комната (для преподавателя)
    private String currentRoomCode = null;

    // Квиз
    private List<Question> currentQuiz;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int[] userAnswers;
    private String studentName = "";   // имя студента для сохранения результата
    private String studentRoomCode = ""; // код комнаты, в которой студент сдаёт тест

    // Виджеты квиза
    private JLabel questionLabel;
    private JPanel optionsPanel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionsGroup;

    // Кнопка доступна только преподавателю
    private ModernButton btnCreate;

    // Редактор вопросов
    private DefaultListModel<Question> listModel;
    private JList<Question> questionList;
    private JTextField tfEditorQuestion;
    private JTextField[] tfEditorOptions;
    private JComboBox<Integer> cbCorrectAnswer;
    private int currentEditingId = -1;

    // Результаты
    private JLabel scoreLabel;
    private JLabel resultMessageLabel;
    private JPanel breakdownPanel;

    // Панель комнаты преподавателя
    private JLabel roomCodeDisplayLabel;
    private JPanel resultsTablePanel;

    // ===================== КОНСТРУКТОР =====================
    public QuizAppGUI() {
        setTitle("Quiz Pro — Система тестирования");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        mainPanel.setBackground(BG_COLOR);
        mainPanel.add(createRolePanel(),        "RoleScreen");
        mainPanel.add(createMainMenuPanel(),    "MainMenu");
        mainPanel.add(createEditorPanel(),      "EditorScreen");
        mainPanel.add(createRoomManagerPanel(), "RoomManager");   // ← НОВЫЙ экран преподавателя
        mainPanel.add(createJoinRoomPanel(),    "JoinRoom");       // ← НОВЫЙ экран студента
        mainPanel.add(createTakeQuizPanel(),    "TakeQuiz");
        mainPanel.add(createResultPanel(),      "ResultScreen");

        add(mainPanel);
        cardLayout.show(mainPanel, "RoleScreen");
    }

    // ===================== КНОПКА =====================
    class ModernButton extends JButton {
        private Color bgColor, hoverColor;
        public ModernButton(String text, Color bg, Color hover) {
            super(text);
            this.bgColor = bg; this.hoverColor = hover;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(300, 50));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { repaint(); }
                public void mouseExited(MouseEvent e)  { repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? hoverColor : bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================== ЭКРАН ВЫБОРА РОЛИ =====================
    private JPanel createRolePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        JLabel title = new JLabel("ВЫБЕРИТЕ РОЛЬ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 56));
        title.setForeground(ACCENT_COLOR);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 60, 0);
        panel.add(title, gbc);
        gbc.insets = new Insets(10, 0, 10, 0);

        ModernButton btnTeacher = new ModernButton("Я — ПРЕПОДАВАТЕЛЬ", ACCENT_COLOR, ACCENT_HOVER);
        btnTeacher.addActionListener(e -> {
            isTeacher = true;
            btnCreate.setVisible(true);
            cardLayout.show(mainPanel, "MainMenu");
        });
        gbc.gridy = 1; panel.add(btnTeacher, gbc);

        ModernButton btnStudent = new ModernButton("Я — СТУДЕНТ", SECONDARY_BTN, SECONDARY_HOVER);
        btnStudent.addActionListener(e -> {
            isTeacher = false;
            btnCreate.setVisible(false);
            cardLayout.show(mainPanel, "MainMenu");
        });
        gbc.gridy = 2; panel.add(btnStudent, gbc);

        return panel;
    }

    // ===================== ГЛАВНОЕ МЕНЮ =====================
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

        // Студент: войти по коду
        ModernButton btnJoin = new ModernButton("ВОЙТИ В КОМНАТУ ПО КОДУ", ACCENT_COLOR, ACCENT_HOVER);
        btnJoin.addActionListener(e -> cardLayout.show(mainPanel, "JoinRoom"));
        gbc.gridy = 2; panel.add(btnJoin, gbc);

        // Преподаватель: создать комнату
        btnCreate = new ModernButton("СОЗДАТЬ КОМНАТУ / УПРАВЛЕНИЕ", SECONDARY_BTN, SECONDARY_HOVER);
        btnCreate.addActionListener(e -> {
            refreshEditorList();
            cardLayout.show(mainPanel, "RoomManager");
        });
        gbc.gridy = 3; panel.add(btnCreate, gbc);

        // Преподаватель: редактор вопросов
        ModernButton btnEditor = new ModernButton("РЕДАКТОР ВОПРОСОВ", SECONDARY_BTN, SECONDARY_HOVER);
        btnEditor.addActionListener(e -> {
            if (!isTeacher) {
                JOptionPane.showMessageDialog(this, "Только для преподавателей!");
                return;
            }
            refreshEditorList();
            cardLayout.show(mainPanel, "EditorScreen");
        });
        gbc.gridy = 4; panel.add(btnEditor, gbc);

        ModernButton btnChangeRole = new ModernButton("СМЕНИТЬ РОЛЬ", SECONDARY_BTN, SECONDARY_HOVER);
        btnChangeRole.addActionListener(e -> cardLayout.show(mainPanel, "RoleScreen"));
        gbc.gridy = 5; panel.add(btnChangeRole, gbc);

        ModernButton btnExit = new ModernButton("ВЫХОД", SECONDARY_BTN, SECONDARY_HOVER);
        btnExit.addActionListener(e -> System.exit(0));
        gbc.gridy = 6; panel.add(btnExit, gbc);

        return panel;
    }

    // ===================== МЕНЕДЖЕР КОМНАТ (ПРЕПОДАВАТЕЛЬ) =====================
    private JPanel createRoomManagerPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(40, 80, 40, 80));

        // --- Заголовок ---
        JLabel title = new JLabel("УПРАВЛЕНИЕ КОМНАТАМИ", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(ACCENT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        // --- Центр: создание новой комнаты + активный код + таблица результатов ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BG_COLOR);

        // Блок создания комнаты
        JPanel createBlock = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        createBlock.setBackground(CARD_BG);
        createBlock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                new EmptyBorder(20, 30, 20, 30)));
        createBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel lblRoomName = new JLabel("Название теста:");
        lblRoomName.setForeground(TEXT_COLOR);
        lblRoomName.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JTextField tfRoomName = new JTextField("Тест по теме 1");
        tfRoomName.setPreferredSize(new Dimension(300, 40));
        tfRoomName.setBackground(new Color(45, 45, 45));
        tfRoomName.setForeground(TEXT_COLOR);
        tfRoomName.setCaretColor(TEXT_COLOR);
        tfRoomName.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfRoomName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        ModernButton btnNewRoom = new ModernButton("СОЗДАТЬ КОМНАТУ", ACCENT_COLOR, ACCENT_HOVER);
        btnNewRoom.setPreferredSize(new Dimension(220, 42));
        btnNewRoom.addActionListener(e -> {
            String name = tfRoomName.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Введите название теста!"); return; }
            String code = quizManager.createRoom(name, teacherName);
            if (code != null) {
                currentRoomCode = code;
                roomCodeDisplayLabel.setText("КОД КОМНАТЫ:  " + code);
                roomCodeDisplayLabel.setVisible(true);
                refreshResultsTable(code);
                JOptionPane.showMessageDialog(this,
                        "Комната создана!\n\nКод для студентов:  " + code + "\n\nСообщите этот код студентам.",
                        "Комната открыта", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка создания комнаты. Проверьте подключение к БД.");
            }
        });

        createBlock.add(lblRoomName);
        createBlock.add(tfRoomName);
        createBlock.add(btnNewRoom);
        centerPanel.add(createBlock);
        centerPanel.add(Box.createVerticalStrut(15));

        // Активный код комнаты (большой)
        roomCodeDisplayLabel = new JLabel("", SwingConstants.CENTER);
        roomCodeDisplayLabel.setFont(new Font("Segoe UI Mono", Font.BOLD, 48));
        roomCodeDisplayLabel.setForeground(SUCCESS_COLOR);
        roomCodeDisplayLabel.setVisible(false);
        roomCodeDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(roomCodeDisplayLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Кнопка "Закрыть комнату"
        ModernButton btnCloseRoom = new ModernButton("ЗАКРЫТЬ ТЕКУЩУЮ КОМНАТУ", DANGER_COLOR, DANGER_HOVER);
        btnCloseRoom.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCloseRoom.addActionListener(e -> {
            if (currentRoomCode == null) { JOptionPane.showMessageDialog(this, "Нет активной комнаты."); return; }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Закрыть комнату " + currentRoomCode + "?\nСтуденты больше не смогут подключиться.",
                    "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                quizManager.closeRoom(currentRoomCode);
                roomCodeDisplayLabel.setText("Комната " + currentRoomCode + " закрыта");
                roomCodeDisplayLabel.setForeground(DANGER_COLOR);
                currentRoomCode = null;
            }
        });
        centerPanel.add(btnCloseRoom);
        centerPanel.add(Box.createVerticalStrut(20));

        // Кнопка "Обновить результаты"
        ModernButton btnRefresh = new ModernButton("ОБНОВИТЬ РЕЗУЛЬТАТЫ", SECONDARY_BTN, SECONDARY_HOVER);
        btnRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRefresh.addActionListener(e -> {
            if (currentRoomCode != null) refreshResultsTable(currentRoomCode);
            else JOptionPane.showMessageDialog(this, "Нет активной комнаты.");
        });
        centerPanel.add(btnRefresh);
        centerPanel.add(Box.createVerticalStrut(15));

        // Таблица результатов
        JLabel lblTableTitle = new JLabel("РЕЗУЛЬТАТЫ СТУДЕНТОВ", SwingConstants.CENTER);
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTableTitle.setForeground(SECONDARY_TEXT);
        lblTableTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTableTitle);
        centerPanel.add(Box.createVerticalStrut(8));

        resultsTablePanel = new JPanel();
        resultsTablePanel.setLayout(new BoxLayout(resultsTablePanel, BoxLayout.Y_AXIS));
        resultsTablePanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(resultsTablePanel);
        scrollPane.getVerticalScrollBar().setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.setPreferredSize(new Dimension(900, 300));
        centerPanel.add(scrollPane);

        panel.add(centerPanel, BorderLayout.CENTER);

        // --- Кнопка назад ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR);
        ModernButton btnBack = new ModernButton("В ГЛАВНОЕ МЕНЮ", SECONDARY_BTN, SECONDARY_HOVER);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        bottomPanel.add(btnBack);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /** Перезагружает таблицу результатов для указанной комнаты. */
    private void refreshResultsTable(String roomCode) {
        resultsTablePanel.removeAll();

        // Заголовок таблицы
        JPanel header = buildResultRow("СТУДЕНТ", "ОЧКИ", "ИЗ", "ПРОЦЕНТ", "ОЦЕНКА", "ВРЕМЯ", true);
        resultsTablePanel.add(header);

        List<String[]> rows = quizManager.getResultsForRoom(roomCode);
        if (rows.isEmpty()) {
            JLabel empty = new JLabel("Пока нет результатов для этой комнаты.", SwingConstants.CENTER);
            empty.setForeground(SECONDARY_TEXT);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsTablePanel.add(Box.createVerticalStrut(20));
            resultsTablePanel.add(empty);
        } else {
            for (String[] row : rows) {
                resultsTablePanel.add(buildResultRow(row[0], row[1], row[2], row[3], row[4], row[5], false));
            }
        }

        resultsTablePanel.revalidate();
        resultsTablePanel.repaint();
    }

    /** Строит одну строку таблицы результатов. */
    private JPanel buildResultRow(String name, String score, String total, String pct, String grade, String time, boolean isHeader) {
        JPanel row = new JPanel(new GridLayout(1, 6, 10, 0));
        row.setBackground(isHeader ? new Color(50, 40, 100) : CARD_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                new EmptyBorder(10, 20, 10, 20)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        Font f = isHeader
                ? new Font("Segoe UI", Font.BOLD, 14)
                : new Font("Segoe UI", Font.PLAIN, 14);

        Color gradeColor = TEXT_COLOR;
        if (!isHeader) {
            switch (grade) {
                case "A": gradeColor = SUCCESS_COLOR; break;
                case "B": gradeColor = new Color(136, 196, 64); break;
                case "C": gradeColor = WARNING_COLOR; break;
                case "D": gradeColor = new Color(253, 126, 20); break;
                case "F": gradeColor = DANGER_COLOR; break;
            }
        }

        for (String[] cell : new String[][]{{name, null},{score, null},{total, null},{pct, null},{grade, null},{time, null}}) {
            JLabel lbl = new JLabel(cell[0], SwingConstants.CENTER);
            lbl.setFont(f);
            // Для колонки "grade" — цвет оценки
            lbl.setForeground(cell == new String[][]{{name,null}}[0] ? TEXT_COLOR : TEXT_COLOR);
            row.add(lbl);
        }

        // Повторно создаём метки (предыдущий цикл некорректен для grade-цвета)
        row.removeAll();
        String[] vals = {name, score, total, pct, grade, time};
        for (int i = 0; i < vals.length; i++) {
            JLabel lbl = new JLabel(vals[i], SwingConstants.CENTER);
            lbl.setFont(f);
            lbl.setForeground(i == 4 && !isHeader ? gradeColor : TEXT_COLOR);
            row.add(lbl);
        }

        return row;
    }

    // ===================== ЭКРАН ВХОДА В КОМНАТУ (СТУДЕНТ) =====================
    private JPanel createJoinRoomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(12, 0, 12, 0);

        JLabel title = new JLabel("ВОЙТИ В КОМНАТУ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(ACCENT_COLOR);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(title, gbc);
        gbc.insets = new Insets(10, 0, 10, 0);

        // Поле: имя студента
        JLabel lblName = new JLabel("ВАШЕ ИМЯ:");
        lblName.setForeground(SECONDARY_TEXT);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 1; panel.add(lblName, gbc);

        JTextField tfName = createStyledField("Введите ваше имя...");
        tfName.setPreferredSize(new Dimension(380, 50));
        gbc.gridy = 2; panel.add(tfName, gbc);

        // Поле: код комнаты
        JLabel lblCode = new JLabel("КОД КОМНАТЫ:");
        lblCode.setForeground(SECONDARY_TEXT);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 3; panel.add(lblCode, gbc);

        JTextField tfCode = createStyledField("Например: AB12CD");
        tfCode.setPreferredSize(new Dimension(380, 50));
        tfCode.setFont(new Font("Segoe UI Mono", Font.BOLD, 24));
        tfCode.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4; panel.add(tfCode, gbc);

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(DANGER_COLOR);
        gbc.gridy = 5; panel.add(statusLabel, gbc);

        ModernButton btnEnter = new ModernButton("ВОЙТИ И НАЧАТЬ ТЕСТ", ACCENT_COLOR, ACCENT_HOVER);
        btnEnter.addActionListener(e -> {
            String name = tfName.getText().trim();
            String code = tfCode.getText().trim().toUpperCase();

            if (name.isEmpty()) { statusLabel.setText("Введите ваше имя!"); return; }
            if (code.length() < 4) { statusLabel.setText("Введите корректный код комнаты!"); return; }

            String roomName = quizManager.validateRoomCode(code);
            if (roomName == null) {
                statusLabel.setText("Комната не найдена или уже закрыта. Проверьте код.");
                return;
            }

            // Всё хорошо — запускаем тест
            studentName = name;
            studentRoomCode = code;
            statusLabel.setText("Подключение к комнате «" + roomName + "»...");
            startQuizForStudent();
        });
        gbc.gridy = 6; panel.add(btnEnter, gbc);

        ModernButton btnBack = new ModernButton("НАЗАД", SECONDARY_BTN, SECONDARY_HOVER);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        gbc.gridy = 7; panel.add(btnBack, gbc);

        return panel;
    }

    // ===================== РЕДАКТОР ВОПРОСОВ =====================
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
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(15, 20, 15, 20));
                label.setBackground(isSelected ? ACCENT_COLOR : CARD_BG);
                label.setForeground(TEXT_COLOR);
                return label;
            }
        });
        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Question selected = questionList.getSelectedValue();
                if (selected != null) {
                    currentEditingId = selected.getId();
                    tfEditorQuestion.setText(selected.getText());
                    for (int i = 0; i < 4; i++) tfEditorOptions[i].setText(selected.getOptions().get(i));
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

        addFormLabel(form, "ТЕКСТ ВОПРОСА:");
        tfEditorQuestion = createStyledField("Введите текст вопроса...");
        tfEditorQuestion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        tfEditorQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tfEditorQuestion);
        form.add(Box.createVerticalStrut(25));

        addFormLabel(form, "ВАРИАНТЫ ОТВЕТОВ:");
        tfEditorOptions = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            JPanel optionRow = new JPanel(new BorderLayout(15, 0));
            optionRow.setBackground(BG_COLOR);
            optionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel numLabel = new JLabel(String.valueOf(i + 1));
            numLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            numLabel.setForeground(ACCENT_COLOR);
            numLabel.setPreferredSize(new Dimension(25, 45));
            numLabel.setHorizontalAlignment(SwingConstants.CENTER);
            tfEditorOptions[i] = createStyledField("Вариант " + (i + 1));
            optionRow.add(numLabel, BorderLayout.WEST);
            optionRow.add(tfEditorOptions[i], BorderLayout.CENTER);
            form.add(optionRow);
            form.add(Box.createVerticalStrut(12));
        }
        form.add(Box.createVerticalStrut(15));

        addFormLabel(form, "ПРАВИЛЬНЫЙ ОТВЕТ (НОМЕР):");
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
            if (currentEditingId == -1)
                quizManager.addQuestion(new Question(tfEditorQuestion.getText().trim(), opts, correctIndex));
            else
                quizManager.updateQuestion(new Question(currentEditingId, tfEditorQuestion.getText().trim(), opts, correctIndex));
            refreshEditorList();
        });

        ModernButton btnDelete = new ModernButton("УДАЛИТЬ", DANGER_COLOR, DANGER_HOVER);
        btnDelete.addActionListener(e -> {
            if (currentEditingId != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Точно удалить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) { quizManager.deleteQuestion(currentEditingId); refreshEditorList(); }
            } else {
                JOptionPane.showMessageDialog(this, "Выберите вопрос из списка слева!");
            }
        });

        ModernButton btnBack = new ModernButton("В ГЛАВНОЕ МЕНЮ", SECONDARY_BTN, SECONDARY_HOVER);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        btnPanel.add(btnClear); btnPanel.add(btnSave);
        btnPanel.add(btnDelete); btnPanel.add(btnBack);

        rightPanel.add(formWrapper, BorderLayout.CENTER);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    // ===================== ЭКРАН ТЕСТА =====================
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

    // ===================== ЭКРАН РЕЗУЛЬТАТОВ =====================
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(50, 150, 50, 150));

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

        breakdownPanel = new JPanel();
        breakdownPanel.setLayout(new BoxLayout(breakdownPanel, BoxLayout.Y_AXIS));
        breakdownPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(breakdownPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.getVerticalScrollBar().setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(BG_COLOR);

        ModernButton btnHome = new ModernButton("В ГЛАВНОЕ МЕНЮ", SECONDARY_BTN, SECONDARY_HOVER);
        btnHome.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        btnPanel.add(btnHome);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ===================== ЛОГИКА =====================

    /** Запуск теста для студента (через код комнаты). */
    private void startQuizForStudent() {
        currentQuiz = quizManager.loadQuiz();
        if (currentQuiz.isEmpty()) {
            JOptionPane.showMessageDialog(this, "В этой комнате пока нет вопросов!");
            return;
        }
        currentQuestionIndex = 0;
        score = 0;
        userAnswers = new int[currentQuiz.size()];
        showQuestion();
        cardLayout.show(mainPanel, "TakeQuiz");
    }

    private void showQuestion() {
        Question q = currentQuiz.get(currentQuestionIndex);
        questionLabel.setText("<html><body style='text-align: center'>[" + (currentQuestionIndex + 1) + "/" + currentQuiz.size() + "]  " + q.getText() + "</body></html>");
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(q.getOptions().get(i));
            optionButtons[i].setSelected(false);
        }
        optionsGroup.clearSelection();
    }

    private void processAnswer() {
        int selected = -1;
        for (int i = 0; i < 4; i++) if (optionButtons[i].isSelected()) selected = i;
        if (selected == -1) { JOptionPane.showMessageDialog(this, "Пожалуйста, выберите вариант ответа!"); return; }

        userAnswers[currentQuestionIndex] = selected;
        if (selected == currentQuiz.get(currentQuestionIndex).getCorrectAnswer()) score++;
        currentQuestionIndex++;
        if (currentQuestionIndex < currentQuiz.size()) showQuestion();
        else showResults();
    }

    private void showResults() {
        int total = currentQuiz.size();
        double percentage = (double) score / total * 100;
        String grade = quizManager.calcGrade(percentage);

        Color gradeColor;
        String message;
        switch (grade) {
            case "A": gradeColor = SUCCESS_COLOR;              message = "Превосходно! Вы отлично усвоили материал."; break;
            case "B": gradeColor = new Color(136, 196, 64);   message = "Хорошая работа. Твердые знания."; break;
            case "C": gradeColor = WARNING_COLOR;              message = "Удовлетворительно. Можно было и лучше."; break;
            case "D": gradeColor = new Color(253, 126, 20);   message = "Слабовато. Стоит повторить пройденное."; break;
            default:  gradeColor = DANGER_COLOR;               message = "Тест провален. Обязательно подучите материал!";
        }

        scoreLabel.setText(grade + " (" + score + " / " + total + ")");
        scoreLabel.setForeground(gradeColor);
        resultMessageLabel.setText(message);

        // Сохраняем результат в БД (только если студент входил по коду комнаты)
        if (!studentRoomCode.isEmpty() && !studentName.isEmpty()) {
            quizManager.saveResult(studentRoomCode, studentName, score, total);
        }

        // Разбор ошибок
        breakdownPanel.removeAll();
        String hexSuccess = "#28a745", hexDanger = "#dc3545";
        for (int i = 0; i < total; i++) {
            Question q = currentQuiz.get(i);
            int userAns = userAnswers[i], correctAns = q.getCorrectAnswer();
            boolean isCorrect = (userAns == correctAns);

            JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
            itemPanel.setBackground(CARD_BG);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                    new EmptyBorder(15, 20, 15, 20)));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

            JLabel qLabel = new JLabel("<html><b>" + (i + 1) + ". " + q.getText() + "</b></html>");
            qLabel.setForeground(TEXT_COLOR);
            qLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            itemPanel.add(qLabel, BorderLayout.NORTH);

            String ansText = "<html>Ваш ответ: <font color='" + (isCorrect ? hexSuccess : hexDanger) + "'>"
                    + q.getOptions().get(userAns) + "</font>";
            if (!isCorrect) ansText += "&nbsp;&nbsp;|&nbsp;&nbsp;Правильный ответ: <font color='" + hexSuccess + "'>" + q.getOptions().get(correctAns) + "</font>";
            ansText += "</html>";

            JLabel aLabel = new JLabel(ansText);
            aLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            itemPanel.add(aLabel, BorderLayout.CENTER);

            breakdownPanel.add(itemPanel);
            breakdownPanel.add(Box.createVerticalStrut(10));
        }
        breakdownPanel.revalidate();
        breakdownPanel.repaint();
        cardLayout.show(mainPanel, "ResultScreen");
    }

    // ===================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====================

    private void refreshEditorList() {
        List<Question> questions = quizManager.loadQuiz();
        listModel.clear();
        for (Question q : questions) listModel.addElement(q);
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
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        return f;
    }

    private void addFormLabel(JPanel form, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(ACCENT_COLOR);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(Box.createVerticalStrut(8));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizAppGUI().setVisible(true));
    }
}