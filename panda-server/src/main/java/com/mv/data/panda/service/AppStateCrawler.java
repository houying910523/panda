package com.mv.data.panda.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mv.data.panda.model.SubmitState;
import com.mv.data.panda.model.Submit;
import com.mv.data.panda.util.HttpUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * author: houying
 * date  : 17-7-3
 * desc  :
 */
public class AppStateCrawler {

    private static final Logger logger = LoggerFactory.getLogger(AppStateCrawler.class);

    private static final String URL_FORMAT = "http://%s:8088/ws/v1/cluster/apps/%s";

    public boolean curlAppInfo(Submit submit) {
        logger.info("curl app: {}", submit.getId());
        try {
            String url = buildUrl(submit.getClusterMaster(), submit.getApplicationId());
            HttpUtils.HttpResult result = HttpUtils.doGet(url);
            if (result.getStatusCode() != 200) {
                logger.warn("访问url: [{}]失败， stateCode: {}", url, result.getStatusCode());
                return false;
            }
            JSONObject jsonObject = JSON.parseObject(result.getContent());
            if (!jsonObject.containsKey("app")) {
                logger.warn("do not found application_id: {} in cluster: {}", submit.getApplicationId(), submit.getClusterMaster());
                return false;
            }
            jsonObject = jsonObject.getJSONObject("app");
            if (!jsonObject.containsKey("state")) {
                logger.warn("do not found state in application_id: {}, json: {}", submit.getApplicationId(), result.getContent());
                return false;
            }
            String state = jsonObject.getString("state");
            int intState = SubmitState.valueOf(state);
            if (intState == submit.getState()) {
                return true;
            }
            if (intState == SubmitState.RUNNING) {
                submit.setState(intState);
            } else if (intState == SubmitState.FINISHED
                    || intState == SubmitState.FAILED
                    || intState == SubmitState.KILLED) {
                submit.setState(intState);
                DateTime dateTime = new DateTime(jsonObject.getLongValue("finishedTime"));
                submit.setEndTime(dateTime.toDate());
            }
            return true;
        } catch (IOException e) {
            logger.error("抓取应用状态异常", e);
            return false;
        }
    }

    private String buildUrl(String clusterMaster, String applicationId) {
        return String.format(URL_FORMAT, clusterMaster, applicationId);
    }
}
