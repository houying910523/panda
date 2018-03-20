package com.mv.data.panda.service;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.mv.data.panda.common.Result;
import com.mv.data.panda.common.mapper.RowWrapper;
import com.mv.data.panda.dao.InstanceDao;
import com.mv.data.panda.model.Submit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
@Service
public class HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private static final String HISTORY_URL = "http://%s:8088/cluster/app/%s";

    @Resource
    private InstanceDao instanceDao;

    @Resource
    private InstanceService instanceService;

    public List<Submit> history(long projectId, int page, int size) {
        int start = (page - 1) * size;
        List<Submit> submitList = instanceDao.queryHistoryByPid(projectId, start, size);
        for (Submit submit : submitList) {
            submit.setTrackingUrl(String.format(HISTORY_URL, submit.getClusterMaster(), submit.getApplicationId()));
        }
        return submitList;
    }

    public List<String> log(long instanceId) throws IOException {
        Submit submit = instanceDao.queryById(instanceId);
        if (submit == null) {
            return null;
        }
        long pid = submit.getProjectId();
        String logFilePath = instanceService.logPath(pid, instanceId);
        File logFile = new File(logFilePath);
        if (!logFile.exists() || logFile.isDirectory()) {
            logger.warn("日志文件未找到");
            return null;
        }
        return Files.readLines(logFile, Charsets.UTF_8);
    }
}
