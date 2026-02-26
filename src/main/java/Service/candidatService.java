package Service;
import Model.Candidat;
import utils.Mydb;

import java.sql.*;

public class candidatService {
    private Connection cnx;

    public candidatService() {
        cnx = Mydb.getInstance().getConnection();
    }

    private static Candidat currentCandidate = new Candidat(
            1,                     // id
            "Falta",               // first name
            "falta@gmail.com",     // email
            "password123"          // password
    );


    public static Candidat getCurrentCandidate() {
        return currentCandidate;
    }
}
