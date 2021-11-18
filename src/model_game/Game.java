package model_game;

import model_question.Question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {

    private final static String MESSAGE_NO_QUESTIONS = "Нет вопросов для игры";

    protected final ArrayList<Question> allQuestions;
    protected ArrayList<Question> actualQuestions;
    protected Question currentQuestion;

    protected final List<Round> rounds;
    protected int step;

    public Game(ArrayList<Question> allQuestions) {
        this.allQuestions = allQuestions;
        rounds = new ArrayList<>();
        reset();
    }

    public void reset() {
        createActualQuestions();
        rounds.clear();
        step = -1;
    }

    private void createActualQuestions() {
        actualQuestions = new ArrayList<>(allQuestions);
    }

    public int size() {
        return actualQuestions.size();
    }

    public void start() {
        reset();
        nextQuestion();
    }

    public void nextQuestion() {
        if (isEnd()) {
            return;
        }

        if (actualQuestions.size() == 0) {
            throw new GameException(MESSAGE_NO_QUESTIONS);
        }

        step++;

        int num = random(actualQuestions.size());
        currentQuestion = actualQuestions.remove(num);
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public int getNumQuestion() {
        return step + 1;
    }

    public void sendAnswer(String selectedAnswer) {
        if (isEnd()) {
            return;
        }
        rounds.add(new Round(currentQuestion, selectedAnswer, getBet()));
    }

    public boolean isEnd() {
        if (rounds.size() == 0) {
            return false;
        }

        Round lastRound = getLastRound();
        return !lastRound.isWin() || step == Bet.size();
    }

    public boolean isWin() {
        Round lastRound = getLastRound();
        return lastRound.isWin();
    }

    public int getNumCorrectAnswers() {
        for (int i = rounds.size() - 1; i >= 0 ; i--) {
            Round round = rounds.get(i);
            if(round.isWin()) {
                return i + 1;
            }
        }
        return 0;
    }

    public int getWinningAmount() {
        for (int i = rounds.size() - 1; i >= 0 ; i--) {
            Round round = rounds.get(i);
            if(round.isWin() && round.getBet().isIrreparable()) {
                return round.getBet().getAmount();
            }
        }
        return 0;
    }

    private Round getLastRound() {
        return rounds.get(rounds.size() - 1);
    }

    public Question getLastQuestion() {
        return getLastRound().getQuestion();
    }

    public Bet getBet() {
        return Bet.get(step);
    }

    int random(int max) {
        return (int) (Math.random() * max);
    }

    //роунд
    public static class Round implements Serializable {
        private final Question question;
        private final String playerAnswer;
        private final Bet bet;

        public Round(Question question, String playerAnswer, Bet bet) {
            this.question = question;
            this.playerAnswer = playerAnswer;
            this.bet = bet;
        }

        public Question getQuestion() {
            return question;
        }

        public String getPlayerAnswer() {
            return playerAnswer;
        }

        public Bet getBet() {
            return bet;
        }

        public boolean isWin() {
            return question.checkCorrectAnswer(playerAnswer);
        }
    }

    //СТАВКА
    protected final static boolean SIMPLE_BET = false;
    protected final static boolean IRREPARABLE_BET = true;

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
}