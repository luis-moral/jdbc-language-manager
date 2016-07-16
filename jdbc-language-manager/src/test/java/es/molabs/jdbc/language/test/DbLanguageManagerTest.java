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
package es.molabs.jdbc.language.test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import es.molabs.jdbc.DbManager;
import es.molabs.jdbc.language.DbLanguageManager;
import es.molabs.jdbc.language.LocaleValue;
import es.molabs.jdbc.language.LocalizedKey;

@RunWith(MockitoJUnitRunner.class)
public class DbLanguageManagerTest 
{	
	private final static Locale LOCALE_EN = new Locale("en");
	private final static Locale LOCALE_ES = new Locale("es");
	
	private static JdbcConnectionPool dataSource = null;
	private static DbManager dbManager = null;
	private static DbLanguageManager languageManager = null;
		
	@Test
	public void testInitialization() throws Throwable
	{
		// Destroys the manager
		languageManager.destroy();
		
		Throwable exception = null;
				
		// Checks that an IllegalStateException is thrown
		try
		{
			languageManager.getLocalizedKey("KEY_1", LOCALE_EN, dbManager.getDbNonTransaction());
		}
		catch (Throwable t)
		{
			exception = t;
		}
		Assert.assertEquals("Value must be [" + true + "].", true, exception.getClass() == IllegalStateException.class);
		
		// Initializes the manager
		languageManager.init();
		
		// Checks that the properties are loaded
		testLocalizeString(languageManager, "KEY_1", LOCALE_EN, "english_1");
		
		// Checks that its initialized
		boolean expectedValue = true;
		boolean value = languageManager.isInitialized();
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue, value);
				
		// Destroys the manager
		languageManager.destroy();
		
		// Checks that an IllegalStateException is thrown
		try
		{
			languageManager.getLocalizedKey("KEY_1", LOCALE_EN, dbManager.getDbNonTransaction());
		}
		catch (Throwable t)
		{
			exception = t;
		}
		Assert.assertEquals("Value must be [" + true + "].", true, exception.getClass() == IllegalStateException.class);
		
