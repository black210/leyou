package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    public SpuMapper spuMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    /**
     * 根据条件分页查询spu
     *
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //添加查询条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //添加上下架的过滤条件
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //添加分页
        PageHelper.startPage(page, rows);
        //执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> PageInfo = new PageInfo<>(spus);
        //spu集合转化成spubo集合
        List<SpuBo> spuBos = spus.stream().map(spu -> {
                    SpuBo spuBo = new SpuBo();
                    //copy共同属性的值到新对象
                    BeanUtils.copyProperties(spu, spuBo);
                    //查询品牌名称
                    Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
                    spuBo.setBname(brand.getName());
                    //查询分类名称
                    List<String> names = this.categoryService.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
                    spuBo.setCname(StringUtils.join(names, "/"));
                    return spuBo;//勿忘
                }
        ).collect(Collectors.toList());

        //返回pageResult<spuBo>
        return new PageResult<>(PageInfo.getTotal(), spuBos);

    }

    /**
     * 新增商品
     *
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //先新增spu
        //新增默认字段
        spuBo.setId(null);//防止恶意注入
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());

        this.spuMapper.insertSelective(spuBo);

        //再去新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());

        this.spuDetailMapper.insertSelective(spuDetail);
        saveSkuAndStock(spuBo);

    }

    /**
     * 新增商品及库存
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            //新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            this.skuMapper.insertSelective(sku);

            //新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            this.stockMapper.insertSelective(stock);

        });

    }

    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);//主键查询
    }

    /**
     * 根据spuId查询sku集合
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku =new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus =this.skuMapper.select(sku);//不可直接返回，还需获取库存
        skus.forEach(s -> {
            Stock stock =this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 商品更新
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //查询以前的sku
        List<Sku> skus =this.querySkusBySpuId(spuBo.getId());
        //如果存在，则删除
        if (!CollectionUtils.isEmpty(skus)){
            List<Long> ids =skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            //删除以前的库存
            Example example =new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);
            //删除以前的sku
            Sku record =new Sku();
            record.setSpuId(spuBo.getId());
            this.skuMapper.delete(record);
        }
        //新增sku和库存
        saveSkuAndStock(spuBo);
        //更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        //更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

    }
}
