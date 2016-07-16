/* Table Definitions */
CREATE TABLE IF NOT EXISTS multilanguage2
(
	key2 VARCHAR(240) NOT NULL,
	en CLOB,
	es CLOB,	
	
	CONSTRAINT multilanguage2 PRIMARY KEY (key2)
);

/* Data Definitions */
DELETE FROM multilanguage2; 

INSERT INTO multilanguage2 (key2, en, es) VALUES ('KEY_1', 'english_1', 'castellano_1');
INSERT INTO multilanguage2 (key2, en, es) VALUES ('KEY_2', 'english_2', 'castellano_2');
INSERT INTO multilanguage2 (key2, en, es) VALUES ('KEY_3', 'english_3', 'castellano_3');