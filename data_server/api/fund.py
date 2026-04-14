from data_server.models.fund import Fund
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List, Dict, Optional, Any
from sqlalchemy.orm import Session
from sqlalchemy import func 
from database.init_db import get_db
from models.fund_nav_history import FundNavHistory

router = APIRouter(prefix="/api/fund", tags=["fund"])

class BatchFundNavRequest(BaseModel):
    fund_codes: List[str] = Field(..., example=["000001", "000002", "320007"])

class FundNavResponse(BaseModel):
    nav: float
    date: Optional[str]

@router.post("/nav", response_model=Dict[str, Optional[FundNavResponse]])
async def get_fund_latest_nav(request: BatchFundNavRequest, db: Session = Depends(get_db)):
    if not request.fund_codes:
        return {}

    try:
        # 1. 构造子查询
        subquery = (
            db.query(
                FundNavHistory.fund_code,
                FundNavHistory.nav,
                FundNavHistory.date,
                func.row_number().over(
                    partition_by=FundNavHistory.fund_code,
                    order_by=FundNavHistory.date.desc()
                ).label("rn")
            )
            .filter(FundNavHistory.fund_code.in_(request.fund_codes))
            .subquery()
        )

        # 2. 主查询：只取序号为 1 的记录
        latest_navs = db.query(
            subquery.c.fund_code, 
            subquery.c.nav,
            subquery.c.date
        ).filter(subquery.c.rn == 1).all()

        # 3. 构造结果字典
        result = {code: None for code in request.fund_codes}
        
        for nav in latest_navs:
            result[nav.fund_code] = {"nav": float(nav.nav), "date": nav.date.isoformat()} if nav.nav is not None else {"nav": 0.0, "date": None}
        
        return result

    except Exception as e:
        # 实际开发建议使用 logging 记录具体错误堆栈
        print(f"Batch NAV Fetch Error: {e}")
        raise HTTPException(status_code=500, detail="批量获取净值服务异常")

class BatchFundUpdatedRequest(BaseModel):
    startTime: str = Field(..., example="2023-01-01 00:00:00")
    endTime: str = Field(..., example="2023-01-31 23:59:59")

@router.post("/updated", response_model=Dict[str, Any])
async def get_fund_updated_date(request: BatchFundUpdatedRequest, db: Session = Depends(get_db)):
    try:
        # 1. 根据时间范围查询 Fund 表，使用 nav_updated_at 字段过滤
        updated_funds = db.query(Fund).filter(
            Fund.nav_updated_at >= request.startTime,
            Fund.nav_updated_at <= request.endTime
        ).all()

        # 2. 构造结果列表
        fund_list = []
        for fund in updated_funds:
            fund_data = {
                "fund_code": fund.fund_code,
                "latest_nav": fund.latest_nav,
                "latest_nav_date": fund.latest_nav_date.isoformat() if fund.latest_nav_date else None,
            }
            fund_list.append(fund_data)

        # 3. 构造响应对象
        result = {
            "data": fund_list,
            "message": "success",
            "code": 200
        }
        
        return result

    except Exception as e:
        # 实际开发建议使用 logging 记录具体错误堆栈
        print(f"Get Fund Updated Error: {e}")
        raise HTTPException(status_code=500, detail="获取基金更新服务异常")
