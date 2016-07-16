/**
 * Copyright (C) 2016 Luis Moral Guerrero <luis.moral@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.molabs.jdbc.language.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import es.molabs.jdbc.DbQuery;
import es.molabs.jdbc.exception.DbException;
import es.molabs.jdbc.language.DbLanguageManager;
import es.molabs.jdbc.language.LocaleValue;
import es.molabs.jdbc.language.LocalizedKey;
import es.molabs.jdbc.language.db.dao.GetKeyRowMapper;
import es.molabs.jdbc.mapper.ClobRowMapper;

public class DbMultilanguage 
{
	private DbLanguageManager languageManager = null;
	
	public DbMultilanguage(DbLanguageManager languageManager)
	{
		this.languageManager = languageManager;
	}
	
	public String getKey(String key, String field, DbQuery dbQuery) throws DbException
	{		
		return dbQuery.getObject(ClobRowMapper.getInstance(), "SELECT " + field + " FROM " + languageManager.getTableName() + " WHERE " + languageManager.getKeyName() + " = ?", key);
	}
	
	public List<LocaleValue> getKey(String key, DbQuery dbQuery)
	{
		String fields = StringUtils.join(languageManager.getLocaleSet().iterator(), ", ");
				
		return toLocaleValueList(languageManager.getLocaleSet(), dbQuery.getObject(GetKeyRowMapper.getInstance(), "SELECT " + fields + " FROM " + languageManager.getTableName() + " WHERE " + languageManager.getKeyName() + " = ?", key));
	}
	
	public int insertKey(DbQuery dbQuery, LocalizedKey...localizedKeys)
	{
		// Checks that localizedKeys is not null or empty
		if (localizedKeys == null || localizedKeys.length == 0) throw new IllegalArgumentException("LocalizedKeys parameter cannot be null or empty.");
		
		StringBuffer sql = null;
		
		// For each LocalizedKey
		for (int k=0; k<localizedKeys.length; k++)
		{
			LocaleValue[] localeValues = localizedKeys[k].getLocaleValues();
			
			// If its the first value we look for the fields (locale) and the values
			if (k == 0)
			{
				StringBuilder fieldBuilder = new StringBuilder();
				StringBuilder valueBuilder = new StringBuilder();
				
				for (int i=0; i<localeValues.length; i++)
				{
					if (i != 0)
					{
						fieldBuilder.append(", ");
						valueBuilder.append(", ");
					}
					
					fieldBuilder.append(localeValues[i].getLocale().toString());			
					valueBuilder.append("?");
				}				
				
				sql = new StringBuffer("INSERT INTO " + languageManager.getTableName() + " (" + languageManager.getKeyName() + ", " + fieldBuilder.toString() + ") VALUES (?, " + valueBuilder.toString() + ")");
			}
			// If there is only values
			else
			{
				StringBuilder valueBuilder = new StringBuilder();
				
				for (int i=0; i<localeValues.length; i++)
				{
					if (i != 0) valueBuilder.append(", ");
					
					valueBuilder.append("?");
				}
				
				sql.append(",(?, " + valueBuilder.toString() + ")");
			}						
		}		

		return dbQuery.executeUpdate(sql.toString(), toInsertValues(localizedKeys));
	}	
	
	public int updateKey(DbQuery dbQuery, LocalizedKey...localizedKeys)
	{		
		// Checks that localizedKeys is not null or empty
		if (localizedKeys == null || localizedKeys.length == 0) throw new IllegalArgumentException("LocalizedKeys parameter cannot be null or empty.");
				
		Map<String, StringBuilder> fieldValueMap = new HashMap<String, StringBuilder>();		
		
		StringBuilder sql = null;
		StringBuilder keyBuilder = new StringBuilder();		
				
		// For each LocalizedKey
		for (int k=0; k<localizedKeys.length; k++)
		{
			// Adds the key
			if (k != 0) keyBuilder.append(",");			
			keyBuilder.append("?");
			
			LocaleValue[] localeValues = localizedKeys[k].getLocaleValues();			
			
			// Anadimos los valores de esta key para este campo (locale) al map
			for (int i=0; i<localeValues.length; i++)
			{				
				if (fieldValueMap.get(localeValues[i].getLocale().toString()) == null) fieldValueMap.put(localeValues[i].getLocale().toString(), new StringBuilder());				
				
				fieldValueMap.get(localeValues[i].getLocale().toString()).append(" WHEN " + languageManager.getKeyName() + " = '" + localizedKeys[k].getKey() +  "' THEN '" + localeValues[i].getValue() + "'");				
			}
		}		
		
		sql = new StringBuilder("UPDATE " + languageManager.getTableName() + " SET ");
		
		boolean first = true;
		Iterator<String> iterator = fieldValueMap.keySet().iterator();
		
		// For each value in the map adds it to the sql
		while (iterator.hasNext())
		{
			String field = iterator.next();
			
			if (!first) sql.append(", ");
		
			sql.append(field);		
			sql.append(" = (CASE" + fieldValueMap.get(field).toString() + " END)");
			
			if (!first) sql.append(" ");
			
			first = false;
		}
		
		sql.append("WHERE " + languageManager.getKeyName() + " IN (" + keyBuilder.toString() + ")");
				
		return dbQuery.executeUpdate(sql.toString(), toUpdateValues(localizedKeys));
	}
	
	public int deleteKey(String key, DbQuery dbQuery)
	{		
		return dbQuery.executeUpdate("DELETE FROM " + languageManager.getTableName() + " WHERE " + languageManager.getKeyName() + " = ?", key);		
	}
	
	public int duplicateKey(String sourceKey, String destinationKey, DbQuery dbQuery)
	{
		String fields = StringUtils.join(languageManager.getLocaleSet().iterator(), ", ");
		
		return dbQuery.executeUpdate("INSERT INTO " + languageManager.getTableName() + " (SELECT '" + StringEscapeUtils.escapeJava(destinationKey) + "', " + fields + " FROM " + languageManager.getTableName() + " WHERE " + languageManager.getKeyName() + " = ?)", sourceKey);
	}
	
	public StringBuilder export(String key, StringBuilder sql, DbQuery dbQuery)
	{		
		// Fields to insert
		String fields = StringUtils.join(languageManager.getLocaleSet().iterator(), ", ");
				
		sql.append("INSERT INTO " + languageManager.getTableName() + " (" + languageManager.getKeyName() + ", " + fields + ") VALUES ('" + key + "'");
				
		// For each locale
		Iterator<Locale> iterator = languageManager.getLocaleSet().iterator();
		while (iterator.hasNext())
		{
			Locale locale = iterator.next();
		
			sql.append(", '");			
			sql.append(getKey(key, languageManager.getField(locale), dbQuery));
			sql.append("'");
		}
		
		sql.append(");");
		
		return sql;
	}
	
	private Object[] toInsertValues(LocalizedKey...localizedKeys)
	{
		List<Object> valueList = new LinkedList<Object>();
		
		// For each LocalizedKey
		for (int k=0; k<localizedKeys.length; k++)
		{
			valueList.add(localizedKeys[k].getKey());
			
			// For each LocaleValue of the LocalizedKey
			for (int i=0; i<localizedKeys[k].getLocaleValues().length; i++)
			{
				valueList.add(localizedKeys[k].getLocaleValues()[i].getValue());
			}
		}
		
		return valueList.toArray();
	}
	
	private Object[] toUpdateValues(LocalizedKey...localizedKeys)
	{
		Object[] values = new Object[localizedKeys.length];
		
		// For each LocalizedKey
		for (int k=0; k<localizedKeys.length; k++)
		{
			values[k] = localizedKeys[k].getKey();			
		}
		
		return values;
	}
	
	private List<LocaleValue> toLocaleValueList(Set<Locale> localeSet, List<String> valueList)
	{
		if (localeSet == null || valueList == null) return null;
		
		List<LocaleValue> localeValueList = new ArrayList<LocaleValue>(valueList.size());
		
		Iterator<String> valueIterator = valueList.iterator();
		Iterator<Locale> localeIterator = localeSet.iterator();
		
		while (valueIterator.hasNext() && localeIterator.hasNext())
		{
			localeValueList.add(new LocaleValue(localeIterator.next(), valueIterator.next()));
		}
		
		return localeValueList;
	}
}
