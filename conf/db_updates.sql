-- -------------------------------------------
-- version 1.0
-- -------------------------------------------

CREATE DATABASE IF NOT EXISTS Period_Core;

USE Period_Core;

CREATE TABLE IF NOT EXISTS rec_status_ref (

	rec_status_id INT NOT NULL PRIMARY KEY,
	description VARCHAR(128) DEFAULT '' NOT NULL
);

INSERT INTO rec_status_ref VALUES
(-1, 'Deleted'),
(0, 'Inactive'),
(1, 'Active');

CREATE TABLE IF NOT EXISTS users (

	user_id VARCHAR(36) NOT NULL PRIMARY KEY,
	descr VARCHAR(100) DEFAULT ''
);

INSERT INTO users VALUES ('a9998ce6-da2d-11e5-b5d2-0a1d41d68578', 'System users');

CREATE TABLE IF NOT EXISTS db_info (

	info_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	db_version INT NOT NULL,
	app_version INT,
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36) DEFAULT NULL,
	rec_modified_when VARCHAR(128) DEFAULT NULL,
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id)
);

INSERT INTO db_info (db_version, app_version, rec_created_by, rec_created_when, rec_status)
VALUES (1, 1.0, 'a9998ce6-da2d-11e5-b5d2-0a1d41d68578', NOW(), 1);


CREATE TABLE IF NOT EXISTS device_info (

	device_id VARCHAR(36) NOT NULL PRIMARY KEY,
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36),
	rec_modified_when VARCHAR(128),
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id)
);


CREATE TABLE IF NOT EXISTS usage_stat (

	usage_id INT NOT NULL AUTO_INCREMENT,
	device_id VARCHAR(36) NOT NULL,
	application_version VARCHAR(10) NOT NULL,
	usage_counter INT NOT NULL,
	period_button_usage_counter INT NOT NULL,
	non_period_button_usage_counter INT NOT NULL,
	comment_button_usage_counter INT NOT NULL,
	comment_text_usage_counter INT NOT NULL,
	menu_button_usage_counter INT NOT NULL,
	review_now INT NOT NULL,
	review_later INT NOT NULL,
	review_non INT NOT NULL,
	fetch_next_usage_counter INT NOT NULL,
	fetch_previous_usage_counter INT NOT NULL,
	menu_setting_click_counter INT NOT NULL,
	menu_summary_click_counter INT NOT NULL,
	menu_month_view_click_counter INT NOT NULL,
	menu_help_click_counter INT NOT NULL,
	menu_review_click_counter INT NOT NULL,
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36),
	rec_modified_when VARCHAR(128),
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (device_id) REFERENCES device_info (device_id),
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id),
	PRIMARY KEY (usage_id),
	UNIQUE (device_id, application_version)
);

CREATE OR REPLACE VIEW usage_stat_vu AS SELECT * FROM usage_stat;

CREATE OR REPLACE VIEW device_info_vu AS SELECT * FROM device_info;

CREATE OR REPLACE VIEW users_vu AS SELECT * FROM users;

CREATE OR REPLACE VIEW db_info_vu AS SELECT * FROM db_info;

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version 2.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 2,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

CREATE TABLE IF NOT EXISTS daily_usage
(
 daily_usage_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 device_id VARCHAR(36) NOT NULL,
 data_date VARCHAR(128) NOT NULL,
 data_hour INT NOT NULL,
 usage_counter INT NOT NULL,
 rec_created_by VARCHAR(36) NOT NULL,
 rec_created_when VARCHAR(128) NOT NULL,
 rec_modified_by VARCHAR(36),
 rec_modified_when VARCHAR(128),
 rec_status INT NOT NULL,
 FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
 FOREIGN KEY (rec_modified_by) REFERENCES users(user_id),
 FOREIGN KEY (device_id) REFERENCES device_info(device_id),
 CONSTRAINT UNIQUE KEY (device_id, data_date, data_hour)
);

CREATE OR REPLACE VIEW daily_usage_vu AS (SELECT * FROM daily_usage);

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version 3.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 3,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE daily_usage
ADD COLUMN application_version VARCHAR(10) NOT NULL DEFAULT '25' AFTER device_id;