		// Checks that is not initialized
		expectedValue = false;
		value = languageManager.isInitialized();
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue, value);
		
		// Initialized the manager again
		languageManager.init();
		
		// Checks that the properties are loaded
		testLocalizeString(languageManager, "KEY_1", LOCALE_EN, "english_1");
		
		// Checks that its initialized
		expectedValue = true;
		value = languageManager.isInitialized();
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue, value);
		
		languageManager.destroy();
	}
	
	@Test
	public void testChangeDefaultLocale() throws Throwable
	{		
		// Checks that if a value for a locale does not exists gets the default locale value
		testLocalizeString(languageManager, "KEY_1", new Locale("fr"), "english_1");
		
		// Sets a new default locale
		languageManager.setDefaultLocale(LOCALE_ES);
		
		// Checks that if a value for a locale does not exists gets the default locale value
		testLocalizeString(languageManager, "KEY_1", new Locale("fr"), "castellano_1");
		
		languageManager.destroy();
	}
	
	@Test
	public void testGetLocalizedKey() throws Throwable
	{		
		// Checks a value for LOCALE_EN
		testLocalizeString(languageManager, "KEY_1", LOCALE_EN, "english_1");
		
		// Checks a value for LOCALE_ES
		testLocalizeString(languageManager, "KEY_1", LOCALE_ES, "castellano_1");
		
		// Checks a value for a locale that does not exists so it gets the default locale value
		testLocalizeString(languageManager, "KEY_1", new Locale("fr"), "english_1");
		
		// Checks a value for a locale with language and country so it gets the value for that locale language instead 
		testLocalizeString(languageManager, "KEY_1", new Locale("es", "ES", "test"), "castellano_1");
		
		// Checks a value for a null locale so it gets the default locale value
		testLocalizeString(languageManager, "KEY_1", null, "english_1");
		
		// Checks a value for LOCALE_EN
		testLocalizeString(languageManager, "KEY_3", LOCALE_EN, "english_3");
		
		// Checks a value for LOCALE_ES
		testLocalizeString(languageManager, "KEY_3", LOCALE_ES, "castellano_3");
		
		languageManager.destroy();		
	}
	
	@Test 
	public void testGetKey() throws Throwable
	{
		List<LocaleValue> localeValueList = languageManager.getKey("KEY_1", dbManager.getDbNonTransaction());
		
		// Checks that the list has 2 elements
		Assert.assertEquals("Value must be [" + 2 + "].", 2, localeValueList.size());
		
		Iterator<LocaleValue> iterator = localeValueList.iterator();
		while (iterator.hasNext())
		{
			LocaleValue localeValue = iterator.next();
			
			if (localeValue.getLocale().equals(LOCALE_EN))
			{				
				Assert.assertEquals("Value must be [" + "english_1" + "].", "english_1", localeValue.getValue());
			}
			else
			{
				Assert.assertEquals("Value must be [" + "castellano_1" + "].", "castellano_1", localeValue.getValue());
			}
		}
	}
	
	@Test
	public void testInsertKey() throws Throwable
	{
		// Checks that the key does not exists
		testLocalizeString(languageManager, "KEY_INSERT", LOCALE_EN, null);
		
		// Insert new key for LOCALE_EN
		languageManager.addKey(dbManager.getDbNonTransaction(), new LocalizedKey("KEY_INSERT", new LocaleValue(LOCALE_EN, "inserted")));
		
		// Checks that the new key exists in the database
		testLocalizeString(languageManager, "KEY_INSERT", LOCALE_EN, "inserted");
		
		// Deletes the inserted key
		languageManager.removeKey("KEY_INSERT", dbManager.getDbNonTransaction());
		
		// Insert new key with various locales
		languageManager.addKey(dbManager.getDbNonTransaction(), new LocalizedKey("KEY_INSERT", new LocaleValue(LOCALE_EN, "inserted"), new LocaleValue(LOCALE_ES, "insertada")), new LocalizedKey("KEY_INSERT_2", new LocaleValue(LOCALE_EN, "inserted 2"), new LocaleValue(LOCALE_ES, "insertada 2")));
		
		// Checks that the new keys exists in the database
		testLocalizeString(languageManager, "KEY_INSERT", LOCALE_EN, "inserted");
		testLocalizeString(languageManager, "KEY_INSERT", LOCALE_ES, "insertada");
		testLocalizeString(languageManager, "KEY_INSERT_2", LOCALE_EN, "inserted 2");
		testLocalizeString(languageManager, "KEY_INSERT_2", LOCALE_ES, "insertada 2");
	}
	
	@Test
	public void testUpdateKey() throws Throwable
	{
		// Inserts 2 new keys
		languageManager.addKey(dbManager.getDbNonTransaction(), new LocalizedKey("KEY_UPDATE", new LocaleValue(LOCALE_EN, "update_1"), new LocaleValue(LOCALE_ES, "actualizar_1")), new LocalizedKey("KEY_UPDATE_2", new LocaleValue(LOCALE_EN, "update2_1"), new LocaleValue(LOCALE_ES, "actualizar2_1")));
		
		// Updates its value for a locale
		languageManager.setKey(dbManager.getDbNonTransaction(), new LocalizedKey("KEY_UPDATE", new LocaleValue(LOCALE_EN, "update_2")));
		
		// Checks that its value was updated
		testLocalizeString(languageManager, "KEY_UPDATE", LOCALE_EN, "update_2");
		
		// Updates its value for various locale		
		languageManager.setKey(dbManager.getDbNonTransaction(), new LocalizedKey("KEY_UPDATE", new LocaleValue(LOCALE_EN, "update_3"), new LocaleValue(LOCALE_ES, "actualizar_3")), new LocalizedKey("KEY_UPDATE_2", new LocaleValue(LOCALE_EN, "update2_2"), new LocaleValue(LOCALE_ES, "actualizar2_2")));
		
		// Checks that their values were updated
		testLocalizeString(languageManager, "KEY_UPDATE", LOCALE_EN, "update_3");
		testLocalizeString(languageManager, "KEY_UPDATE", LOCALE_ES, "actualizar_3");
		testLocalizeString(languageManager, "KEY_UPDATE_2", LOCALE_EN, "update2_2");
		testLocalizeString(languageManager, "KEY_UPDATE_2", LOCALE_ES, "actualizar2_2");
	}
	
	@Test
	public void testDeleteKey() throws Throwable
	{
		// Checks that the key does not exists
		testLocalizeString(languageManager, "KEY_DELETE", LOCALE_EN, null);
		
		// Insert a new key for LOCALE_EN
		languageManager.addKey(dbManager.getDbNonTransaction(), new LocalizedKey("KEY_DELETE", new LocaleValue(LOCALE_EN, "key_delete")));
		
		// Deletes the inserted key
		languageManager.removeKey("KEY_DELETE", dbManager.getDbNonTransaction());
		
		// Checks that the key does not exist
		testLocalizeString(languageManager, "KEY_DELETE", LOCALE_EN, null);
	}
	
	@Test
	public void testDuplicateKey() throws Throwable
	{
		// Duplicates KEY_3
		languageManager.duplicateKey("KEY_3", "DUPLICATED_KEY", dbManager.getDbNonTransaction());
		
		// Checks that the duplicated key exists
		testLocalizeString(languageManager, "DUPLICATED_KEY", LOCALE_EN, "english_3");
		testLocalizeString(languageManager, "DUPLICATED_KEY", LOCALE_ES, "castellano_3");
	}
	
	@Test
	public void testExportKey() throws Throwable
	{
		// Creates a StringBuilder to store the export
		StringBuilder sql = new StringBuilder();
		
		// Exports the key
		languageManager.exportKey("KEY_2", sql, dbManager.getDbNonTransaction());
		
		String expectedValue = "INSERT INTO multilanguage2 (key2, en, es) VALUES ('KEY_2', 'english_2', 'castellano_2');";
		
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue, sql.toString());
	}
	
	private void testLocalizeString(DbLanguageManager languageManager, String key, Locale locale, String expectedValue)
	{	
		String value = languageManager.getLocalizedKey(key, locale, dbManager.getDbNonTransaction());
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue, value);
	}
	
	@Before
	public void setUp() throws Throwable
	{
		languageManager = new DbLanguageManager(LOCALE_EN, "multilanguage2", "key2");
		languageManager.setField("en", LOCALE_EN);
		languageManager.setField("es", LOCALE_ES);
		
		languageManager.init();
	}
	
	@After
	public void tearDown() throws Throwable
	{
		languageManager.destroy();
		languageManager = null;
	}
	
	@BeforeClass
	public static void runBeforeClass() throws Throwable
	{
		String url = "jdbc:h2:./src/test/resources/test-db;"; 
		url = url + "INIT=CREATE SCHEMA IF NOT EXISTS TEST_SCHEMA\\;";
		url = url + "SET SCHEMA TEST_SCHEMA";	
			
		dataSource = JdbcConnectionPool.create(url, "testUser", "testPassword");;
		dbManager = new DbManager();
		dbManager.init(dataSource);

		// Creates the test tables
		dbManager.getDbNonTransaction().executeUpdate(readSql("/es/molabs/jdbc/language/test/script/create_tables.sql"));
	}
	
	private static String readSql(String path) throws Throwable
	{
		String sql = null;
		
		InputStream inputStream = DbLanguageManagerTest.class.getResourceAsStream(path);
		
		sql = IOUtils.toString(inputStream);
		inputStream.close();
		
		return sql;
	}
	
	@AfterClass
	public static void runAfterClass() throws Throwable
	{
		// Drops all the tables and deletes the database file
		QueryRunner query = new QueryRunner(dataSource);
		query.update("DROP ALL OBJECTS DELETE FILES");
		
		dbManager.destroy();
		dbManager = null;
	}
}