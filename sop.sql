
CREATE DATABASE IF NOT EXISTS `sop` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
USE `sop`;




DROP TABLE IF EXISTS `user_info`;
DROP TABLE IF EXISTS `perm_role_permission`;
DROP TABLE IF EXISTS `perm_role`;
DROP TABLE IF EXISTS `perm_isv_role`;
DROP TABLE IF EXISTS `isv_keys`;
DROP TABLE IF EXISTS `isv_info`;
DROP TABLE IF EXISTS `config_service_route`;
DROP TABLE IF EXISTS `config_route_base`;
DROP TABLE IF EXISTS `config_limit`;
DROP TABLE IF EXISTS `config_ip_blacklist`;
DROP TABLE IF EXISTS `config_gray_instance`;
DROP TABLE IF EXISTS `config_gray`;
DROP TABLE IF EXISTS `config_common`;
DROP TABLE IF EXISTS `admin_user_info`;
DROP TABLE IF EXISTS `monitor_info`;
DROP TABLE IF EXISTS `monitor_info_error`;


CREATE TABLE `admin_user_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(128) NOT NULL DEFAULT '' COMMENT '密码',
  `status` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '状态，1：启用，2：禁用',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='后台用户表';


CREATE TABLE `config_common` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `config_group` varchar(64) NOT NULL DEFAULT '' COMMENT '配置分组',
  `config_key` varchar(64) NOT NULL DEFAULT '' COMMENT '配置key',
  `content` varchar(128) NOT NULL DEFAULT '' COMMENT '内容',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_groupkey` (`config_group`,`config_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='通用配置表';


CREATE TABLE `config_gray` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `service_id` varchar(64) NOT NULL DEFAULT '',
  `user_key_content` text COMMENT '用户key，多个用引文逗号隔开',
  `name_version_content` text COMMENT '需要灰度的接口，goods.get1.0=1.2，多个用英文逗号隔开',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_serviceid` (`service_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务灰度配置';


CREATE TABLE `config_gray_instance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `instance_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'instance_id',
  `service_id` varchar(64) NOT NULL DEFAULT '' COMMENT 'service_id',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0：禁用，1：启用',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instanceid` (`instance_id`) USING BTREE,
  KEY `idx_serviceid` (`service_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='开启灰度服务器实例';


CREATE TABLE `config_ip_blacklist` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `ip` varchar(64) NOT NULL DEFAULT '' COMMENT 'ip',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip` (`ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='IP黑名单';


CREATE TABLE `config_limit` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `route_id` varchar(128) DEFAULT NULL COMMENT '路由id',
  `app_key` varchar(128) DEFAULT NULL,
  `limit_ip` varchar(300) DEFAULT NULL COMMENT '限流ip，多个用英文逗号隔开',
  `service_id` varchar(64) NOT NULL DEFAULT '' COMMENT '服务id',
  `limit_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '限流策略，1：窗口策略，2：令牌桶策略',
  `exec_count_per_second` int(11) DEFAULT NULL COMMENT '每秒可处理请求数',
  `duration_seconds` int(11) NOT NULL DEFAULT '1' COMMENT '限流持续时间，默认1秒，即每durationSeconds秒允许多少请求（当limit_type=1时有效）',
  `limit_code` varchar(64) DEFAULT NULL COMMENT '返回的错误码',
  `limit_msg` varchar(100) DEFAULT NULL COMMENT '返回的错误信息',
  `token_bucket_count` int(11) DEFAULT NULL COMMENT '令牌桶容量',
  `limit_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '限流开启状态，1:开启，0关闭',
  `order_index` int(11) NOT NULL DEFAULT '0' COMMENT '顺序，值小的优先执行',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='限流配置';


CREATE TABLE `config_route_base` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `route_id` varchar(64) NOT NULL DEFAULT '' COMMENT '路由id',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态，1：启用，2：禁用',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_routeid` (`route_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='路由配置表';


CREATE TABLE `config_service_route` (
  `id` varchar(128) NOT NULL DEFAULT '' COMMENT '路由id',
  `service_id` varchar(128) NOT NULL DEFAULT '',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '接口名',
  `version` varchar(64) NOT NULL DEFAULT '' COMMENT '版本号',
  `predicates` varchar(256) DEFAULT NULL COMMENT '路由断言（SpringCloudGateway专用）',
  `filters` varchar(256) DEFAULT NULL COMMENT '路由过滤器（SpringCloudGateway专用）',
  `uri` varchar(128) NOT NULL DEFAULT '' COMMENT '路由规则转发的目标uri',
  `path` varchar(128) NOT NULL DEFAULT '' COMMENT 'uri后面跟的path',
  `order_index` int(11) NOT NULL DEFAULT '0' COMMENT '路由执行的顺序',
  `ignore_validate` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否忽略验证，业务参数验证除外',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态，0：待审核，1：启用，2：禁用',
  `merge_result` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否合并结果',
  `permission` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否需要授权才能访问',
  `need_token` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否需要token',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_serviceid` (`service_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='路由配置';


CREATE TABLE `isv_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_key` varchar(100) NOT NULL COMMENT 'appKey',
  `status` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '1启用，2禁用',
  `sign_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1:RSA2,2:MD5',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_key` (`app_key`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COMMENT='isv信息表';


CREATE TABLE `isv_keys` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_key` varchar(128) NOT NULL DEFAULT '',
  `sign_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1:RSA2,2:MD5',
  `secret` varchar(200) NOT NULL DEFAULT '' COMMENT 'sign_type=2时使用',
  `key_format` tinyint(4) NOT NULL DEFAULT '1' COMMENT '秘钥格式，1：PKCS8(JAVA适用)，2：PKCS1(非JAVA适用)',
  `public_key_isv` text NOT NULL COMMENT '开发者生成的公钥',
  `private_key_isv` text NOT NULL COMMENT '开发者生成的私钥（交给开发者）',
  `public_key_platform` text NOT NULL COMMENT '平台生成的公钥（交给开发者）',
  `private_key_platform` text NOT NULL COMMENT '平台生成的私钥',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appkey` (`app_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 COMMENT='ISV秘钥';


CREATE TABLE `perm_isv_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `isv_id` bigint(20) NOT NULL COMMENT 'isv_info表id',
  `role_code` varchar(64) NOT NULL COMMENT '角色code',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`isv_id`,`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8 COMMENT='isv角色';


CREATE TABLE `perm_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) NOT NULL COMMENT '角色代码',
  `description` varchar(64) NOT NULL COMMENT '角色描述',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='角色表';


CREATE TABLE `perm_role_permission` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) NOT NULL COMMENT '角色表code',
  `route_id` varchar(64) NOT NULL COMMENT 'api_id',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_code`,`route_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8 COMMENT='角色权限表';


CREATE TABLE `user_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(128) NOT NULL DEFAULT '' COMMENT '密码',
  `nickname` varchar(64) NOT NULL DEFAULT '' COMMENT '昵称',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_unamepwd` (`username`,`password`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用户信息表';

CREATE TABLE `monitor_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `route_id` varchar(128) NOT NULL DEFAULT '' COMMENT '路由id',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '接口名',
  `version` varchar(64) NOT NULL DEFAULT '' COMMENT '版本号',
  `service_id` varchar(64) NOT NULL DEFAULT '',
  `instance_id` varchar(128) NOT NULL DEFAULT '',
  `max_time` int(11) NOT NULL DEFAULT '0' COMMENT '请求耗时最长时间',
  `min_time` int(11) NOT NULL DEFAULT '0' COMMENT '请求耗时最小时间',
  `total_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '总时长，毫秒',
  `total_request_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '总调用次数',
  `success_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '成功次数',
  `error_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '失败次数（业务主动抛出的异常算作成功，如参数校验，未知的错误算失败）',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_routeid` (`route_id`,`instance_id`) USING BTREE,
  KEY `idex_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口监控信息';



CREATE TABLE `monitor_info_error` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `error_id` varchar(64) NOT NULL DEFAULT '' COMMENT '错误id,md5Hex(instanceId + routeId + errorMsg)',
  `instance_id` varchar(128) NOT NULL DEFAULT '' COMMENT '实例id',
  `route_id` varchar(128) NOT NULL DEFAULT '',
  `error_msg` text NOT NULL,
  `error_status` int(11) NOT NULL DEFAULT '0' COMMENT 'http status，非200错误',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '错误次数',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_errorid` (`error_id`) USING BTREE,
  KEY `idx_routeid` (`route_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `admin_user_info` (`id`, `username`, `password`, `status`, `gmt_create`, `gmt_modified`) VALUES
	(1,'admin','a62cd510fb9a8a557a27ef279569091f',1,'2019-04-02 19:55:26','2019-04-02 19:55:26');


INSERT INTO `config_route_base` (`id`, `route_id`, `status`, `gmt_create`, `gmt_modified`) VALUES
	(1,'story.get1.1',1,'2019-04-09 19:15:58','2019-04-09 19:16:54'),
	(2,'alipay.story.get1.0',1,'2019-04-09 19:19:57','2019-04-19 14:45:33'),
	(3,'alipay.story.find1.0',1,'2019-04-11 09:29:55','2019-04-11 09:35:02'),
	(4,'alipay.book.story.get1.0',1,'2019-04-16 10:23:44','2019-04-16 10:23:44'),
	(5,'spirngmvc.goods.get1.0',2,'2019-04-16 10:24:08','2019-04-16 10:24:08'),
	(6,'alipay.category.get1.0',1,'2019-05-06 16:50:39','2019-05-20 17:01:48'),
	(7,'permission.story.get1.0',1,'2019-05-06 20:03:17','2019-05-06 20:03:21'),
	(8,'goods.add1.0',1,'2019-05-13 17:23:00','2019-05-13 17:23:11');


INSERT INTO `isv_info` (`id`, `app_key`, `status`, `sign_type`, `remark`, `gmt_create`, `gmt_modified`) VALUES
	(1,'2019032617262200001',1,1,NULL,'2019-03-27 10:10:34','2019-05-09 11:10:38'),
	(3,'asdfasdf',2,1,NULL,'2019-03-27 11:01:11','2019-05-11 10:45:01'),
	(5,'20190331562013861008375808',1,2,NULL,'2019-03-31 20:34:12','2019-04-19 17:30:09'),
	(6,'20190331562037310372184064',2,2,NULL,'2019-03-31 22:07:20','2019-05-07 18:14:53'),
	(7,'20190401562373672858288128',1,1,NULL,'2019-04-01 20:24:01','2019-04-01 20:27:33'),
	(8,'20190401562373796095328256',1,1,NULL,'2019-04-01 20:24:25','2019-04-01 20:24:48'),
	(9,'201904035630907729292csharp',1,1,NULL,'2019-04-03 19:54:05','2019-04-03 20:11:58'),
	(10,'easyopen_test',1,2,NULL,'2019-04-19 17:19:34','2019-04-19 17:30:09'),
	(11,'20190513577548661718777856',1,1,NULL,'2019-05-13 17:24:17','2019-05-13 17:24:17'),
	(12,'20190513577548661718777857',1,1,NULL,'2019-05-13 17:24:17','2019-05-13 17:24:17');


INSERT INTO `isv_keys` (`id`, `app_key`, `sign_type`, `secret`, `key_format`, `public_key_isv`, `private_key_isv`, `public_key_platform`, `private_key_platform`, `gmt_create`, `gmt_modified`) VALUES
	(1,'2019032617262200001',1,'',1,'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','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(2,'asdfasdf',1,'',1,'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDOJTAsGoAsNGJdzaWm835mtpcY2YxGr4NPjnhQmfUrPdT+Zgi6jmm+olYuygNeB8cOSALmTzgXlef+6SdwfRJVEMYVAO7hqF0Ood9zTOc+kolgnWJtqX54CoodfABt0SNS/bsr6hCAWu17RGnbgBaK+ZaJD3NVqXVXE8E30cYHiQIDAQAB','MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAM4lMCwagCw0Yl3Npabzfma2lxjZjEavg0+OeFCZ9Ss91P5mCLqOab6iVi7KA14Hxw5IAuZPOBeV5/7pJ3B9ElUQxhUA7uGoXQ6h33NM5z6SiWCdYm2pfngKih18AG3RI1L9uyvqEIBa7XtEaduAFor5lokPc1WpdVcTwTfRxgeJAgMBAAECgYAM3XFGL1k0aQiChiUCaEvJKTgAywLgHm/5dRC5JwKP8knqnn+I9P5QcV0jimPvaFjZ4VCdAvCjOC3EUNSvRn7wR2Lb1+BGZZePTdxtHWE2aqJ1W1SvgQTqMsLlPBRPnXo5XH/ng3WEH15ynd5NR035xAluaI0X/y+PsRxE6TlfIQJBAPSYUyXa2yaEqmvIN+ECKALCLLeDdi2YW3Kjahgz0X9V4Y4aTdrHh8y603zXC0Wy8HeOhwGoyciaS8SmjxCMn4UCQQDXweW8xsUreLH8hfVUtyiY/KgUz+R5foJDNXD7TLE9CDoPSHy09qBe99HyVCZg/gNJH4O+tNr6C4916dYaVk01AkBYZ2HOEc8ZmeOaty/zJHtfm9zbqykgi6upwISNINV8Z4bxfHJdO7bKeVANFBBf7a/aFmqXX/EmjxYJioW03o6dAkEAp7ViXJCtJpNU1pNSFZ2hgvmxtSu7zuyVWKSrw8rjYiuI5eRUe13RXsCHgzQB+Ra5exdyEsUGCaL+yosPD73RmQJBALGuM8EQUcBgrpgpeLZ39Ni1DYXYG9aj+u+ar/UL6kI1mCNFgwroO4EVIvXPVxikMxUgiE2tVaBML5nm8VDNJ7s=','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(3,'20190331562013861008375808',2,'29864b93427447f5ac6c44df746f84ef',1,'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4aMIx2q8rCVu5z6dgNQQSX2vwhvIcRb7FaqSk0ZK8AV9qQeE1TvfFVlAzOHlysE1yTRb0Mb6W2aw7IAS7Bkc3onYBQR4zNQYjYoDBzLukjF8o84hoVFRnh7sV8zszid2vb5H/YQr3M+5sYhlXY8KfILk3vhdbWpHM/umplcrxlwIDAQAB','MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALhowjHarysJW7nPp2A1BBJfa/CG8hxFvsVqpKTRkrwBX2pB4TVO98VWUDM4eXKwTXJNFvQxvpbZrDsgBLsGRzeidgFBHjM1BiNigMHMu6SMXyjziGhUVGeHuxXzOzOJ3a9vkf9hCvcz7mxiGVdjwp8guTe+F1takcz+6amVyvGXAgMBAAECgYBj40LFVGoryp7n0CYeg7kX5p4GJGKCk/jY4IIcUPTFZ4zydorxoDuvpag9hmqqh/r7XeyAC23sMi4LvLUzRRxPh+7PuwL6nLce7vytsMCZQTPpBgz7dUfbi2HAxsuMOLjH3sVGycutARJsz6bT+9PyBEuVtUqwBrDGpFvwT0z6yQJBAOANC8nysb+O4rn/fbJtHIhtQoV74yu00mLnfwv8/J1+WyAEc32WZ4KYINqCe8ft1UknhPQx9UV6JaPCnlpF6w0CQQDStJhd38uQ7dVUQZHGP24xS/K38AYiSheEr7uewhkJfC2cKqE/lBk3oEG4s7asjhwlFLWLWSBLVM/Ta9Yj0hYzAkBe82hxl1bY9bcEWFBu02rqLlOouk4V8bXPkIf5DqgIHsqDkR9Ys+r+H3ac4/uNSS/ApuzjiGCHpzJYalwtqb/pAkEAvAKlSm5dCC8QAaSYXJtQyfAI3hPwhTwzjBP6iAiNqqcBU62+QCr37Wiz/Alv4LzVZEj8TSDz7gP5hZ9dbo0RfQJBALJ7NhTaeMN4jxBJ6Xg4rNZPb4yhAXuFxCp+a+FyXTsbWnW/ar8KJ3LHox0GOao6wne4qN3h7eqLOrYnnvOSFl0=','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(4,'20190331562037310372184064',2,'d6b2a6603236491f87eed958292be136',1,'','','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(5,'20190401562373672858288128',1,'',1,'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOgSo6GfwM8a+rUT4m61nZa7q5cJpjL7luxQKuJvkMl/QydVrzRPinCh6zqgBJDa7YY2X6z5TyTkjYuDkYMrdyHcYvCDuJ0NpRPZMR+QDE70yp9wJ+kOqTa8bepJtxzuCYLxFz+wRsIFt0nIxC6ttCaM9OEw/Bflhws3xczw4uqQIDAQAB','MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAI6BKjoZ/Azxr6tRPibrWdlrurlwmmMvuW7FAq4m+QyX9DJ1WvNE+KcKHrOqAEkNrthjZfrPlPJOSNi4ORgyt3Idxi8IO4nQ2lE9kxH5AMTvTKn3An6Q6pNrxt6km3HO4JgvEXP7BGwgW3ScjELq20Joz04TD8F+WHCzfFzPDi6pAgMBAAECgYA/zuQ6ieILZbjUDBe5U46yxQMh/6KRoQ/14m81zauckPm+EkA8R3jTSru+lPN1wpO0vqUuLf6ylI0XxT2DcUokOgY07ZdE54pu5XAsyY0eanFwt6C1LrHYpORV3Mp9XuI3fXrxYqVlxLuj1N7MGinXUuW7aZCHaEuSnZ55OL9dkQJBANKOeFiYDISSuIFHSrndSgr+a8E44jS/2/7lE49p5l3WVOFTHh0IZQNTs/IKsKJkUnYDE4W/Ab7NTnWZpXYeM30CQQCtQrtxPEzLl4dtupPOBJJoApj2lq7Q6tIGx178K6wS9Rz3GEvkA7fz1Tpm+nmPZflWZ9mVmEaVuMTMpl3HN/edAkBylyzx8lYltIALg5QskT1hvFNChkW9tYjyMROzIkxIV4Q8WPLzlAT9iYlOOfkld/nU1hnC2VAG2k9P+z2sigU1AkBAl1AptsEqZSMn1RalBy9NdypvQ12IpQIHZOwUNnO/3YEe3P/t0TUSwbs0CMyomOuLOsvy6QHnbypu4Na1HjhBAkAWjtdhuvU15HAa5jMgiUVfQM3YFuz2k3QkRagtZZ33bqnYs4wNxEZqB5t+vEj+8r3fmSN0BpNR1VW71j53Ir0H','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(6,'20190401562373796095328256',1,'',1,'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbxn0En2cj29t8i+m1dT7seF9XQx3cEaoClXFTlmc78Y1ZuQFbyY99VSwv8JbtpGxPvBSVD9SPmXWNBRBbSHT+gxStcdXD9QeD3knGFy/1V+z3E2Rj/6LIlFmgTlGh1fmEgttPjsUW1Dj704/ydyIrw+CAyAMKQyrFg0wYmNW+UQIDAQAB','MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJvGfQSfZyPb23yL6bV1Pux4X1dDHdwRqgKVcVOWZzvxjVm5AVvJj31VLC/wlu2kbE+8FJUP1I+ZdY0FEFtIdP6DFK1x1cP1B4PeScYXL/VX7PcTZGP/osiUWaBOUaHV+YSC20+OxRbUOPvTj/J3IivD4IDIAwpDKsWDTBiY1b5RAgMBAAECgYAE27/ycPZKjATgcYyseCeqQGbY1eMMhhCDXB3YuYwmtnXuInMEZdjv08Q5CovqhYJLSlZp/8BlaifcahgEgNIFQXmxAF0U0HsNC6W4Dk1gGgQaVmYaZv5ex7uIcFB1qFvlO60kWf82YeRnO5KsFBODOJ1XSNwqjL2GeLSHBSVyQQJBAOsvDmClBsETSdiNSFMz+D9WCnCh1Ip4AoCzA/yG+PRSwYjZDdceP2DXieiZXPlxTFZ7MIXxAafgeyeQA2hpkQUCQQCpkCUSbrZ+nd4BYdnxZOSf0//cUT0o6+3kROX7gsXV7zRAWWxojT6DkGVlduDLZM/hjWeHRjWUxKC/jgbzvundAkBckhUSrWJPNQxoFJRXS6l3JKLPWqOSLVKu3ce/6lCrurc66lSsS9eegrhhuZwDAzmNAMhEsGx6a72OAP2WZ5cRAkBd8cT4X2qw4BpePa6YdcPNYZHCqSfvgje9XwbkwGGH1A3pESJlEsxt7BShkKmfRu1+E/AmHJoXIJHHT5M+fKnpAkA+VfyAAviKeCwUSq+5oUa0B+ozEA3frp/40cKQP7k02aamocAQCDRaC1ZlWffeQqYMnYe1/Mjr/SdX/Ut3X0CC','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(7,'201904035630907729292csharp',1,'',1,'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5+OvJxeSzf44NxQ/cl7Ii+BzPg2k6sRcvH4ffOtU5Dzq1/oEvg02nxIhmwOHBZmjbmuUu0aLsfglUTAwqfXftfAKZidshsgj9NNh0/kxk0avRZ1UoljWGz/FxVZA0ogbxxhohPZ9jWcD+eBQcIwF2DtHfAJqWWZrYFnCMeHD8mPzxo2kwXSvDzi0vf9I2tKiYvNG26a9FqeYtPOoi81sdS3+70HOMdxP8ejXtyfnKpKz7Dx506LCIRS5moWS3Q5eTLV3NGX/1CSJ8wpQA2DAQTjVhX5eVu7Yqz12t8W+sjWM/tHUR6cgwYYR10p7tSCeCPzkigjGxKm4cYXWtATQJQIDAQAB','MIIEowIBAAKCAQEA5+OvJxeSzf44NxQ/cl7Ii+BzPg2k6sRcvH4ffOtU5Dzq1/oEvg02nxIhmwOHBZmjbmuUu0aLsfglUTAwqfXftfAKZidshsgj9NNh0/kxk0avRZ1UoljWGz/FxVZA0ogbxxhohPZ9jWcD+eBQcIwF2DtHfAJqWWZrYFnCMeHD8mPzxo2kwXSvDzi0vf9I2tKiYvNG26a9FqeYtPOoi81sdS3+70HOMdxP8ejXtyfnKpKz7Dx506LCIRS5moWS3Q5eTLV3NGX/1CSJ8wpQA2DAQTjVhX5eVu7Yqz12t8W+sjWM/tHUR6cgwYYR10p7tSCeCPzkigjGxKm4cYXWtATQJQIDAQABAoIBAHFDsgrrJca+NKEan77ycwx3jnKx4WrWjOF4zVKL9AQjiSYDNgvKknJyPb3kpC/lEoHdxGERHSzJoxib7DkoIqRQYhPxj73pxj5QfYk3P7LLJNNg/LTrpXDb3nL8JV9wIflGf87qQvstZTDJEyFWE4jBs7Hr0BxovWvri8InnzkmERJ1cbGJgNHe1Y3Zo2tw0yaHxQCxLuajP+notRZhD9bEp7uKeI0w9AvlW6k8m/7y10F0BK/TlyW8rQiEC391yOiRYoMcUh4hd2Q9bMx3jngZgX8PXIvZZcup4/pvWlv1alwhB2tsnLdazP62r1MO80vLyLunzGO+7WwCjEYlVaECgYEA+lQRFmbhKaPuAuXMtY31Fbga8nedka5TjnEV7+/kX+yowE2OlNujF+ZG8UTddTxAGv56yVNi/mjRlgD74j8z0eOsgvOq9mwbCrgLhLo51H9O/wAxtb+hBKtC5l50pBr4gER6d8W6EQNTSGojnMIaLXTkAZ5Qf6Z8e2HFVdOn0X0CgYEA7SSrTokwzukt5KldNu5ukyyd+C3D1i6orbg6qD73EP9CfNMfGSBn7dDv9wMSJH01+Ty+RgTROgtjGRDbMJWnfbdt/61NePr9ar5sb6Nbsf7/I0w7cZF5dsaFYgzaOfQYquzXPbLQHkpMT64bqpv/Mwy4F2lFvaYWY5fA4pC2uckCgYEAg75Ym9ybJaoTqky8ttQ2Jy8UZ4VSVQhVC0My02sCWwWXLlXi8y7An+Rec73Ve0yxREOn5WrQT6pkmzh7V/ABWrYi5WxODpCIjtSbo0fLBa3Wqle00b0/hdCITetqIa/cFs1zUrOqICgK3bKWeXqiAkhhcwSZwwSgwOKM04Wn7ZUCgYBvhHX2mbdVJfyJ8kc+hMOE/E9RHRxiBVEXWHJlGi8PVCqNDq8qHr4g7Mdbzprig+s0yKblwHAvrpkseWvKHiZEjVTyDipHgShY4TGXEigVvUd37uppTrLi8xpYcJjS9gH/px7VCdiq1d+q/MJP6coJ1KphgATm2UrgDMYNBWaYWQKBgEHRxrmER7btUF60/YgcqPHFc8RpYQB2ZZE0kyKGDqk2Data1XYUY6vsPAU28yRLAaWr/D2H17iyLkxP80VLm6QhifxCadv90Q/Wl1DFfOJQMW6avyQ0so6G0wFq/LJxaFK4iLXQn1RJnmTp6BYiJMmK2BhFbRzw8ssMoF6ad2rr','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(8,'easyopen_test',2,'G9w0BAQEFAAOCAQ8AMIIBCgKCA',1,'','','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(9,'20190513577548661718777856',1,'',1,'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdGjz3IE2VQteDjQaFacXLCAIqdEIIVcf7LW7f142V55Q0xtcjDTOHjBHExZsG64/Y5WKz4oQVWGnXwtrled8Qg0YAA7ueat8mE8NzJSm9txbfU9hwXB77nJxVkFyaSG1p0IZFrNQpbbUxTX9755deP7DdcSF148LLr091V++S3QIDAQAB','MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJ0aPPcgTZVC14ONBoVpxcsIAip0QghVx/stbt/XjZXnlDTG1yMNM4eMEcTFmwbrj9jlYrPihBVYadfC2uV53xCDRgADu55q3yYTw3MlKb23Ft9T2HBcHvucnFWQXJpIbWnQhkWs1ClttTFNf3vnl14/sN1xIXXjwsuvT3VX75LdAgMBAAECgYB68z/nQDa3q/oykDocS21qujfHtfi/wTKjVylAsdezC+wnab6RRhGf8XUuhGARiGWpn8whcBNjCTC8lVju4vQ5IIx4Hb74vwDDMtNXeqwkLmARLYu2ELibauezSeqom8/J8cR3ho7Hr4VHPTiC8qvePRmu8AvXVQz2T7SOhEjDGQJBAOm8XOivr+atiknLbQhmo508ON3sjoN9VMwK9cmnup+ZPCsurJTHRja0MJQNdOXObUVJ6wJhs1PHWT+vITfXGJ8CQQCsESzxOYTkZaqBUFjbWVf1rSwjOOsylweTuq44YIJkHhwMjHf3kN/UTXbxsBPUGeT7/+2K5UwQ9snUPr0yTBcDAkA0FMezBWqxgNu+g7iA1bYBVCjrskkzHVsmuA56Z4hbBZ71lEnaQOjxSYdFhhYVGsEYXlciSbjWoyXM3e4N7jzLAkB0ejv+H33CTsAZQZalBdnxSQTz4vf0CyDp9BkzuMELnQZHyF79i2i5gqbd/N+vWMgVfq4CtC3F3gnKT54rii6ZAkAMBIvHriT5Zbs1fW+oxBP1rHqdsRvqs1zEyIadvJgKAFwFEisryfdw2mWm3vxQQ22RlOquBiZEDIlyM0z2m9PJ','','','2019-08-13 15:18:39','2019-08-13 15:18:39'),
	(10,'20190513577548661718777857',1,'',1,'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdGjz3IE2VQteDjQaFacXLCAIqdEIIVcf7LW7f142V55Q0xtcjDTOHjBHExZsG64/Y5WKz4oQVWGnXwtrled8Qg0YAA7ueat8mE8NzJSm9txbfU9hwXB77nJxVkFyaSG1p0IZFrNQpbbUxTX9755deP7DdcSF148LLr091V++S3QIDAQAB','MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJ0aPPcgTZVC14ONBoVpxcsIAip0QghVx/stbt/XjZXnlDTG1yMNM4eMEcTFmwbrj9jlYrPihBVYadfC2uV53xCDRgADu55q3yYTw3MlKb23Ft9T2HBcHvucnFWQXJpIbWnQhkWs1ClttTFNf3vnl14/sN1xIXXjwsuvT3VX75LdAgMBAAECgYB68z/nQDa3q/oykDocS21qujfHtfi/wTKjVylAsdezC+wnab6RRhGf8XUuhGARiGWpn8whcBNjCTC8lVju4vQ5IIx4Hb74vwDDMtNXeqwkLmARLYu2ELibauezSeqom8/J8cR3ho7Hr4VHPTiC8qvePRmu8AvXVQz2T7SOhEjDGQJBAOm8XOivr+atiknLbQhmo508ON3sjoN9VMwK9cmnup+ZPCsurJTHRja0MJQNdOXObUVJ6wJhs1PHWT+vITfXGJ8CQQCsESzxOYTkZaqBUFjbWVf1rSwjOOsylweTuq44YIJkHhwMjHf3kN/UTXbxsBPUGeT7/+2K5UwQ9snUPr0yTBcDAkA0FMezBWqxgNu+g7iA1bYBVCjrskkzHVsmuA56Z4hbBZ71lEnaQOjxSYdFhhYVGsEYXlciSbjWoyXM3e4N7jzLAkB0ejv+H33CTsAZQZalBdnxSQTz4vf0CyDp9BkzuMELnQZHyF79i2i5gqbd/N+vWMgVfq4CtC3F3gnKT54rii6ZAkAMBIvHriT5Zbs1fW+oxBP1rHqdsRvqs1zEyIadvJgKAFwFEisryfdw2mWm3vxQQ22RlOquBiZEDIlyM0z2m9PJ','','','2019-08-13 15:18:39','2019-08-13 15:18:39');


INSERT INTO `perm_isv_role` (`id`, `isv_id`, `role_code`, `gmt_create`, `gmt_modified`) VALUES
	(18,5,'normal','2019-03-31 22:07:50','2019-03-31 22:07:50'),
	(32,7,'normal','2019-04-01 20:27:33','2019-04-01 20:27:33'),
	(36,9,'normal','2019-04-03 20:11:58','2019-04-03 20:11:58'),
	(37,10,'normal','2019-04-19 17:19:34','2019-04-19 17:19:34'),
	(38,10,'vip','2019-04-19 17:19:34','2019-04-19 17:19:34'),
	(52,6,'normal','2019-05-07 18:14:53','2019-05-07 18:14:53'),
	(57,1,'normal','2019-05-09 11:10:38','2019-05-09 11:10:38'),
	(58,11,'normal','2019-05-13 17:24:17','2019-05-13 17:24:17');


INSERT INTO `perm_role` (`id`, `role_code`, `description`, `gmt_create`, `gmt_modified`) VALUES
	(1,'normal','普通权限','2019-03-29 15:00:10','2019-03-29 15:00:10'),
	(2,'vip','VIP权限','2019-03-29 15:00:27','2019-03-29 15:00:27');


INSERT INTO `perm_role_permission` (`id`, `role_code`, `route_id`, `gmt_create`, `gmt_modified`) VALUES
	(29,'normal','permission.story.get1.0','2019-05-06 18:29:16','2019-05-06 18:29:16');


INSERT INTO `user_info` (`id`, `username`, `password`, `nickname`, `gmt_create`, `gmt_modified`) VALUES
	(1,'zhangsan','123456','张三','2019-04-27 08:32:57','2019-04-27 08:32:57');

