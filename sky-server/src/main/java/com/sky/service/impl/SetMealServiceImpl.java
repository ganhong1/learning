package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {


    @Autowired
    private SetmealMapper setmealMapper;


/*新增套餐接口
*
* */
    @Override
    public void addSetMeal(SetmealDTO setmealDTO) {

        /*标准化实体类*/
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        /*新增套餐基础信息*/
        setmealMapper.addSetMeal(setmeal);
        /*主键返回取得套餐ID*/
        setmealDTO.setId(setmeal.getId());
        /*新增套餐内菜品信息*/
        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealMapper.addSetMealDishes(setmealDTO.getSetmealDishes());
    }

/*根据Id查询套餐
 *
 * */
    @Override
    @Transactional
    public SetmealVO searchSetMeal(Long id) {
        SetmealVO setmealVO = setmealMapper.searchSetMeal(id);
        setmealVO.setSetmealDishes(setmealMapper.searchSetmealDishById(id));
        return setmealVO;
    }

/*根据分类Id查询菜品
*
* */
    @Override
    public List<DishVO> searchDishByCateId(Integer categoryId) {
        List<DishVO> dishVOList = setmealMapper.searchDishByCateId(categoryId);
        return dishVOList;
    }
/*套餐分页查询
 *
 * */
    @Override
    public PageResult searchSetMealByCondition(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.searchSetMealByCondition(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
/*删除套餐
*
* */
    @Override
    @Transactional
    public void deleteSetMeal(List<Long> ids) {
        /*判断套餐起售状态*/
        for (Long id : ids) {
            SetmealVO setmealVO = setmealMapper.searchSetMeal(id);
            if (setmealVO.getStatus() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        /*删除套餐基础信息*/
        setmealMapper.deleteSetMeal(ids);
        /*删除套餐内的菜品信息*/
        setmealMapper.deleteSetMealDishes(ids);
    }
/*更新套餐
*
* */
    @Override
    @Transactional
    public void updateSetMealById(SetmealDTO setmealDTO) {
//        /*更新前统一删除套餐除id外所有信息*/
//            List<Long> ids = new ArrayList<>();
//            ids.add(setmealDTO.getId());
//            /*删除套餐基础信息*/
//            setmealMapper.deleteSetMeal(ids);
//            /*删除套餐内的菜品信息*/
//            setmealMapper.deleteSetMealDishes(ids);
//        /*标准化实体类*/
//        Setmeal setmeal = new Setmeal();
//        BeanUtils.copyProperties(setmealDTO, setmeal);
//        /*新增套餐基础信息*/
//        setmealMapper.addSetMeal(setmeal);
//        /*主键返回取得套餐ID*/
//        setmealDTO.setId(setmeal.getId());
//        /*新增套餐内菜品信息*/
//        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
//            setmealDish.setSetmealId(setmeal.getId());
//        }
//        setmealMapper.addSetMealDishes(setmealDTO.getSetmealDishes());

        /*对套餐的基本信息做修改操作*/
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.updateSetmealById(setmeal);

        /*删除套餐内所有菜品,并重新新增*/
        List<Long> ids = new ArrayList<>();
        ids.add(setmeal.getId());
        setmealMapper.deleteSetMealDishes(ids);
        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealMapper.addSetMealDishes(setmealDTO.getSetmealDishes());



    }

    @Override
    public void updateSetmealStatusById(Integer status, Long id) {
        /*先查询套餐内包含了那些菜品,再查询这些菜品的当前状态*/
        List<Long> ids = new ArrayList<>();
        List<SetmealDish> setmealDishes = setmealMapper.searchSetmealDishById(id);
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.getDishId();
            ids.add(setmealDish.getDishId());
        }
        List<Dish> dishes = setmealMapper.searchDishByIds(ids);
        log.info(dishes.toString());
        for (Dish dish : dishes) {
            /*判断套餐内菜品状态,存在停售菜品则禁止套餐起售*/
            if (dish.getStatus() == 0) {
                log.info("侦测到停售菜品,菜品id为;{}",dish.getId());
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        log.info("未侦测到停售菜品");
        /*开始更改套餐的状态*/
        Setmeal setmeal = Setmeal.builder()
                        .status(status)
                        .id(id).build();

        setmealMapper.updateSetmealStatusById(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
