-- -------------------------------------------
-- version 1.0
-- -------------------------------------------

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

	usage_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
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
	rec_created_by VARCHAR(36) NOT NULL,
	rec_created_when VARCHAR(128) NOT NULL,
	rec_modified_by VARCHAR(36),
	rec_modified_when VARCHAR(128),
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (device_id) REFERENCES device_info (device_id),
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id),
	FOREIGN KEY (rec_created_by) REFERENCES users(user_id),
	FOREIGN KEY (rec_modified_by) REFERENCES users(user_id)
);

CREATE OR REPLACE VIEW usage_stat_vu AS SELECT * FROM usage_stat;

CREATE OR REPLACE VIEW device_info_vu AS SELECT * FROM device_info;

CREATE OR REPLACE VIEW users_vu AS SELECT * FROM users;

CREATE OR REPLACE VIEW db_info_vu AS SELECT * FROM db_info;

SHOW ENGINE INNODB STATUS;