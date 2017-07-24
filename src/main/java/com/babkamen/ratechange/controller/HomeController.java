package com.babkamen.ratechange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by babkamen on 19.07.2017.
 */
@Controller
public class HomeController {
    @RequestMapping(path="/", method= RequestMethod.GET)
    public String goHome(){
        return "index";
    }

}
