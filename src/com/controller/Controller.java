package com.controller;

import constants.IConst;
import model_game.Game;
import model_question.Question;
import model_question.QuestionFabric;
import model_question.QuestionFabricException;
import model_readers_only_java_console.*;
import view.Color;
import view.Display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller implements IConst {

    private static final String CONSOLE_CONTROLLER_VERSION = "1.3";
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

    ArrayList<Question> questions;

    Game game;

    public Controller() {
        display = Display.getInstance();
        map = new HashMap<>();
    }

    public void go() {
        printOnStart();

        boolean result = loadQuestions();
        if(!result) {
            display.printlRed("\nНе удалось получить список вопросов для игры");
        } else {
            gameAction();
        }

    }

    private void gameAction() {
        display.println();
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

    private void endGame(Game.Result result) {
        display.setColor(COLOR_END_GAME);

        display.println("Игра окончена");
        display.println("Правильные ответы: " + result.getNumAnswerQuestion());
        String text = String.format("Выигрыш: %d %s", result.getAmount(), MONEY_SIGN);
        display.println(text);

        display.resetColor();
    }

    private void showNewQuestion(Question question, Game.Bet bet) {
        List<String> answers = question.getShuffledAllAnswers();
        String addInfo = "";
        if(bet.isIrreparable()) {
            addInfo = "[несгораемая сумма]";
        }
        String stringBet = String.format("Вопрос(%d): %d %s %s", game.getNumQuestion(), bet.getAmount(), MONEY_SIGN, addInfo);
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

        char charMin = 'A';
        char charMax = (char) (charMin + answers.size() - 1);
        char charAnswer = Util.nextChar("Ваш ответ: ", charMin, charMax);
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

    private boolean loadQuestions(){
        ArrayList<String> strings = new ArrayList<>();
        String text = String.format("Загрузка вопросов для игры (%d-из сервера, %d-из локального файла): ", CMD_LOAD_FROM_SERVER, CMD_LOAD_FROM_CSV);
        int cmd = Util.nextInt(text, CMD_LOAD_FROM_SERVER, CMD_LOAD_FROM_CSV);

        //получение вопросов в виде списка строк разных типов(csv, json)
        try {
            if(cmd == CMD_LOAD_FROM_CSV) {
                strings = loadFromCsv();
            } else if(cmd == CMD_LOAD_FROM_SERVER){
                strings = loadFromTcp();
            }
        } catch (ReaderException ex) {
            display.println(ex.getMessage());
            return false;
        }

        //формирование списка вопросов из строк разных типов
        try {
            if(cmd == CMD_LOAD_FROM_CSV) {
                questions = QuestionFabric.createFromCsv(strings);
            } else {
                questions = QuestionFabric.createFromJson(strings);
            }
        } catch (QuestionFabricException ex) {
            display.println(ex.getMessage());
            return false;
        }

        return true;
    }

    private ArrayList<String> loadFromCsv() {
        String fileName = FileReader.getFilenameWithAbsolutePatch(FILE_LOCAL_PATCH, FILE_NAME_CSV_QUESTIONS);
        ArrayList<String> strings = FileReader.read(fileName);
        display.println("loaded strings from file *.csv: " + strings.size());
        return strings;
    }

    private ArrayList<String> loadFromTcp() {
        TcpReader tcpReader = new TcpReader(HOST, PORT, TIMEOUT);
        tcpReader.read();
        ArrayList<String> strings = tcpReader.getStrings();
        display.println("loaded strings from server: " + strings.size());
        return strings;
    }




}
