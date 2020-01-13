package com.gs.community.cache;

import com.gs.community.dto.HotTagDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Data
public class HotTagCache {
    private Map<String, Integer> tags = new HashMap<>();
    private List<String> hots = new ArrayList();

    public void updateTags(Map<String, Integer> tags) {
        int max = 5;
        PriorityQueue<HotTagDTO> priorityQueue = new PriorityQueue<>(max);
        tags.forEach((name, priority) -> {
            HotTagDTO hotTagDTO = new HotTagDTO();
            hotTagDTO.setName(name);
            hotTagDTO.setPriority(priority);
            if (priorityQueue.size() < max) {
                priorityQueue.add(hotTagDTO);
            } else {
                HotTagDTO minHot = priorityQueue.peek();
                if (hotTagDTO.compareTo(minHot) > 0) {
                    priorityQueue.poll();
                    priorityQueue.add(hotTagDTO);
                }
            }
        });
        List<String> sortTags = new ArrayList();
        HotTagDTO poll = priorityQueue.poll();
        while (poll != null) {
            sortTags.add(poll.getName());
            poll = priorityQueue.poll();
        }
        Collections.reverse(sortTags);
        hots = sortTags;
    }
}
