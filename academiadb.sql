USE `academia`

CREATE TABLE `students` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `last_name` varchar(100) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `study_cycle` enum('licență','master') NOT NULL,
  `study_year` int(11) NOT NULL,
  `group` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `studenti_unique` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `professors` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `last_name` varchar(100) NOT NULL,
  `first name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `teaching_degree` enum('asist','șef lucr','conf','prof') DEFAULT NULL,
  `association_type` enum('titular','asociat','extern') NOT NULL DEFAULT 'extern',
  `affiliation` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `profesori_unique` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `lectures` (
  `CODE` varchar(100) NOT NULL,
  `ID_holder` int(11) NOT NULL,
  `lecture_name` varchar(100) NOT NULL,
  `study_year` varchar(100) NOT NULL,
  `lecture_type` enum('impusă','opțională','liber_aleasă') NOT NULL,
  `lecture_category` enum('domeniu','specialitate','adiacență') NOT NULL,
  `examination_type` enum('examen','colocviu') NOT NULL,
  PRIMARY KEY (`CODE`),
  KEY `discipline_profesori_FK` (`ID_holder`),
  CONSTRAINT `discipline_profesori_FK` FOREIGN KEY (`ID_holder`) REFERENCES `professors` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `join_ds` (
  `DisciplineID` varchar(100) NOT NULL,
  `StudentID` int(11) NOT NULL,
  PRIMARY KEY (`DisciplineID`,`StudentID`),
  KEY `join_ds_studenți_FK` (`StudentID`),
  CONSTRAINT `join_ds_discipline_FK` FOREIGN KEY (`DisciplineID`) REFERENCES `lectures` (`CODE`),
  CONSTRAINT `join_ds_studenți_FK` FOREIGN KEY (`StudentID`) REFERENCES `students` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;