-- MySQL dump 10.13  Distrib 8.0.25, for Win64 (x86_64)
--
-- Host: localhost    Database: online_shop_prod
-- ------------------------------------------------------
-- Server version	8.0.25

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `address_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL DEFAULT 'No address was set',
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`address_id`),
  UNIQUE KEY `address_id_UNIQUE` (`address_id`),
  KEY `fk_address_to_user_idx` (`user_id`),
  CONSTRAINT `fk_address_to_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (55,'Oslo, Norway, Holbergs gate 30',42);
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `cart_id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`cart_id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (40),(41),(42);
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts_items`
--

DROP TABLE IF EXISTS `carts_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts_items` (
  `cart_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `quantity` bigint unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`cart_id`,`item_id`),
  KEY `item_to_cart_idx` (`item_id`),
  CONSTRAINT `cart_to_item` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`),
  CONSTRAINT `item_to_cart` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts_items`
--

LOCK TABLES `carts_items` WRITE;
/*!40000 ALTER TABLE `carts_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `carts_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_name` varchar(45) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_name_UNIQUE` (`category_name`),
  UNIQUE KEY `category_id_UNIQUE` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (16,'Computer devices and parts'),(17,'Computer peripherals');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_verification`
--

DROP TABLE IF EXISTS `email_verification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_verification` (
  `verification_id` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `status` tinyint NOT NULL,
  `sent` tinyint NOT NULL,
  `tried_to_send` tinyint NOT NULL,
  `expiration_date` datetime NOT NULL,
  PRIMARY KEY (`verification_id`),
  UNIQUE KEY `verification_id_UNIQUE` (`verification_id`),
  KEY `verification_to_user_ref_idx` (`user_id`),
  CONSTRAINT `verification_to_user_ref` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_verification`
--

LOCK TABLES `email_verification` WRITE;
/*!40000 ALTER TABLE `email_verification` DISABLE KEYS */;
INSERT INTO `email_verification` VALUES ('2668bbb086faa7f7f38265f8bedbdd64098c47ae24819f7e41efe325b8f3aa45',40,1,1,1,'2022-09-25 20:42:51'),('26d4d4f1dd96deaec074cfd124ea5f44035e4fb428a6cb646bde5d346f8a529b',41,1,1,1,'2022-10-02 20:24:17'),('fe2eecb13300d3206331d2f6fc493f01a41db639bac947ede28e23918877e872',42,1,1,1,'2022-10-02 20:24:53');
/*!40000 ALTER TABLE `email_verification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `images`
--

DROP TABLE IF EXISTS `images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `images` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(256) NOT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`image_id`),
  UNIQUE KEY `image_id_UNIQUE` (`image_id`),
  UNIQUE KEY `file_name_UNIQUE` (`file_name`),
  KEY `image_to_item_idx` (`item_id`),
  CONSTRAINT `image_to_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `images`
--

LOCK TABLES `images` WRITE;
/*!40000 ALTER TABLE `images` DISABLE KEYS */;
INSERT INTO `images` VALUES (29,'bcc8f440f236f748e1a387b4b05e34689c184959feffdb82390affbdfb40689f.jpg',1030),(30,'6679f7ce090a21b35a524c2eee177297be789dda4bf75431d5e4d737d5140297.jpg',1030),(31,'wwwwqweqwe.jpg',1031),(32,'b2a703ce0eb1cb334d43656de99c17402ad559b0fd067be2052a92fa2bc68938.jpg',1031),(33,'05eda9006d15ff0b799604e724f78921bb4c71d8d5a65208cb82322cf3408708.jpg',1032),(34,'6714250498943a8d23933391eccd783a5b782805619f65b037e1fe842d6ea075.jpg',1032),(35,'ebe44b2f49104d9a808bcbf0f038a232a1b85fc59c895dc08d5eb84d019690a6.jpg',1033),(36,'d2cb554aa7b0752e5b70d6e4c0bcde7a98bf5afd03b2ced264b2b74a339c6a5f.jpg',1033),(37,'b06e003ced5c674fd35e44ee37f8b06a21e8b566e72eaf46ca056ac629423ade.jpg',1034),(38,'b2f0f4ed511dd8dfd0f9d5e230d637bc5517039756000173c588fa25c1099a8e.jpg',1034),(39,'851baedd2e8fbbc033583dfec482aaf08760c45878880c6226ff8a6b2eb5b4af.jpg',1035),(40,'15ea6952dfd980ba2ae2e4c058b1a2633be879c6a7c04352d972e01e65221c03.jpg',1035),(41,'614e76bed39ec68cdd18b0ea7566e1a7379212aed4e0d4b33fcec5900c6591ef.jpg',1036),(42,'9d60c3126428e56ab1f015058941af7661ff368478d5d50a832bd5f19ba5589a.png',1036),(43,'cffc4d7f0b26fcd61c23bacdef929f12713008af8756d9703d027c2700bbe76d.jpg',1037),(44,'2c6a7b1b3911fa0c976031379e0604106f59a073f71e447846b60b40f9c9c2d0.jpg',1037),(45,'eb55b2a538dbf9358892a8352a7a600dc8694d659cdf1ee5cc8fb786f51b1945.jpg',1038),(46,'77758c47fa6fe23fbde11d6eb294c7ded3aa23128af42760cbb03071087c8cbd.png',1038),(47,'9da185cea2e2e07d90971211df698eb4a8a9d5943f511a731813fdc00dc16529.jpg',1039),(48,'433409d54e371a5fff81d00df378ef763eccb547f6dd7313df12149fc9d7ebf4.jpg',1039),(49,'d68347f15bd3014afcdfb07cddade7be478ce544ec9ac2029746c5153207efc6.jpg',1040),(50,'f2d41ff7547ce9b5695b1b4b6c365a30bfa97ffb3ed64a0e20b5d830dfecae18.jpg',1040),(51,'10ea48c7924c7f38a1c6651ff4c5715d43db2906885f1c407a9ebad7e127fc05.jpg',1041),(52,'8c84fdbcbd49b4262483cecc15e441c8d55f3f18d43d04fdeea74e4c713bdf95.jpg',1041);
/*!40000 ALTER TABLE `images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_types`
--

DROP TABLE IF EXISTS `item_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_types` (
  `item_type_id` bigint NOT NULL AUTO_INCREMENT,
  `item_type_name` varchar(255) NOT NULL,
  `category_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`item_type_id`),
  UNIQUE KEY `item_type_id_UNIQUE` (`item_type_id`),
  KEY `fk_category_to_type_idx` (`category_id`),
  CONSTRAINT `fk_category_to_type` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_types`
--

LOCK TABLES `item_types` WRITE;
/*!40000 ALTER TABLE `item_types` DISABLE KEYS */;
INSERT INTO `item_types` VALUES (27,'Processors',16),(28,'Motherboards',16),(29,'Monitors',17);
/*!40000 ALTER TABLE `item_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) DEFAULT 'No additional description',
  `item_type_id` bigint NOT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `item_id_UNIQUE` (`item_id`),
  KEY `fk_items_to_item_types_idx` (`item_type_id`),
  CONSTRAINT `fk_items_to_item_types` FOREIGN KEY (`item_type_id`) REFERENCES `item_types` (`item_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1042 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES (1030,'Intel Core i5-11600KF','The Intel Core i5-11600KF BOX processor is based on the Rocket Lake-S architecture, uses a 14nm process technology, and is characterized by high performance and efficiency, which will allow you to easily perform any task. Thanks to this, this model can be an excellent choice for gaming systems and powerful workstations. For the performance of the Intel Core i5-11600KF BOX, 6 cores and 12 threads are responsible, which can operate in a wide frequency range with the possibility of additional overclocking due to the free multiplier. Thanks to this, as well as a large cache of different levels, the processor can provide high performance in a variety of conditions.',27),(1031,'AMD Ryzen 7 3800X','The AMD Ryzen 7 3800X BOX processor is an extra-class computing device that is designed to complete high-performance gaming computers and powerful workstations. The model is produced using a 7-nanometer process technology. The processor has 8 cores. The base frequency of the device is very high - 3900 MHz. The maximum frequency in turbo mode is 4500 MHz',27),(1032,'AMD Ryzen 7 5700X','Based on the Zen 3 architecture, the AMD Ryzen 7 5700X processor can work effectively as part of a high-performance gaming PC. The device has 8 cores. The potential of the processor is fully realized thanks to a significant (32 MB) L3 cache memory. The base frequency of the model is 3400 MHz, and the maximum frequency in turbo mode is 4600 MHz. There is no integrated video core. The AMD Ryzen 7 5700X processor is guaranteed to be compatible with DDR4 memory operating at frequencies from 1600 to 3200 MHz. The maximum amount of memory is 128 GB. Processor power consumption is reduced by supporting Pure Power technology. When choosing a cooling system, you need to focus on a TDP value of 65 watts. The safe operating temperature of the processor is up to 90 °C.',27),(1033,'Intel Core i9-11900KF','The Intel Core i9-11900KF BOX processor is a high-performance solution for productive work and exciting games. This model is made on the Rocket Lake-S architecture using a 14 nm process technology and is equipped with 8 physical cores and 16 virtual threads. Thanks to this, as well as support for a frequency range from 3.5 to 5.3 GHz with the possibility of additional overclocking, the model can provide high-speed computer performance in various tasks. There is no graphics chip in the processor, which reduces the load on the module and allows the use of any discrete video cards. Another feature of the model is support for up to 128 GB of RAM, which will allow you to create an uncompromising solution for work and entertainment.',27),(1034,'Intel Core i9-12900KS','The Intel Core i9-12900KS processor is based on the Alder Lake-S architecture, uses a 7nm process technology, and is characterized by high performance and efficiency, which will allow you to easily perform any task. Thanks to this, this model can be an excellent choice for gaming systems and powerful workstations.',27),(1035,'ASUS TUF GAMING Z690-PLUS','The ASUS TUF GAMING Z690-PLUS motherboard is an excellent choice for creating a high-performance gaming system or workstation. This model corresponds to the Standard-ATX frame size, thanks to which it has a wide range of configuration options. It is based on the Intel Z690 chipset and the LGA 1700 socket, which allows you to use a wide range of Intel processors. Also, this platform supports the installation of discrete video cards - 2 PCI-E x16 slots are provided for them. The maximum amount of RAM can reach 128 GB of DDR5 type with a frequency of up to 6000 MHz. There are 4 DIMM slots for installation. File storage can be represented by 4 SATA drives, as well as 4 M.2 drives.',28),(1036,'MSI MPG Z690 CARBON EK X','The MSI MPG Z690 CARBON EK X motherboard features an original design with branded MPG series decorative elements and offers a powerful computing potential, which is available in a variety of slots and connectors. This board received an LGA 1700 socket and is compatible with Intel processors. The board is equipped with 4 RAM slots, 3 PCI-E x16 slots, 6 SATA III slots, 5 M.2 slots for solid state drives.',28),(1037,'ASUS ROG Crosshair VIII Impact','The ASUS ROG Crosshair VIII Impact Motherboard lets you create a compact yet powerful gaming or work system. This model is made in the Mini-DTX form factor and differs not only in its size, but also in a stylish appearance that combines unusual shapes, premium materials and components, as well as multi-color RGB backlighting. The ASUS ROG Crosshair VIII Impact motherboard is based on the AMD X570 chipset and socket AM4, allowing you to use a variety of AMD processors. To install graphics accelerators, 1 PCI-E 3.0 x16 line is provided. The motherboard supports 2 DIMMs of DDR4 type RAM, its maximum capacity can reach 64 GB, and the frequency is 4800 MHz.',28),(1038,'ASUS ROG STRIX B550-XE GAMING WIFI','ASUS ROG STRIX B550-XE GAMING WIFI motherboard is packed with powerful features, enhanced connectivity and stylish design with multi-zone lighting. It is made in the popular Standard-ATX size and is based on the AMD B550 chipset. The board\'s connection features include 4 slots for RAM memory modules, 3 PCI-E x16 slots and 2 PCI-E x1 slots, 6 SATA III connectors and 2 M.2 slots for drives.',28),(1039,'Acer Nitro VG252QPbmiipx','The Acer Nitro VG252QPbmiipx [UM.KV2EE.P01] monitor belongs to a line of solutions for gamers, which emphasize its design with original colors and powerful technical characteristics. The monitor uses an Acer Agile-Splendor fast liquid crystal panel with a 99% sRGB color gamut. The IPS matrix with a diagonal of 24.5 inches of the FullHD standard demonstrates a colorful realistic picture. 0.9ms response time and 144Hz refresh rate with G-Sync support eliminate stuttering and stuttering in fast-paced games.',29),(1040,'AOC 27G2AE/BK','The AOC 27G2AE/BK monitor has an attractive design with original decorative inserts and a number of features that make it functional for gamers. On the IPS FullHD screen with a diagonal of 27 inches, the image will please with clarity, brightness and saturation. Matte finish and eye protection technologies promote safe viewing, while 1ms response time and 144Hz refresh rate help prevent image blur.',29),(1041,'LG UltraGear 24GN600-B','The LG 24GN600-B [24GN600-B.ARUZ] monitor is characterized by high performance and high-quality detailed images in FullHD format. The 23.8-inch model supports HDR10 technology and AMD FreeSync Premium, which ensures natural reproduction and vivid colors. IPS-matrix with a matte finish and LED-backlit prevents distortion of the image when viewed from the side. Scenes move smoothly with 144Hz and 1ms pixel response.',29);
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items_properties`
--

DROP TABLE IF EXISTS `items_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items_properties` (
  `item_id` bigint NOT NULL,
  `property_value_id` bigint NOT NULL,
  PRIMARY KEY (`item_id`,`property_value_id`),
  KEY `fk_to_property_value_id_idx` (`property_value_id`),
  CONSTRAINT `fk_to_item_id` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `fk_to_property_value_id` FOREIGN KEY (`property_value_id`) REFERENCES `property_values` (`property_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items_properties`
--

LOCK TABLES `items_properties` WRITE;
/*!40000 ALTER TABLE `items_properties` DISABLE KEYS */;
INSERT INTO `items_properties` VALUES (1030,56),(1031,56),(1032,56),(1033,56),(1034,56),(1030,57),(1030,58),(1033,58),(1030,59),(1033,59),(1030,60),(1030,61),(1030,62),(1030,63),(1031,63),(1032,63),(1033,63),(1034,63),(1031,64),(1031,65),(1031,66),(1032,66),(1031,67),(1032,67),(1033,67),(1031,68),(1032,68),(1033,68),(1031,69),(1032,69),(1032,70),(1032,71),(1033,72),(1033,73),(1034,74),(1034,75),(1034,76),(1034,77),(1034,78),(1031,79),(1032,79),(1030,80),(1033,80),(1034,80),(1035,81),(1037,81),(1038,81),(1036,82),(1035,83),(1037,83),(1038,83),(1036,84),(1035,85),(1036,85),(1037,86),(1038,86),(1035,87),(1036,87),(1038,87),(1037,88),(1035,89),(1036,89),(1037,90),(1038,90),(1035,91),(1036,91),(1037,91),(1038,91),(1035,92),(1037,93),(1038,93),(1036,94),(1035,95),(1037,95),(1036,96),(1038,96),(1035,97),(1036,98),(1037,99),(1038,100),(1039,102),(1040,103),(1041,104),(1039,105),(1040,105),(1041,105),(1039,106),(1040,107),(1041,107),(1039,108),(1040,108),(1041,109),(1039,110),(1040,111),(1041,111),(1039,112),(1040,112),(1041,113);
/*!40000 ALTER TABLE `items_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items_quantity_changes`
--

DROP TABLE IF EXISTS `items_quantity_changes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items_quantity_changes` (
  `change_id` bigint NOT NULL AUTO_INCREMENT,
  `change_value` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `change_date` date NOT NULL,
  PRIMARY KEY (`change_id`),
  KEY `fk_item_to_change_idx` (`item_id`),
  CONSTRAINT `fk_item_to_change` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1404 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items_quantity_changes`
--

LOCK TABLES `items_quantity_changes` WRITE;
/*!40000 ALTER TABLE `items_quantity_changes` DISABLE KEYS */;
INSERT INTO `items_quantity_changes` VALUES (1386,50,1030,NULL,'2022-08-01'),(1387,50,1031,NULL,'2022-08-01'),(1388,50,1032,NULL,'2022-08-01'),(1389,50,1033,NULL,'2022-08-01'),(1390,25,1034,NULL,'2022-08-01'),(1391,25,1035,NULL,'2022-08-01'),(1392,25,1036,NULL,'2022-08-01'),(1393,25,1038,NULL,'2022-08-01'),(1394,20,1039,NULL,'2022-08-01'),(1395,25,1040,NULL,'2022-08-01'),(1396,25,1041,NULL,'2022-08-01'),(1397,-4,1030,144,'2022-08-02'),(1398,-5,1032,144,'2022-08-02'),(1399,-2,1034,144,'2022-08-02'),(1400,-1,1036,144,'2022-08-02'),(1401,-2,1040,144,'2022-08-02'),(1402,-1,1034,145,'2022-09-25'),(1403,-1,1041,145,'2022-09-25');
/*!40000 ALTER TABLE `items_quantity_changes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `status` varchar(100) NOT NULL DEFAULT 'CREATED',
  `paid` tinyint NOT NULL,
  `user_id` bigint NOT NULL,
  `address_id` bigint unsigned NOT NULL,
  `description` varchar(255) NOT NULL DEFAULT 'None',
  `payment_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `order_id_UNIQUE` (`order_id`),
  KEY `order_to_user_idx` (`user_id`),
  KEY `fk_order_to_address_idx` (`address_id`),
  CONSTRAINT `fk_order_to_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`address_id`),
  CONSTRAINT `fk_order_to_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (144,'2022-08-02','2022-09-25','COMPLETED',1,42,55,'Order is completed, items are delivered','SENSITIVE INFO'),(145,'2022-09-25','2022-09-25','COMPLETED',1,42,55,'Order is completed, items are delivered','SENSITIVE INFO');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders_items`
--

DROP TABLE IF EXISTS `orders_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders_items` (
  `order_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `quantity` bigint unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`order_id`,`item_id`),
  KEY `item_to_order_idx` (`item_id`),
  CONSTRAINT `item_to_order` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `order_to_item` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders_items`
--

LOCK TABLES `orders_items` WRITE;
/*!40000 ALTER TABLE `orders_items` DISABLE KEYS */;
INSERT INTO `orders_items` VALUES (144,1030,4),(144,1032,5),(144,1034,2),(144,1036,1),(144,1040,2),(145,1034,1),(145,1041,1);
/*!40000 ALTER TABLE `orders_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_recovery`
--

DROP TABLE IF EXISTS `password_recovery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_recovery` (
  `recovery_id` varchar(255) NOT NULL,
  `used` tinyint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`recovery_id`),
  UNIQUE KEY `recovery_id_UNIQUE` (`recovery_id`),
  KEY `recovery_to_user_ref_idx` (`user_id`),
  CONSTRAINT `recovery_to_user_ref` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_recovery`
--

LOCK TABLES `password_recovery` WRITE;
/*!40000 ALTER TABLE `password_recovery` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_recovery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prices`
--

DROP TABLE IF EXISTS `prices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prices` (
  `price_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `price` decimal(18,6) NOT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`price_id`),
  UNIQUE KEY `price_id_UNIQUE` (`price_id`),
  KEY `price_to_item_idx` (`item_id`),
  CONSTRAINT `price_to_item_ref` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prices`
--

LOCK TABLES `prices` WRITE;
/*!40000 ALTER TABLE `prices` DISABLE KEYS */;
INSERT INTO `prices` VALUES (33,'2022-01-01',NULL,326.550000,1030),(34,'2022-01-01',NULL,345.560000,1031),(35,'2022-01-01',NULL,388.750000,1032),(36,'2022-01-01',NULL,587.460000,1033),(37,'2022-01-01',NULL,1382.270000,1034),(38,'2022-01-01',NULL,483.780000,1035),(39,'2022-01-01',NULL,950.310000,1036),(40,'2022-01-01',NULL,518.340000,1037),(41,'2022-01-01',NULL,518.340000,1038),(42,'2022-01-01',NULL,362.830000,1039),(43,'2022-01-01',NULL,354.190000,1040),(44,'2022-01-01',NULL,354.190000,1041);
/*!40000 ALTER TABLE `prices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `properties`
--

DROP TABLE IF EXISTS `properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `properties` (
  `property_id` bigint NOT NULL AUTO_INCREMENT,
  `property_name` varchar(255) NOT NULL,
  `item_type_id` bigint NOT NULL,
  PRIMARY KEY (`property_id`),
  UNIQUE KEY `property_id_UNIQUE` (`property_id`),
  KEY `fk_to_item_type_idx` (`item_type_id`),
  CONSTRAINT `fk_to_item_types` FOREIGN KEY (`item_type_id`) REFERENCES `item_types` (`item_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `properties`
--

LOCK TABLES `properties` WRITE;
/*!40000 ALTER TABLE `properties` DISABLE KEYS */;
INSERT INTO `properties` VALUES (33,'Max RAM',27),(34,'Frequency',27),(35,'Core',27),(36,'Socket',27),(37,'Cores quantity',27),(38,'L2 cache',27),(39,'L3 cache',27),(40,'Assembled in',27),(41,'Manufacturer',27),(42,'Manufacturer',28),(43,'Assembled in',28),(44,'Socket',28),(45,'RAM slots',28),(46,'Supported RAM type',28),(47,'Max RAM',28),(48,'M.2 slots quantity',28),(49,'SATA slots quantity',28),(50,'USB',28),(51,'Resolution',29),(52,' Screen diagonal',29),(53,'Matrix type',29),(54,'Refresh frequency',29),(55,'View angle',29),(56,'NVIDIA G-Sync',29),(57,' Matrix backlight type',29);
/*!40000 ALTER TABLE `properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_values`
--

DROP TABLE IF EXISTS `property_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_values` (
  `property_value_id` bigint NOT NULL AUTO_INCREMENT,
  `property_id` bigint NOT NULL,
  `property_value` varchar(255) NOT NULL,
  PRIMARY KEY (`property_value_id`),
  UNIQUE KEY `property_value_id_UNIQUE` (`property_value_id`),
  KEY `fk_property_to_pr_val_idx` (`property_id`),
  CONSTRAINT `fk_property_to_pr_val` FOREIGN KEY (`property_id`) REFERENCES `properties` (`property_id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_values`
--

LOCK TABLES `property_values` WRITE;
/*!40000 ALTER TABLE `property_values` DISABLE KEYS */;
INSERT INTO `property_values` VALUES (56,33,'128 GB'),(57,34,'4.6 GHz'),(58,35,'Intel Rocket Lake-S'),(59,36,'LGA 1200'),(60,37,'6'),(61,38,'3 MB'),(62,39,'12 MB'),(63,40,'China'),(64,34,'3.4 GHz'),(65,35,'AMD Matisse'),(66,36,'AM4'),(67,37,'8'),(68,38,'4 MB'),(69,39,'32 MB'),(70,34,'4.5 GHz'),(71,35,'AMD Vermeer'),(72,34,'5.3 GHz'),(73,39,'16 MB'),(74,36,'LGA 1700'),(75,37,'16'),(76,35,'Intel Alder Lake-S'),(77,39,'30 MB'),(78,34,'5.5 GHz'),(79,41,'AMD'),(80,41,'Intel'),(81,42,'ASUS'),(82,42,'MSI'),(83,43,'China'),(84,43,'Vietnam'),(85,44,'LGA 1700'),(86,44,' AM4'),(87,45,'4'),(88,45,'2'),(89,46,'DDR5'),(90,46,'DDR4'),(91,47,'128 GB'),(92,48,'4'),(93,48,'2'),(94,48,'5'),(95,49,'4'),(96,49,'6'),(97,50,'USB 3.2 Gen1 Type A x4, USB 3.2 Gen2 Type A x2, USB 3.2 Gen1 Type C, USB 3.2 Gen2x2 Type C'),(98,50,'USB 2.0 x4, USB 3.2 Gen2x2 Type C, USB 3.2 Gen2 Type A x5'),(99,50,'USB 3.2 Gen2 Type C, USB 3.2 Gen1 Type A x2, USB 3.2 Gen2 Type A x5'),(100,50,'USB 2.0 x4, USB 3.2 Gen2 Type C, USB 3.2 Gen2 Type A x2, USB 2.0 Type C'),(101,51,'1920x1080'),(102,52,'24.5\"'),(103,52,' 27\"'),(104,52,'23.8\"'),(105,53,'IPS'),(106,54,'165 HZ'),(107,54,'144 HZ'),(108,55,'178°'),(109,55,'	170°'),(110,56,'Yes'),(111,56,'No'),(112,57,'LED'),(113,57,'CCFL');
/*!40000 ALTER TABLE `property_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(256) NOT NULL,
  `roles` varchar(45) NOT NULL,
  `cart_id` bigint NOT NULL,
  `non_expired` tinyint NOT NULL DEFAULT '1',
  `non_locked` tinyint NOT NULL DEFAULT '1',
  `credentials_non_expired` tinyint NOT NULL DEFAULT '1',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `email` varchar(255) NOT NULL,
  `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `registration_ip` varchar(20) NOT NULL,
  `last_ip` varchar(20) NOT NULL,
  `last_access_date` datetime NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  KEY `user_to_cart_idx` (`cart_id`),
  CONSTRAINT `user_to_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (40,'admin','$2a$10$StDytPPshZ3ntBlQ4iXY.OmxIPYYkGn4gFB8dgiQBkIoT8YLvWcoK','ADMIN',40,1,1,1,1,'admin@admin.com','2022-09-18 20:42:51','0:0:0:0:0:0:0:1','0:0:0:0:0:0:0:1','2022-09-25 20:29:35'),(41,'Operator','$2a$10$aRsgeBahqBcIjzlcmnuQreKi5ycBDJRwKtnHX8mYLrHvGE05NO8Ru','OPERATOR',41,1,1,1,1,'operator@online.shop','2022-09-25 20:24:17','0:0:0:0:0:0:0:1','0:0:0:0:0:0:0:1','2022-09-25 20:24:17'),(42,'Robert','$2a$10$Ivpmv6jJ2nHERTJAjuw6Ne8P7ZcCVpnggqfyd69.Qsui7LIeWa.XW','USER',42,1,1,1,1,'robert@company.com','2022-09-25 20:24:53','0:0:0:0:0:0:0:1','0:0:0:0:0:0:0:1','2022-09-25 20:24:53');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-09-25 20:45:22
