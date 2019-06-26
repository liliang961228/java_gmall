package com.liliang.gmall.config;

import com.alibaba.fastjson.JSON;
import com.liliang.gmall.util.CookieUtil;
import com.liliang.gmall.util.HttpClientUtil;
import com.liliang.gmall.util.WebConst;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    //表示用户进入处理器之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 需要获取token 并放入cookie 中 只能在登录的时候才能获取到newToken！
        String token = request.getParameter("newToken");
        if (token!=null){
            // 放入cookie
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        // 用户访问的可能是其他模块，token=null
        if (token==null){
            token=CookieUtil.getCookieValue(request,"token",false);
        }
        //token 不为空才能得到用户昵称
        if (token!=null){
            Map map = this.getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName",nickName);
        }


        // 获取方法上的自定义注解@LoginRequire 获取
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        // 说明该方法上有注解
        if (methodAnnotation!=null){
            // 认证：如果认证通过success 则表示用户已经登录
            // fail 表示用户未登录 注解中如果是false ，则不需要管，如果是true ，则必须要跳转到登录页面！
            // 获取currentIp
            String currentIp = request.getHeader("X-forwarded-for");
            // 远程调用verify  使用httpClient 工具类
            // http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ&currentIp=192.168.67.1
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + currentIp);

            // 判断result 结果
            if ("success".equals(result)){
                // 保存用户的Id
                // 解密token 得到map
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                // 存储用户Id
                request.setAttribute("userId",userId);

                return true;
            }else {
                // 必须登录
                if (methodAnnotation.autoRedirect()){
                    // 需要跳转到登录页面  http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F38.html
                    // 获取当前的url 路径
                    String requestURL  = request.getRequestURL().toString();
                    System.out.println("requestURL:"+requestURL);  // http://item.gmall.com/38.html
                    // 借助URLEncoder 转码
                    String encodeURL  = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println("encodeURL:"+encodeURL);
                    // http%3A%2F%2Fitem.gmall.com%2F38.html
                    // 进行重定向到浏览器
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);

                    return false;
                }
            }
        }

        return true;
    }

    private Map getUserMapByToken(String token) {

        //获取token中的用户信息
        String tokenUserInfo = StringUtils.substringBetween(token, ".");

        //解密
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;

        try {
            tokenJson = new String(decode, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }

    //用户进入处理器，视图渲染之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //视图渲染完成之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }


}
