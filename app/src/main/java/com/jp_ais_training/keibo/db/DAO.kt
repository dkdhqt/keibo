package com.jp_ais_training.keibo.db

import androidx.room.*
import com.jp_ais_training.keibo.model.entity.ExpenseItem
import com.jp_ais_training.keibo.model.entity.IncomeItem
import com.jp_ais_training.keibo.model.entity.MainCategory
import com.jp_ais_training.keibo.model.entity.SubCategory
import com.jp_ais_training.keibo.model.response.*

@Dao
interface DAO {
    //Main Category - expense_type에 따라서 Main Category Name 불러오기
    @Query("SELECT * from `MainCategory` " +
            "WHERE expense_type LIKE :expense_type||'%'"  +
            "ORDER BY main_category_id ASC;")
    fun loadMainCategory(expense_type : String) : List<MainCategory>

    //Main Category - 초기 Main Category 데이터 설정
    @Query("INSERT OR IGNORE INTO `MainCategory`(main_category_id,main_category_name,expense_type) VALUES" +
            "(1,'公課金','fix')," +
            "(2,'生活','fix')," +
            "(3,'その他','fix')," +
            "(4,'食費','flex')," +
            "(5,'生活','flex')," +
            "(6,'余暇','flex')," +
            "(7,'文化','flex')," +
            "(8,'自己開発','flex')," +
            "(9,'その他','flex');"
    )fun insertMainCategory()

    //--------------------------------------------

    //Sub Category - 데이터 불러오기
    @Query("SELECT * FROM SubCategory " +
            "WHERE deleted_yn = 'n' AND main_category_id = :select "  +
            "ORDER BY sub_category_id ASC;")
    fun loadSubCategory(select: Int) : List<SubCategory>

    //Sub Category- SubCategory Check
    @Query( "SELECT sub_category_id FROM SubCategory as S "+
            "WHERE main_category_id == :main_id and S.sub_category_name == :sub_name and deleted_yn = 'n';")
    fun checkSubCategory(main_id : Int, sub_name : String) : List<Int>

    //Sub Category- SubCategory Check
    @Query( "SELECT sub_category_id FROM SubCategory as S "+
            "WHERE main_category_id == :main_id and S.sub_category_name == :sub_name and deleted_yn = 'y';")
    fun checkSubCategoryWithDeleted(main_id : Int, sub_name : String) : List<Int>

    //Sub Category- 데이터 추가
    @Query("INSERT INTO SubCategory(main_category_id,sub_category_name,deleted_yn) VALUES" +
            "(:main_id,:sub_name,'n') ;")
    fun insertSubCategory(main_id : Int, sub_name : String)

    //Sub Category - 데이터 삭제 취소 (Soft Delete)
    @Query("UPDATE SubCategory " +
            "SET deleted_yn ='n' " +
            "WHERE sub_category_id = :select")
    fun updateDeletedSubCategory(select: Int);

    //Sub Category - 데이터 삭제 (Soft Delete)
    @Query("UPDATE SubCategory " +
            "SET deleted_yn ='y' " +
            "WHERE sub_category_id = :select")
    fun deleteSubCategory(select: Int);

    //--------------------------------------------

    //IncomeItem - 전체 데이터 불러오기
    @Query("SELECT * FROM IncomeItem " +
            "WHERE datetime Like :select||'%'" +
            "ORDER BY datetime ASC;")
    fun loadII(select:String) : List<IncomeItem>

    //IncomeItem 가장 마지막 데이터 id 값 가져오기
    @Query("SELECT income_item_id FROM IncomeItem " +
            "ORDER BY income_item_id DESC LIMIT 1;")
    fun loadIILastId() : Int

    //IncomeItem - flex기준 특정 데이터 불러오기
    @Query("SELECT * FROM IncomeItem " +
            "WHERE datetime Like :select||'%' AND income_type ='flex'" +
            "ORDER BY income_item_id ASC;")
    fun loadFlexII(select:String) : List<ResponseItem>

    //IncomeItem - fix기준 특정 데이터 불러오기
    @Query("SELECT * FROM IncomeItem " +
            "WHERE datetime Like :select||'%' AND  income_type ='fix'" +
            "ORDER BY income_item_id ASC;")
    fun loadFixII(select:String) : List<ResponseItem>

