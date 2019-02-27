-- MySQL dump 10.13  Distrib 8.0.14, for Win64 (x86_64)
--
-- Host: localhost    Database: galaxy
-- ------------------------------------------------------
-- Server version	8.0.14

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_sky_object`
--

DROP TABLE IF EXISTS `t_sky_object`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_sky_object` (
  `id` varchar(50) NOT NULL,
  `object_name` varchar(50) NOT NULL,
  `object_description` text NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_sky_object`
--

LOCK TABLES `t_sky_object` WRITE;
/*!40000 ALTER TABLE `t_sky_object` DISABLE KEYS */;
INSERT INTO `t_sky_object` VALUES ('1','Cassiopeia','','2019-02-27 09:02:26','2019-02-27 09:02:26'),('10','Messier 81','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('11','Messier 82','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('12','NGC 3982','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('13','Messier 66','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('14','NGC 3521','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('15','Messier 95','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('16','Messier 96','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('17','Messier 94','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('2','Usra Major','','2019-02-27 09:02:26','2019-02-27 09:02:26'),('3','Orion','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('4','Southern Cross','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('5','Lyra','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('6','NGC 1300','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('7','NGC 1309','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('8','NGC 1232','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('9','NGC 4102','It is the constellation','2019-02-27 15:02:00','2019-02-27 15:02:00'),('abcd','Ursa Major','It is the constellation','2019-02-11 15:34:28','2019-02-11 15:34:28');
/*!40000 ALTER TABLE `t_sky_object` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'galaxy'
--

--
-- Dumping routines for database 'galaxy'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-02-27 15:06:00
