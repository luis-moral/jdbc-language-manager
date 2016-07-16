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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.molabs.jdbc.DbQuery;
import es.molabs.jdbc.language.db.DbMultilanguage;

public class DbLanguageManager 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Locale defaultLocale = null;
	private String tableName = null;
	private String keyName = null;
	
	private Map<Locale, String> localeMap = null;
	private DbMultilanguage dbMultilanguage = null;
	
	private boolean initialized;
	
	public DbLanguageManager(Locale defaultLocale)
	{
		this(defaultLocale, "multilanguage", "key");
	}
	
	public DbLanguageManager(Locale defaultLocale, String tableName, String keyName)
	{
		this.defaultLocale = defaultLocale;		
		this.tableName = tableName;
		this.keyName = keyName;
		
		localeMap = new LinkedHashMap<Locale, String>();
		dbMultilanguage = new DbMultilanguage(this);
		
		initialized = false;
	}
	
	public void init()
	{		
		if (!initialized)
		{
			// Sets the manager as initialized
			initialized = true;
			
			logger.info("Initialized.");
		}
		else
		{
			logger.warn("Already initialized.");
		}
	}
	
	public void destroy()
	{
		if (initialized)
		{			
			// Sets the manager as not initialized
			initialized = false;
			
			logger.info("Destroyed.");
		}
		else
		{
			logger.warn("Already destroyed.");
		}
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public Locale getDefaultLocale()
	{
		return defaultLocale;
	}
	
	public void setDefaultLocale(Locale defaultLocale)
	{
		this.defaultLocale = defaultLocale;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public String getKeyName()
	{
		return keyName;
	}
	
	public void setField(String field, Locale locale)
	{
		localeMap.put(locale, field);
	}
	
	public String getField(Locale locale)
	{
		return localeMap.get(normalizeLocale(locale));
	}
	
	public String getLocalizedKey(String key, Locale locale, DbQuery dbQuery)
	{
		if (!initialized) throw new IllegalStateException("Not initialized.");
		
		return dbMultilanguage.getKey(key, getField(locale), dbQuery);
	}
	
	public List<LocaleValue> getKey(String key, DbQuery dbQuery)
	{
		return dbMultilanguage.getKey(key, dbQuery);
	}
	
	public void addKey(DbQuery dbQuery, LocalizedKey...localizedKeys)
	{
		dbMultilanguage.insertKey(dbQuery, localizedKeys);
	}
	
	public void setKey(DbQuery dbQuery, LocalizedKey...localizedKeys)
	{
		dbMultilanguage.updateKey(dbQuery, localizedKeys);
	}
	
	public void removeKey(String key, DbQuery dbQuery)
	{
		dbMultilanguage.deleteKey(key, dbQuery);
	}
	
	public void duplicateKey(String sourceKey, String destinationKey, DbQuery dbQuery)
	{
		dbMultilanguage.duplicateKey(sourceKey, destinationKey, dbQuery);
	}
	
	public StringBuilder exportKey(String key, StringBuilder sql, DbQuery dbQuery)
	{
		return dbMultilanguage.export(key, sql, dbQuery);
	}
	
	public Locale normalizeLocale(Locale locale)
	{
		// If the locale map does not exists the locale
		if (!localeMap.containsKey(locale))
		{		
			// Gets the locale lookupList, if the locale is null returns the default locale because LocaleUtils.localeLookupList returns an empty list
			List<Locale> lookupList = LocaleUtils.localeLookupList((locale != null ? locale : defaultLocale), defaultLocale);
						
			// For each locale in the list, looks for the key till is not null of the iterator ends			
			Iterator<Locale> iterator = lookupList.iterator();
			
			while (iterator.hasNext())
			{
				Locale lookupLocale = iterator.next();
				
				if (localeMap.containsKey(lookupLocale))
				{
					locale = lookupLocale;
					
					break;
				}			
			}
		}
		
		return locale;
	}
	
	public Set<Locale> getLocaleSet()
	{
		return localeMap.keySet();
	}
}