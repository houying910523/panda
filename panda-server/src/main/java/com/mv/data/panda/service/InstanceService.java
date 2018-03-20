package com.mv.data.panda.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.base.Preconditions;
import com.mv.data.panda.common.SparkSubmitCommand;
import com.mv.data.panda.common.shell.Shell;
import com.mv.data.panda.dao.ClusterDao;
import com.mv.data.panda.dao.InstanceDao;
import com.mv.data.panda.dao.ProjectDao;
import com.mv.data.panda.model.*;
import com.mv.data.panda.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * author: houying
 * date  : 18-1-23
 * desc  :
 */
@Service
public class InstanceService {
    private static final Logger logger = LoggerFactory.getLogger(InstanceService.class);

    private static final String MAKE_HADOOP_CONF_CMD = "make_hadoop_conf.sh";
    private static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    private static final String SPARK_CONF_DIR = "SPARK_CONF_DIR";
    private static final String YARN_CONF_DIR = "YARN_CONF_DIR";
    private static final String SUBMIT_LOG = "submit.log";

    @Value("${cluster.conf.path}")
    private String clusterConfPath;
    @Value("${deploy.path}")
    private String deployPath;
    @Value("${log.path}")
    private String logPath;
    @Value("${yarn.kill.cmd}")
    private String yarnKillCmd;
    @Resource
    private ProjectDao projectDao;
    @Resource
    private ClusterDao clusterDao;
    @Resource
    private InstanceDao instanceDao;
    @Resource
    private AppStateManager appStateManager;

    private String classpath;

    private ThreadPoolExecutor pool;

