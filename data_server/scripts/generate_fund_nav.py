import pandas as pd
import sys
import os

# 添加项目根目录到 Python 搜索路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database.init_db import SessionLocal
from models.fund_nav_history import FundNavHistory
from models.fund import Fund

def load_fund_nav():
    db = SessionLocal()
    try:
        from datetime import date
        query = db.query(
            FundNavHistory.fund_code,
            Fund.fund_type,
            FundNavHistory.daily_growth_rate
        ).join(
            Fund, FundNavHistory.fund_code == Fund.fund_code
        ).filter(
            FundNavHistory.date == date(2026, 2, 6),
            Fund.is_valid_fund == True
        )
        
        results = query.all()
        return [(row.fund_code, row.fund_type, row.daily_growth_rate) for row in results if row.fund_code]
    finally:
        db.close()

if __name__ == "__main__":
    data = load_fund_nav()
    pd.DataFrame(data, columns=['fund_code', 'fund_type', 'daily_growth_rate']).to_csv('config/fund_nav.csv', index=False)