    //IncomeItem - Month基準合計データ読み込み
    @Query("SELECT SUM(price) AS price " +
            "FROM IncomeItem " +
            "WHERE substr(datetime,1,7) Like :select||'%' " +
            "GROUP BY substr(datetime,1,7) " +
            "ORDER BY substr(datetime,1,7) ASC;")
    fun loadMonthSumHII(select:String ) : Int

    //IncomeItem - Month 기준 합계 데이터 불러오기
    @Query("SELECT substr(datetime,1,7) AS date, SUM(price) AS price " +
            "FROM IncomeItem " +
            "WHERE substr(datetime,1,7) Like :select||'%' " +
            "GROUP BY substr(datetime,1,7) " +
            "ORDER BY substr(datetime,1,7) ASC;")
    fun loadMonthSumII(select:String ) : List<LoadSumII>

    //IncomeItem - Day 기준 합계 데이터 불러오기
    @Query("SELECT datetime AS date, SUM(price) AS price " +
            "FROM IncomeItem " +
            "WHERE datetime Like :select||'%' " +
            "GROUP BY datetime " +
            "ORDER BY datetime ASC;")
    fun loadDaySumII(select:String ) : List<LoadSumII>

    //IncomeItem - Week 기준 합계 데이터 불러오기
    @Query("SELECT strftime('%W',datetime) AS date, SUM(price) AS price " +
            "FROM IncomeItem WHERE datetime Like :select||'%' " +
            "GROUP BY strftime('%W',datetime)" +
            "ORDER BY strftime('%W',datetime) ASC;")
    fun loadWeekSumII(select:String ) : List<LoadSumII>

    //IncomeItem - Week-Day 기준 합계 데이터 불러오기
    @Query("SELECT datetime AS date, SUM(price) AS price " +
            "FROM IncomeItem WHERE substr(datetime,1,4) = :year AND strftime('%W',datetime) Like :week||'%'  " +
            "GROUP BY datetime ORDER BY datetime ASC;")
    fun loadWeekDaySumII(year : String, week : String) : List<LoadSumII>

    //IncomeItem - 데이터 추가
    @Query("INSERT INTO IncomeItem(income_type,name,price,datetime) VALUES" +
            "(:type,:name,:price,:datetime);")
    fun insertII(type : String, name : String, price : Int, datetime : String)

    //IncomeItem - 데이터 추가2
    @Insert
    fun insertII(entity: IncomeItem)

    //IncomeItem- 데이터 업데이트
    @Update
    fun updateII(entity: IncomeItem)

    //IncomeItem - 데이터 삭제
    @Query("DELETE FROM IncomeItem WHERE income_item_id == :id;")
    fun deleteII(id : Int);

    //--------------------------------------------

    //Expense Item - 전체 데이터 불러오기
    @Query("SELECT E.expense_item_id,E.sub_category_id,E.name,E.price,E.datetime,M.expense_type AS type " +
            "FROM ExpenseItem AS E " +
            "INNER JOIN SubCategory AS S " +
            "ON E.sub_category_id = S.sub_category_id " +
            "INNER JOIN MainCategory AS M " +
            "ON S.main_category_id = M.main_category_id " +
            "WHERE E.datetime Like :select||'%'" +
            "ORDER BY datetime ASC;")
    fun loadEI(select:String) : List<ExpenseItemType>

    //Expense Item 가장 마지막 데이터 id 값 가져오기
    @Query("SELECT expense_item_id FROM ExpenseItem " +
            "ORDER BY expense_item_id DESC LIMIT 1;")
    fun loadEILastId() : Int

    //Expense Item - flex기준 특정 데이터 불러오기
    @Query("SELECT * " +
            "FROM ExpenseItem AS E " +
            "INNER JOIN SubCategory AS S " +
            "ON E.sub_category_id = S.sub_category_id " +
            "INNER JOIN MainCategory AS M " +
            "ON S.main_category_id = M.main_category_id " +
            "WHERE datetime Like :select||'%' AND M.expense_type ='flex'" +
            "ORDER BY expense_item_id ASC;")
    fun loadFlexEI(select:String) : List<ResponseItem>

    //Expense Item - fix기준 특정 데이터 불러오기
    @Query("SELECT * " +
            "FROM ExpenseItem AS E " +
            "INNER JOIN SubCategory AS S " +
            "ON E.sub_category_id = S.sub_category_id " +
            "INNER JOIN MainCategory AS M " +
            "ON S.main_category_id = M.main_category_id " +
            "WHERE datetime Like :select||'%' AND M.expense_type ='fix'" +
            "ORDER BY expense_item_id ASC;")
    fun loadFixEI(select:String) : List<ResponseItem>

