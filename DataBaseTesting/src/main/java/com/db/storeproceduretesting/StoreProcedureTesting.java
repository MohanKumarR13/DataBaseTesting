package com.db.storeproceduretesting;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

public class StoreProcedureTesting {
	Connection connection = null;
	Statement statement = null;
	ResultSet resultSet;
	ResultSet resultSet1;
	ResultSet resultSet2;
	CallableStatement callableStatement;

	@BeforeClass
	void setup() throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels", "root", "root");
	}

	@AfterClass
	void tearDown() throws SQLException {
		connection.close();
	}

	@Test(priority = 1, enabled = false)
	void store_functionexists() throws SQLException {
		statement = connection.createStatement();
		resultSet = statement.executeQuery("SHOW FUNCTION STATUS WHERE Name='customerlevel'");
		resultSet.next();
		Assert.assertEquals(resultSet.getString("Name"), "customerlevel");
	}

	@Test(priority = 2, enabled = true)
	void customerLevelWithSQLStatement() throws SQLException {
		resultSet1=connection.createStatement().executeQuery("Select customerName,customerLevel(creditLimit)From customers");
		 resultSet2=  connection.createStatement().executeQuery("select customerName,case when creditLimit>50000 then 'platinum' when creditlimit>=10000 and creditLimit<50000 then 'gold' when creditLimit<10000 then 'silver' end as customerLevel from customers;");
		Assert.assertEquals(compareResultSets(resultSet1, resultSet2), true);
	}

	public boolean compareResultSets(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
		while (resultSet1.next()) {
			resultSet2.next();
			int count = resultSet1.getMetaData().getColumnCount();
			for (int i = 1; i <= count; i++) {
				if (!StringUtils.equals(resultSet1.getString(i), resultSet2.getString(i))) {
					return false;
				}
			}
		}
		return true;
	}

	@Test(priority = 3, enabled = true)
	void getCustomersLevel() throws SQLException {
		callableStatement = connection.prepareCall("{CALL GETCUSTOMERLEVEL(?,?)}");
		callableStatement.setInt(1, 131);
		callableStatement.registerOutParameter(2, Types.VARCHAR);
		callableStatement.executeQuery();
		String customerLevel=callableStatement.getString(2);
		 resultSet=  connection.createStatement().executeQuery("select customerName,case when creditLimit>50000 then 'platinum' when creditlimit>=10000 and creditLimit<50000 then 'gold' when creditLimit<10000 then 'silver' end as customerLevel from customers where customernumber=131;");
		resultSet.next();
		String exp_customerLevel=resultSet.getString("customerLevel");

		  Assert.assertEquals(customerLevel,exp_customerLevel);

	}

}
