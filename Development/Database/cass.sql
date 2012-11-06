SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


-- -----------------------------------------------------
-- Table `cass`.`Session`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`Session` ;

CREATE  TABLE IF NOT EXISTS `cass`.`Session` (
  `pkSessionID` INT NOT NULL AUTO_INCREMENT ,
  `Source` VARCHAR(145) NOT NULL ,
  `StartTime` DATETIME NOT NULL ,
  `EndTime` DATETIME NOT NULL ,
  `taskStartId` INT NOT NULL ,
  `taskEndId` INT NOT NULL ,
  `p_devs` INT NULL ,
  `p_avgTask` INT NULL ,
  `p_avgFile` INT NULL ,
  `p_MF` DECIMAL(5) NULL ,
  `p_BF` DECIMAL(5) NULL ,
  `p_TF` DECIMAL(5) NULL ,
  `p_DR` VARCHAR(45) NULL ,
  PRIMARY KEY (`pkSessionID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `cass`.`Tasks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`Tasks` ;

CREATE  TABLE IF NOT EXISTS `cass`.`Tasks` (
  `pkTaskID` INT NOT NULL AUTO_INCREMENT ,
  `fkSessionID` INT NOT NULL ,
  `WS_ID` INT NOT NULL ,
  `Description` VARCHAR(145) NOT NULL ,
  `Date` DATETIME NOT NULL ,
  `IsClean` BINARY NOT NULL ,
  PRIMARY KEY (`pkTaskID`) ,
  INDEX `fk_Tasks_Session1` (`fkSessionID` ASC) ,
  CONSTRAINT `fk_Tasks_Session1`
    FOREIGN KEY (`fkSessionID` )
    REFERENCES `cass`.`Session` (`pkSessionID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cass`.`Files`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`Files` ;

CREATE  TABLE IF NOT EXISTS `cass`.`Files` (
  `pkFileID` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(145) NOT NULL ,
  `Path` VARCHAR(245) NULL ,
  PRIMARY KEY (`pkFileID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cass`.`Tasks2File`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`Tasks2File` ;

CREATE  TABLE IF NOT EXISTS `cass`.`Tasks2File` (
  `pkId` INT NOT NULL AUTO_INCREMENT ,
  `fkFileID` INT NOT NULL ,
  `fkTaskID` INT NOT NULL ,
  PRIMARY KEY (`pkId`) ,
  INDEX `fk_Tasks2File_Files` (`fkFileID` ASC) ,
  INDEX `fk_TaskstoFile_Tasks1` (`fkTaskID` ASC) ,
  CONSTRAINT `fk_Tasks2File_Files`
    FOREIGN KEY (`fkFileID` )
    REFERENCES `cass`.`Files` (`pkFileID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TaskstoFile_Tasks1`
    FOREIGN KEY (`fkTaskID` )
    REFERENCES `cass`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cass`.`Conflicts`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`Conflicts` ;

CREATE  TABLE IF NOT EXISTS `cass`.`Conflicts` (
  `pkConflictId` INT NOT NULL AUTO_INCREMENT ,
  `Type` VARCHAR(45) NOT NULL ,
  `fkTaskID1` INT NOT NULL ,
  `fkTaskID2` INT NOT NULL ,
  PRIMARY KEY (`pkConflictId`) ,
  INDEX `fk_Conflicts_Tasks1` (`fkTaskID1` ASC) ,
  INDEX `fk_Conflicts_Tasks2` (`fkTaskID2` ASC) ,
  CONSTRAINT `fk_Conflicts_Tasks1`
    FOREIGN KEY (`fkTaskID1` )
    REFERENCES `cass`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Conflicts_Tasks2`
    FOREIGN KEY (`fkTaskID2` )
    REFERENCES `cass`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cass`.`User`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`User` ;

CREATE  TABLE IF NOT EXISTS `cass`.`User` (
  `pkUserId` INT NOT NULL AUTO_INCREMENT ,
  `UID` INT NOT NULL ,
  `Name` VARCHAR(145) NOT NULL ,
  PRIMARY KEY (`pkUserId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cass`.`Task2User`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cass`.`Task2User` ;

CREATE  TABLE IF NOT EXISTS `cass`.`Task2User` (
  `pkId` INT NOT NULL AUTO_INCREMENT ,
  `fkUserId` INT NOT NULL ,
  `fkTaskID` INT NOT NULL ,
  `Preference` INT NULL ,
  PRIMARY KEY (`pkId`) ,
  INDEX `fk_Task2User_User1` (`fkUserId` ASC) ,
  INDEX `fk_Task2User_Tasks1` (`fkTaskID` ASC) ,
  CONSTRAINT `fk_Task2User_User1`
    FOREIGN KEY (`fkUserId` )
    REFERENCES `cass`.`User` (`pkUserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Task2User_Tasks1`
    FOREIGN KEY (`fkTaskID` )
    REFERENCES `cass`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
