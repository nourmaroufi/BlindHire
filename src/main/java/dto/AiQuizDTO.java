package dto;

import java.util.List;

public class AiQuizDTO {
    public String skill;
    public List<AiQuestionDTO> questions;

    public static class AiQuestionDTO {
        public String statement;
        public Double points;
        public List<AiChoiceDTO> choices;
    }

    public static class AiChoiceDTO {
        public String text;
        public Boolean is_correct;
    }
}
