package com.ikats.scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

/**
 * @Author : liu kuo
 * @Date : 2017/11/6 16:27.
 * @Description : Indulge in study , wasting away
 */
@Controller
@RequestMapping("User")
public class UserController
{
    @ResponseBody
    @RequestMapping(value = "login",method = {RequestMethod.POST})
    public boolean login(@RequestBody Map param)
    {
        try
        {
           String username = (String) param.get("username");
           String password = (String) param.get("password");
            if(username.equals("admin") && password.equals("123457"))
           {
                return true;
           }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
