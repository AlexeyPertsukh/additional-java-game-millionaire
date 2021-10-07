package model_question;

import java.util.ArrayList;
import java.util.List;

public class QuestionFabric {
    private static final String MESSAGE_CSV_CONVERT_FILED = "Ошибка преобразования вопросов из формата csv";
    private static final String MESSAGE_JSON_CONVERT_FILED = "Ошибка преобразования вопросов из формата json";

    private final static String SPLIT_STR = ";";

    private QuestionFabric() {
    }

    public static ArrayList<Question> createFromJson(List<String> listJson) throws QuestionFabricException {
        ArrayList<Question> listQuestion;

        try {
            listQuestion = createQuestionsFromJson(listJson);
            return listQuestion;
        } catch (QuestionException ex) {
            throw new QuestionFabricException(MESSAGE_JSON_CONVERT_FILED);
        }
    }

    private static ArrayList<Question> createQuestionsFromJson(List<String> listJson) {
        ArrayList<Question> listQuestion = new ArrayList<>();
        Question question;
        for (String json : listJson) {
            question = Question.of(json);

            listQuestion.add(question);
        }
        return listQuestion;
    }

    public static ArrayList<Question> createFromCsv(List<String> listCsv) throws QuestionFabricException {
        ArrayList<Question> listQuestion;

        try {
            listQuestion = createQuestionsFromCsv(listCsv);
            return listQuestion;
        } catch (Exception ex) {
            throw new QuestionFabricException(MESSAGE_CSV_CONVERT_FILED);
        }
    }

    private static ArrayList<Question> createQuestionsFromCsv(List<String> listCsv) {
        int amountWrongAnswers = Question.AMOUNT_ANSWERS - 1;

        String strQuestion;
        String correctAnswer;
        String[] wrongAnswers = new String[amountWrongAnswers];

        ArrayList<Question> listQuestion = new ArrayList<>();
        for (String csv : listCsv) {
            String[] strings = сsvToStrings(csv);

            strQuestion = strings[0];
            correctAnswer = strings[1];
            wrongAnswers[0] = strings[2];
            wrongAnswers[1] = strings[3];
            wrongAnswers[2] = strings[4];
//            System.arraycopy(wrongAnswers,0,strings,2,numWrongAnswers);

            Question question = new Question(strQuestion, correctAnswer, wrongAnswers);
            listQuestion.add(question);
        }
        return listQuestion;
    }

    @SuppressWarnings("NonAsciiCharacters")
    public static String[] сsvToStrings(String line) {
        return line.split(SPLIT_STR);
    }


}
