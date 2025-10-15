package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

/*新增套餐接口
 *
 * */
    @PostMapping("/setmeal")
    public Result addSetMeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("激活新增套餐,新增信息为:{}", setmealDTO);
        setMealService.addSetMeal(setmealDTO);
        return Result.success();
    }

/*根据分类Id查询菜品
*
* */
    @GetMapping("/dish/list")
    public Result<List<DishVO>> searchDishByCateId(Integer categoryId) {
        log.info("激活根据分类Id查询菜品,分类Id为:{}", categoryId);
        List<DishVO> dishVOList = setMealService.searchDishByCateId(categoryId);
        return Result.success(dishVOList);
    }



/*根据Id查询套餐
*
* */
    @GetMapping("/setmeal/{id}")
    public Result<SetmealVO> searchSetMeal(@PathVariable  Long id) {
        log.info("激活套餐查询,套餐id为:{}",id);
        SetmealVO setmealVO = setMealService.searchSetMeal(id);
        return Result.success(setmealVO);
    }

/*套餐分页查询
*
* */
    @GetMapping("/setmeal/page")
    public Result<PageResult> searchSetMealByCondition(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("激活套餐分页查询功能,传入参数为:{}", setmealPageQueryDTO);
        PageResult setmealVO = setMealService.searchSetMealByCondition(setmealPageQueryDTO);
        return Result.success(setmealVO);
    }

/*删除套餐
*
* */
    @DeleteMapping("/setmeal")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result deleteSetMeal(@RequestParam List<Long> ids) {
        log.info("激活批量删除套餐,批量ids为:{}",ids);
        setMealService.deleteSetMeal(ids);
        return Result.success();
    }

    @PutMapping("/setmeal")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result updateSetMealById(@RequestBody SetmealDTO setmealDTO) {
        log.info("激活套餐更新,更新数据为:{}", setmealDTO);
        setMealService.updateSetMealById(setmealDTO);
        return Result.success();
    }

    @PostMapping("/setmeal/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result updateStatus(@PathVariable Integer status,@RequestParam Long id) {
        log.info("激活状态切换,切换目标状态为:{}", status);
        setMealService.updateSetmealStatusById(status,id);
        return Result.success();
    }
}

