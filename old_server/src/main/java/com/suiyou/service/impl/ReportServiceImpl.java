package com.suiyou.service.impl;

import com.suiyou.service.ReportService;
import org.springframework.stereotype.Service;

/**
 * 报告服务实现类
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public Object generateAssetReport() {
        // 资产总览报告实现，当前暂时返回占位数据
        return "资产总览报告功能待实现";
    }

    @Override
    public Object generateGoalReport() {
        // 目标总览报告实现，当前暂时返回占位数据
        return "目标总览报告功能待实现";
    }
}
