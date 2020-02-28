package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {//尽量使用#代替$，效率更高

    /**
     * 新增商品分类和品牌中间表数据
     * @param cid
     * @param id
     */
    @Insert("INSERT INTO tb_category_brand(category_id, brand_id) VALUES (#{cid}, #{bid})")
    void insertCategoryAndBrand(@Param("cid") Long cid,@Param("bid") Long id);


    @Select("SELECT * from tb_brand b INNER JOIN tb_category_brand cb on b.id=cb.brand_id where cb.category_id=#{cid}")
    List<Brand> selectBrandsByCid(Long cid);
}
