package Service;

import Model.Candidat;
import Model.JobOffer;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

public class MatchingService {

    public static double getMatchScore(Candidat candidat, JobOffer jobOffer) {

        if (candidat == null || jobOffer == null) return 0;

        String candidateSkills = candidat.getSkills();   // ✅ FIXED
        String jobSkills = jobOffer.getRequiredSkills();

        if (candidateSkills == null || jobSkills == null) return 0;

        String[] jobSkillsArray = jobSkills.toLowerCase().split(",");
        String[] candidateSkillsArray = candidateSkills.toLowerCase().split(",");

        int totalSkills = 0;
        int matchCount = 0;

        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

        for (String jobSkill : jobSkillsArray) {

            String trimmedJobSkill = jobSkill.trim();

            if (trimmedJobSkill.isEmpty()) continue;

            totalSkills++;

            for (String candidateSkill : candidateSkillsArray) {

                String trimmedCandidateSkill = candidateSkill.trim();

                double score = similarity.apply(trimmedJobSkill, trimmedCandidateSkill);

                if (score > 0.85) {   // higher precision
                    matchCount++;
                    break;
                }
            }
        }

        if (totalSkills == 0) return 0;

        return (double) matchCount / totalSkills * 100;
    }
}