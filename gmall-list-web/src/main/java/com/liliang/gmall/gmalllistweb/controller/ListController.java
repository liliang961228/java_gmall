package com.liliang.gmall.gmalllistweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.BaseAttrInfo;
import com.liliang.gmall.bean.BaseAttrValue;
import com.liliang.gmall.bean.dto.SkuLsInfo;
import com.liliang.gmall.bean.dto.SkuLsParams;
import com.liliang.gmall.bean.dto.SkuLsResult;
import com.liliang.gmall.service.ListService;
import com.liliang.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    //http://list.gmall.com/list.html?keyword=
    //http://list.gmall.com/list.html?catalog3Id=61

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    //@LoginRequire
    @RequestMapping("list.html")
    public String list(SkuLsParams skuLsParams, HttpServletRequest request) {

        System.out.println("进入到list.html映射中");

        skuLsParams.setPageSize(2);

        //得到skuLsResult的结果集
        SkuLsResult skuLsResult = listService.search(skuLsParams);

        //获取SkuLsInfo的结果
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();


        //获取平台属性值id
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getBaseAttrByValueIds(attrValueIdList);

        //制作一个查询参数，urlparam
        String urlParam = this.makeUrlParam(skuLsParams);
        System.out.println("urlParam================" + urlParam);

        // 声明一个面包屑集合 平台属性名称：平台属性值名称
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        // 使用迭代器
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {

            BaseAttrInfo baseAttrInfo = iterator.next();

            // 平台属性值Id
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

            // 判断skuLsParams 中的平台属性值不能为空！
            if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                //判断平台属性值是否有值
                if (attrValueList != null && attrValueList.size() > 0) {

                    //循环判断平台属性值
                    for (BaseAttrValue baseAttrValue : attrValueList) {

                        //循环判断入力参数中的valueId
                        for (String valueId : skuLsParams.getValueId()) {

                            if (baseAttrValue.getId().equals(valueId)) {

                                // 将平台属性对象移除
                                iterator.remove();

                                // 组成面包屑集合
                                BaseAttrValue attrValue = new BaseAttrValue();
                                // 将原来平台属性值的名称 ，改为平台属性名称：平台属性值名称
                                attrValue.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                                // 需要制作最新的urlParam
                                String newUrlParam = this.makeUrlParam(skuLsParams, valueId);

                                attrValue.setUrlParam(newUrlParam);

                                baseAttrValueArrayList.add(attrValue);

                            }
                        }

                    }
                }
            }

        }

        /*for (BaseAttrValue value : baseAttrValueArrayList) {
            System.out.println("value======================"+value);
        }*/

        //把面包屑放到域中
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);

        //把keyName放到域中
        request.setAttribute("keyword",skuLsParams.getKeyword());

        //把urlpsram添加到域中
        request.setAttribute("urlParam", urlParam);

        //把平台属性和平台属性值添加到域中
        request.setAttribute("baseAttrInfoList", baseAttrInfoList);

        //把skuLsInfo的结果放到request域中
        request.setAttribute("skuLsInfoList", skuLsInfoList);

        //把总页数放到域中
        request.setAttribute("totalPages",skuLsResult.getTotalPages());

        //把当前页放到域中
        request.setAttribute("pageNo",skuLsParams.getPageNo());

        System.out.println("skuLsResult===" + JSON.toJSONString(skuLsResult));

        return "list";
    }

    /**
     * 制作查询参数的方法
     *
     * @param skuLsParams
     * @return
     */
    //http://list.gmall.com/list.html?  null  &valueId=null
    //http://list.gmall.com/list.html?catalog3Id=61
    //http://list.gmall.com/list.html?keyword=%E6%89%8B%E6%9C%BA
    //http://list.gmall.com/list.html?catalog3Id=61&keyword=%E6%89%8B%E6%9C%BA
    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {

        String urlParam = "";

        //判断三级分类id
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            //拼接urlparam
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }

        //判断keyname
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {

            //是在三级分类的id上做的查询需要在拼接一个  &
            if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
                urlParam += "&";
            }

            urlParam += "keyword=" + skuLsParams.getKeyword();
        }

        //判断valueID，如果有值在遍历循环valueId
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            //遍历循环valueId
            for (String valueId : skuLsParams.getValueId()) {

                // 如果两个平台属性值Id 相同 {用户点击面包屑的时候获取的平台属性值Id 与 url 后面的平台属性值Id}
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    // 获取到用户点击面包屑的时候的平台属性值Id
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // continue ：结束本次循环，继续下次循环
                        // return ：直接结束方法
                        // break : 结束循环
                        continue;
                    }
                }

                if (urlParam.length() > 0) {
                    urlParam += "&";
                }
                urlParam += "valueId=" + valueId;
            }
        }

        System.out.println("urlParam===========" + urlParam);

        return urlParam;

    }

}
