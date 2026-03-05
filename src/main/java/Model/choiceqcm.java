package Model;

public class choiceqcm {
    private int idChoice;
    private int idQuestion;
    private String choiceText;
    private boolean correct;

    public choiceqcm() {}

    public choiceqcm(int idQuestion, String choiceText, boolean correct) {
        this.idQuestion = idQuestion;
        this.choiceText = choiceText;
        this.correct = correct;
    }

    public choiceqcm(int idChoice, int idQuestion, String choiceText, boolean correct) {
        this.idChoice = idChoice;
        this.idQuestion = idQuestion;
        this.choiceText = choiceText;
        this.correct = correct;
    }

    public int getIdChoice() {
        return idChoice;
    }

    public void setIdChoice(int idChoice) {
        this.idChoice = idChoice;
    }

    public int getIdQuestion() {
        return idQuestion;
    }

    public void setIdQuestion(int idQuestion) {
        this.idQuestion = idQuestion;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
