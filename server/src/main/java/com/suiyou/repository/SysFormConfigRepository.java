package com.suiyou.repository;

import com.suiyou.model.SysFormConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysFormConfigRepository extends JpaRepository<SysFormConfig, Long> {
    /**
     * 查询当前最大的版本号
     * @return 最大版本号
     */
    Long findMaxVersion();
    
    /**
     * 获取大于指定版本号的数据
     * @param version 指定版本号
     * @return 表单配置列表
     */
    Iterable<SysFormConfig> findByVersionGreaterThan(Long version);
}