    //Expense Item - Month基準合計データ読み込み
    @Query("SELECT SUM(price) AS price " +
            "FROM ExpenseItem " +
            "WHERE substr(datetime,1,7) Like :select||'%' " +
            "GROUP BY substr(datetime,1,7) " +
            "ORDER BY substr(datetime,1,7) ASC;")
    fun loadMonthSumHEI(select:String ) : Int

    //Expense Item - Month 기준 합계 데이터 불러오기
    @Query("SELECT substr(datetime,1,7) AS date, SUM(price) AS price " +
            "FROM ExpenseItem " +
            "WHERE substr(datetime,1,7) Like :select||'%' " +
            "GROUP BY substr(datetime,1,7) " +
            "ORDER BY substr(datetime,1,7) ASC;")
    fun loadMonthSumEI(select:String ) : List<LoadSumEI>

    //Expense Item - Day 기준 합계 데이터 불러오기
    @Query("SELECT datetime AS date, SUM(price) AS price " +
            "FROM ExpenseItem WHERE datetime Like :select||'%' " +
            "GROUP BY datetime " +
            "ORDER BY datetime ASC;")
    fun loadDaySumEI(select:String ) : List<LoadSumEI>

    //Expense Item - Week 기준 합계 데이터 불러오기
    @Query("SELECT strftime('%W',datetime) AS date, SUM(price) AS price " +
            "FROM ExpenseItem WHERE datetime Like :select||'%' " +
            "GROUP BY strftime('%W',datetime)" +
            "ORDER BY strftime('%W',datetime) ASC;")
    fun loadWeekSumEI(select:String ) : List<LoadSumEI>

    //Expense Item - Week-Day 기준 합계 데이터 불러오기
    @Query("SELECT datetime AS date, SUM(price) AS price " +
            "FROM ExpenseItem WHERE substr(datetime,1,4) = :year AND strftime('%W',datetime) Like :week||'%' " +
            "GROUP BY datetime ORDER BY datetime ASC;")
    fun loadWeekDaySumEI(year: String , week:String ) : List<LoadSumEI>

    //Expense Item - main category 기준 합계 데이터 불러오기
    @Query("SELECT substr(E.datetime,1,7) AS 'date' ," +
            "SUM(E.price) AS 'price', " +
            "M.main_category_name AS 'main_name'," +
            "M.main_category_id AS 'main_id'," +
            "M.expense_type AS 'type'" +
            "FROM ExpenseItem AS E " +
            "INNER JOIN SubCategory AS S " +
            "ON E.sub_category_id = S.sub_category_id " +
            "INNER JOIN MainCategory AS M " +
            "ON S.main_category_id = M.main_category_id " +
            "WHERE E.datetime like :select||'%' " +
            "GROUP BY M.main_category_id " +
            "ORDER BY M.main_category_id ASC; "
    )fun loadMonthSumMainCategoryEI(select:String ) : List<LoadSumMainCategoryEI>

    //Expense Item - sub category 기준 합계 데이터 불러오기
    @Query("SELECT substr(E.datetime,1,7) AS date, " +
            "SUM(E.price) AS price, " +
            "S.sub_category_name AS sub_name, " +
            "S.main_category_id AS main_id " +
            "FROM ExpenseItem AS E " +
            "INNER JOIN SubCategory AS S " +
            "ON E.sub_category_id = S.sub_category_id " +
            "WHERE E.datetime Like :select||'%' " +
            "GROUP BY E.sub_category_id " +
            "ORDER BY E.sub_category_id ASC;")
    fun loadMonthSumSubCategoryEI(select:String ) : List<LoadSumSubCategoryEI>

    //Expense Item - 데이터 추가
    @Query("INSERT INTO ExpenseItem(sub_category_id,name,price,datetime) VALUES" +
            "(:sub_id,:name,:price,:datetime);")
    fun insertEI(sub_id : Int, name : String, price : Int, datetime : String)


    //Expense Item - 데이터 추가
    @Insert
    fun insertEI(entity: ExpenseItem)

    //Expense Item- 데이터 업데이트
    @Update
    fun updateEI(entity: ExpenseItem)

    //Expense Item - 데이터 삭제
    @Query("DELETE FROM ExpenseItem WHERE expense_item_id == :id;")
    fun deleteEI(id : Int);

}
