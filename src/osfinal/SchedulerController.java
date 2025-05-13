package osfinal;
/**
 * This acts as the controller class of the MVC architecture.
 */

import java.util.Comparator;
import java.util.List;


/**
 * Maintains and calculates performance metrics.
 */
public class SchedulerController {
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    
    /**
     * @param jobs: the list of PrintJobs to be scheduled. - will be sorted by
     * arrival time during the processing.
     *
     * Steps: 
     * 1. It sorts the jobs by arrival time (earliest first) 
     * 2. Processes each job in order: 
     * - sets start time (max of current time or job arrival)
     * - calculates end time; start + burst 
     * - computes turnaround and waiting times
     *
     * 3. Updates cumulative timing statistics 
     * 4. Calculates final averages
     */
    public void runFCFS(List<PrintJob> jobs) {
        // Core of the FCFS sorting by arrival time.
        jobs.sort(Comparator.comparingInt(PrintJob::getArrivalTime));
        
        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        
        
        // Process each job in arrival order
        for (PrintJob job : jobs) {
            // The one that handles the idle time between job if it is needed.
            if (currentTime < job.getArrivalTime()) {
                currentTime = job.getArrivalTime();
            }
            // It schedults the job execution.
            job.setStartTime(currentTime);
            job.setEndTime(currentTime + job.getBurstTime());
            
            // Calculate metrics for turnaround and waiting time.
            int turnaroundTime = job.getEndTime() - job.getArrivalTime();
            int waitingTime = turnaroundTime - job.getBurstTime();
            
            // Stores the calculated metrics in job object
            job.setTurnaroundTime(turnaroundTime);
            job.setWaitingTime(waitingTime);
            
            // Updates the cumulative totals
            totalWaitingTime += waitingTime;
            totalTurnaroundTime += turnaroundTime;
            
            // Advance simulation clock
            currentTime = job.getEndTime();
        }
        
        // Calculate final average
        avgWaitingTime = (double) totalWaitingTime / jobs.size();
        avgTurnaroundTime = (double) totalTurnaroundTime / jobs.size();
    }
     /**
     * Retrieves the calculated average waiting time.
     *
     * @return Average waiting time across all scheduled jobs. Returns 0 if no
     * jobs have been processed.
     */
    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }
    
     /**
     * Retrieves the calculated average turnaround time.
     * 
     * @return Average turnaround time across all scheduled jobs.
     *         Returns 0 if no jobs have been processed.
     */
    public double getAvgTurnaroundTime() {
        return avgTurnaroundTime;
    }
}
