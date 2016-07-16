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
package es.molabs.jdbc.language.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import es.molabs.jdbc.mapper.DbRowMapper;

public class GetKeyRowMapper implements DbRowMapper<List<String>> 
{
	/*
	 * Static
	 */
	private static GetKeyRowMapper INSTANCE = null;
	
	static
	{
		INSTANCE = new GetKeyRowMapper();
	}
	
	public static GetKeyRowMapper getInstance()
	{
		return INSTANCE;
	}
	
	
	/*
	 * Instanced
	 */
	private GetKeyRowMapper()
	{			
	}
	
	public List<String> mapRow(ResultSet resultSet, int rowNum) throws SQLException 
	{
		List<String> valueList = new LinkedList<String>();
				
		int count = resultSet.getMetaData().getColumnCount();
		
		for (int i=0; i<count; i++)
		{
			valueList.add(resultSet.getString(i+1));
		}
		
		return valueList;
	}
}
