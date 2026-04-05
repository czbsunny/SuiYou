from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List, Dict, Optional
from sqlalchemy.orm import Session

from sqlalchemy.sql import row_number
from database.init_db import get_db
from models.fund_nav_history import FundNavHistory

router = APIRouter(prefix="/api/fund", tags=["fund"])

class BatchFundNavRequest(BaseModel):
    fund_codes: List[str] = Field(..., example=["000001", "000002", "320007"])

@router.post("/nav", response_model=Dict[str, Optional[float]])
async def get_fund_latest_nav(request: BatchFundNavRequest, db: Session = Depends(get_db)):
    """
    批量获取基金最新净值
    - **fund_codes**: 基金代码列表
    返回格式: { "基金代码": 单位净值 }
    """
    if not request.fund_codes:
        return {}

    try:
        subquery = (
            db.query(
                FundNavHistory,
                row_number().over(
                    partition_by=FundNavHistory.fund_code,
                    order_by=FundNavHistory.date.desc()
                ).label("rn")
            )
            .filter(FundNavHistory.fund_code.in_(request.fund_codes))
            .subquery()
        )

        # 2. 主查询：只取序号为 1 的记录（即每个基金最新的那一条）
        latest_navs = db.query(subquery).filter(subquery.c.rn == 1).all()

        # 3. 构造结果字典
        result = {code: None for code in request.fund_codes} # 初始化，防止漏掉请求的参数
        
        for nav in latest_navs:
            result[nav.fund_code] = float(nav.nav) if nav.nav else 0.0
        
        return result

    except Exception as e:
        print(f"Batch NAV Fetch Error: {e}")
        raise HTTPException(status_code=500, detail="批量获取净值服务异常")
