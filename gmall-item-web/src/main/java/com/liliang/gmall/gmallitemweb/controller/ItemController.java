package com.liliang.gmall.gmallitemweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.bean.SkuSaleAttrValue;
import com.liliang.gmall.bean.SpuSaleAttr;
import com.liliang.gmall.config.LoginRequire;
import com.liliang.gmall.service.ItemService;
import com.liliang.gmall.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品详情
 * @author liliang
 * @since 2019.6.12
 */
@Controller
public class ItemController {

    @Reference
    private ItemService itemService;

    @Reference
    private ListService listService;

    /**
     * 根据skuId查看商品详情页面
     * @param skuId
     * @return
     */
    @LoginRequire(autoRedirect = false)
    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable String skuId,
                              HttpServletRequest request){

        System.out.println("skuId:"+skuId);

        //通过skuId查询到指定的sku_Info的信息
        SkuInfo skuInfo = itemService.getSkuInfoBySkuId(skuId);

        //得到spuId的值
        String spuId = skuInfo.getSpuId();

        request.setAttribute("skuInfo",skuInfo);

        //通过skuId查询到指定的sku_image的信息
//        List<SkuImage> skuImageList = itemService.getSkuImageBySkuId(skuId);
//        request.setAttribute("skuImage",skuImageList);

        //根据skuId ，spuId 查询销售属性，销售属性值并加锁定数据
        List<SpuSaleAttr> spuSaleAttrList = itemService.getSpuSaleAttrListCheckBySkuId(spuId,skuId);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        // 获取销售属性值Id集合拼接字符串
        List<SkuSaleAttrValue> skuSaleAttrValueList = itemService.getSkuSaleAttrValueListBySpuId(spuId);

        //从skuSaleAttrValueList中拼接字符串
        Map<String, String> map = new HashMap<>();
        String saleAttrValueId="";
        for (int i = 1; i < skuSaleAttrValueList.size(); i++) {

            //通过比较skuId来得到要不要拼接
            if (skuSaleAttrValueList.get(i-1).getSkuId().equals(skuSaleAttrValueList.get(i).getSkuId())){
                //拼接字符串，格式如：12|13|14|
                saleAttrValueId=saleAttrValueId+skuSaleAttrValueList.get(i-1).getSaleAttrValueId()+"|";
            }else {

                saleAttrValueId=saleAttrValueId+skuSaleAttrValueList.get(i-1).getSaleAttrValueId();

                //截取字符串，去掉最后一个“|”
                //saleAttrValueId.substring(0,saleAttrValueId.length()-1);

                System.out.println("saleAttrValueId:"+saleAttrValueId);

                //把拼接好的字符串和对呀的skuId 放到map中
                map.put(saleAttrValueId,skuSaleAttrValueList.get(i-1).getSkuId());

                //清空saleAttrValueId
                saleAttrValueId="";
            }
        }

        //把最后一个拼接好的字符串和对呀的skuId 放到map中
        saleAttrValueId=saleAttrValueId+skuSaleAttrValueList.get(skuSaleAttrValueList.size()-1).getSaleAttrValueId();
        map.put(saleAttrValueId,skuSaleAttrValueList.get(skuSaleAttrValueList.size()-1).getSkuId());

        //把map集合转成json字符串格式
        String valuesSkuJson = JSON.toJSONString(map);
        //String valuesSkuJson = (String) JSON.toJSON(map);
        System.out.println("valuesSkuJson:"+valuesSkuJson);

        request.setAttribute("valuesSkuJson",valuesSkuJson);

        listService.incrHotScore(skuId);

        return "item";
    }
}
