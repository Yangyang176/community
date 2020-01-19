package com.gs.community.service;

import com.gs.community.enums.AdPosEnum;
import com.gs.community.mapper.AdMapper;
import com.gs.community.model.Ad;
import com.gs.community.model.AdExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdService {
    @Autowired
    private AdMapper adMapper;

    public List<Ad> list(String adPos) {
        AdExample adExample = new AdExample();
        adExample.createCriteria().andStatusEqualTo(1)
                .andPosEqualTo(adPos)
                .andGmtStartLessThan(System.currentTimeMillis())
                .andGmtEndGreaterThan(System.currentTimeMillis());
        return adMapper.selectByExample(adExample);
    }
}
