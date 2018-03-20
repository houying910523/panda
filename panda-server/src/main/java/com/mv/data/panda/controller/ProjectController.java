package com.mv.data.panda.controller;

import com.google.common.base.Preconditions;
import com.mv.data.panda.common.Result;
import com.mv.data.panda.common.auth.UserCache;
import com.mv.data.panda.model.Project;
import com.mv.data.panda.model.User;
import com.mv.data.panda.service.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * author: houying
 * date  : 18-1-11
 * desc  :
 */
@RestController
@RequestMapping("/project")
public class ProjectController {
    private final static Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Resource
    private ProjectManager projectManager;
    @Resource
    private UserCache userCache;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result list(@RequestParam int page, @RequestParam int size,
            @CookieValue("login_user") String username) {
        User user = userCache.getLoginUser(username);
        try {
            List<Project> projectList = projectManager.list(user, page, size);
            return new Result(projectList);
        } catch (Exception e) {
            logger.error("查询project列表失败", e);
            return new Result(-1, "查询失败");
        }
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Result create(@RequestBody Project project,
            @CookieValue("login_user") String username) {
        User user = userCache.getLoginUser(username);
        try {
            return projectManager.create(project, user);
        } catch (Exception e) {
            logger.error("error when create [name={}, managers={}, username={}]", project.getName(), project.getManagers(), username, e);
            return new Result(-1, e.getMessage());
        }
    }

    /**
     * 本接口只能更新name, managers, retryCount三个属性
     * @param project
     * @param username
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Result update(@RequestBody Project project,
            @CookieValue("login_user") String username) {
        User user = userCache.getLoginUser(username);
        try {
            return projectManager.update(project, user.getUsername());
        } catch (Exception e) {
            logger.error("error when update [name={}, managers={}, retryCount={}, username={}]",
                    project.getName(), project.getManagers(), project.getRetryCount(), username, e);
            return new Result(-1, e.getMessage());
        }
    }

    @RequestMapping("/unique/{name}")
    public Result unique(@PathVariable String name) {
        return projectManager.uniqueProjectName(name);
    }

    @RequestMapping(value = "/upload/{id}", method = RequestMethod.POST)
    public Result upload(@PathVariable int id,
            @RequestParam("file") MultipartFile file,
            @CookieValue("login_user") String username) {

        User user = userCache.getLoginUser(username);
        String filename = file.getOriginalFilename();
        try {
            logger.info("file = {}, id={}", filename, id);
            if (!filename.endsWith(".zip")) {
                return new Result(-1, "仅支持zip格式文件");
            }
            return projectManager.upload(id, file, user.getUsername());
        } catch (Exception e) {
            logger.error("上传文件出错 [id={}, filename={}]", id, filename, e);
            return new Result(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/detail/{id}")
    public Result detail(@PathVariable long id,
            @CookieValue("login_user") String username) {
        userCache.getLoginUser(username);
        Project project = projectManager.detail(id);
        if (project != null) {
            return new Result(project);
        } else {
            return new Result(-1, "工程不存在");
        }
    }
}
