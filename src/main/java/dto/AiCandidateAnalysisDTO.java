package dto;

import java.util.List;

public class AiCandidateAnalysisDTO {
    public String strength_summary;
    public String weakness_summary;
    public String hire_recommendation; // "Hire" / "No hire" / "Borderline"
    public String reasoning;           // short

    public List<Resource> resources;

    public static class Resource {
        public String title;
        public String type;   // "video" | "article" | "docs" | "course"
        public String url;
        public String why;
    }
}