ALTER TABLE daily_usage ALTER COLUMN application_version DROP DEFAULT;

CREATE OR REPLACE VIEW daily_usage_vu AS (SELECT * FROM daily_usage);

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version 4.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 4,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE usage_stat
ADD COLUMN setting_notify_notification_click_counter INT NOT NULL DEFAULT 0 AFTER menu_review_click_counter,
ADD COLUMN setting_notify_ovulation_days INT NOT NULL DEFAULT 0 AFTER menu_review_click_counter,
ADD COLUMN setting_notify_period_days INT NOT NULL DEFAULT 0 AFTER menu_review_click_counter,
ADD COLUMN setting_notify_ovulation_usage_counter INT NOT NULL DEFAULT 0 AFTER menu_review_click_counter,
ADD COLUMN setting_notify_period_usage_counter INT NOT NULL DEFAULT 0 AFTER menu_review_click_counter;

CREATE OR REPLACE VIEW usage_stat_vu AS (SELECT * FROM usage_stat);

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version 5.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 5,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE device_info
ADD COLUMN language VARCHAR(10) AFTER device_id;

CREATE OR REPLACE VIEW device_info_vu AS (SELECT * FROM device_info);

ALTER TABLE `usage_stat`
ADD COLUMN setting_displayed_language VARCHAR(10) AFTER application_version,
ADD COLUMN setting_language_change_usage_counter INT AFTER setting_notify_notification_click_counter;

CREATE OR REPLACE VIEW usage_stat_vu AS (SELECT * FROM usage_stat);

-- -------------------------------------------
-- version 6.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 6,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

CREATE TABLE IF NOT EXISTS experiment (
	experiment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	description VARCHAR(255),
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36),
	rec_modified_when VARCHAR(128),
	rec_status INT NOT NULL DEFAULT 1,
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS experiment_ads_run (
	experiment_run_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	experiment_id INT NOT NULL,
	displayed_language VARCHAR(2) NOT NULL,
	a_ads_url VARCHAR(2000),
	a_ads_text NVARCHAR(255),
	a_ads_show INT,
	a_ads_click INT,
	b_ads_url NVARCHAR(2000),
	b_ads_text NVARCHAR(255),
	b_ads_show INT,
	b_ads_click INT,
	c_ads_url NVARCHAR(2000),
	c_ads_text NVARCHAR(255),
	c_ads_show INT,
	c_ads_click INT,
	d_ads_url NVARCHAR(2000),
	d_ads_text NVARCHAR(255),
	d_ads_show INT,
	d_ads_click INT,
	e_ads_url NVARCHAR(2000),
	e_ads_text NVARCHAR(255),
	e_ads_show INT,
	e_ads_click INT,
	f_ads_url NVARCHAR(2000),
	f_ads_text NVARCHAR(255),
	f_ads_show INT,
	f_ads_click INT,
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36),
	rec_modified_when VARCHAR(128),
	rec_status INT NOT NULL DEFAULT 1,
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id),
	FOREIGN KEY (experiment_id) REFERENCES experiment(experiment_id)
);

CREATE OR REPLACE VIEW experiment_vu AS (SELECT * FROM experiment);
CREATE OR REPLACE VIEW experiment_ads_run_vu AS (SELECT * FROM experiment_ads_run);

INSERT INTO experiment (
description,
rec_created_by,
rec_created_when,
rec_modified_by,
rec_modified_when,
rec_status
) VALUES (
'Ovulation test: cms test',
(SELECT user_id FROM users LIMIT 1),
NOW(),
null,
null,
1
);

