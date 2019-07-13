/**
 * The MIT License
 * 
 * Copyright (c) 2016 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven Chen
 *
 */
public abstract class Jdbcs {

	public static String composeDialectLimitRowsSQL(String sql, Connection conn, int limited) throws SQLException {
		String dbVendor = conn.getMetaData().getDatabaseProductName();
		StringBuilder sb = new StringBuilder(sql);
		if ("MySQL".equals(dbVendor))
			return sb.append(" limit ").append(limited).toString();
		else if (dbVendor.startsWith("Microsoft SQL Server"))
			return sb.insert(sb.indexOf("select") + 6, " top " + limited).toString();
		else if ("Sybase SQL Server".equals(dbVendor) || "Adaptive Server Enterprise".equals(dbVendor)) {
			return sb.insert(sb.indexOf("select") + 6, " top " + limited).toString();
		} else if (dbVendor.startsWith("DB2")) {
			return sb.append(" fetch first ").append(limited).append(" rows only").toString();
		}
		throw new IllegalStateException("Unsupported DB Vendor:" + dbVendor);
	}

	public static String dailectTestQuery(Connection conn) throws SQLException {
		String dbVendor = conn.getMetaData().getDatabaseProductName();
		if ("MySQL".equals(dbVendor))
			return "select 1";
		else if (dbVendor.startsWith("Microsoft SQL Server"))
			return "select 1";
		else if ("Sybase SQL Server".equals(dbVendor) || "Adaptive Server Enterprise".equals(dbVendor))
			return "select 1";
		else if ("PostgreSQL".equals(dbVendor) || "Adaptive Server Enterprise".equals(dbVendor))
			return "select 1";
		else if (dbVendor.startsWith("DB2"))
			return "select 1 from sysibm.sysdummy1";
		else if (dbVendor.startsWith("Oracle"))
			return "select 1 from dual";

		throw new IllegalStateException("Unsupported DB Vendor:" + dbVendor);
	}

	static class ResultColumn {
		String name;
		int resultIndex;
		int type;
		Object value;

		public Object getTypedValue() {
			if (value == null)
				return null;
			switch (type) {
			case java.sql.Types.TIMESTAMP:
				return ((java.sql.Timestamp) value).getTime();
			case java.sql.Types.DATE:
				return ((java.sql.Date) value).getTime();
			case java.sql.Types.NUMERIC:
			case java.sql.Types.DECIMAL:
			case java.sql.Types.INTEGER:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.BIGINT:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.CHAR:
				return value;
			default:
				return String.valueOf(value);
			}

		}

		public String toString() {
			return name;
		}
	}

	public static List<Map<String, Object>> handleResultSetAsListMap(ResultSet rs) throws SQLException {
		List<Map<String, Object>> resultList = new ArrayList<>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		ResultColumn[] resultColumns = new ResultColumn[cols];
		for (int i = 0; i < cols; i++) {
			ResultColumn item = resultColumns[i] = new ResultColumn();
			int rsIdx = i + 1;
			item.name = rsmd.getColumnLabel(rsIdx).toLowerCase();
			item.type = rsmd.getColumnType(rsIdx);
			item.resultIndex = rsIdx;
		}
		while (rs.next()) {
			Map<String, Object> resultMap = new LinkedHashMap<>();
			for (ResultColumn resultColumn : resultColumns) {
				resultColumn.value = rs.getObject(resultColumn.resultIndex);
				if (resultMap.get(resultColumn.name) == null)
					resultMap.put(resultColumn.name, resultColumn.getTypedValue());
			}
			resultList.add(resultMap);
		}
		return resultList;
	}

	public static void commit(Connection conn) {
		try {
			if (conn != null) {
				conn.commit();
			}
		} catch (SQLException e) {
		}
	}

	public static void rollback(Connection conn) {
		try {
			if (conn != null) {
				conn.rollback();
			}
		} catch (SQLException e) {
		}
	}

	public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		close(rs);
		close(pstmt);
		close(conn);
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
		}

	}

	public static void close(PreparedStatement pstmt) {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException e) {
		}

	}

	public static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.setAutoCommit(true);
				conn.close();
			}
		} catch (SQLException e) {
		}

	}

	public static java.sql.Timestamp toSQLTimestamp(Date date) {
		if (date == null)
			return null;
		return new java.sql.Timestamp(date.getTime());
	}

	public static java.sql.Date toSQLDate(Date date) {
		if (date == null)
			return null;
		return new java.sql.Date(date.getTime());
	}

	public static Integer getInteger(ResultSet rs, int index) throws SQLException {
		Object v = rs.getObject(index);
		if (v == null)
			return null;
		return (Integer) v;
	}

	public static Long getLong(ResultSet rs, int index) throws SQLException {
		Object v = rs.getObject(index);
		if (v == null)
			return null;
		return (Long) v;
	}

	public static Boolean getIntegerAsBoolean(ResultSet rs, int index) throws SQLException {
		Integer value = rs.getInt(index);
		if (rs.wasNull())
			return false;
		if (value == 0)
			return false;
		return true;
	}

	public static void setInteger(PreparedStatement pstmt, int index, Integer value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, java.sql.Types.INTEGER);
		else
			pstmt.setInt(index, value);
	}

	public static void setDouble(PreparedStatement pstmt, int index, Double value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, java.sql.Types.DOUBLE);
		else
			pstmt.setDouble(index, value);
	}

	public static void setLong(PreparedStatement pstmt, int index, Long value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, java.sql.Types.INTEGER);
		else
			pstmt.setLong(index, value);
	}

	public static void setBigDecimal(PreparedStatement pstmt, int index, BigDecimal value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, java.sql.Types.NUMERIC);
		else
			pstmt.setBigDecimal(index, value);
	}

}
