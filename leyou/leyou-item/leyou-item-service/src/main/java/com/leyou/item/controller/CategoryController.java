package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据（pid）父节点id查询子节点
     * 请求方式：get
     *请求路径：/api/item/category/list。
     * 请求参数：pid=0
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid",defaultValue = "0")Long pid){
        if (pid == null || pid.longValue() < 0){
            // 响应400，不合法。相当于ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories =this.categoryService.queryCategoriesByPid(pid);
            //404    判断是否为空
        if (CollectionUtils.isEmpty(categories)){
            return ResponseEntity.notFound().build();
        }
        //200 succeed
        return ResponseEntity.ok(categories);
        }

    /**
     * 根据id查询名称
     * @param ids
     * @return
     */
    @GetMapping("names")
        public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids")List<Long> ids){
        List<String> names =this.categoryService.queryNameByIds(ids);
        if (CollectionUtils.isEmpty(names)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);
        }
    }
