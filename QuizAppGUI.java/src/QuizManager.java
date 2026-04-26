import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class QuizManager {
    private static final String FILE_PATH = "quiz_data.txt";

    // Сохранение списка вопросов в файл
    public void saveQuiz(List<Question> questions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(questions);
            System.out.println("✅ Вопрос успешно сохранен в базу!");
        } catch (IOException e) {
            System.out.println("❌ Ошибка при сохранении: " + e.getMessage());
        }
    }

    // Загрузка списка вопросов из файла
    @SuppressWarnings("unchecked")
    public List<Question> loadQuiz() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>(); // Если файла нет, возвращаем пустой список
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (List<Question>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Ошибка при загрузке данных: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}