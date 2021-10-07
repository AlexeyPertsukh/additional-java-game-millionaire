package model_game;

import adapters_compatible_for_android.Handler;
import model_question.Question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game implements Serializable {
    public final static boolean ENABLE_PAUSE = true;
    public final static boolean DISABLE_PAUSE = false;

    private final static String MESSAGE_NO_QUESTIONS = "Нет вопросов для игры";
    private static final int DELAY_INTRIGUE = 3_000;
    private static final int DELAY_AFTER_QUESTION_RESULT = 3_000;

    private final ArrayList<Question> allQuestions;
    private ArrayList<Question> actualQuestions;
    private Question currentQuestion;
    private final Round round;

    private OnReportQuestionResultListener onReportResultListener;
    private OnSelectNewQuestionListener onSelectNewQuestionListener;
    private OnSelectAnswerListener onSelectAnswerListener;
    private OnEndGameListener onEndGameListener;

    private State state;

    private String currentAnswer;

    private boolean isAnswerExecute;



    public Game(ArrayList<Question> allQuestions, boolean enablePause) {
        this.allQuestions = allQuestions;
        if(enablePause) {
            state = new StateEnabledPause();
        } else {
            state = new StateDisabledPause();
        }

        round = new Round();
    }

    public void reset() {
        createActualQuestions();
        round.reset();
    }

    private void createActualQuestions() {
        actualQuestions = new ArrayList<>(allQuestions);
    }

    public int size() {
        return actualQuestions.size();
    }

    public int getNumQuestion() {
        return round.getStep() + 1;
    }

    public int getWin() {
        return round.getWin();
    }

    public void start() {
        reset();
        nextQuestion();
    }

    private void wrongAnswer() {
        if (onEndGameListener != null) {
            onEndGameListener.onEndGame();
        }
    }

    private void nextQuestion() {
        isAnswerExecute = false;
        if (actualQuestions.size() == 0) {
            throw new GameException(MESSAGE_NO_QUESTIONS);
        }

        round.inc();
        if (isEnd()) {
            if (onEndGameListener != null) {
                onEndGameListener.onEndGame();
            }

        } else {
            int num = random(actualQuestions.size());
            currentQuestion = actualQuestions.remove(num);
            if (onSelectNewQuestionListener != null) {
                onSelectNewQuestionListener.onSelectNewQuestion(currentQuestion, round.getBet());
            }

        }
    }

    public boolean checkCorrectAnswer(String answer) {
        return currentQuestion.checkCorrectAnswer(answer);
    }

    public void sendAnswer(String selectedAnswer) {
        if (isAnswerExecute || isEnd()) {
            return;
        }
        isAnswerExecute = true;
        currentAnswer = selectedAnswer;
        state.sendAnswer(currentAnswer);
    }

    private void pauseAfterQuestionResult() {
        state.pauseAfterQuestionResult();
    }

    public boolean isEnd() {
        return round.isEnd();
    }

    public void setOnSelectAnswerListener(OnSelectAnswerListener onSelectAnswerListener) {
        this.onSelectAnswerListener = onSelectAnswerListener;
    }

    public void setOnReportQuestionResultListener(OnReportQuestionResultListener onReportResultListener) {
        this.onReportResultListener = onReportResultListener;
    }

    public void setOnSelectNewQuestionListener(OnSelectNewQuestionListener onSelectNewQuestionListener) {
        this.onSelectNewQuestionListener = onSelectNewQuestionListener;
    }

    public void setOnEndGameListener(OnEndGameListener onEndGameListener) {
        this.onEndGameListener = onEndGameListener;
    }


    public static boolean checkIrreparableAmount(int num) {
        return Round.checkIrreparableAmount(num);
    }

    int random(int max) {
        return (int) (Math.random() * max);
    }

    //РАУНД
    private static class Round {
        private final static int[] BETS = {100, 200, 300, 500, 1000,
                2_000, 4_000, 8_000, 16_000, 32_000,
                64_000, 125_000, 255_000, 500_000, 1000_000};

        private final static int[] IRREPARABLE_AMOUNTS = {1000, 32_000, 1000_000};
        private int step;

        public Round() {
        }

        public void reset() {
            step = -1;
        }

        public void inc() {
            if (!isEnd()) {
                step++;
            }
        }

        public boolean isEnd() {
            return step >= (BETS.length);
        }

        public int getBet() {
            return BETS[step];
        }

        public int getStep() {
            return step;
        }

        public static boolean  checkIrreparableAmount(int num) {
            for (int amount : IRREPARABLE_AMOUNTS) {
                if(amount == num) {
                    return true;
                }
            }
            return false;
        }

        public int getWin() {
            int win = 0;

            int numQuestion = step;

            if (step >= BETS.length) {
                numQuestion = BETS.length - 1;
            }

            for (int i = 0; i < IRREPARABLE_AMOUNTS.length; i++) {
                int bet = BETS[numQuestion];
                if (bet >= IRREPARABLE_AMOUNTS[i]) {
                    win = IRREPARABLE_AMOUNTS[i];
                }
            }

            return win;
        }
    }

    //Слушатели
    @FunctionalInterface
    public interface OnReportQuestionResultListener {
        void onReportQuestionResult(String selectedAnswer, String correctAnswer);
    }

    @FunctionalInterface
    public interface OnSelectAnswerListener {
        void onSelectAnswer(String selectedAnswer);
    }

    @FunctionalInterface
    public interface OnSelectNewQuestionListener {
        void onSelectNewQuestion(Question question, int bet);
    }

    @FunctionalInterface
    public interface OnEndGameListener {
        void onEndGame();
    }

    //State - состояние в зависимости от вкл/откл паузы
    public abstract class State {
        public abstract void sendAnswer(String selectedAnswer);
        public abstract void pauseAfterQuestionResult();
    }

    public class StateEnabledPause extends State {
        @Override
        public void sendAnswer(String selectedAnswer) {
            if(onSelectAnswerListener != null) {
                onSelectAnswerListener.onSelectAnswer(selectedAnswer);
            }

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (onReportResultListener != null) {
                    onReportResultListener.onReportQuestionResult(selectedAnswer, currentQuestion.getCorrectAnswer());
                }
                pauseAfterQuestionResult();
            }, DELAY_INTRIGUE);
        }

        @Override
        public void pauseAfterQuestionResult() {
            Handler handler = new Handler();
            if (checkCorrectAnswer(currentAnswer)) {
                handler.postDelayed(Game.this::nextQuestion, DELAY_AFTER_QUESTION_RESULT);
            } else {
                handler.postDelayed(Game.this::wrongAnswer, DELAY_AFTER_QUESTION_RESULT);
            }
        }
    }

    public class StateDisabledPause extends State {
        @Override
        public void sendAnswer(String selectedAnswer) {
            if (onSelectAnswerListener != null) {
                onSelectAnswerListener.onSelectAnswer(selectedAnswer);
            }

            if (onReportResultListener != null) {
                onReportResultListener.onReportQuestionResult(selectedAnswer, currentQuestion.getCorrectAnswer());
            }
            pauseAfterQuestionResult();

        }

        @Override
        public void pauseAfterQuestionResult() {
            if (checkCorrectAnswer(currentAnswer)) {
                nextQuestion();
            } else {
                wrongAnswer();
            }
        }
    }

}