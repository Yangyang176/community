package com.gs.community.schedule;

import com.gs.community.cache.HotTagCache;
import com.gs.community.mapper.QuestionMapper;
import com.gs.community.model.Question;
import com.gs.community.model.QuestionExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class HotTagTasks {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private HotTagCache hotTagCache;

    @Scheduled(fixedRate = 20000)
    public void hotTagSchedule() {
//        log.info("The time is now {}", new Date());
        int offset = 0;
        int limit = 10;
        List<Question> questions = new ArrayList<>();
        Map<String, Integer> priorities = new HashMap<>();
        while (offset == 0 || questions.size() == limit) {
            questions = questionMapper.selectByExampleWithRowbounds(new QuestionExample(), new RowBounds(offset, limit));
            for (Question question : questions) {
                String[] tags = StringUtils.split(question.getTag(), ",");
                for (String tag : tags) {
                    Integer priority = priorities.get(tag);
                    if (priority != null) {
                        priorities.put(tag, priority + 5 + question.getCommentCount());
                    } else {
                        priorities.put(tag, 5 + question.getCommentCount());
                    }
                }
            }
            offset += limit;
        }
        hotTagCache.setTags(priorities);
        hotTagCache.updateTags(hotTagCache.getTags());
    }
}
