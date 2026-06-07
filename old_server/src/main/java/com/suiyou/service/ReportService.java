package com.suiyou.service;

/**
 * 报告服务接口
 * 提供各类报告生成功能
 */
public interface ReportService {
    
    /**
     * 生成资产总览报告
     * @return 资产总览报告数据
     */
    Object generateAssetReport();
    
    /**
     * 生成目标总览报告
     * @return 目标总览报告数据
     */
    Object generateGoalReport();
}
