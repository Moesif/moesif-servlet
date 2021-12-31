package com.moesif.servlet;

import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.models.EventModel;

/***
 * TimerTask to maintain the events queue and send it to moesif periodically.
 *  Task runner method sends event in batches if at least one batch of events available. It
 *  maintains :
 *   1. queue to hold events and events related configuration settings.
 *   2. flag to show if a job is in-progress/running.
 *
 *   It prints few helpful log statements, if debug has been enabled.
 */
public class BatchProcessor extends TimerTask {

    private static final Logger logger = Logger.getLogger(BatchProcessor.class.toString());

    private final MoesifConfiguration moesifConfig;
    private final MoesifAPIClient moesifApi;
    private final Boolean debug;
    private final BlockingQueue<EventModel> batchQueue;    // Queue to hold the batched events

    private boolean jobRunning = false;

    public BatchProcessor(MoesifAPIClient moesifApi, MoesifConfiguration moesifConfig, boolean debug) {
        this.moesifApi = moesifApi;
        this.moesifConfig = moesifConfig;
        this.debug = debug;

        // Initialize the queue with queue size capacity.
        this.batchQueue = new ArrayBlockingQueue<>(this.moesifConfig.queueSize);
    }

    public int getBatchMaxTimeInSec() {
        return this.moesifConfig.batchMaxTime;
    }

    public int getUpdateConfigTimeInMin() {
        return this.moesifConfig.updateConfigTime;
    }

    /***
     * Method to add event into a queue for batch-based event transfer. It maintains
     * time when the last event was added to the event queue.
     * @param event: EventModel
     */
    public void addEvent(EventModel event) {
        try {
            // Add the event to the queue
            this.batchQueue.put(event);

            if (this.debug) {
                String msg = String.format("Event successfully added to queue. Queue Size = %d", this.batchQueue.size());
                logger.info(msg);
            }

        } catch (Exception e) {
            logger.warning("Add event failed. " + e.toString());
        }

    }

    /***
     * Method to check if job is running or not.
     * @return boolean
     *   Returns true if job is running otherwise false.
     */
    public boolean isJobRunning() {
        return this.jobRunning;
    }

    private void sendBatchEvents(final List<EventModel> curEventList) {

        // callback for async createBatchEvents
        APICallBack<com.moesif.api.http.response.HttpResponse> callBack = new APICallBack<com.moesif.api.http.response.HttpResponse>() {

            @Override
            public void onSuccess(HttpContext httpContext, HttpResponse httpResponse) {
                final int status = httpContext.getResponse().getStatusCode();
                if (status != 201) {
                    if (debug) {
                        logger.warning("Status is not 201");
                    }
                }
            }

            public void onFailure(HttpContext context, Throwable error) {

                if (debug) {
                    String msg = String.format("send to Moesif error. %s", error);
                    logger.info(msg);
                    logger.info( error.toString());
                }
            }
        };

        try {
            this.moesifApi.getAPI().createEventsBatchAsync(curEventList, callBack);
        } catch(JsonProcessingException e) {
            logger.warning("Failed to send batch events. " + e.toString());
        } catch(Exception e) {
            logger.warning("Failed to send batch events. " + e.toString());
        }

    }

    /***
     * Task runner method to send events in batches if at least a batch of events available.
     * It also keeps track of a flag to show if job is in-progress.
     * If debug has been enabled then it prints how many batches were sent with total events count.
     */
    @Override
    public void run() {
        // set jobRunning
        this.jobRunning = true;

        // Return if queue is empty
        if (this.batchQueue.isEmpty()) {
            if (this.debug) {
                logger.info("No events to send.");
            }

            // reset jobRunning
            this.jobRunning = false;
            return;
        }

        try {
            // Get all the Events till now
            List<EventModel> allEventList = new ArrayList<>();
            this.batchQueue.drainTo(allEventList);

            // Prepare batches and send them.
            int batchCount = 0;
            for(int i=0; i< allEventList.size(); i += this.moesifConfig.batchSize, batchCount +=1) {
                final int endIndex = Math.min(allEventList.size(), i + this.moesifConfig.batchSize);
                List<EventModel> curEventList = allEventList.subList(i, endIndex);
                this.sendBatchEvents(curEventList);
            }

            if (this.debug) {
                String msg = String.format("%d Batch of events successfully sent to Moesif. Total events count = %d", batchCount, allEventList.size());
                logger.info(msg);
            }

        } catch (Throwable e) {
            logger.warning(e.toString());
        } finally {
            // reset jobRunning
            this.jobRunning = false;
        }
    }

}
