package com.example.controller;

import com.example.Service.UserService;
import com.example.common.Result;
import com.example.entity.User;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

@RestController
//通过注解@RequestMapping来指定访问路径
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;

    /*新增数据*/
    @PostMapping("/add")
    public Result add(@RequestBody User user){
        userService.add(user);
        return Result.success();
    }

    /*更新数据*/
    @PutMapping("/update")
    public Result update(@RequestBody User user){
        userService.update(user);
        return Result.success();
    }

    /*根据id删除数据*/
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        userService.deleteById(id);
        return Result.success();
    }
    /**
     * 查询所有用户
     */
    //接口路径为/users/selectAll
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<User> users = userService.selectAll();
        return Result.success(users);
    }

    //多个参数在后面再加/即可，如/users/selectById/{id}/{name} 等
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }
    //或者通过@RequestParam注解来获取参数,查询方法 /users/selectByName?name=xxx(&age=xxx, &sex=xxx等多参数)
/*    @GetMapping("/selectOne")
    public Result selectByName(@RequestParam String name){
        User user = userService.selectByName(name);
        return Result.success(user);
    } */

    //也可以通过实体类来接收参数,这样不固定参数数量
    @GetMapping("/selectList")
    public Result selectList(User user) {
        List<User> list = userService.selectList(user);
        return Result.success(list);
    }

    //分页查询
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> pageInfo = userService.selectPage(pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
