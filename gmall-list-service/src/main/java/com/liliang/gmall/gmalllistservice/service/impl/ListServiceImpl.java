package com.liliang.gmall.gmalllistservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.liliang.gmall.bean.dto.SkuLsInfo;
import com.liliang.gmall.bean.dto.SkuLsParams;
import com.liliang.gmall.bean.dto.SkuLsResult;
import com.liliang.gmall.gmallserviceutil.config.RedisUtil;
import com.liliang.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liliang
 * @since
 */
@Service
public class ListServiceImpl implements ListService {

    //保存到elasticseacrh，需要用到jestClient
    @Autowired
    private JestClient jestClient;

    private static final String ES_INDEX = "gmall";

    private static final String ES_TYPE = "SkuInfo";

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 保存数据
     * @param skuLsInfo
     */
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        /*
         put /index/type/id
         {
            "id":"33",
            "skuName":"小米手机一代"
         }
          */
        // 保存Index
        Index index = new Index.Builder(skuLsInfo)
                .index(ES_INDEX)
                .type(ES_TYPE)
                .id(skuLsInfo.getId())
                .build();



        //执行，把数据保存到es中
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param skuLsParams {用户在前台输入的参数}
     * @return
     */
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        // 查询步骤：
        /*
            get /index/type/_search
            1.  制作query 语句
            2.  定义查询的动作
            3.  获取返回结果集
         */

        //1
        String query = this.makeQueryStringForSearch(skuLsParams);

        //2
        Search search = new Search.Builder(query)
                .addIndex(ES_INDEX)
                .addType(ES_TYPE)
                .build();

        SearchResult searchResult = null;

        try {
            searchResult = jestClient.execute(search);


        } catch (IOException e) {
            e.printStackTrace();
        }

        //3
        // 将searchResult 结果集进行转换为skuLsResult
        SkuLsResult skuLsResult = this.makeResuleForSearch(skuLsParams,searchResult);

        return skuLsResult;
    }

    /**
     * 设置商品的热度，更新商品的热度
     * @param skuId
     */
    @Override
    public void incrHotScore(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if (hotScore%10==0){
            this.updateHotScore(skuId,Math.round(hotScore));
        }

        jedis.close();
    }

    private void updateHotScore(String skuId, long round) {

        // 更新es
        String updQuery = "{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+round+"\n" +
                "  }\n" +
                "}";

        // 执行动作
        Update build = new Update.Builder(updQuery)
                .index(ES_INDEX)
                .type(ES_TYPE)
                .id(skuId)
                .build();

        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 制作query dsl 语句
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

//        //定义查询器
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //定义query下的bool
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        //操作keyword skuName
//        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
//
//            //有查询的关键词
//            //制作match
//            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
//            // bool -- must -- match
//            boolQueryBuilder.must(matchQueryBuilder);
//
//            //设置高亮
//            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
//            highlighter.preTags("<span style=color:red>");
//            highlighter.postTags("</span>");
//            highlighter.field("skuName");
//
//            //将设置好的高亮对象放到查询其中
//            searchSourceBuilder.highlight(highlighter);
//
//        }
//
//        //设置三级分类ID
//        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
//
//            //制作有三级分类ID的查询器
//            //filter---term
//            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
//            //bool---filter
//            boolQueryBuilder.filter(termQueryBuilder);
//
//        }
//
//        //设置平台值Id
//        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
//            //遍历循环
//            for (String valueId : skuLsParams.getValueId()) {
//
//                //filter---term
//                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
//                //bool---filter
//                boolQueryBuilder.filter(termQueryBuilder);
//
//            }
//        }
//
//        //query
//        searchSourceBuilder.query(boolQueryBuilder);
//
//        // 设置分页
//        // 计算从第几条开始查询数据 (pageNo-1)*pageSize
//        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
//        searchSourceBuilder.from(from);
//
//        //默认值20
//        searchSourceBuilder.size(skuLsParams.getPageSize());
//
//        //设置排序
//        searchSourceBuilder.sort("hotScore",SortOrder.DESC);
//
//        //设置聚合
//        TermsBuilder groupbyAttr = AggregationBuilders
//                .terms("groupby_attr")
//                .field("skuAttrValueList.valueId");
//
//        //将聚合好的对象放到查询器中
//        searchSourceBuilder.aggregation(groupbyAttr);
//
//        String query = searchSourceBuilder.toString();
//        System.out.println("query======="+query);
//
//        return query;

        // 定义一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 定义query 下的bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 操作keyword！ skuName
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            // 有查询的关键词
            // 制作 match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            // bool -- must -- match
            boolQueryBuilder.must(matchQueryBuilder);

            // 设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");
            highlighter.field("skuName");

            // 将设置好的高亮对象放入查询器中
            searchSourceBuilder.highlight(highlighter);
        }

        // 设置三级分类Id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            // filter --- term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            // bool -- filter
            boolQueryBuilder.filter(termQueryBuilder);
        }
        // 平台属性值Id
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            // 循环遍历
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                // bool -- filter
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        // query
        searchSourceBuilder.query(boolQueryBuilder);
        // 设置分页
        // 计算从第几条开始查询数据 (pageNo-1)*pageSize
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();

        searchSourceBuilder.from(from);
        // 默认值20
        searchSourceBuilder.size(skuLsParams.getPageSize());
        // 设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        // 设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        // 将聚合好的对象放入查询器
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();

        System.out.println(query);

        return query;
    }

    /**
     * 制作返回的结果集
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResuleForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult = new SkuLsResult();
        // 将属性赋值！
        // List<SkuLsInfo> skuLsInfoList;
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        // 向skuLsInfoArrayList 这个集合中添加数据，数据来源searchResult
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        if (hits!=null && hits.size()>0){
            // 循环遍历
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                // 添加skuLsInfo
                SkuLsInfo skuLsInfo = hit.source;
                // 获取高亮字段skuName
                if (hit.highlight!=null && hit.highlight.size()>0){
                    List<String> list = hit.highlight.get("skuName");
                    String skuNameHi = list.get(0);
                    // 将原来的skuName 进行覆盖
                    skuLsInfo.setSkuName(skuNameHi);
                }
                skuLsInfoArrayList.add(skuLsInfo);
            }
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
        // long total;
        skuLsResult.setTotal(searchResult.getTotal());
        // long totalPages; 10 3 4  | 9 3 3
        // long totalPage = (searchResult.getTotal()%skuLsParams.getPageSize()==0?searchResult.getTotal()/skuLsParams.getPageSize():searchResult.getTotal()/skuLsParams.getPageSize()+1);
        long totalPage = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);
        // List<String> attrValueIdList;
        ArrayList<String> strValueIdList = new ArrayList<>();

        // 采用聚合来获取平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        // 获取buckets
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        if (buckets!=null && buckets.size()>0){
            // 循环遍历
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                strValueIdList.add(valueId);
            }
        }
        skuLsResult.setAttrValueIdList(strValueIdList);

        return skuLsResult;

    }
}
