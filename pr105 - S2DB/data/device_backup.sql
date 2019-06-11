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
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
INSERT INTO `device` VALUES (1,'08-00-27-D8-73-57','VM101 - Development on Windows VM','tiles.perspective=desktop',NULL),(2,'B8-27-EB-94-1E-94','RPi - Prototype device','tiles.perspective=test',NULL),(3,'B8-27-EB-6A-F7-87','RPi - Master Bathroom','tiles.perspective=daily_rotate',NULL),(4,'B8-27-EB-4D-14-E2','RPi - Kitchen Window Sill','tiles.perspective=camera',NULL),(5,'B8-27-EB-90-07-BD','RPi - Camera: Front (portable, wired)','tiles.perspective=camera','Bad video? (RPi S2 Client crashes after hours)'),(6,'B8-27-EB-A4-EA-4C','RPi - [do not use]','\ntiles.perspective=REMOTE\nremote=REMOTE_TEST\n','Bad eth0 (using eth1).'),(7,'B8-27-EB-1F-F3-56','RPi - Home MediaWiki',NULL,NULL),(8,'B8-27-EB-E6-6A-EC','L32--B8-27-EB-E6-6A-EC--0098E--160C492C844',NULL,NULL),(9,'B8-27-EB-5E-91-EB','RPi - Media Server','tiles.perspective=remote\nremote=media',NULL),(10,'08-00-27-D7-DE-79','VM106 - Development on Windows VM','tiles.perspective=top_page',NULL),(11,'B8-27-EB-5B-1E-3B','RPi - Experimental GPIO/Relay','tiles.perspective=gpio\ngpio=development','No wlan0, eth0 sometimes drops out. Graphics problems.'),(12,'B8-27-EB-A4-53-04','L32--B8-27-EB-A4-53-04--003C0--161BB7658CE',NULL,NULL),(13,'B8-27-EB-65-2D-01','RPi - Media Closet Sensors','tiles.perspective=auto_hat\n   IN_A_1=SUMP_WATER_LEVEL:60,-2.79,8.91,0.06,inches\n   IN_D_1=L_POWER_THEATER_RECEIVER\n   IN_D_2=L_POWER_THEATER_PROJECTOR\n   IN_D_3=L_POWER_HOT_WATER\n   OUT_D_1=\n   OUT_R_1=AUDIO_BROADCAST_POWER_ENCODER\n   remote=MEDIA_CLOSET\n\n','Layered lexan enclosure (red)'),(14,'08-00-27-50-AA-7A','VM105 - Linux (Ubuntu) workstation',NULL,NULL),(15,'B8-27-EB-13-8B-C0','RPi - Garage Entrance','tiles.perspective=tesla',NULL),(16,'B8-27-EB-33-89-A6','RPi - IO-001 - Garage IO','tiles.perspective=auto_hat\nIN_D_1=GARAGE_PED_DOOR_CLOSED_STOP\nIN_D_2=L_POWER_LAUNDY_DRYER\nIN_D_3=L_POWER_LAUNDY_WASHER\nOUT_R_1=GARAGE_PARK_ASSIST_1\nIN_A_2=POWER_HPWC\nIN_A_2.input=POWER_HPWC\nIN_A_2.threshold=10.0\n\n',NULL),(17,'B8-27-EB-09-D3-9E','RPi - Camera (portable, wireless)','tiles.perspective=camera',NULL),(18,'00-E0-4C-36-08-FD','L32--00-E0-4C-36-08-FD--002A7--1625F85CA56',NULL,NULL),(19,'B8-27-EB-4E-46-E2','RPi - IO-002 - Garage IO','  tiles.perspective=auto_hat\n    IN_D_1=GARAGE_PED_DOOR_CLOSED_STOP\n    IN_D_2=L_POWER_WELL_PUMP\n    IN_D_3=L_POWER_LAUNDY_DRYER\n    OUT_R_1=GARAGE_PARK_ASSIST_1\n    IN_A_1=POWER_AUX2C_OUTLET:20,0.0,1.0,3.0,volts\n    IN_A_2=POWER_HPWC\n    IN_A_2.input=POWER_HPWC\n    IN_A_2.threshold=10.0\n',NULL),(20,'E0-B9-4D-B0-80-65','L32--E0-B9-4D-B0-80-65--00546--1634D558B09',NULL,NULL),(21,'E0-B9-4D-B0-80-65','L32--E0-B9-4D-B0-80-65--006F6--1634D558F04',NULL,NULL),(22,'B8-27-EB-18-41-B7','L32--B8-27-EB-18-41-B7--0027C--1635377C532',NULL,NULL),(23,'B8-27-EB-C5-52-E8','L32--B8-27-EB-C5-52-E8--00399--163561FFB9A',NULL,NULL),(24,'B8-27-EB-5C-86-CB','L32--B8-27-EB-5C-86-CB--00976--1635764FCE6',NULL,NULL),(25,'B8-27-EB-2F-71-02','RPi - Cameras: Deck, Shop',NULL,NULL),(26,'B8-27-EB-7A-24-57','L32--B8-27-EB-7A-24-57--0042D--164256ED8EE',NULL,NULL),(27,'B8-27-EB-42-40-8B','RPi - Cameras: Garage Door',NULL,NULL),(28,'B8-27-EB-17-15-DE','L32--B8-27-EB-17-15-DE--0033A--164263E2A11',NULL,NULL),(29,'B8-27-EB-E7-71-43','L32--B8-27-EB-E7-71-43--005AC--164C08E39FE',NULL,NULL),(30,'B8-27-EB-B2-24-16','L32--B8-27-EB-B2-24-16--003D3--164C4D96BA0',NULL,NULL),(31,'B8-27-EB-46-DE-95','L32--B8-27-EB-46-DE-95--0029A--165227302B7',NULL,NULL),(32,'ZZ-KD-TJ-FV-WT-LI','L32--ZZ-IM-IW-WS-WG-QO--003B9--16523D286A2',NULL,NULL),(33,'00-00-00-00-00-00-00-E0','NUC - S111 - (wrong NIC)',NULL,NULL),(34,'B8-27-EB-4E-93-AF','RPi - IC2 - Image Processor 02','tiles.perspective=PROC_IMAGE\n  proc_image.1.name=Test RPi\n  proc_image.1.file=/Share/Sessions/B8-27-EB-70-3C-8D/capture_cam.jpg\n  proc_image.1.threshold=1.000\n  proc_image.1.expected.objects=\"Window\"\n  \n  proc_image.2.name=Deck, from Kitchen\n  proc_image.2.file=/Share/Sessions/B8-27-EB-09-D3-9E/capture_cam.jpg\n  proc_image.2.threshold=1.500\n  proc_image.2.expected.objects=\"Chair\"','Immersive Cooling Test, BEC Test'),(35,'94-C6-91-18-C8-33','NUC - S112 - Windows Remote',NULL,NULL),(36,'94-C6-91-19-C5-CC','NUC - S111 - Windows Development',NULL,NULL),(37,'B8-27-EB-EC-58-92','RPi - Garage Sensors','tiles.perspective=auto_hat\n    IN_A_1=VEH_SPACE_1_RANGE_DOWN:50,-1.66,109,0.10,inches\n    IN_D_1=GARAGE_VEH_DOOR_1_CLOSED_STOP\n    IN_D_2=GARAGE_VEH_DOOR_1_CLOSED_AWAY\n    IN_D_3=GARAGE_VEH_DOOR_1_OPEN_STOP\n    OUT_D_1=GARAGE_PARK_ASSIST_1\n    OUT_D_2=GARAGE_FAST_LIGHTS\n    OUT_D_3=\n    OUT_R_1=\n    remote=GARAGE_LIGHTS',NULL),(38,'B8-27-EB-90-A3-BD','RPi - Image processor 1 (alternate)','','Bad eth0'),(39,'B8-27-EB-70-3C-8D','RPi','','3rd party system camera'),(40,'00-0F-00-5B-B2-D8','L32--00-0F-00-5B-B2-D8--03180--16AD4AF314D',NULL,NULL),(41,'B8-27-EB-25-69-D8','L32--B8-27-EB-25-69-D8--018F5--16AD8550A86',NULL,NULL),(42,'B8-27-EB-B3-3F-B9','L32--B8-27-EB-B3-3F-B9--005FE--16AD8F01FC7',NULL,NULL),(43,'B8-27-EB-B3-3F-B9','L32--B8-27-EB-B3-3F-B9--005FF--16AD8F01FC7',NULL,NULL),(44,'B8-27-EB-B3-3F-B9','L32--B8-27-EB-B3-3F-B9--00600--16AD8F01FD4',NULL,NULL),(45,'B8-27-EB-C5-F6-E8','L32--B8-27-EB-C5-F6-E8--00BE6--16AD8F9D85A',NULL,NULL),(46,'B8-27-EB-1E-F3-31','RPi - IC1 - Image Processing 01','tiles.perspective=PROC_IMAGE\n     proc_image.1.name=Garage, Inside\n     proc_image.1.threshold=50.0\n     proc_image.1.file=/Share/Sessions/94-C6-91-18-C8-33/capture_vid1.jpg\n     proc_image.1.expected.objects=\"\"\n     \n     proc_image.2.name=Driveway, from Deck\n     proc_image.2.threshold=0.001\n     proc_image.2.file=/Share/Sessions/94-C6-91-18-C8-33/capture_vid2.jpg\n     proc_image.2.expected.objects=\"\"\n     \n     proc_image.3.name=Front Yard\n     proc_image.3.threshold=0.600\n     proc_image.3.file=/Share/Sessions/B8-27-EB-90-07-BD/capture_cam.jpg\n     proc_image.3.expected.objects=\"\"\n','Immersive Cooling Test'),(47,'B8-27-EB-84-4F-5A','L32--B8-27-EB-84-4F-5A--003ED--16AD20E241C',NULL,NULL);
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-06-10 21:59:40
