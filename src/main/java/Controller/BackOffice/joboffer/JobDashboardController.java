package Controller.BackOffice.joboffer;

import Service.JobOfferService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class JobDashboardController {

    @FXML
    private Label totalJobsLabel;

    private JobOfferService jobService = new JobOfferService();

    @FXML
    public void initialize() {
        loadJobCount();
    }

    private void loadJobCount() {
        int totalJobs = jobService.getJobCount();
        totalJobsLabel.setText(String.valueOf(totalJobs));
    }
}
