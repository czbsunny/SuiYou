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

const cfu = {
    "type": ["goalLine", "assetArea", "assetLine", "allocationRing", "goalArc", "cashFlowColumn"],
    "range": ["目标曲线", "资产走势", "资产走势", "资产配置", "目标完成率", "现金流"],
    "instance": {},
    "option": {},
    "formatter": {
        "money": function(val, index, opts){
            if (val >= 10000) {
                return (val / 10000).toFixed(val >= 1000000 ? 0 : 1) + '万'
            }
            return val
        },
        "percent": function (val, index, opts) {
            return val + "%"
        },
        "moneyTooltip": function (item, category, index, opts) {
            let result = category + "\n"
            item.forEach(row => {
                result += row.name + "：" + formatter.money(row.data, index, opts) + "\n"
            })
            return result
        }
    },

    "goalLine": {
        "type": "line",
        "color": [primaryColors.lightGreen, primaryColors.green],
        "padding": [16, 8, 20, 8],
        "dataLabel": false,
        "dataPointShape": false,
        "xAxis": {
            "disableGrid": true,
            "labelCount": 2,
            "boundaryGap": false,
            "textAlign": "edges"
        },
        "yAxis": {
            "min": 0,
            "gridType": "dash",
            "dashLength": 2,
            "data": [{
                "axisLine": false,
                "format": "money"
            }]
        },
        "legend": {
            "show": false
        },
        "grid": {
            "left": 40,
            "right": 40
        },
        "extra": {
            "line": {
                "type": "curve",
                "width": 2,
                "activeType": "hollow"
            }
        }
    },

    "assetArea": {
        "type": "area",
        "color": [primaryColors.green],
        "padding": [0, 8, 8, 8],
        "dataLabel": false,
        "dataPointShape": false,
        "xAxis": {
            "disableGrid": true,
            "labelCount": 4,
            "boundaryGap": "justify",
        },
        "yAxis": {
            "disabled": true,
            "show": false,
            "disableGrid": true,
            "min": 0
        },
        "legend": {
            "show": false
        },
        "extra": {
            "area": {
                "type": "curve",
                "opacity": 0.5,
                "addLine": true,
                "width": 2,
                "gradient": true,
                "activeType": "none",
                "color": "#d0e8e2"
            }
        }
    },

    "assetLine": {
        "type": "line",
        "color": [primaryColors.green],
        "padding": [10, 15, 20, 50],
        "dataLabel": false,
        "xAxis": {
            "disableGrid": true
        },
        "yAxis": {
            "axisLine": false,
            "gridType": "dash"
        },
        "legend": {
            "show": false
        },
        "extra": {
            "line": {
                "type": "curve",
                "width": 3,
                "activeType": "hollow"
            }
        }
    },

    "allocationRing": {
        "type": "ring",
        "color": color,
        "padding": [5, 5, 5, 5],
        "dataLabel": false,
        "legend": {
            "show": true,
            "position": "right"
        },
        "extra": {
            "ring": {
                "ringWidth": 30,
                "activeOpacity": 0.5
            }
        }
    },

    "goalArc": {
        "type": "arcbar",
        "color": [primaryColors.green],
        "title": {
            "fontSize": 26
        },
        "subtitle": {
            "fontSize": 14
        },
        "extra": {
            "arcbar": {
                "width": 15
            }
        }
    },

    "cashFlowColumn": {
        "type": "column",
        "color": color,
        "padding": [10, 15, 20, 50],
        "dataLabel": false,
        "xAxis": {
            "disableGrid": true
        },
        "yAxis": {
            "axisLine": false,
            "gridType": "dash"
        },
        "legend": {
            "show": false
        },
        "extra": {
            "column": {
                "type": "group",
                "width": 20
            }
        }
    }
}

export default cfu;