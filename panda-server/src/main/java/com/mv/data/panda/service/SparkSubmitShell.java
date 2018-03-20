package com.mv.data.panda.service;

import com.mv.data.panda.common.shell.Shell;
import com.mv.data.panda.model.SubmitState;
import com.mv.data.panda.util.SpringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: houying
 * date  : 18-2-5
 * desc  :
 */
public class SparkSubmitShell extends Shell {
    private static final Logger logger = LoggerFactory.getLogger(SparkSubmitShell.class);

    private final Pattern applicationIdPattern = Pattern.compile("application_[\\d_]+");
    private final Pattern statePattern = Pattern.compile("\\(state: ([A-Z]+)\\)");
    private int state = SubmitState.SUBMITTED;
    private String applicationId = null;
    private String startTime = null;
    private String trackingUrl = null;
    private long tid;

    public SparkSubmitShell(long tid, String cmd, String dir, String output) {
        super(cmd, dir, output);
        this.tid = tid;
    }

    @Override
    protected void process(String line) {
        switch (state) {
        case SubmitState.SUBMITTED:
            if (line.contains("Application report for")) {
                applicationId = parseApplicationId(line);
                state = parseState(line);
                SpringUtils.getInstanceDao().updateState(tid, state);
            }
            break;
        case SubmitState.ACCEPTED:
            if (line.contains("start time: ")) {
                long timestamp = Long.valueOf(line.trim().split(": ")[1]);
                DateTime dateTime = new DateTime(timestamp);
                startTime = dateTime.toString("yyyy-MM-dd HH:mm:ss");
            } else if (line.contains("tracking URL: ")) {
                handleTrackingUrl(line);
            } else if (line.contains("Application report for")) {
                state = parseState(line);
            }
            break;
        case SubmitState.RUNNING:
            logger.info("state turn to {} and kill process", SubmitState.valueOf(state));
            kill();
            break;
        default:
            break;
        }
    }

    private String parseApplicationId(String line) {
        Matcher m = applicationIdPattern.matcher(line);
        if (!m.find()) {
            return applicationId;
        }
        return m.group();
    }

    private int parseState(String line) {
        Matcher m = statePattern.matcher(line);
        if (m.find()) {
            String stateStr = m.group(1);
            return SubmitState.valueOf(stateStr);
        }
        return state;
    }

    private void handleTrackingUrl(String outputLine) {
        trackingUrl = outputLine.trim().split(": ")[1];
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public int getState() {
        return state;
    }
}
