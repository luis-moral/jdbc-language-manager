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
package es.molabs.jdbc.language;

import java.util.Locale;

public class LocaleValue
{
	private Locale locale = null;
	private String value = null;
	
	public LocaleValue(Locale locale, String value)
	{
		this.locale = locale;
		this.value = value;
	}
	
	public Locale getLocale() 
	{
		return locale;
	}
	
	public void setLocale(Locale locale) 
	{
		this.locale = locale;
	}
	
	public String getValue() 
	{
		return value;
	}
	
	public void setValue(String value) 
	{
		this.value = value;
	}
}
