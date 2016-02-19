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

CREATE TABLE IF NOT EXISTS db_info (

	info_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	db_version INT NOT NULL,
	app_version INT,
	rec_created_when DATETIME NOT NULL,
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id)
);

INSERT INTO db_info VALUES (1, 1.0, NULL, NOW(), 1);

CREATE TABLE IF NOT EXISTS device_info (

	device_id VARCHAR(36) NOT NULL PRIMARY KEY,
	rec_created_when DATETIME NOT NULL,
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id)
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
	rec_created_when DATETIME NOT NULL,
	rec_status INT NOT NULL DEFAULT 0,
	FOREIGN KEY (device_id) REFERENCES device_info (device_id),
	FOREIGN KEY (rec_status) REFERENCES rec_status_ref (rec_status_id)
);
