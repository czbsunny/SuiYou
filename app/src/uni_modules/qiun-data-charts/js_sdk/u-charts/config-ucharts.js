/*
 * uCharts®
 * 高性能跨平台图表库，支持H5、APP、小程序（微信/支付宝/百度/头条/QQ/360）、Vue、Taro等支持canvas的框架平台
 * Copyright (c) 2021 QIUN®秋云 https://www.ucharts.cn All rights reserved.
 * Licensed ( http://www.apache.org/licenses/LICENSE-2.0 )
 * 复制使用请保留本段注释，感谢支持开源！
 *
 * uCharts®官方网站
 * https://www.uCharts.cn
 *
 * 开源地址:
 * https://gitee.com/uCharts/uCharts
 *
 * uni-app插件市场地址：
 * http://ext.dcloud.net.cn/plugin?id=271
 *
 */

const primaryColors = {
    green: "#006754",
    lightGreen: "#bec9c4",
    yellow: "#FAC858",
    red: "#EE6666",
    blue: "#5470C6"
}

const color = [
    primaryColors.green,
    primaryColors.lightGreen,
    primaryColors.yellow,
    primaryColors.red,
    primaryColors.blue
];

const formatter = {
    money: function (value) {
        if (value >= 10000) {
            return (value / 10000).toFixed(value >= 1000000 ? 0 : 1) + '万'
        }
        return value
    },
    percent: function (value) {
        return value + "%"
    },
    moneyTooltip: function (item, category) {
        let result = category + "\n"
        item.forEach(row => {
            result += row.name + "：" + formatter.money(row.data) + "\n"
        })
        return result
    }
}

const cfu = {
    type: ["goalLine", "assetLine", "allocationRing", "goalArc", "cashFlowColumn"],
    range: ["目标曲线", "资产走势", "资产配置", "目标完成率", "现金流"],
    instance: {},
    option: {},
    formatter: formatter,

    goalLine: {
        type: "line",
        color: [primaryColors.lightGreen, primaryColors.green],
        padding: [10, 15, 20, 50],
        dataLabel: false,
        xAxis: {
            disableGrid: true,
            labelCount: 2
        },
        yAxis: {
            axisLine: false,
            gridType: "dash",
            dashLength: 2
        },
        legend: {
            show: false
        },
        tooltip: {
            show: true,
            format: "moneyTooltip"
        },
        extra: {
            line: {
                type: "curve",
                width: 2,
                activeType: "none"
            }
        }
    },

    assetLine: {
        type: "line",
        color: [primaryColors.green],
        padding: [10, 15, 20, 50],
        dataLabel: false,
        xAxis: {
            disableGrid: true
        },
        yAxis: {
            axisLine: false,
            gridType: "dash"
        },
        legend: {
            show: false
        },
        extra: {
            line: {
                type: "curve",
                width: 3,
                activeType: "hollow"
            }
        }
    },

    allocationRing: {
        type: "ring",
        color: color,
        padding: [5, 5, 5, 5],
        dataLabel: false,
        legend: {
            show: true,
            position: "right"
        },
        extra: {
            ring: {
                ringWidth: 30,
                activeOpacity: 0.5
            }
        }
    },

    goalArc: {
        type: "arcbar",
        color: [primaryColors.green],
        title: {
            fontSize: 26
        },
        subtitle: {
            fontSize: 14
        },
        extra: {
            arcbar: {
                width: 15
            }
        }
    },

    cashFlowColumn: {
        type: "column",
        color: color,
        padding: [10, 15, 20, 50],
        dataLabel: false,
        xAxis: {
            disableGrid: true
        },
        yAxis: {
            axisLine: false,
            gridType: "dash"
        },
        legend: {
            show: false
        },
        extra: {
            column: {
                type: "group",
                width: 20
            }
        }
    }
}

export default cfu;