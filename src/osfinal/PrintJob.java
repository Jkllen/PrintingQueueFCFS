package osfinal;

/**
 * Represents the model of the MVC architecture:
 * - Tracks all timing metrics including arrival, execution, and completion times
 * - Provides calculations for scheduling performances metrics like waiting and
 *   turnaround times.
 */

public class PrintJob {
    /**
     * Unique identifier for the print job.
     */
    private final String jobId;
    
    /**
     * The time when the job arrives in the system.
     */
    private final int arrivalTime;
    
    /**
     * The CPU burst time required by the job,
     * representing how long the job needs to execute.
     */
    private final int burstTime;
    
    /**
     * The time when the job begins execution,
     * initially zero until scheduled by the CPU.
     */
    private int startTime;
    
    /**
     * The time when the job completes execution.
     * Calculated as startTime + burstTime.
     */
    private int endTime;
    
    /**
     * The time the job spent waiting in the ready queque.
     * Calculated as startTime - arrivalTime
     */
    private int waitingTime;
    
    /**
     * The total time from arrival to completion.
     * Calculated as endTime - arrivalTime.
     */
    private int turnaroundTime;
    
    /**
     * Constructs a new PrintJob with required parameters.
     * 
     * @param jobId Unique identifier for the job
     * @param arrivalTime Time when job enters the system
     * @param burstTime CPU time required for job completion
     * @throws IllegalArgumentException if burstTime is not positive
     */
    public PrintJob(String jobId, int arrivalTime, int burstTime) {
        this.jobId = jobId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
    }

    /**
     * The Getter Methods:
     * @return The unique identifier of this job.
     */
    public String getJobId() { 
        return jobId; 
    
    }
    
    // @return the arrival time of this job.
    public int getArrivalTime() { 
        return arrivalTime; 
    }
    
    // @return the CPU burst time required by this job.
    public int getBurstTime() { 
        return burstTime; 
    }
    
    // @return the time when this job began execution.
    public int getStartTime() { 
        return startTime; 
    }
    
    // @retun the time when this job completed its execution.
    public int getEndTime() { 
        return endTime; 
    }
    
    // @return the turnaround time
    public int getTurnaroundTime(){
        return turnaroundTime;
    }
    
    // @return the waiting time
    public int getWaitingTime() { 
        return waitingTime; 
    }
    
    // Setter Methods
    
    /**
     * @param startTime: the time when the execution begins.
     */
    public void setStartTime(int startTime) {       
        this.startTime = startTime; 
    }
    
    /**
     * @param endTime: the timestamp when execution is done.
     */
    public void setEndTime(int endTime) { 
        this.endTime = endTime; 
    }
    
    /**
     * @param turnarountTime: total duration from arrival to completion
     */
    public void setTurnaroundTime(int turnaroundTime){
        this.turnaroundTime = turnaroundTime;
    }
    
    /**
     * @param waitingTime: duration spent waiting before execution.
     */
    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime; 
    }
}
