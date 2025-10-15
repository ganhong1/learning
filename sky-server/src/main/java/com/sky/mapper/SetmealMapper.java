package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @AutoFill(OperationType.INSERT)
    void addSetMeal(Setmeal setmeal);

    void addSetMealDishes(List<SetmealDish> setmealDishes);

    @Select("select * from setmeal where id=#{id}")
    SetmealVO searchSetMeal(Long id);

    @Select("select * from dish where category_id=#{categoryId}")
    List<DishVO> searchDishByCateId(Integer categoryId);

    Page<SetmealVO> searchSetMealByCondition(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteSetMeal(List<Long> ids);

    void deleteSetMealDishes(List<Long> ids);

    @Select("select id,setmeal_id,dish_id,name,price,copies from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> searchSetmealDishById(Long id);

    List<Dish> searchDishByIds(List<Long> ids);

    @Update("update setmeal set status=#{status},update_time=#{updateTime},update_user=#{updateUser} where id=#{id}")
    @AutoFill(OperationType.UPDATE)
    void updateSetmealStatusById(Setmeal setmeal);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    void updateSetmealById(Setmeal setmeal);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
