USE `IDM`

CREATE TABLE `IDM` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('admin', 'service', 'professor','student') NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IDM_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `Invalid` (
  `UUID` varchar(100) NOT NULL,
  `expiration` datetime NOT NULL,
  PRIMARY KEY (`UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT INTO `IDM` (`email`, `password`, `role`) VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', 'admin');
INSERT INTO `IDM` (`email`, `password`, `role`) VALUES ('courses', '5f4dcc3b5aa765d61d8327deb882cf99', 'service');
INSERT INTO `IDM` (`email`, `password`, `role`) VALUES ('academia', '5f4dcc3b5aa765d61d8327deb882cf99', 'service');