INSERT INTO experiment_ads_run
(
	experiment_id,
	displayed_language,
	a_ads_url,
	a_ads_text,
	a_ads_show,
	a_ads_click,
	b_ads_url,
	b_ads_text,
	b_ads_show,
	b_ads_click,
	c_ads_url,
	c_ads_text,
	c_ads_show,
	c_ads_click,
	d_ads_url,
	d_ads_text,
	d_ads_show,
	d_ads_click,
	e_ads_url,
	e_ads_text,
	e_ads_show,
	e_ads_click,
	f_ads_url,
	f_ads_text,
	f_ads_show,
	f_ads_click,
	rec_created_by,
	rec_created_when,
	rec_modified_by,
	rec_modified_when,
	rec_status
)
VALUES
(
	(SELECT experiment_id FROM experiment LIMIT 1),
	'th',
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA1TDZ4ANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-lh-ovulation-test-strip-3-3050752.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'อุปกรณ์ตรวจไข่ตก',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA2OH4JANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-1-1-lh-ovulation-test-strip-preg-test-strip-4501171.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'อุปกรณ์ตรวจไข่ตก',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA1TDZ4ANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-lh-ovulation-test-strip-3-3050752.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'อุปกรณ์ตรวจตกไข่',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA2OH4JANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-1-1-lh-ovulation-test-strip-preg-test-strip-4501171.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'อุปกรณ์ตรวจตกไข่',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA1TDZ4ANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-lh-ovulation-test-strip-3-3050752.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'ชุดตรวจไข่ตก',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA2OH4JANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-1-1-lh-ovulation-test-strip-preg-test-strip-4501171.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'ชุดตรวจไข่ตก',
	0,
	0,
	(SELECT user_id FROM users LIMIT 1),
	'2016-04-22T20:29:47.578+07:00',
	null,
	null,
	1
);

-- -------------------------------------------
-- version 7.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 7,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

UPDATE		experiment_ads_run
SET				rec_status = 0
WHERE		rec_status = 1;

INSERT INTO experiment_ads_run
(
	experiment_id,
	displayed_language,
	a_ads_url,
	a_ads_text,
	a_ads_show,
	a_ads_click,
	b_ads_url,
	b_ads_text,
	b_ads_show,
	b_ads_click,
	c_ads_url,
	c_ads_text,
	c_ads_show,
	c_ads_click,
	d_ads_url,
	d_ads_text,
	d_ads_show,
	d_ads_click,
	e_ads_url,
	e_ads_text,
	e_ads_show,
	e_ads_click,
	f_ads_url,
	f_ads_text,
	f_ads_show,
	f_ads_click,
	rec_created_by,
	rec_created_when,
	rec_modified_by,
	rec_modified_when,
	rec_status
)
VALUES
(
	(SELECT experiment_id FROM experiment LIMIT 1),
	'th',
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA1TDZ4ANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-lh-ovulation-test-strip-3-3050752.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'ส่งฟรี อุปกรณ์ตรวจไข่ตก',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=PH250HBAA2CK7SANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fphecare-3945304.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=pregnancy_test_cheap',
	N'ส่งฟรี ชุดทดสอบการตั้งครรภ์แบบจุ่ม',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=KL511HBAPQRTANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fklick-as-3-734537.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=pregnancy_test_expensive',
	N'ส่งฟรี ชุดทดสอบการตั้งครรภ์แบบปากกา',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=OE857HBAA225NNANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fshecan-1-3459875.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=shecan',
	N'ส่งฟรี อุปกรณ์ยืนปัสสาวะสำหรับสตรี',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=SA564HBAA2OH4WANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fsanita-10-3-4501184.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=sanita',
	N'ส่งฟรี ผ้าอนามัยแบบห่วง สำหรับสตรีคลอดบุตร ',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=OE857HBBP2IXANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Ftravelmate-25-1916169.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=travelmate',
	N'ส่งฟรี แผ่นรองนั่งโถส้วม กันเชื้อโรค',
	0,
	0,
	(SELECT user_id FROM users LIMIT 1),
	'2016-04-22T20:29:47.578+07:00',
	null,
	null,
	1
);

-- -------------------------------------------
-- version 8.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 8,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

UPDATE		experiment_ads_run
SET				rec_status = 0
WHERE		rec_status = 1;

