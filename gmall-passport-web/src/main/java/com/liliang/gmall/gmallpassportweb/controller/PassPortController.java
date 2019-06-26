package com.liliang.gmall.gmallpassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.UserInfo;
import com.liliang.gmall.service.UserInfoService;
import com.liliang.gmall.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassPortController {

    @Reference
    private UserInfoService userInfoService;

    @Value("${token.key}")
    private String key;

    //http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F39.html
    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");

        //把originUrl保存到域中，提供给前台使用跳转页面
        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){

        //调动gmall-user-manage服务查询该用户是否存在
        UserInfo user = userInfoService.getUserInfoByNamePwd(userInfo);

        //获取服务器的IP地址
        String salt = request.getHeader("X-forwarded-for");

        if (user!=null){

            //返回token 如何制作token！
            String id = user.getId();
            String nickName = user.getNickName();
            Map<String, Object> map = new HashMap<>();
            map.put("userId",id);
            map.put("nickName",nickName);

            //制作token
            String token = JwtUtil.encode(key, map, salt);

            System.out.println("token================="+token);
            //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.IY8MS9XD67EV7dzEQ82GkPKaru1WFK4L80dmSGw651U
            return token;
        }

        return "fail";
    }

    //http://passport.atguigu.com/verify?
    //     token=eyJhbGciOiJIUzI1NiJ9.
    //     eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywU
    //     AFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
    //     &currentIp=192.168.67.1

    // verify?salt=xxx&token=xxx

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){

        // 利用key，salt 解密token 能够得到用户的信息{userInfo.getId()}
        String token = request.getParameter("token");
        // 获取salt
        String currentIp = request.getParameter("currentIp");
        Map<String, Object> map = JwtUtil.decode(token, key, currentIp);

        if (map!=null){
            String userId = (String) map.get("userId");
            System.out.println("userId=============="+userId);
            // 调用服务层
            UserInfo userInfo = userInfoService.verify(userId);

            if (userInfo!=null){
                return "success";
            }
        }

        return "fail";
    }
}
