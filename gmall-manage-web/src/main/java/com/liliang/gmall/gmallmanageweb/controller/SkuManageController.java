package com.liliang.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.bean.SpuImage;
import com.liliang.gmall.bean.SpuSaleAttr;
import com.liliang.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    //http://127.0.0.1:8082/spuSaleAttrList?spuId=61
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(@RequestParam String spuId){
        return manageService.spuSaleAttrList(spuId);
    }

    //查询指定spuId 的图片信息
    //http://127.0.0.1:8082/spuImageList?spuId=61
    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(@RequestParam  String spuId){
        List<SpuImage> spuImageList = manageService.spuImageList(spuId);
        return spuImageList;
    }

    //http://127.0.0.1:8082/saveSkuInfo
    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "ok";
    }


}
