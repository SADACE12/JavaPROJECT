package org.example.javaalmas20.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to serve Thymeleaf UI views.
 */
@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard() {
        return "teacher_dashboard";
    }

    @GetMapping("/student/join")
    public String studentJoin() {
        return "student_join";
    }

    @GetMapping("/student/quiz")
    public String studentQuiz() {
        return "student_quiz";
    }

    @GetMapping("/student/result")
    public String studentResult() {
        return "student_result";
    }
}