INSERT INTO experiment_ads_run
(
	experiment_id,
	displayed_language,
	a_ads_url,
	a_ads_text,
	a_ads_show,
	a_ads_click,
	b_ads_url,
	b_ads_text,
	b_ads_show,
	b_ads_click,
	c_ads_url,
	c_ads_text,
	c_ads_show,
	c_ads_click,
	d_ads_url,
	d_ads_text,
	d_ads_show,
	d_ads_click,
	e_ads_url,
	e_ads_text,
	e_ads_show,
	e_ads_click,
	f_ads_url,
	f_ads_text,
	f_ads_show,
	f_ads_click,
	rec_created_by,
	rec_created_when,
	rec_modified_by,
	rec_modified_when,
	rec_status
)
VALUES
(
	(SELECT experiment_id FROM experiment LIMIT 1),
	'th',
	'http://ho.lazada.co.th/SHGDGu?sku=IB365HBAA1TDZ4ANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fibabi-lh-ovulation-test-strip-3-3050752.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D',
	N'ส่งฟรี อุปกรณ์ตรวจไข่ตก',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=PH250HBAA2CK7SANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fphecare-3945304.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=pregnancy_test_cheap',
	N'ส่งฟรี ชุดทดสอบการตั้งครรภ์แบบจุ่ม',
	0,
	0,
	'http://ho.lazada.co.th/SHGDGu?sku=KL511HBAPQRTANTH&redirect=http%3A%2F%2Fho.lazada.co.th%2FSHGDGs%3Furl%3Dhttp%253A%252F%252Fwww.lazada.co.th%252Fklick-as-3-734537.html%253Foffer_id%253D%257Boffer_id%257D%2526affiliate_id%253D%257Baffiliate_id%257D%2526offer_name%253D%257Boffer_name%257D_%257Boffer_file_id%257D%2526affiliate_name%253D%257Baffiliate_name%257D%2526transaction_id%253D%257Btransaction_id%257D&aff_sub=pregnancy_test_expensive',
	N'ส่งฟรี ชุดทดสอบการตั้งครรภ์แบบปากกา',
	0,
	0,
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	(SELECT user_id FROM users LIMIT 1),
	'2016-04-22T20:29:47.578+07:00',
	null,
	null,
	1
);

-- -------------------------------------------
-- version 9.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 9,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

CREATE TABLE IF NOT EXISTS usage_duration (
	duration_id	BIGINT	AUTO_INCREMENT PRIMARY KEY,
	device_id 	VARCHAR(36) NOT NULL,
	data_date		VARCHAR(10) NOT NULL,
	data_hour		INT NOT NULL,
	duration		INT NOT NULL,
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36) DEFAULT NULL,
	rec_modified_when VARCHAR(128) DEFAULT NULL,
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (device_id) REFERENCES device_info(device_id),
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id)
);

ALTER TABLE usage_stat
ADD COLUMN duration INT NOT NULL AFTER setting_language_change_usage_counter;

CREATE OR REPLACE VIEW usage_duration_vu AS SELECT * FROM usage_duration;

CREATE OR REPLACE VIEW usage_stat_vu AS SELECT * FROM usage_stat;

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version 10.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 10,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE usage_duration
ADD COLUMN application_version VARCHAR(10) NOT NULL DEFAULT '38' AFTER data_hour;

CREATE OR REPLACE VIEW usage_duration_vu AS SELECT * FROM usage_duration;

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version 11.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 11,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

UPDATE		experiment_ads_run
SET				rec_status = 0
WHERE		rec_status = 1;

INSERT INTO experiment_ads_run
(
	experiment_id,
	displayed_language,
	a_ads_url,
	a_ads_text,
	a_ads_show,
	a_ads_click,
	b_ads_url,
	b_ads_text,
	b_ads_show,
	b_ads_click,
	c_ads_url,
	c_ads_text,
	c_ads_show,
	c_ads_click,
	d_ads_url,
	d_ads_text,
	d_ads_show,
	d_ads_click,
	e_ads_url,
	e_ads_text,
	e_ads_show,
	e_ads_click,
	f_ads_url,
	f_ads_text,
	f_ads_show,
	f_ads_click,
	rec_created_by,
	rec_created_when,
	rec_modified_by,
	rec_modified_when,
	rec_status
)
VALUES
(
	(SELECT experiment_id FROM experiment LIMIT 1),
	'th',
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	'',
	N'',
	0,
	0,
	(SELECT user_id FROM users LIMIT 1),
	'2016-06-02T20:29:47.578+07:00',
	null,
	null,
	1
);



