from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List, Dict, Optional
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