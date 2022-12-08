package com.moesif.servlet;

import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.models.AppConfigModel;

import java.io.InputStream;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Logger;

/***
 * TimerTask to manage the appConfig and get it from moesif periodically
 * to update local cache.
 */
public class AppConfigManager extends TimerTask {

    private static final Logger logger = Logger.getLogger(AppConfigManager.class.toString());
    private static AppConfigManager instance = null;

    private MoesifAPIClient moesifApi = null;
    private AppConfigModel appConfigModel = new AppConfigModel();
    private String cachedConfigEtag;
    private Date lastUpdatedTime = new Date(0);
    private boolean jobRunning = false; // to avoid running multiple job
    private boolean debug = false;

    public boolean isJobRunning() {
        return jobRunning;
    }

    public static synchronized AppConfigManager getInstance() {
        if (instance == null) {
            instance = new AppConfigManager();
        }

        return instance;
    }

    public void setMoesifApiClient(MoesifAPIClient moesifApi, boolean debug) {
        this.moesifApi = moesifApi;
        this.debug = debug;
    }

    private AppConfigManager() {
    }

    public int getSampleRate(String userId, String companyId) {
        int sampleRate = appConfigModel.getSampleRate();
        if (userId != null && appConfigModel.getUserSampleRate().containsKey(userId)) {
            sampleRate = appConfigModel.getUserSampleRate().get(userId);
        } else if (companyId != null && appConfigModel.getCompanySampleRate().containsKey(companyId)) {
            sampleRate = appConfigModel.getCompanySampleRate().get(companyId);
        }
        return sampleRate;
    }

    /***
     * Method to update the appConfigModel based on Etag
     * if cached copy is stale.
     * @param responseConfigEtag: String
     */
    public void updateIfStale(String responseConfigEtag) {
        // Ignore, if job is already in-progress.
        if (this.isJobRunning()) {
            return;
        }

        // Check if needed to call the getConfig api to update app config
        if (responseConfigEtag != null
                && !(responseConfigEtag.equals(this.cachedConfigEtag))
                && new Date().after(new Date(this.lastUpdatedTime.getTime() + 5 * 60 * 1000))
        ) {
            if (this.debug) {
                logger.info("Calling API to update appConfig based on Etag.");
            }
            // Call api to update samplingPercentage
            this.run();
        }
    }

    /***
     * TaskRunner to update the local cache of ppConfig from the server.
     * It also keeps track of a flag to show if job is in-progress.
     */
    @Override
    public void run() {
        // Skip, if job is already in-progress.
        if (this.isJobRunning()) {
            return;
        }

        try {
            this.jobRunning = true;

            // Calling the api
            HttpResponse configApiResponse = moesifApi.getAPI().getAppConfig();
            // Fetch the response ETag
            String responseConfigEtag = configApiResponse.getHeaders().get("x-moesif-config-etag");

            // Read the response body
            InputStream respBodyIs = configApiResponse.getRawBody();
            AppConfigModel newConfig = APIController.parseAppConfigModel(respBodyIs);
            respBodyIs.close();

            this.appConfigModel = newConfig;
            this.cachedConfigEtag = responseConfigEtag;
        } catch(Throwable e) {
            logger.warning("Fetched configuration failed; using default configuration " + e.toString());
            this.appConfigModel = new AppConfigModel();
            this.appConfigModel.setSampleRate(100);
        } finally {
            jobRunning = false;
        }
        this.lastUpdatedTime = new Date();
    }

}