SELECT * FROM experiment_ads_run_vu;


-- -------------------------------------------
-- version 12.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 12,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE device_info
ADD COLUMN application_version VARCHAR(10) NOT NULL DEFAULT '0' AFTER language;

CREATE OR REPLACE VIEW device_info_vu AS SELECT * FROM device_info;

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
 -- version 13.0
 -- -------------------------------------------

 UPDATE db_info
 SET db_version = 13,
   `rec_modified_by`= `rec_created_by`,
   `rec_modified_when` = CURRENT_TIMESTAMP();

 CREATE TABLE IF NOT EXISTS rsa_data(
     rsa_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
     rsa_uuid VARCHAR(36) NOT NULL,
     e VARCHAR(70) NOT NULL,
     d VARCHAR(70) NOT NULL,
     n VARCHAR(70) NOT NULL,
     rec_created_by VARCHAR(36) NOT NULL,
  	rec_created_when VARCHAR(128) NOT NULL,
 	rec_modified_by VARCHAR(36),
 	rec_modified_when VARCHAR(128),
 	rec_status INT NOT NULL,
 	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
 	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id)
 );

 CREATE OR REPLACE VIEW rsa_data_vu AS SELECT * FROM rsa_data;

 SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
 -- version 14.0
 -- -------------------------------------------

 UPDATE db_info
 SET db_version = 14,
   `rec_modified_by`= `rec_created_by`,
   `rec_modified_when` = CURRENT_TIMESTAMP();

 CREATE TABLE IF NOT EXISTS user_info(
     user_info_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
     user_id VARCHAR(36) NOT NULL,
     user_token VARCHAR(36) NOT NULL,
     user_email VARCHAR(70) NOT NULL,
     password VARCHAR(70) NOT NULL,
     rec_created_by VARCHAR(36) NOT NULL,
  	rec_created_when VARCHAR(128) NOT NULL,
 	rec_modified_by VARCHAR(36),
 	rec_modified_when VARCHAR(128),
 	rec_status INT NOT NULL,
 	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
 	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id),
 	FOREIGN KEY (user_id) REFERENCES users(user_id)
 );

 CREATE OR REPLACE VIEW user_info_vu AS SELECT * FROM user_info;

 SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version15.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 15,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE users
ADD COLUMN rec_created_by VARCHAR(36) NOT NULL,
ADD COLUMN 	rec_created_when VARCHAR(128) NOT NULL,
ADD COLUMN 	rec_modified_by VARCHAR(36),
ADD COLUMN 	rec_modified_when VARCHAR(128),
ADD COLUMN 	rec_status INT NOT NULL;

CREATE OR REPLACE VIEW users_vu AS (SELECT * FROM users);

SHOW ENGINE INNODB STATUS;

-- latest change
-- -------------------------------------------
-- version16.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 16,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = CURRENT_TIMESTAMP();

ALTER TABLE user_info
ADD CONSTRAINT UNIQUE(user_email);

SHOW ENGINE INNODB STATUS;

-- -------------------------------------------
-- version17.0
-- -------------------------------------------

UPDATE db_info
SET db_version = 17,
  `rec_modified_by`= `rec_created_by`,
  `rec_modified_when` = '2016-07-05T20:10:22.288+07:00';

INSERT INTO rsa_data VALUES
(
	0,
	'6338cd4e-431d-11e6-beb8-9e71128cae77',
	'nz',
	'ad8cemekse6eh1hrkp8j39kvb',
	'aq7k03xk4ouvzsrktw2lasctp',
	'a9998ce6-da2d-11e5-b5d2-0a1d41d68578',
	CURRENT_TIMESTAMP(),
	NULL,
	NULL,
	1
);

ALTER TABLE user_info
MODIFY password VARCHAR(450) NOT NULL;

CREATE OR REPLACE VIEW user_info_vu as (SELECT * FROM user_info);

SHOW ENGINE INNODB STATUS;


