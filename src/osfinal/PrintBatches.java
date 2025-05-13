/**
 * This class PrintBatches acts as 'view' structure for the JavaFX application that
 * simulates the First-Come-First-Served (FCFS) CPU Scheduling Algorithm.
 *      It provides the following graphical user interface for:
 * 1. Adding print jobs with arrival and burst time
 * 2. Visualizing the scheduling process through a table view
 * 3. Displaying performance metrics (average waiting and turnaround times)
 * 4. Showing an animated Gantt chart of the execution sequence
 *    -- this includes real-time update adding better visual feedback.
 **/

package osfinal;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * 
 * @author Augustine L. Barredo
 * 
 */


public class PrintBatches extends Application {
    /**
     * ObservableList: to hold print jobs and automatically notify the
     * changes in UI.
     */
    private final ObservableList<PrintJob> jobs = FXCollections.observableArrayList();
    
    /**
     * TableView: to display job details in a table form
     */
    private final TableView<PrintJob> jobTable = new TableView<>();
    
    /**
     * TexField: for inputting job properties.
     */
    private final TextField jobIdField = new TextField();
    private final TextField arrivalField = new TextField();
    private final TextField burstField = new TextField();
    
    /**
     * Label: to display calculated metrics
     */
    private final Label avgWaitingLabel = new Label("Average Waiting Time: -");
    private final Label avgTurnaroundLabel = new Label("Average Turnaround Time: -");
    
    /**
     * SchedulerController: the controller that handles the scheduling 
     * algorithm.
     */
    private final SchedulerController scheduler = new SchedulerController();
    
    /**
     * Global Flag useful for storing different mode.
     */
    private boolean isRealisticMode = true;
    private boolean hasSimulationStarted = false;
    
    /**
     * ganttChartStage: secondary window for Gantt Chart visualization
     */
    private Stage ganttChartStage;
    private BarChart<String, Number> ganttChart;
    
    
    /**
     * @param stage: the primary application window.
     *      * This includes the stage involve with:
     *      - Left Panel Containing input form and results
     *        (such as the text field of Job ID, Arrival Time & Burst Time etc.)
     *      - Center area with job table
     *      - Applied CSS styling path
     *      - Proper cleanup on window close.
     */
    
