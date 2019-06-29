-- MySQL dump 10.13  Distrib 5.7.15, for Win64 (x86_64)
--
-- Host: localhost    Database: s2db
-- ------------------------------------------------------
-- Server version	5.7.15-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `mac` varchar(45) NOT NULL,
  `name` varchar(90) DEFAULT NULL,
  `options` varchar(1024) DEFAULT NULL,
  `comment` text,
  PRIMARY KEY (`seq`,`mac`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `time` bigint(20) NOT NULL,
  `type` char(1) NOT NULL,
  `subject` varchar(45) NOT NULL,
  `seq_session` bigint(20) DEFAULT NULL,
  `seq_page` bigint(20) DEFAULT NULL,
  `seq_trigger` bigint(20) DEFAULT NULL,
  `seq_log` bigint(20) DEFAULT NULL,
  `value` varchar(45) NOT NULL,
  `threshold` varchar(45) DEFAULT NULL,
  `data` mediumtext NOT NULL,
  PRIMARY KEY (`seq`),
  KEY `fk_event_session_idx` (`seq_session`),
  KEY `fk_event_page_idx` (`seq_page`),
  KEY `fk_event_trigger_idx` (`seq_trigger`),
  KEY `fk_event_log_idx` (`seq_log`),
  CONSTRAINT `fk_event_log` FOREIGN KEY (`seq_log`) REFERENCES `log` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_page` FOREIGN KEY (`seq_page`) REFERENCES `page` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_session` FOREIGN KEY (`seq_session`) REFERENCES `session` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_trigger` FOREIGN KEY (`seq_trigger`) REFERENCES `trigger` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=84747 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `seq_session` bigint(20) NOT NULL,
  `seq_device_target` bigint(20) DEFAULT NULL,
  `seq_trigger` bigint(20) DEFAULT NULL,
  `state` char(1) NOT NULL,
  `seq_part` bigint(20) DEFAULT NULL,
  `part_count` int(11) DEFAULT NULL,
  `request` varchar(2048) NOT NULL,
  `request_time` bigint(20) NOT NULL,
  `complete_time` bigint(20) DEFAULT NULL,
  `result` text,
  `data` json DEFAULT NULL,
  `step` int(11) DEFAULT NULL,
  PRIMARY KEY (`seq`),
  KEY `fk_job_session_idx` (`seq_session`),
  KEY `fk_job_device_target_idx` (`seq_device_target`),
  KEY `fk_job_trigger_idx` (`seq_trigger`),
  KEY `fk_job_job_idx` (`seq_part`),
  KEY `idx_step` (`step`),
  KEY `idx_state` (`state`),
  CONSTRAINT `fk_job_device_target` FOREIGN KEY (`seq_device_target`) REFERENCES `device` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_job` FOREIGN KEY (`seq_part`) REFERENCES `job` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_session` FOREIGN KEY (`seq_session`) REFERENCES `session` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_job_trigger` FOREIGN KEY (`seq_trigger`) REFERENCES `trigger` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=28234 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `seq_session` bigint(20) DEFAULT NULL,
  `time` bigint(20) NOT NULL,
  `text` varchar(255) DEFAULT NULL,
  `value` bigint(20) DEFAULT NULL,
  `source` varchar(80) DEFAULT NULL,
  `level` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`seq`),
  KEY `fk_log_session_idx` (`seq_session`),
  CONSTRAINT `fk_log_session` FOREIGN KEY (`seq_session`) REFERENCES `session` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=12380494 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `page`
--

DROP TABLE IF EXISTS `page`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `page` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `seq_path` bigint(20) NOT NULL,
  `seq_session` bigint(20) DEFAULT NULL,
  `last_modified` datetime DEFAULT NULL,
  `state` char(1) DEFAULT NULL,
  PRIMARY KEY (`seq`),
  KEY `fk_page_path_idx` (`seq_path`),
  KEY `fk_page_session_idx` (`seq_session`),
  CONSTRAINT `fk_page_path` FOREIGN KEY (`seq_path`) REFERENCES `path` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_page_session` FOREIGN KEY (`seq_session`) REFERENCES `session` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=966514 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `path`
--

DROP TABLE IF EXISTS `path`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `path` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`seq`)
) ENGINE=InnoDB AUTO_INCREMENT=2852 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prop`
--

DROP TABLE IF EXISTS `prop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prop` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `seq_page` bigint(20) NOT NULL,
  `name` varchar(80) NOT NULL,
  `value` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`seq`),
  KEY `fk_prop_page_idx` (`seq_page`),
  CONSTRAINT `fk_prop_page` FOREIGN KEY (`seq_page`) REFERENCES `page` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3709104 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `session` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `seq_device` bigint(20) NOT NULL,
  `start` datetime NOT NULL,
  `ip_address` varchar(15) NOT NULL,
  `class` varchar(80) NOT NULL,
  PRIMARY KEY (`seq`),
  KEY `fk_session_device_idx` (`seq_device`),
  CONSTRAINT `fk_session_device` FOREIGN KEY (`seq_device`) REFERENCES `device` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=31986 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trigger`
--

DROP TABLE IF EXISTS `trigger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trigger` (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `seq_session` bigint(20) NOT NULL,
  `seq_log` bigint(20) DEFAULT NULL,
  `time` bigint(20) NOT NULL,
  `type` char(1) NOT NULL COMMENT 'I - Hardware input port (digital or analog)\nO - Hardware output port (digital or analog)\nP - Page updated (may be imported content)\nT - Time event\nS - Shell script',
  `detail` varchar(1024) DEFAULT NULL,
  `match` varchar(1024) DEFAULT NULL COMMENT 'Regex to match or threshold to cross.',
  `url` varchar(2048) NOT NULL,
  PRIMARY KEY (`seq`),
  KEY `fj_trigger_session_idx` (`seq_session`),
  KEY `fk_trigger_log_idx` (`seq_log`),
  CONSTRAINT `fk_trigger_log` FOREIGN KEY (`seq_log`) REFERENCES `log` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_trigger_session` FOREIGN KEY (`seq_session`) REFERENCES `session` (`seq`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-06-28 22:05:14
