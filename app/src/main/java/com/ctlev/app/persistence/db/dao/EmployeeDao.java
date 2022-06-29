package com.ctlev.app.persistence.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ctlev.app.persistence.db.tables.Employee;

import java.util.List;

@Dao
public interface EmployeeDao {
    @Query("SELECT * FROM Employee")
    LiveData<List<Employee>> loadAllEmployees();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Employee> products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertUsers(Employee... users);

    @Update
    public void updateUsers(Employee... users);

    @Query("select * from Employee where id = :productId")
    LiveData<Employee> loadEmployee(int productId);

    @Query("select * from Employee where id = :productId")
    Employee loadEmployeeSync(int productId);

    @Query("SELECT  * FROM Employee")
    LiveData<List<Employee>> searchAllEmployees();

    @Delete
    void delete(Employee user);

    @Delete
    public void deleteUsers(Employee... users);
}