    @Override
    public void start(Stage stage) {
        //Window Setup
        stage.setTitle("FCFS CPU Scheduling");
        stage.setOnCloseRequest(e -> {
            if (ganttChartStage != null) {
                ganttChartStage.close();
            }
        });
        
        // Container Main Layout 
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        
        // Create and position UI related components such as Input and Results Panel
        VBox leftPanel = new VBox(10, createInputForm(), createResultsPanel());
        leftPanel.setPrefWidth(300);
        
        root.setLeft(leftPanel);
        root.setCenter(createJobTable());

        // Create scene and load CSS stylesheets
        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // Load CSS
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * createInputForm: the section that contains the following:
     * - Text fields for jobID, arrivalTime, and burstTime
     * - Buttons such as addJob, runFCFS, and clearing data
     * - Grid Layout & CSS styling classes
     * 
     * @return Vbox: containing the complete input form.
     */

    private VBox createInputForm() {
        // GridPane to form the layout
        GridPane grid = new GridPane();
        grid.getStyleClass().add("input-grid");
        grid.setVgap(10);
        grid.setHgap(10);
        
        // Add form text fields with labels
        grid.add(new Label("Job ID:"), 0, 0);
        grid.add(jobIdField, 1, 0);
        grid.add(new Label("Arrival Time:"), 0, 1);
        grid.add(arrivalField, 1, 1);
        grid.add(new Label("Burst Time:"), 0, 2);
        grid.add(burstField, 1, 2);
        
        /**
         * The following contains the action buttons with styling and
         * event handlers.
         * - addJob()
         * - runFCFS()
         * - clearAll()
         */
        Button addBtn = new Button("Add Job");
        addBtn.getStyleClass().add("button-add");
        addBtn.setOnAction(e -> addJob());

        Button runBtn = new Button("Run FCFS");
        runBtn.getStyleClass().add("button-run");
        runBtn.setOnAction(e -> runFCFS());

        Button clearBtn = new Button("Clear All");
        clearBtn.getStyleClass().add("button-clear");
        clearBtn.setOnAction(e -> clearAll());
        
        /**
         * Block for Mode Toggle (Realistic / Dynamic)
         * As for reasons why this is done:
         * - Switching to dynamic mode lets you visualize better on how
         *   FCFS do its process being able to add jobs on runtime. It is simply
         *   for SIMULATION and for visual guide.
         * - Comparing it to the realistic mode where it is simply impractical
         *   and impossible to even interrupt an ongoing print job by adding more
         *   in the queue.
         */ 
        ToggleButton modeToggle = new ToggleButton("Realistic Mode");
        // By default, realistic mode.
        modeToggle.setSelected(true);
        modeToggle.setOnAction(event -> {
            isRealisticMode = modeToggle.isSelected();
            modeToggle.setText(isRealisticMode ? "Realistic Mode" : "Dynamic Mode");
        });
        modeToggle.getStyleClass().add("mode-toggle");
        
        // The button container
        HBox buttonBox = new HBox(10, addBtn, runBtn, clearBtn);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        // The complete form components set-up.
        VBox form = new VBox(10, modeToggle, grid, buttonBox);
        form.setPadding(new Insets(10));
        form.getStyleClass().add("input-form");

        return form;
    }
    
    // Displaying the performance metrics
    private VBox createResultsPanel() {
        VBox box = new VBox(10, avgWaitingLabel, avgTurnaroundLabel);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("results-panel");
        return box;
    }
    
    /**
     * createJobTable: creates the table containing the following columns:
     * - Print Job identification (ID entries e.g, P1, P2, J1, J2 etc.)
     * - Timing information  (arrival, burst, start end)
     * - Calculated metrics (waiting time, turnaround time)
     * 
     */ 
    private TableView<PrintJob> createJobTable() {
        /**
         * Binds to observable list where a  list that allows listeners to 
         * track changes when they occur.
         */ 
        jobTable.setItems(jobs);
        jobTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Create and add all table columns respectively
        jobTable.getColumns().addAll(
            createColumn("Job ID", "jobId"),
            createColumn("Arrival", "arrivalTime"),
            createColumn("Burst", "burstTime"),
            createColumn("Start", "startTime"),
            createColumn("End", "endTime"),
            createColumn("Turnaround", "turnaroundTime"),
            createColumn("Wait", "waitingTime")
        );

        return jobTable;
    }
    
    /**
     * Creates a default PropertyValueFactory to extract the value from a given
     * TableView row item reflectively, using the given property name.
     *
     * @param property The name of the property with which to attempt to
     *      reflectively extract a corresponding value for in a given object.
     * 
     * In short, to bind columns to PrintJob properties.
     */

    private TableColumn<PrintJob, ?> createColumn(String title, String prop) {
        TableColumn<PrintJob, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setStyle("-fx-alignment: CENTER;");
        return col;
    }
    
    /**
     * Adds a new print job to the system and
     * Performs validation checks for:
     * - Non-empty jobID
     * - Positive burstTime
     * - Non-negative arrivalTime
     * - If jobID already existed.
     * Automatically refreshes UI and clears input fields upon success
     */

    private void addJob() {
        if(isRealisticMode && hasSimulationStarted){
            showAlert("In a realistic scenario you cannot add jobs once simulation has started."
                    + " Disable realistic mode to add job during runtime.");
            return;
        }
        
        try {
            // Get and validate input values.
            String id = jobIdField.getText().trim();
            int arrival = Integer.parseInt(arrivalField.getText().trim());
            int burst = Integer.parseInt(burstField.getText().trim());

            if (id.isEmpty()) {
                showAlert("Job ID cannot be empty");
                return;
            }
            if (burst <= 0) {
                showAlert("Burst time must be positive");
                return;
            }
            if (arrival < 0) {
                showAlert("Arrival time cannot be negative");
                return;
            }
            if (existsJob(id)) {
                showAlert("Job ID already exists");
                return;
            }
            
            // Add valid job and update UI

            jobs.add(new PrintJob(id, arrival, burst));
            jobTable.refresh();
            jobIdField.clear();
            arrivalField.clear();
            burstField.clear();

            /**
             * A simple UI update where it helps not triggering another window
             * if a gantt stage window is already opened.
             * 
             * As long as the condition is satisfied, we won't have to worry
             * about window duplications.
             */
            if (ganttChartStage != null && ganttChartStage.isShowing()) {
                runFCFS();
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers");
        }
    }
    
    /**
     * @param id: the job ID to check for any existence within the system.
     * @return true if a job with same ID (case-insensitive) already exists,
     *              otherwise return false.
     * 
     * Uses anyMatch() that returns as soon as a match is found which is much
     * more efficient than a full iteration.
     * 
     */
    private boolean existsJob(String id) {
        return jobs.stream().anyMatch(j -> j.getJobId().equalsIgnoreCase(id));
    }
    
    /**
     * This runs the FCFS scheduling algorithm by:
     * 1. Verify if there are jobs to schedule
     * 2. Delegating to the SchedulerController
     * 3. Update UI with Results
     * 4. Refresh the Gantt chart Visualization
     */
    private void runFCFS() {
        if (jobs.isEmpty()) {
            showAlert("No jobs to schedule");
            return;
        }
        
        // This Ensures Realistic Mode Locks after Starting
        hasSimulationStarted = true;
        
        // Run algorithm and update the metrics
        scheduler.runFCFS(jobs);
        jobTable.refresh();
        
        /**
         * Connected to Controller class:
         * @return the average waiting time as a double value.
         * 
         * The waiting time is the duration a job spends waiting
         * in the ready queue before execution.
         * 
         * As for the avg, totalWaitingTime / jobSize;
         */
        avgWaitingLabel.setText(String.format("Average Waiting Time: %.2f", scheduler.getAvgWaitingTime()));
        
        /**
         * @return the average turnaround time for all job as a double value.
         */
        avgTurnaroundLabel.setText(String.format("Average Turnaround Time: %.2f", scheduler.getAvgTurnaroundTime()));

        updateGanttChart();
    }
    
    /**
     * Main method for updating interface of Gantt Chart
     * Gantt Chart Lifecyle:
     * - creates new window if needed
     * - updates existing chart if condition is satisfied
     * Uses Animation for transitions.
     */

    private void updateGanttChart() {
        /**
         * This condition basically indicates that:
         * if window is not opened, create a gantt chart window,
         * else, if the window is opened, just update the UI using 
         * Animations for better transitions instead of creating a new
         * duplicated window..
         */
        if (ganttChartStage == null || !ganttChartStage.isShowing()) {
            createGanttChartWindow();
        } else {
            animateGanttChartUpdate();
        }
    }
    
    /**
     * Creates and configures the Gantt Chart window tih:
     * - category axis for job IDs
     * - number axis for durations
     * - bar chart visualization
     * - proper styling and animation setup
     */
    private void createGanttChartWindow() {
        ganttChartStage = new Stage();
        ganttChartStage.setTitle("FCFS Gantt Chart");
        ganttChartStage.setOnCloseRequest(e -> ganttChartStage = null);
        
        // Configure Chart Axis
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        ganttChart = new BarChart<>(xAxis, yAxis);
        ganttChart.setTitle("Execution Order");
        ganttChart.setCategoryGap(10);
        ganttChart.setBarGap(5);
        ganttChart.setAnimated(false); 
        xAxis.setLabel("Job");
        yAxis.setLabel("Duration");
        
        // Create scene and show window
        Scene scene = new Scene(ganttChart, 600, 400);
        ganttChartStage.setScene(scene);
        ganttChartStage.show();
        
        /**
         * Will call initial window;
         */
        animateGanttChartInitial();
    }
    
    /**
     * Initial window animation for the Gantt Chart:
     * - This handles the first initial scene user will see
     *   after running the FCFS scheduling algorithm.
     * - This creates series data including idle periods
     * - Styling for bars.
     * - Applies fade-in animation
     */
    private void animateGanttChartInitial() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Execution");

        int currentTime = 0;

        /**
         * The Chart Data with jobs and idle periods
         */
        for (PrintJob job : jobs) {
            // Add idle periods if needed
            if (job.getStartTime() > currentTime) {
                int idleDuration = job.getStartTime() - currentTime;
                series.getData().add(new XYChart.Data<>(
                        "IDLE\n(" + currentTime + "-" + job.getStartTime() + ")",
                        idleDuration
                ));
            }

            // Add job execution period
            series.getData().add(new XYChart.Data<>(
                    job.getJobId() + "\n(" + job.getStartTime() + "-" + job.getEndTime() + ")",
                    job.getBurstTime()
            ));

            currentTime = job.getEndTime();
        }

        ganttChart.getData().add(series);

        /**
         * Show first the data before applying style and animate 
         * chart elements.
         * 
         * This was done to avoid complications especially run-time errors
         * during styling of nodes in its initial appearance.
         * 
         * Hence, you can see in the program after running, there is 0.5 seconds
         * where the bar is in a different color. 
         */ 
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    if (data.getXValue().contains("IDLE")) {
                        node.setStyle("-fx-bar-fill: gray;");
                    } else {
                        node.setStyle("-fx-bar-fill: steelblue;");
                    }
                    
                    node.setOpacity(0); 
                }
                
            }
            // Style Legend
            for (Node legendItem : ganttChart.lookupAll(".chart-legend-item")) {
                if (legendItem instanceof Label label && label.getText().equals("Execution")) {
                    Node symbol = label.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: steelblue, steelblue;");
                    }
                }
            }

            // Animate sequentially
            SequentialTransition seqTransition = new SequentialTransition();
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(300), node);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    seqTransition.getChildren().add(ft);
                }
            }
            seqTransition.play();
        });
    }
    
    /**
     * Simply copy pasted logic and methods.
     * This acts as the update during run-time.
     * 
     * The method that will update the user interface when a user inputs
     * another job during run-time.
     */
    private void animateGanttChartUpdate() {
        XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
        newSeries.setName("Execution Time");

        int currentTime = 0;

        // First pass: Create all data items with IDLE periods included
        for (PrintJob job : jobs) {
            // Add IDLE period if needed
            if (job.getStartTime() > currentTime) {
                int idleDuration = job.getStartTime() - currentTime;
                newSeries.getData().add(new XYChart.Data<>(
                        "IDLE\n(" + currentTime + "-" + job.getStartTime() + ")",
                        idleDuration
                ));
            }

            // Add job execution period
            newSeries.getData().add(new XYChart.Data<>(
                    job.getJobId() + "\n(" + job.getStartTime() + "-" + job.getEndTime() + ")",
                    job.getBurstTime()
            ));

            currentTime = job.getEndTime();
        }

        // Fade out old chart
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), ganttChart);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            ganttChart.getData().clear();
            ganttChart.getData().add(newSeries);
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : newSeries.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        if (data.getXValue().contains("IDLE")) {
                            
                            node.setStyle("-fx-bar-fill: gray;");
                        } else {
                            
                            node.setStyle("-fx-bar-fill: steelblue;");
                        }
                    }
                }

                // Styling legend
                for (Node legendItem : ganttChart.lookupAll(".chart-legend-item")) {
                    if (legendItem instanceof Label label
                            && label.getText().equals("Execution Time")) {
                        Node symbol = label.getGraphic();
                        if (symbol != null) {
                            symbol.setStyle("-fx-background-color: steelblue, steelblue;");
                        }
                        
                    }
                }

                // Fade in
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), ganttChart);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
        });
        fadeOut.play();
    }
    
    /**
     * Resets all application state:
     * - Clears job list
     * - Resets performance metrics display
     * - Closes Gantt Chart window if opened since there is no data in
     *   the table for any visualization
     * - Resets simulation state.
     */
    private void clearAll() {
        jobs.clear();
        jobTable.refresh();
        avgWaitingLabel.setText("Average Waiting Time: -");
        avgTurnaroundLabel.setText("Average Turnaround Time: -");
        hasSimulationStarted = false;
        
        if (ganttChartStage != null) {
            ganttChartStage.close();
            ganttChartStage = null;
        }
    }
    
    /**
     * @param msg: the warning message to display.
     * A simple utility method to display alert messages.
     */

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    // Runnable main
    public static void main(String[] args) {
        launch();
    }
}