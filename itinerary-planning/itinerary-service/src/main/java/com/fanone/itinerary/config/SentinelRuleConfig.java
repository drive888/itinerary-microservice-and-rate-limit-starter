package com.fanone.itinerary.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.fanone.itinerary.service.DestinationClientGuard;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelRuleConfig {

    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        rules.add(exceptionRatioRule(DestinationClientGuard.GET_DESTINATION_RESOURCE));
        rules.add(exceptionRatioRule(DestinationClientGuard.SAVE_AMAP_RESOURCE));
        DegradeRuleManager.loadRules(rules);
    }

    private DegradeRule exceptionRatioRule(String resource) {
        DegradeRule rule = new DegradeRule(resource);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setCount(0.5);
        rule.setTimeWindow(10);
        rule.setMinRequestAmount(3);
        rule.setStatIntervalMs(10000);
        return rule;
    }
}
