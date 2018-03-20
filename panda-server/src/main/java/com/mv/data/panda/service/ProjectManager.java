package com.mv.data.panda.service;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.io.ByteStreams;
import com.mv.data.panda.common.Result;
import com.mv.data.panda.common.SparkSubmitCommand;
import com.mv.data.panda.common.mapper.RowWrapper;
import com.mv.data.panda.dao.ProjectDao;
import com.mv.data.panda.model.Project;
import com.mv.data.panda.model.ProjectState;
import com.mv.data.panda.model.User;
import com.mv.data.panda.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * author: houying
 * date  : 18-1-10
 * desc  :
 */
@Service
public class ProjectManager {
    private static final Logger logger = LoggerFactory.getLogger(ProjectManager.class);
    @Resource
    private JdbcTemplate jdbcTemplate;

    private final RowWrapper<Project> projectRowMapper;

    @Resource
    private ProjectDao projectDao;

    @Value("${project.path}")
    private String projectPath;

    public ProjectManager() {
        this.projectRowMapper = new RowWrapper<>(Project.class);
    }

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        Path jarPath = Paths.get(projectPath);
        if (Files.notExists(jarPath)) {
            Files.createDirectories(jarPath);
        }
        Process process = new ProcessBuilder().command(new String[]{"which", "spark-submit"}).start();
        process.getInputStream().read(new byte[1024]);
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new IOException("spark-submit not found in $PATH");
        }
    }

    public List<Project> list(User user, int page, int size) {
        return projectDao.listByDep(user.getDepartment(), page, size);
    }

    public Result create(Project project, User user) {
        List<Project> list = jdbcTemplate.query(Sql.QUERY_PROJECT_BY_NAME, projectRowMapper, project.getName());
        if (!list.isEmpty()) {
            logger.info("创建失败，工程名称已存在");
            return new Result(-1, "该名称已存在");
        }
        jdbcTemplate.update(Sql.INSERT_PROJECT, project.getName(),
                user.getDepartment(), user.getUsername(),
                project.getManagers(), ProjectState.INIT.value(),
                project.getRetryCount(),
                project.getDescription());
        return new Result(true);
    }

    public Result upload(int id, MultipartFile file, String username) throws IOException {
        if (!checkPermission(id, username)) {
            return new Result(-1, "没有权限");
        }
        //1. save to local tmp dir
        File tmpDir = Utils.createTempDir();
        final File archive = saveFile(file, tmpDir);
        if (archive == null) {
            return new Result(-1, "保存文件失败");
        }
        try {
            //2. unzip file and check main.sh and spark-submit cmd
            String errorMsg = checkZipFile(archive);
            if (errorMsg != null) {
                return new Result(-1, errorMsg);
            }
            String dir = getOrCreateProjectPath(id);
            if (dir == null) {
                return new Result(-1, "创建工程文件目录失败");
            }
            Path target = Paths.get(dir, file.getOriginalFilename());
            Files.deleteIfExists(target);
            Files.copy(archive.toPath(), target);
            jdbcTemplate.update(Sql.UPDATE_PROJECT_ZIP_PATH, target.toString(), id);
        } finally {
            FileUtil.del(tmpDir);
        }
        return new Result(true);
    }

    public Result update(Project project, String username) {
        if (!checkPermission(project.getId(), username)) {
            return new Result(-1, "没有权限");
        }
        jdbcTemplate.update(Sql.UPDATE_PROJECT_BY_ID, project.getManagers(),
                project.getRetryCount(), project.getDescription(),
                project.getId());
        return new Result(true);
    }

    public Result uniqueProjectName(String name) {
        List<Project> list = jdbcTemplate.query(Sql.QUERY_PROJECT_BY_NAME, projectRowMapper, name);
        if (list.isEmpty()) {
            return new Result(true);
        } else {
            return new Result(false);
        }
    }

    private boolean checkPermission(long id, String username) {
        Project p = null;
        try {
            p = jdbcTemplate.queryForObject(Sql.QUERY_PROJECT_BY_ID, projectRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        if (!p.getOwner().equals(username) && !p.getManagers().contains(username)) {
            return false;
        }
        return true;
    }

    /**
     * 解压到临时目录，check zip文件合法性以及是否有main.sh脚本
     * @param archive
     * @return
     */
    private String checkZipFile(File archive) {
        File destDir = archive.getParentFile();
        try {
            ZipUtil.unzip(archive, destDir);
            File[] files = destDir.listFiles();
            if (files == null || files.length == 0) {
                return "空的zip文件";
            }
            //check main.sh exist
            Optional<File> mainSh = Arrays.stream(files).filter(file -> file.getName().equals("main.sh")).findFirst();
            if (!mainSh.isPresent()) {
                return "未找到驱动脚本main.sh";
            }
            String errorMsg = checkSparkSubmitCmd(mainSh.get());
            if (errorMsg != null) {
                return errorMsg;
            }
            return null;
        } catch (UtilException e) {
            logger.error("无法识别的zip压缩文件, {}", archive.getPath());
            return "无法识别的zip压缩文件";
        }
    }

    /**
     * 校验脚本是否包含spark-submit命令，及是否有executor-*相关参数 {@link com.mv.data.panda.common.SparkSubmitCommand}
     * @param file
     * @return
     */
    private String checkSparkSubmitCmd(File file) {
        String cmd = Utils.extractSparkSubmitCmd(file);
        if (cmd == null) {
            return "main.sh中无spark-submit命令";
        }
        SparkSubmitCommand sparkSubmitCommand = new SparkSubmitCommand(cmd);
        return sparkSubmitCommand.validate();
    }

    /**
     * 保存上传的zip包
     * @param file
     * @param tmpDir
     * @return zip包路径
     */
    private File saveFile(MultipartFile file, File tmpDir) {
        File zipFile = new File(tmpDir, file.getOriginalFilename());
        InputStream in = null;
        OutputStream out = null;
        try {
            in = file.getInputStream();
            out = new BufferedOutputStream(new FileOutputStream(zipFile));
            ByteStreams.copy(in, out);
            return zipFile;
        } catch (IOException e) {
            logger.error("文件保存失败", e);
            return null;
        } finally {
            IoUtil.close(in);
            IoUtil.close(out);
        }
    }

    private String getOrCreateProjectPath(int id) {
        Path dir = Paths.get(projectPath, String.valueOf(id));
        if (Files.exists(dir)) {
            return dir.toString();
        }
        try {
            return Files.createDirectories(dir).toString();
        } catch (IOException e) {
            logger.error("创建文件目录失败", e);
            return null;
        }
    }

    public Project detail(long id) {
        return projectDao.queryById(id);
    }
}
