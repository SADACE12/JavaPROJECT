import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class QuizManager {
    private static final String FILE_PATH = "quiz_data.txt";

    public void saveQuiz(List<Question> questions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(questions);
            System.out.println("Тест сохранен!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Question> loadQuiz() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (List<Question>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}