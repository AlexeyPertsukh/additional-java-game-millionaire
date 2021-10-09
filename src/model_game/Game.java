package model_game;

import adapters_compatible_for_android.Handler;
import model_question.Question;

import java.io.Serializable;
import java.util.ArrayList;

public class Game implements Serializable {
    public final static boolean ENABLE_PAUSE = true;
    public final static boolean DISABLE_PAUSE = false;

    public final static boolean SIMPLE_BET = false;
    public final static boolean IRREPARABLE_BET = true;

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

    private final State state;

    private String currentAnswer;

    private boolean isAnswerExecute;
    private final Result result;

    public Game(ArrayList<Question> allQuestions, boolean enablePause) {
        this.allQuestions = allQuestions;
        state = getState(enablePause);
        result = new Result();
        round = new Round();
    }

    public void reset() {
        createActualQuestions();
        round.reset();
        result.reset();
    }

    private State getState(boolean enablePause) {
        if(enablePause) {
            return new StateEnabledPause();
        } else {
            return new StateDisabledPause();
        }
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

    public Result getResult() {
        return result;
    }

    private void putResult() {
        result.put(round.getBet(), round.getStep() + 1);
    }

    public void start() {
        reset();
        nextQuestion();
    }

    private void wrongAnswer() {
        if (onEndGameListener != null) {
            onEndGameListener.onEndGame(result);
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
                onEndGameListener.onEndGame(result);
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

    int random(int max) {
        return (int) (Math.random() * max);
    }

    //ВЫИГРЫШ
    public static class Result implements Serializable {
        private int amount;
        private int numAnswerQuestion;

        public void reset() {
            amount = 0;
            numAnswerQuestion = 0;
        }

        public void put(Bet bet, int numAnswerQuestion) {
            this.numAnswerQuestion = numAnswerQuestion;
            if(bet.isIrreparable()) {
                amount = bet.getAmount();
            }
        }

        public int getAmount() {
            return amount;
        }

        public int getNumAnswerQuestion() {
            return numAnswerQuestion;
        }
    }

    //СТАВКА
    public enum Bet {
        BET1(100, SIMPLE_BET),
        BET2(200, SIMPLE_BET),
        BET3(300, SIMPLE_BET),
        BET4(500, SIMPLE_BET),
        BET5(1_000, IRREPARABLE_BET),
        BET6(2_000, SIMPLE_BET),
        BET7(4_000, SIMPLE_BET),
        BET8(8_000, SIMPLE_BET),
        BET9(16_000, SIMPLE_BET),
        BET10(32_000, IRREPARABLE_BET),
        BET11(64_000, SIMPLE_BET),
        BET12(125_000, SIMPLE_BET),
        BET13(255_000, SIMPLE_BET),
        BET14(500_000, SIMPLE_BET),
        BET15(1_000_000, IRREPARABLE_BET),
        ;
        private final int amount;
        private final boolean isIrreparable;

        Bet(int amount, boolean isIrreparable) {
            this.amount = amount;
            this.isIrreparable = isIrreparable;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isIrreparable() {
            return isIrreparable;
        }

        public static int size() {
            return Bet.values().length;
        }

        public static Bet get(int num) {
            Bet[] bets = Bet.values();
            return bets[num];
        }

    }

    //РАУНД
    private static class Round implements Serializable {
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
            return step >= (Bet.size());
        }

        public Bet getBet() {
            return Bet.get(step);
        }

        public int getStep() {
            return step;
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
        void onSelectNewQuestion(Question question, Bet bet);
    }

    @FunctionalInterface
    public interface OnEndGameListener {
        void onEndGame(Result result);
    }

    //State - состояние в зависимости от вкл/откл паузы
    public abstract static class State {
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
                putResult();
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
                putResult();
                nextQuestion();
            } else {
                wrongAnswer();
            }
        }
    }
}