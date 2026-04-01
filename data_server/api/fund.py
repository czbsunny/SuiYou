from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel
from typing import List, Dict, Union
from datafetch.fund_fetcher import fund_fetcher
from services.fund_estimate_service import fund_estimate_service

router = APIRouter(prefix="/api/fund", tags=["fund"])

# 基金数据请求模型
class FundDataRequest(BaseModel):
    fund_code: str
    period: int = 20

# 基金涨跌数据模型
class FundChangeData(BaseModel):
    fund_id: str
    change: float
    timestamp: str

# 基金涨跌请求模型
class FundChangeRequest(BaseModel):
    fund_codes: List[str]

@router.get("/list", response_model=List[str])
async def get_fund_codes():
    """
    获取基金代码列表
    
    返回所有可用的基金代码列表
    """
    return await fund_fetcher.get_fund_codes()

@router.get("/info", response_model=Dict[str, str])
async def get_fund_info(fund_code: str = Query(..., description="基金代码")):
    """
    获取基金信息
    
    返回基金代码和名称的映射关系（key为基金代码，value为基金名称）
    """
    return await fund_fetcher.fetch_fund_profile(fund_code)

@router.post("/nav", response_model=Dict[str, List[Union[float, None]]])
async def get_fund_nav(request: FundDataRequest):
    """
    获取基金净值
    
    - **fund_code**: 基金代码
    - **period**: 查询周期（天），默认20天
    """
    # 验证基金代码格式
    if not await fund_fetcher.validate_fund_code(request.fund_code):
        raise HTTPException(status_code=400, detail="基金代码格式不正确")
    
    # 调用基金获取器获取净值数据
    return await fund_fetcher.fetch_fund_nav(request.fund_code, request.period)

@router.post("/estimate", response_model=Dict[str, FundChangeData])
async def get_fund_estimate(request: FundChangeRequest):
    """
    获取基金预估涨跌
    
    从ES的fund_change_latest索引中获取基金涨跌数据，使用缓存机制提升性能
    
    - **fund_codes**: 基金代码列表
    
    返回格式: {基金代码: FundChangeData}
    """
    if not fund_estimate_service:
        raise HTTPException(status_code=500, detail="基金预估涨跌服务未初始化")
    
    if not request.fund_codes:
        raise HTTPException(status_code=400, detail="基金代码列表不能为空")
    
    try:
        # 使用服务获取基金预估涨跌数据（带缓存）
        estimates = fund_estimate_service.get_fund_estimates(request.fund_codes)
        
        # 转换为响应格式
        result = {}
        for fund_code, data in estimates.items():
            result[fund_code] = FundChangeData(
                fund_id=data['fund_id'],
                change=data['change'],
                timestamp=data['timestamp']
            )
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取基金预估涨跌失败: {str(e)}")
