package com.mv.data.panda.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.mv.data.panda.common.mapper.RowWrapper;
import com.mv.data.panda.dao.InstanceDao;
import com.mv.data.panda.dao.ProjectDao;
import com.mv.data.panda.model.Project;
import com.mv.data.panda.model.Submit;
import com.mv.data.panda.model.SubmitParam;
import com.mv.data.panda.model.SubmitState;
import com.mv.data.panda.util.Mail;
import com.mv.data.panda.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.mv.data.panda.model.SubmitState.*;

/**
 * author: houying
 * date  : 17-7-3
 * desc  :
 */
@Component
public class AppStateManager {
    private final static Logger logger = LoggerFactory.getLogger(AppStateManager.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ProjectDao projectDao;
    @Resource
    private InstanceDao instanceDao;
    @Resource
    private InstanceService instanceService;

    private Thread thread;
    private Map<Long, Submit> map;
    private Cache<String, Boolean> errorAppNames;
    private AppStateCrawler crawler;

    @PostConstruct
    private void init() {
        List<Submit> submitList = jdbcTemplate.query(Sql.QUERY_UN_FINISHED, new RowWrapper<>(Submit.class));
        //submit 里面只包含id, clusterMaster, applicationId, state四个字段的值
        map = Maps.newConcurrentMap();
        for (Submit submit: submitList) {
            map.put(submit.getId(), submit);
        }
        crawler = new AppStateCrawler();
        errorAppNames = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.DAYS).build();
        thread = new CrawlerStateThread();
        thread.setName("state-manage-thread");
        thread.setDaemon(true);
        thread.start();
    }

    public void register(long id, String clusterMaster, String applicationId, int state) {
        Submit submit = new Submit();
        submit.setId(id);
        submit.setClusterMaster(clusterMaster);
        submit.setApplicationId(applicationId);
        submit.setState(state);
        map.put(id, submit);
        //errorAppNames.invalidate(name);
    }

    public void sync(Submit submit) {
        int state = submit.getState();
        boolean isContinue = crawler.curlAppInfo(submit);
        if (!isContinue) {
            map.remove(submit.getId());
            return;
        }
        if (state == submit.getState()) {
            return;
        }
        switch (submit.getState()) {
        case RUNNING:
            jdbcTemplate.update(Sql.UPDATE_INSTANCE_STATE, submit.getState(), submit.getId());
            logger.info("update state to {} of id: {}", valueOf(submit.getState()), submit.getId());
            break;
        case FAILED:
        case KILLED:
        case FINISHED:
            instanceDao.updateEndTime(submit.getId(), submit.getState(), submit.getEndTime());
            logger.info("update state to {}, end_time to {} of id: {}", valueOf(submit.getState()),
                    submit.getEndTime(), submit.getId());
            map.remove(submit.getId());
            submit = instanceDao.queryById(submit.getId());
            Project project = projectDao.queryById(submit.getProjectId());
            sendStopAlarm(submit, project);
            exportAlert(submit, project);
            triggerSubmit(submit, project);
            break;
        default:
            break;
        }
    }

    private void exportAlert(Submit submit, Project project) {
        //errorAppNames.put(submit.getName(), true);
    }

    public Set<String> errorApplications() {
        return errorAppNames.asMap().keySet();
    }

    @VisibleForTesting
    public void add(String name) {
        errorAppNames.put(name, true);
    }

    @VisibleForTesting
    public void remove(String name) {
        errorAppNames.invalidate(name);
    }


    private boolean canRetry(Project project, Submit submit) {
        return (submit.getState() == FAILED || submit.getState() == FINISHED)
                && project.getRetryCount() > submit.getAttempt();
    }

    private void triggerSubmit(Submit submit, Project project) {
        if (!canRetry(project, submit)) {
            sendRetryFailAlarm(submit, project, "已达最大拉起次数上限");
            return;
        }
        try {
            instanceService.autoDeploy(project);
        } catch (IOException e) {
            logger.error("自动发布失败", e);
            sendRetryFailAlarm(submit, project, e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRetryFailAlarm(final Submit submit, Project project, String message) {
        new Thread(() -> {
            long t1 = System.currentTimeMillis();
            String title = "[通知]您的streaming应用： " + project.getName() + "自动拉起失败";
            String content = "<p>" + message + "</p>\n";
            content = content + "<p>集群：" + submit.getClusterMaster() + "</p>\n";
            content = content + "<p>状态：" + SubmitState.valueOf(submit.getState()) + "</p>\n";
            Mail.send(Utils.convertToEmail(project.getManagers()), title, content);
            long t2 = System.currentTimeMillis();
            logger.info("send mail cost {} ms", t2 - t1);
        }).start();
    }

    private void sendStopAlarm(final Submit submit, Project project) {
        new Thread(() -> {
            long t1 = System.currentTimeMillis();
            String title = "[通知]您的streaming应用： " + project.getName() + "停止运行";
            String content = "<p>集群：" + submit.getClusterMaster() + "</p>\n";
            content = content + "<p>状态：" + SubmitState.valueOf(submit.getState()) + "</p>\n";
            content = content + "<p>Application id：" + submit.getApplicationId() + "</p>\n";
            content = content + "<p>Tracking Url：<a href=\"" + submit.getTrackingUrl() + "\">" + submit.getTrackingUrl() + "<a></p>\n";
            content = content + "<p>Start Time：" + submit.getStartTime() + "</p>\n";
            content = content + "<p>End Time：" + submit.getEndTime() + "</p>\n";
            Mail.send(Utils.convertToEmail(project.getManagers()), title, content);
            long t2 = System.currentTimeMillis();
            logger.info("send mail cost {} ms", t2 - t1);
        }).start();
    }

    private class CrawlerStateThread extends Thread {

        private boolean sleepSecond(int second) {
            try {
                Thread.sleep(second * 1000);
                return true;
            } catch (InterruptedException e) {
                logger.info("{} has been interrupted", thread.getName());
                return false;
            }
        }
        @Override
        public void run() {
            int sleepSecond = 60;
            do {
                try {
                    map.values().forEach(AppStateManager.this::sync);
                } catch (Exception e) {
                    logger.info("exception: ", e);
                }
            } while(sleepSecond(sleepSecond));
        }

    }
}
