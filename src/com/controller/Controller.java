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

    private static final String CONSOLE_CONTROLLER_VERSION = "1.4";
    private static final String FILE_LOCAL_PATCH = "\\src\\files\\";

    private static final int CMD_LOAD_FROM_SERVER = 1;
    private static final int CMD_LOAD_FROM_CSV = 2;

    private static final String COLOR_HELP = Color.ANSI_YELLOW;
    private static final String COLOR_QUESTION = Color.ANSI_GREEN;
    private static final String COLOR_END_GAME = Color.ANSI_GREEN;
    private static final String COLOR_CORRECT_ANSWER = Color.ANSI_CYAN;
    private static final String COLOR_FAILED_ANSWER = Color.ANSI_RED;
    private static final int TYPE_NORM = 1;
    private static final int TYPE_TEST = 2;

    private final Display display;
    private final Map<Character, String> map;

    private ArrayList<Question> questions;
    private Game game;
    private int gameType;

    List<String> answers;
    String currentAnswer;

    public Controller() {
        display = Display.getInstance();
        map = new HashMap<>();
    }

    public void go() {
        printOnStart();

        boolean result = loadQuestions();
        if (!result) {
            display.println();
            display.printlnRed("Не удалось получить список вопросов для игры");
            return;
        }

        inputType();
        game = new Game(questions);
        game.start();

        while (!game.isEnd()) {
            printQuestion();
            inputAnswer();
            sendAnswer();
            printRoundResult();
            if(!game.isEnd()) {
                game.nextQuestion();
            }
        }
        display.println();
        printGameResult();
    }

    private void inputType() {
        display.println();
        String text = String.format("Тип игры (%d-обычный, %d-показывать правильный ответ): ", TYPE_NORM, TYPE_TEST);
        gameType = Util.nextInt(text, TYPE_NORM, TYPE_TEST);
        display.println();
    }

    private void printRoundResult() {
        if (game.isWin()) {
            display.printlnColor(COLOR_CORRECT_ANSWER, "Это правильный ответ!");
            display.println();
        } else {
            Question lastQuestion = game.getLastQuestion();
            display.printlnColor(COLOR_FAILED_ANSWER, "Это неправильный ответ...");
            display.printlnColor(COLOR_FAILED_ANSWER, "Правильный ответ: " + lastQuestion.getCorrectAnswer());
        }
    }

    private void printGameResult() {
        display.setColor(COLOR_END_GAME);

        display.println("Игра окончена");

        if(game.isWin()) {
            display.println("⚑⚑⚑");
            display.println("Поздравляем!!!");
            display.println("Вы ответили на все вопросы и стали абсолютным победителем.");
            display.println("⚑⚑⚑");
        } else {
            display.println("Вы НЕ ответили на все вопросы.");
        }
        display.println("Правильные ответы: " + game.getNumCorrectAnswers());
        String text = String.format("Выигрыш: %d %s", game.getWinningAmount(), MONEY_SIGN);
        display.println(text);

        display.resetColor();
    }

    private void printQuestion() {
        Question question = game.getCurrentQuestion();
        answers = question.getShuffledAllAnswers();
        String addInfo = "";
        Game.Bet bet = game.getBet();
        if(bet.isIrreparable()) {
            addInfo = "[несгораемая сумма]";
        }
        String stringBet = String.format("Вопрос(%d): %d %s %s", game.getNumQuestion(), bet.getAmount(), MONEY_SIGN, addInfo);
        display.printlnColor(COLOR_QUESTION, stringBet);
        display.printlnColor(COLOR_QUESTION, question.getStrQuestion());
        display.println("-----");

        char letter = 'A';
        map.clear();
        for (String answer : answers) {
            String text = String.format("%c. %s", letter, answer);
            if(gameType == TYPE_TEST && question.checkCorrectAnswer(answer)) {
                text += " <<";
            }
            display.println(text);
            map.put(letter, answer);
            letter++;
        }

        display.println();
    }


    private void inputAnswer() {
        char charMin = 'A';
        char charMax = (char) (charMin + answers.size() - 1);
        char charAnswer = Util.nextChar("Ваш ответ: ", charMin, charMax);
        currentAnswer = map.get(charAnswer);
    }

    private void sendAnswer() {
        game.sendAnswer(currentAnswer);
    }


    private void printOnStart() {
        display.setColor(COLOR_HELP);
        display.println("******************************************************");
        display.println("Who Wants to Be a Millionaire? (console version) " + CONSOLE_CONTROLLER_VERSION);
        display.println("JAVA A01 2020/21 IT-STEP, Zaporozhye");
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
