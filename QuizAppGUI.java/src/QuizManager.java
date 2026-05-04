import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizManager {
    // Твоя ссылка с вшитым паролем
    private static final String DB_URL = "jdbc:postgresql://aws-1-ap-southeast-2.pooler.supabase.com:6543/postgres?user=postgres.tfikxlxhtoknokpgdbtw&password=FCCVOGOWWC2005";

    // 1. Добавление НОВОГО вопроса
    public void addQuestion(Question q) {
        String sql = "INSERT INTO questions (question_text, opt1, opt2, opt3, opt4, correct_index) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getText());
            pstmt.setString(2, q.getOptions().get(0));
            pstmt.setString(3, q.getOptions().get(1));
            pstmt.setString(4, q.getOptions().get(2));
            pstmt.setString(5, q.getOptions().get(3));
            pstmt.setInt(6, q.getCorrectAnswer());
            pstmt.executeUpdate();
            System.out.println("Вопрос добавлен!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 2. Обновление (РЕДАКТИРОВАНИЕ) существующего вопроса
    public void updateQuestion(Question q) {
        String sql = "UPDATE questions SET question_text=?, opt1=?, opt2=?, opt3=?, opt4=?, correct_index=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getText());
            pstmt.setString(2, q.getOptions().get(0));
            pstmt.setString(3, q.getOptions().get(1));
            pstmt.setString(4, q.getOptions().get(2));
            pstmt.setString(5, q.getOptions().get(3));
            pstmt.setInt(6, q.getCorrectAnswer());
            pstmt.setInt(7, q.getId()); // Указываем, какой именно ID обновляем
            pstmt.executeUpdate();
            System.out.println("Вопрос обновлен!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 3. УДАЛЕНИЕ вопроса
    public void deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Вопрос удален!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 4. ЗАГРУЗКА всех вопросов (теперь тянем и ID тоже)
    public List<Question> loadQuiz() {
        List<Question> questions = new ArrayList<>();
        // Добавили "id" в SELECT и сортировку по ID
        String sql = "SELECT id, question_text, opt1, opt2, opt3, opt4, correct_index FROM questions ORDER BY id";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"),
                        rs.getString("question_text"),
                        Arrays.asList(rs.getString("opt1"), rs.getString("opt2"), rs.getString("opt3"), rs.getString("opt4")),
                        rs.getInt("correct_index")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return questions;
    }
}