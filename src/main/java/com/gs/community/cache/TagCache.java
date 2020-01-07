package com.gs.community.cache;

import com.gs.community.dto.TagDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagCache {
    public static List<TagDTO> get() {
        List<TagDTO> tagDTOS = new ArrayList<TagDTO>();
        TagDTO foreGround = new TagDTO();
        foreGround.setCategoryName("前端");
        foreGround.setTags(Arrays.asList("javascript","前端vue.js","css","html","html5","node.js","react.js","jquery","css3","es6","typescript","chrome","npm","bootstrap","sass","less","chrome-devtools","firefox","angular","coffeescript","postcss","postman","fiddler","webkit","yarn","firebug","edge"));
        tagDTOS.add(foreGround);

        TagDTO backGround = new TagDTO();
        backGround.setCategoryName("后端");
        backGround.setTags(Arrays.asList("php","java","node.js","python","c++","c","go","lang","spring","django","flask","springboot","后端","c#","swoole","ruby","asp.net","ruby-on-rails","scala","rust","lavarel","爬虫"));
        tagDTOS.add(backGround);

        TagDTO mobileTerminal = new TagDTO();
        mobileTerminal.setCategoryName("移动端");
        mobileTerminal.setTags(Arrays.asList("java","android","ios","objective-c","小程序","swift","react-native","xcode","android-studio","webapp","flutter","kotlin"));
        tagDTOS.add(mobileTerminal);

        TagDTO db = new TagDTO();
        db.setCategoryName("数据库");
        db.setTags(Arrays.asList("mysql","redis","mongodb","sql","数据库","json","elasticsearch","nosql","memcached","postgresql","sqlite","mariadb"));
        tagDTOS.add(db);


        TagDTO server = new TagDTO();
        server.setCategoryName("服务器");
        server.setTags(Arrays.asList("linux","nginx","docker","apache","centos","ubuntu","服务器","负载均衡","运维","ssh","vagrant","容器","jenkins","devops","debian","fabric"));
        tagDTOS.add(server);

        TagDTO AI = new TagDTO();
        AI.setCategoryName("人工智能");
        AI.setTags(Arrays.asList("算法","机器学习","人工智能","深度学习","数据挖掘","tensorflow","神经网络","图像识别","人脸识别","自然语言处理","机器人","pytorch","自动驾驶"));
        tagDTOS.add(AI);
        
        return tagDTOS;
    }
    public static String filterInvalid(String tags){
        String[] split = StringUtils.split(tags, ',');
        List<TagDTO> tagDTOS = get();
        List<String> tagList = tagDTOS.stream().flatMap(tag->tag.getTags().stream()).collect(Collectors.toList());
        String invalid = Arrays.stream(split).filter(t -> !tagList.contains(t)).collect(Collectors.joining(","));
        return invalid;
    }
}
