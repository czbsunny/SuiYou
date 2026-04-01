外盘-实时行情数据-东财
接口: futures_global_spot_em

目标地址: https://quote.eastmoney.com/center/gridlist.html#futures_global

描述: 东方财富网-行情中心-期货市场-国际期货-实时行情数据

限量: 单次返回所有期货品种的实时行情数据

输入参数

名称	类型	描述
-	-	-
输出参数

名称	类型	描述
序号	int64	-
代码	object	-
名称	object	-
最新价	float64	-
涨跌额	float64	-
涨跌幅	float64	注意单位: %
今开	float64	-
最高	float64	-
最低	float64	-
昨结	float64	-
成交量	int64	-
买盘	int64	-
卖盘	int64	-
持仓量	int64	-
接口示例

import akshare as ak

futures_global_spot_em_df = ak.futures_global_spot_em()
print(futures_global_spot_em_df)


外盘-历史行情数据-东财
接口: futures_global_hist_em

目标地址: https://quote.eastmoney.com/globalfuture/HG25J.html

描述: 东方财富网-行情中心-期货市场-国际期货-历史行情数据

限量: 单次返回指定品种的历史数据

输入参数

名称	类型	描述
symbol	str	symbol="HG00Y"; 品种代码；可以通过 ak.futures_global_spot_em() 来获取所有可获取历史行情数据的品种代码
输出参数

名称	类型	描述
日期	object	-
代码	object	-
名称	object	-
开盘	float64	-
最新价	float64	-
最高	float64	-
最低	float64	-
总量	int64	-
涨幅	float64	注意单位: %
持仓	object	-
日增	int64	-
接口示例

import akshare as ak

futures_global_hist_em_df = ak.futures_global_hist_em(symbol="HG00Y")
print(futures_global_hist_em_df)


203	SI00Y	COMEX白银
545	GC00Y	COMEX黄金
528	HG00Y	COMEX铜
608	CL00Y	NYMEX原油


https://akshare.akfamily.xyz/data/futures/futures.html#id62