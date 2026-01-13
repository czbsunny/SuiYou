package com.suiyou.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {

    // ============ 1. 净资产变动 (真正花掉或赚到的钱) ============
    EXPENSE("支出"),      // 吃饭、购物
    INCOME("收入"),       // 工资、利息、分红

    // ============ 2. 内部流转 (左口袋进右口袋) ============
    TRANSFER("转账"),     // 提现、充值、银行卡互转、购买基金(资金->持仓)

    // ============ 3. 债权/应收 (别人欠我钱) ============
    // 场景：借钱给朋友、帮公司垫付差旅费
    // 逻辑：资金(Asset) -> 应收账款(Asset)
    LEND("借出/垫付"),    

    // 场景：朋友还钱、公司报销款到账
    // 逻辑：应收账款(Asset) -> 资金(Asset)
    RECOVER("收债/报销"), 

    // ============ 4. 债务/应付 (我欠别人钱) ============
    // 场景：问朋友借钱、借呗提现、信用卡刷卡(如果不直接记支出)
    // 逻辑：资金(Asset) <- 负债(Liability/Asset负值)
    BORROW("借入"),       

    // 场景：还信用卡、还朋友钱
    // 逻辑：资金(Asset) -> 负债(Liability/Asset负值)
    REPAY("还债/还款"),   

    // ============ 5. 系统维护 ============
    ADJUSTMENT("余额校准"); // 盘点、汇率波动修正

    private final String desc;
}