    @PostConstruct
    public void init() throws IOException, InterruptedException, URISyntaxException {
        classpath = Paths.get(InstanceService.class.getClassLoader().getResource(MAKE_HADOOP_CONF_CMD).toURI())
                .getParent().toString();
        Path dp = Paths.get(deployPath);
        if (Files.notExists(dp)) {
            Files.createDirectories(dp);
        }
        Path lp = Paths.get(logPath);
        if (Files.notExists(lp)) {
            Files.createDirectories(lp);
        }
        Path confPath = Paths.get(clusterConfPath);
        if (Files.notExists(confPath)) {
            Files.createDirectories(confPath);
        }
        Process process = new ProcessBuilder().command(new String[]{"which", "spark-submit"}).start();
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new IOException("spark-submit not found");
        }
        pool = new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10));
    }

    public void autoDeploy(Project project) throws IOException, InterruptedException {
        String clusterMaster = clusterDao.queryClusterByDep(project.getDep());
        Preconditions.checkNotNull(clusterMaster, "集群不存在： " + project.getDep());
        String configDir = getOrCreateConfigDir(project.getDep(), clusterMaster);
        File deployDir = new File(deployPath(project.getId()));
        File main = new File(deployDir, "main.sh");
        String cmd = Utils.extractSparkSubmitCmd(main);
        SubmitResource resource = new SparkSubmitCommand(cmd).getSubmitResource();
        //insert deploy record into db
        int tid = instanceDao.insert(project.getId(), "AUTO_RETRY", clusterMaster, 0,
                resource.getExecutorNum(), resource.getExecutorCores(), resource.getExecutorMemory());
        deploy(project.getId(), tid, main, configDir, clusterMaster);
    }

    public long deployRequest(long projectId, User user) {
        Project project = projectDao.queryById(projectId);
        Preconditions.checkNotNull(project, "未找到该工程，project: " + projectId);
        Preconditions.checkState(checkPermission(project.getOwner(), project.getManagers(), user.getUsername()), "没有权限");
        Preconditions.checkState(project.getState() == ProjectState.UPLOAD.value() || project.getState() == ProjectState.STOP.value(), "状态不正确，无法发布");
        //get cluster master according to department
        String clusterMaster = clusterDao.queryClusterByDep(project.getDep());
        Preconditions.checkNotNull(clusterMaster, "集群不存在： " + project.getDep());

        //get or create config dir and file
        String configDir = getOrCreateConfigDir(project.getDep(), clusterMaster);
        File deployDir = new File(deployPath(project.getId()));

        //unzip archive
        logger.info("unzip archive: {}", project.getZipPath());
        FileUtil.del(deployDir);
        FileUtil.mkdir(deployDir);
        ZipUtil.unzip(new File(project.getZipPath()), deployDir);
        File main = new File(deployDir, "main.sh");
        //parse deploy resource
        String cmd = Utils.extractSparkSubmitCmd(main);
        SubmitResource resource = new SparkSubmitCommand(cmd).getSubmitResource();
        //insert deploy record into db
        int tid = instanceDao.insert(project.getId(), user.getUsername(), clusterMaster, 0,
                resource.getExecutorNum(), resource.getExecutorCores(), resource.getExecutorMemory());

        pool.execute(() -> {
            try {
                deploy(projectId, tid, main, configDir, clusterMaster);
            } catch (InterruptedException e) {
                logger.error("interrupted exception", e);
            } catch (Exception e) {
                logger.error("error when deploy async", e);
            }
        });
        return tid;
    }

    public boolean kill(int instanceId, User user) throws InterruptedException {
        Submit submit = instanceDao.queryById(instanceId);
        Project project = projectDao.queryById(submit.getProjectId());
        Preconditions.checkNotNull(project, "未找到该工程，instanceId: " + instanceId);
        Preconditions.checkState(checkPermission(project.getOwner(), project.getManagers(), user.getUsername()), "没有权限");
        Preconditions.checkState(submit.getState() == SubmitState.RUNNING ||
                submit.getState() == SubmitState.ACCEPTED, "状态不正确, 无法kill");
        Preconditions.checkState(project.getState() == ProjectState.RUNNING.value(), "状态不正确，无法kill");
        String clusterMaster = submit.getClusterMaster();
        String configDir = getOrCreateConfigDir(project.getDep(), clusterMaster);
        String killCmd = String.format(yarnKillCmd, submit.getApplicationId());
        String hadoopConfDir = hadoopConfDir(configDir);
        Shell shell = new Shell(killCmd, deployPath(project.getId()), null);
        shell.setEnv(HADOOP_CONF_DIR, hadoopConfDir)
                .setEnv(YARN_CONF_DIR, hadoopConfDir);
        shell.start(pool);
        if (shell.isSuccess()) {
            appStateManager.sync(submit);
            return true;
        } else {
            return false;
        }
    }

    public int state(long tid) {
        Submit submit = instanceDao.queryById(tid);
        if (submit == null) {
            return -1;
        } else {
            return submit.getState();
        }
    }

    public Submit detail(long projectId) {
        return instanceDao.queryRunningByPid(projectId);
    }

    public boolean restart(long instanceId,  User user) {
        //TODO
        return true;
    }

    @Transactional
    private void deploy(long projectId, long tid, File main, String configDir, String clusterMaster)
            throws InterruptedException, IOException {
        //exec main.sh
        String dir = main.getParent();
        String log = logPath(projectId, tid);
        logger.info("execute script: {}", main.getPath());
        SparkSubmitShell shell = new SparkSubmitShell(tid, "bash " + main.getPath(), dir, log);
        shell.setEnv(HADOOP_CONF_DIR, hadoopConfDir(configDir)).setEnv(SPARK_CONF_DIR, sparkConfDir(configDir));
        shell.start(pool);
        boolean completed = shell.awaitCompletion(30000L);
        if (!completed) {
            logger.warn("发布脚本没有完成");
            shell.kill();
            instanceDao.updateState(tid, SubmitState.FAILED);
            return;
        }
        if (shell.isFailed()) {
            logger.warn("发布失败");
            instanceDao.updateState(tid, SubmitState.FAILED);
            return;
        }
        instanceDao.update(tid, shell.getApplicationId(), shell.getTrackingUrl(), shell.getStartTime(), shell.getState());
        projectDao.updateState(projectId, ProjectState.RUNNING.value());
        appStateManager.register(tid, clusterMaster, shell.getApplicationId(), shell.getState());
    }

    private String getOrCreateConfigDir(String dep, String masterAddress) {
        Path path = Paths.get(clusterConfPath, dep, masterAddress);
        if (Files.exists(path) && new File(path.toString()).list().length != 0) {
            return path.toString();
        }
        try {
            Files.deleteIfExists(path);
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.error("create hadoop conf dir error", e);
            return null;
        }
        logger.info("create HADOOP_CONF_DIR={}", path);
        String cmd = "bash " + MAKE_HADOOP_CONF_CMD + " " + masterAddress + " " + path.toString();
        Shell shell = new Shell(cmd, classpath, null);
        shell.start(pool);
        try {
            if (shell.isFailed()) {
                logger.error("create hadoop conf file error");
                return null;
            }
        } catch (InterruptedException e) {
            return null;
        }
        return path.toString();
    }

    private boolean checkPermission(String owner, String managers, String username) {
        return owner.equals(username) || managers.contains(username);
    }

    public String logPath(long projectId, long instanceId) throws IOException {
        Path logDir = Paths.get(logPath, projectId + "");
        if (Files.notExists(logDir)) {
            Files.createDirectories(logDir);
        }
        Path path = logDir.resolve(instanceId + "_" + SUBMIT_LOG);
        return path.toString();
    }

    private String deployPath(long projectId) {
        return Paths.get(deployPath, projectId + "").toString();
    }

    private String hadoopConfDir(String configDir) {
        return configDir + "/hadoop";
    }
    private String sparkConfDir(String configDir) {
        return configDir + "/spark";
    }

}
