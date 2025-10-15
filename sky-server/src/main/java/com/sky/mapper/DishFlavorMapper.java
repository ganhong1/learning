package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * 批量查询菜品口味信息
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    @Delete("delete from dish_flavor where dish_id=#{dish_id}")
    void deleteByDishId(Long id);

    void deleteByDishIds(List<Long> ids);

    @Select("select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> getByDishId(Long id);
}
