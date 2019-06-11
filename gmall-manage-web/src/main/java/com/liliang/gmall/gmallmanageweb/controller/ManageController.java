package com.liliang.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.*;
import com.liliang.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin
public class ManageController {

    @RequestMapping("hello")
    public String hello(){

        return "hello";
    }

//    getCatalog1
    @Reference
    private ManageService manageService;

    /**
     * 获取到所有的一级分类
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        List<BaseCatalog1> list = manageService.getCatalog1();
        return list;
    }
    //getCatalog2

    /**
     * 根据一级分类的id，获取到该一级分类的所有的二级分类
     * @param catalog1Id
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(@RequestParam String catalog1Id){
        List<BaseCatalog2> list = manageService.getCatalog2(catalog1Id);
        return list;
    }

    /**
     * 根据二级分类的id，获取到该二级分类的所有的三级分类
     * @param catalog2Id
     * @return
     */
    @ResponseBody
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(@RequestParam String catalog2Id){
        List<BaseCatalog3> list = manageService.getCatalog3(catalog2Id);
        return list;
    }

    //http://127.0.0.1:8082/attrInfoList?catalog3Id=61
    //attrInfoList
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(@RequestParam String catalog3Id){

        //List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);

        List<BaseAttrInfo> attrList = manageService.attrInfoListByCatalog3Id(catalog3Id);
        return attrList;
    }

    //saveAttrInfo
    @ResponseBody
    @RequestMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        String value = manageService.saveAttrInfo(baseAttrInfo);
        return value;
    }

    //getAttrValueList?attrId=
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueListByAttrId(@RequestParam String attrId){
        List<BaseAttrValue> baseAttrValues = manageService.getAttrValueListByAttrId(attrId);
        return baseAttrValues;
    }

    //spuList?catalog3Id=213
    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuListByCatalog3Id(@RequestParam String catalog3Id){
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;

    }

    //baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){

        return manageService.getBaseSaleAttrList();
    }

    //saveSpuInfo
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){

        return manageService.saveSpuInfo(spuInfo);
    }


}

