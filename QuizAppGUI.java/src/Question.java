import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private int id; // Теперь мы храним ID из базы данных
    private String text;
    private List<String> options;
    private int correctAnswer;

    // Конструктор для загруженных из БД вопросов (с ID)
    public Question(int id, String text, List<String> options, int correctAnswer) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Конструктор для новых вопросов (ID еще неизвестен)
    public Question(String text, List<String> options, int correctAnswer) {
        this(-1, text, options, correctAnswer);
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectAnswer() { return correctAnswer; }

    // Этот метод нужен, чтобы в списке (JList) отображался текст вопроса
    @Override
    public String toString() {
        return text.length() > 50 ? text.substring(0, 50) + "..." : text;
    }
}