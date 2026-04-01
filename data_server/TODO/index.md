指数类数据需要单独处理
1、获取指数当天实时涨幅
2、解析基金跟踪的基准
3、获取基金持仓比例
4、根据十大仓位剩余仓位乘以跟踪的基准计算基金净值



实时行情数据-新浪
接口: stock_zh_index_spot_sina

目标地址: https://vip.stock.finance.sina.com.cn/mkt/#hs_s

描述: 新浪财经-中国股票指数数据

限量: 单次返回所有指数的实时行情数据

输入参数

名称	类型	描述
-	-	-
输出参数

名称	类型	描述
代码	object	-
名称	object	-
最新价	float64	-
涨跌额	float64	-
涨跌幅	float64	注意单位: %
昨收	float64	-
今开	float64	-
最高	float64	-
最低	float64	-
成交量	float64	注意单位: 手
成交额	float64	注意单位: 元
接口示例

import akshare as ak

stock_zh_index_spot_sina_df = ak.stock_zh_index_spot_sina()
print(stock_zh_index_spot_sina_df)


历史行情数据
历史行情数据-新浪
接口: stock_zh_index_daily

目标地址: https://finance.sina.com.cn/realstock/company/sz399552/nc.shtml(示例)

描述: 股票指数的历史数据按日频率更新

限量: 单次返回指定 symbol 的所有历史行情数据

输入参数

名称	类型	描述
symbol	str	symbol="sz399552"
输出参数-历史行情数据

名称	类型	描述
date	object	新浪的数据开始时间, 不是该指数的上市时间
open	float64	-
high	float64	-
low	float64	-
close	float64	-
volume	int64	-
接口示例-历史行情数据

import akshare as ak

stock_zh_index_daily_df = ak.stock_zh_index_daily(symbol="sz399552")
print(stock_zh_index_daily_df)