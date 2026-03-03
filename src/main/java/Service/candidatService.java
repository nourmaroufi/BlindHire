package Service;
import Model.Candidat;
import utils.Mydb;

import java.sql.*;

public class candidatService {
    private Connection cnx;

    public candidatService() {
        cnx = Mydb.getInstance().getConnection();
    }

    // Example candidate with skills
    private static Candidat currentCandidate = new Candidat(
            1,                      // id
            "Falta",                // name
            "falta@gmail.com",      // email
            "password123",          // password
            "Java, SQL, Python, HTML, CSS, JavaScript", // skills
            "2 years in web development",               // experiences
            "Portfolio website, E-commerce project",   // projects
            "falta_cv.pdf",                             // cv
            false                                        // anonym
    );

    public static Candidat getCurrentCandidate() {
        return currentCandidate;
    }
}