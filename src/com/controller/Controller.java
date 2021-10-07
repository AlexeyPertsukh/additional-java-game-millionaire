package com.controller;

import constants.IConst;
import model_game.Game;
import model_question.Question;
import model_question.QuestionFabric;
import view.Color;
import view.Display;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller implements IConst {

    private static final String CONSOLE_CONTROLLER_VERSION = "1.2";
    private static final String FILE_LOCAL_PATCH = "\\src\\files\\";

    private static final int CMD_LOAD_FROM_SERVER = 1;
    private static final int CMD_LOAD_FROM_CSV = 2;

    private static final String COLOR_HELP = Color.ANSI_YELLOW;
    private static final String COLOR_QUESTION = Color.ANSI_GREEN;
    private static final String COLOR_END_GAME = Color.ANSI_GREEN;
    private static final String COLOR_CORRECT_ANSWER = Color.ANSI_CYAN;
    private static final String COLOR_FAILED_ANSWER = Color.ANSI_RED;

    private final Display display;
    private final Map<Character, String> map;
    Game game;

    public Controller() {
        display = Display.getInstance();
        map = new HashMap<>();
    }

    public void go() {
        printOnStart();
        ArrayList<Question> questions = loadQuestions();
        System.out.println();

        game = new Game(questions, Game.DISABLE_PAUSE);
        game.setOnSelectAnswerListener(this::showSelectAnswer);
        game.setOnSelectNewQuestionListener(this::showNewQuestion);
        game.setOnReportQuestionResultListener(this::showResult);
        game.setOnEndGameListener(this::endGame);
        game.start();
    }

    private void showSelectAnswer(String s) {
        //в консольной версии не используется
    }

    private void showResult(String selectedAnswer, String correctAnswer) {
        if(selectedAnswer.equalsIgnoreCase(correctAnswer)) {
            display.printColor(COLOR_CORRECT_ANSWER, "Это правильный ответ!");
        } else {
            display.printColor(COLOR_FAILED_ANSWER, "Это неправильный ответ... \nПравильный ответ: " + correctAnswer);
        }
        display.println();
    }

    private void endGame() {
        String text = String.format("Игра окончена. Ваш выигрыш: %d %s", game.getWin(), MONEY_SIGN);
        display.printColor(COLOR_END_GAME, text);
    }

    private void showNewQuestion(Question question, int bet) {
        List<String> answers = question.getShuffledAllAnswers();
        String addInfo = "";
        if(Game.checkIrreparableAmount(bet)) {
            addInfo = "[несгораемая сумма]";
        }
        String stringBet = String.format("Вопрос(%d): %d %s %s", game.getNumQuestion(), bet, MONEY_SIGN, addInfo);
        display.printColor(COLOR_QUESTION, stringBet);
        display.printColor(COLOR_QUESTION, question.getStrQuestion());
        display.println("-----");

        char letter = 'A';
        map.clear();
        for (String answer : answers) {
            String text = String.format("%c. %s", letter, answer);
            map.put(letter, answer);
            letter++;
            display.println(text);
        }

        display.println();

        char charAnswer = Util.nextChar("Ваш ответ: ", 'A', 'D');
        String currentAnswer = map.get(charAnswer);

        game.sendAnswer(currentAnswer);
    }

    private void printOnStart() {
        display.setColor(COLOR_HELP);
        display.println("******************************************************");
        display.println("Who Wants to Be a Millionaire? (console version) " + CONSOLE_CONTROLLER_VERSION);
        display.println("A01 JAVA 2020/21 IT-STEP, Zaporogue");
        display.println("Pertsukh Alexey");
        display.println("******************************************************");
        display.println();
        display.resetColor();
    }

    private ArrayList<Question> loadQuestions(){
        ArrayList<String> strings = new ArrayList<>();
        String text = String.format("Загрузка вопросов для игры (%d-из сервера, %d-из файла): ", CMD_LOAD_FROM_SERVER, CMD_LOAD_FROM_CSV);
        int cmd = Util.nextInt(text, CMD_LOAD_FROM_SERVER, CMD_LOAD_FROM_CSV);
        if(cmd == CMD_LOAD_FROM_CSV) {
            strings = loadFromCsv();
        }

        return QuestionFabric.createFromCsv(strings);
    }

    private ArrayList<String> loadFromCsv() {
        ArrayList<String> strings = new ArrayList<>();

        String fileName = FileReader.getFilenameWithAbsolutePatch(FILE_LOCAL_PATCH, FILE_NAME_CSV_QUESTIONS);
        try {
            strings = FileReader.read(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            display.printlRed("Failed read file: " + fileName);
        }
        return strings;
    }



}
