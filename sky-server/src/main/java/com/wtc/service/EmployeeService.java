package com.wtc.service;

import com.wtc.dto.EmployeeDTO;
import com.wtc.dto.EmployeeLoginDTO;
import com.wtc.dto.EmployeePageQueryDTO;
import com.wtc.entity.Employee;
import com.wtc.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void updateStatusById(Integer status, Long id);

    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);
}
