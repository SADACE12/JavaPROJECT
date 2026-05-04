import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private List<String> options;
    private int correctAnswer;

    public Question(String text, List<String> options, int correctAnswer) {
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectAnswer() { return correctAnswer; }
}