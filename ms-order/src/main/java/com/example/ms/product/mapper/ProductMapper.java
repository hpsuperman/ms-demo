package com.example.ms.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ms.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE t_product SET stock = stock - #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity} AND deleted_at IS NULL")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE t_product SET stock = stock + #{quantity} WHERE id = #{id} AND deleted_at IS NULL")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}