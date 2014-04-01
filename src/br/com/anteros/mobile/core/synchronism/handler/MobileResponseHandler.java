package br.com.anteros.mobile.core.synchronism.handler;

import java.io.Reader;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.mobile.core.synchronism.model.FieldSynchronism;
import br.com.anteros.mobile.core.synchronism.model.TableSynchronism;
import br.com.anteros.mobile.core.util.Base64;
import br.com.anteros.mobile.core.util.ConvertTypes;
import br.com.anteros.persistence.handler.ResultSetHandler;

@SuppressWarnings("all")
public class MobileResponseHandler implements ResultSetHandler {

	private TableSynchronism tableSynchronism;
	private SynchronismManager synchronismManager;

	public MobileResponseHandler(SynchronismManager synchronismManager, TableSynchronism tableSynchronism) {
		this.tableSynchronism = tableSynchronism;
		this.synchronismManager = synchronismManager;
	}

	public Object handle(ResultSet resultSet) throws SQLException {
		MobileResponse mobileResponse = new MobileResponse("", this.getTableSynchronism().getTableNameMobile());
		mobileResponse.setFields(this.getTableSynchronism().getFieldsSqlString());

		ResultSetMetaData resultSetMetadata = resultSet.getMetaData();
		FieldSynchronism fields[] = tableSynchronism.getFields();
		ArrayList<String> values;
		while (resultSet.next()) {
			values = new ArrayList<String>();
			for (FieldSynchronism fieldSynchronism : fields) {
				String value = getColumnValue(resultSetMetadata, resultSet, fieldSynchronism.getSqlFieldName(),
						((FieldSynchronism) fieldSynchronism).getFieldType()).replace("$", "").replace("#", "").replace("|", "");
				if (value.isEmpty()){
					value = "_";
				}
				values.add(value);
			}
			mobileResponse.getData().add(values.toArray(new String[] {}));
		}
		return mobileResponse;
	}

	public String getColumnValue(ResultSetMetaData resultSetMetadata, ResultSet resultSet, String fieldSqlName,
			Long fieldType) throws SQLException {
		int numberOfCols = resultSetMetadata.getColumnCount();

		for (int i = 1; i <= numberOfCols; i++) {
			if (resultSetMetadata.getColumnName(i).equals(fieldSqlName)) {
				if (fieldType == Types.DATE) {
					if (resultSet.getDate(i) == null)
						return "_";
					else
						return ConvertTypes.dateFormat.format(resultSet.getDate(i));
				}
				if (fieldType == Types.TIMESTAMP) {
					if (resultSet.getDate(i) == null)
						return "_";
					else
						return ConvertTypes.timeStampFormat.format(resultSet.getTimestamp(i));
				}

				if (fieldType == Types.TIME) {
					if (resultSet.getDate(i) == null)
						return "_";
					else
						return ConvertTypes.timeFormat.format(resultSet.getTimestamp(i));
				}

				if (fieldType == Types.BLOB) {
					if (resultSet.getBlob(i) == null)
						return "_";
					else {
						Blob blob = resultSet.getBlob(i);
						byte[] b = blob.getBytes(1, (int) blob.length());
						return "<X64>"+Base64.encodeBytes(b);
					}
				}

				if (fieldType == Types.CLOB) {
					if (resultSet.getClob(i) == null)
						return "_";
					else {
						try {
							Clob clob = resultSet.getClob(i);
							char clobVal[] = new char[(int) clob.length()];
							Reader r = clob.getCharacterStream();
							r.read(clobVal);
							StringWriter sw = new StringWriter();
							sw.write(clobVal);
							return "<X64>"+Base64.encodeBytes(sw.toString().getBytes());
						} catch (Exception e) {
							return "_";
						}
					}
				}

				if (resultSet.getString(i) == null)
					return "_";
				else
					return resultSet.getString(i);
			}
		}
		return "_";
	}

	public TableSynchronism getTableSynchronism() {
		return tableSynchronism;
	}

	public void setTableSynchronism(TableSynchronism tableSynchronism) {
		this.tableSynchronism = tableSynchronism;
	}

	public SynchronismManager getSynchronismManager() {
		return synchronismManager;
	}

	public void setSynchronismManager(SynchronismManager synchronismManager) {
		this.synchronismManager = synchronismManager;
	}

}
