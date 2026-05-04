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
    private final Color ACCENT_COLOR = new Color(110, 86, 232);       // Яркий фиолетовый
    private final Color ACCENT_HOVER = new Color(130, 106, 255);      // Светлый фиолетовый (наведение)
    private final Color SECONDARY_BTN = new Color(45, 45, 45);        // Темно-серый
    private final Color SECONDARY_HOVER = new Color(60, 60, 60);      // Серый (наведение)
    private final Color TEXT_COLOR = new Color(245, 245, 245);
    private final Color SECONDARY_TEXT = new Color(170, 170, 170);

    private QuizManager quizManager = new QuizManager(); //[cite: 2, 3]
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private List<Question> currentQuiz; //[cite: 2]
    private int currentQuestionIndex = 0;
    private int score = 0;

    private JLabel questionLabel;
    private JPanel optionsPanel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionsGroup;

    public QuizAppGUI() {
        setTitle("Quiz Builder Ultra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);

        setExtendedState(JFrame.MAXIMIZED_BOTH); //[cite: 2]

        mainPanel.setBackground(BG_COLOR);
        mainPanel.add(createMainMenuPanel(), "MainMenu");
        mainPanel.add(createAddQuestionPanel(), "AddQuestion");
        mainPanel.add(createTakeQuizPanel(), "TakeQuiz");

        add(mainPanel);
        cardLayout.show(mainPanel, "MainMenu");
    }

    // --- КАСТОМНАЯ КНОПКА СО СКРУГЛЕНИЯМИ ---
    class ModernButton extends JButton {
        private Color bgColor;
        private Color hoverColor;

        public ModernButton(String text, Color bg, Color hover) {
            super(text);
            this.bgColor = bg;
            this.hoverColor = hover;

            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setFocusPainted(false);
            setContentAreaFilled(false); // Отключаем стандартный фон Windows
            setBorderPainted(false);     // Отключаем стандартную рамку
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Фиксированный размер кнопок, чтобы они не растягивались слишком сильно
            setPreferredSize(new Dimension(350, 50));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { repaint(); }
                public void mouseExited(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Включаем сглаживание для красивых круглых краев
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(bgColor);
            }

            // Рисуем фон со скруглением в 20 пикселей
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.dispose();
            super.paintComponent(g); // Рисуем текст кнопки поверх фона
        }
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("QUIZ PRO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 72));
        title.setForeground(ACCENT_COLOR);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Создавайте и проходите тесты в одно касание");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(SECONDARY_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 60, 0); // Больший отступ снизу
        panel.add(subtitle, gbc);

        // Используем новые кастомные кнопки
        ModernButton btnTake = new ModernButton("НАЧАТЬ ТЕСТИРОВАНИЕ", ACCENT_COLOR, ACCENT_HOVER);
        btnTake.addActionListener(e -> startQuiz()); //[cite: 2]
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(btnTake, gbc);

        ModernButton btnCreate = new ModernButton("РЕДАКТОР ВОПРОСОВ", SECONDARY_BTN, SECONDARY_HOVER);
        btnCreate.addActionListener(e -> cardLayout.show(mainPanel, "AddQuestion"));
        gbc.gridy = 3;
        panel.add(btnCreate, gbc);

        ModernButton btnExit = new ModernButton("ВЫХОД", SECONDARY_BTN, SECONDARY_HOVER);
        btnExit.addActionListener(e -> System.exit(0)); //[cite: 2]
        gbc.gridy = 4;
        panel.add(btnExit, gbc);

        return panel;
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
        btnNext.addActionListener(e -> processAnswer()); //[cite: 2]
        btnPanel.add(btnNext);

        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAddQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(50, 200, 50, 200));

        JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
        form.setBackground(BG_COLOR);

        JTextField tfQuestion = createStyledField("Введите текст вопроса...");
        JTextField[] tfOptions = new JTextField[4];
        for(int i=0; i<4; i++) tfOptions[i] = createStyledField("Вариант " + (i+1));

        form.add(new JLabel("ВОПРОС:") {{ setForeground(ACCENT_COLOR); setFont(new Font("Segoe UI", Font.BOLD, 14)); }});
        form.add(tfQuestion);
        form.add(new JLabel("ВАРИАНТЫ ОТВЕТОВ:") {{ setForeground(ACCENT_COLOR); setFont(new Font("Segoe UI", Font.BOLD, 14)); }});
        for(JTextField f : tfOptions) form.add(f);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(BG_COLOR);

        ModernButton btnSave = new ModernButton("СОХРАНИТЬ", ACCENT_COLOR, ACCENT_HOVER);
        btnSave.setPreferredSize(new Dimension(250, 50));
        btnSave.addActionListener(e -> {
            if(tfQuestion.getText().isEmpty()) return;
            List<String> opts = new ArrayList<>();
            for(JTextField f : tfOptions) opts.add(f.getText());

            List<Question> list = quizManager.loadQuiz(); //[cite: 3]
            list.add(new Question(tfQuestion.getText(), opts, 0));
            quizManager.saveQuiz(list); //[cite: 3]

            // Очистка полей после сохранения
            tfQuestion.setText("");
            for(JTextField f : tfOptions) f.setText("");

            JOptionPane.showMessageDialog(this, "Вопрос успешно сохранен!");
        });

        ModernButton btnBack = new ModernButton("В ГЛАВНОЕ МЕНЮ", SECONDARY_BTN, SECONDARY_HOVER);
        btnBack.setPreferredSize(new Dimension(250, 50));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        btnPanel.add(btnSave);
        btnPanel.add(btnBack);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(CARD_BG);
        f.setForeground(TEXT_COLOR);
        f.setCaretColor(TEXT_COLOR);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return f;
    }

    private void startQuiz() {
        currentQuiz = quizManager.loadQuiz(); //[cite: 3]
        if (currentQuiz.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Сначала добавьте вопросы в редакторе!");
            return;
        }
        currentQuestionIndex = 0;
        score = 0;
        showQuestion();
        cardLayout.show(mainPanel, "TakeQuiz");
    }

    private void showQuestion() {
        Question q = currentQuiz.get(currentQuestionIndex); //[cite: 2]
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
        if(selected == currentQuiz.get(currentQuestionIndex).getCorrectAnswer()) score++;

        currentQuestionIndex++; 
        if(currentQuestionIndex < currentQuiz.size()) showQuestion();
        else {
            JOptionPane.showMessageDialog(this, "Результат: " + score + " из " + currentQuiz.size());
            cardLayout.show(mainPanel, "MainMenu");
        }
    }

    public static void main(String[] args) {
        // Мы убрали UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // чтобы избежать конфликтов с темами Windows. Наш код сам рисует всё как нужно.
        SwingUtilities.invokeLater(() -> new QuizAppGUI().setVisible(true));
    }
}