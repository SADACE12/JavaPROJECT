import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizManager {
    // Твоя ссылка с вшитым паролем для пула соединений
    private static final String DB_URL = "jdbc:postgresql://aws-1-ap-southeast-2.pooler.supabase.com:6543/postgres?user=postgres.tfikxlxhtoknokpgdbtw&password=FCCVOGOWWC2005";

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
            System.out.println("Тест сохранен!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Question> loadQuiz() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT question_text, opt1, opt2, opt3, opt4, correct_index FROM questions";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String text = rs.getString("question_text");
                List<String> options = Arrays.asList(
                        rs.getString("opt1"),
                        rs.getString("opt2"),
                        rs.getString("opt3"),
                        rs.getString("opt4")
                );
                int correctIndex = rs.getInt("correct_index");

                questions.add(new Question(text, options, correctIndex));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return questions;
    }
}