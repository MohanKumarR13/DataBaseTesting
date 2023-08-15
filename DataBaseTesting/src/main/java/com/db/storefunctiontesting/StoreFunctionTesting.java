package com.db.storefunctiontesting;

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

public class StoreFunctionTesting {
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
	void store_procedureexists() throws SQLException {
		statement = connection.createStatement();
		resultSet = statement.executeQuery("SHOW PROCEDURE STATUS WHERE Name='SelectAllCustomers'");
		resultSet.next();
		Assert.assertEquals(resultSet.getString("Name"), "SelectAllCustomers");
	}

	@Test(priority = 2, enabled = false)
	void selectAllCustomers() throws SQLException {
		callableStatement = connection.prepareCall("{CALL SelectAllCustomers()}");
		resultSet1 = callableStatement.executeQuery(); // Result set 1
		Statement statement = connection.createStatement();
		resultSet2 = statement.executeQuery("SELECT * FROM CUSTOMERS");
		Assert.assertEquals(compareResultSets(resultSet1, resultSet2), true);
	}

	public boolean compareResultSets(ResultSet resultSet_1, ResultSet resultSet_2) throws SQLException {
		while (resultSet_1.next()) {
			resultSet_2.next();
			int count = resultSet_1.getMetaData().getColumnCount();
			for (int i = 1; i <= count; i++) {
				if (!StringUtils.equals(resultSet_1.getString(i), resultSet_2.getString(i))) {
					return false;
				}
			}
		}
		return true;
	}

	@Test(priority = 3, enabled = false)
	void selectAllCustomersByCity() throws SQLException {
		callableStatement = connection.prepareCall("{CALL SelectAllCustomerByCity(?)}");
		callableStatement.setString(1, "Singapore");
		resultSet1 = callableStatement.executeQuery(); // Result set 1
		Statement statement = connection.createStatement();
		resultSet2 = statement.executeQuery("SELECT * FROM CUSTOMERS WHERE City='Sigapore'");
		// Assert.assertEquals(compareResultSet1(resultSet1, resultSet2), true);

	}

	public static boolean compareResultSet1(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
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

	@Test(priority = 4, enabled = false)
	void selectAllCustomersByCityAndPinCode() throws SQLException {
		callableStatement = connection.prepareCall("{CALL SelectAllCustomerByCityAndPin(?,?)}");
		callableStatement.setString(1, "Singapore");
		callableStatement.setString(2, "079903");

		resultSet1 = callableStatement.executeQuery(); // Result set 1
		Statement statement = connection.createStatement();
		resultSet2 = statement.executeQuery("SELECT * FROM CUSTOMERS WHERE City='Sigapore' And PostalCode='079903'");
		// Assert.assertEquals(compareResultSet(resultSet1, resultSet2), true);
	}

	public boolean compareResultSet(ResultSet resultSet_1, ResultSet resultSet_2) throws SQLException {
		while (resultSet_1.next()) {
			resultSet_2.next();
			int count = resultSet_1.getMetaData().getColumnCount();
			for (int i = 1; i <= count; i++) {
				if (!StringUtils.equals(resultSet_1.getString(i), resultSet_2.getString(i))) {
					return false;
				}
			}
		}
		return true;
	}

	@Test(priority = 5, enabled = true)
	void getOrderByCustomers() throws SQLException {
		callableStatement = connection.prepareCall("{CALL getorderbycustomer(?,?,?,?,?)}");
		callableStatement.setInt(1, 141);
		callableStatement.registerOutParameter(2, Types.INTEGER);
		callableStatement.registerOutParameter(3, Types.INTEGER);
		callableStatement.registerOutParameter(4, Types.INTEGER);
		callableStatement.registerOutParameter(5, Types.INTEGER);

		callableStatement.executeQuery();
		int shipped = callableStatement.getInt(2);
		int cancelled = callableStatement.getInt(3);
		int resolved = callableStatement.getInt(4);
		int disputed = callableStatement.getInt(5);
		System.out.println(shipped + " " + cancelled + " " + resolved + " " + disputed);
		Statement statement = connection.createStatement();
		resultSet = statement.executeQuery(
				"select (select count(*)as shipped from orders where customerNumber=141 and status='Shipped') as Shipped,(select count(*)as canceled from orders where customerNumber=141 and status='Canceled') as Canceled,(select count(*)as resolved from orders where customerNumber=141 and status='Resolved') as Resolved,(select count(*)as disputed from orders where customerNumber=141 and status='Disputed') as Disputed;");
		resultSet.next();
		int exp_shipped = resultSet.getInt("shipped");
		int exp_cancelled = resultSet.getInt("canceled");
		int exp_resolved = resultSet.getInt("resolved");
		int exp_disputed = resultSet.getInt("disputed");
		if (shipped == exp_shipped && cancelled == exp_cancelled && resolved == exp_resolved
				&& disputed == exp_disputed)
			Assert.assertTrue(true);
		else
			Assert.assertTrue(false);
	}

	@Test(priority = 5, enabled = true)
	void getOrderByCustomerShipping() throws SQLException {
		callableStatement = connection.prepareCall("{CALL getcustomershipping(?,?)}");
		callableStatement.setInt(1, 112);
		callableStatement.registerOutParameter(2, Types.VARCHAR);

		callableStatement.executeQuery();
		String shippingtime = callableStatement.getString(2);

		Statement statement = connection.createStatement();
		resultSet = statement.executeQuery(
				"select country,case when country='USA' Then'2-day Shipping' WHEN 'Canada' Then'3-day Shipping' ELSE '5-day Shipping'END AS ShippingTime from customers where customernumber=112;");
		resultSet.next();
		String exp_ShippingTime = resultSet.getString("ShippingTime");
		Assert.assertEquals(shippingtime, exp_ShippingTime);

	}
}
