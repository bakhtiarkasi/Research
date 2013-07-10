use CassandraConflict;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

getAllEventsdelimiter $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getAllEvents`()
BEGIN
Select * from enmeventtype;
END$$


-- -----------------------------------------------------
-- Table `CassandraConflict`.`Session`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CassandraConflict`.`Session` ;

CREATE  TABLE IF NOT EXISTS `CassandraConflict`.`Session` (
  `pkSessionID` INT NOT NULL AUTO_INCREMENT ,
  `UserName` VARCHAR(145) NOT NULL ,
  `UID` INT NOT NULL ,
  PRIMARY KEY (`pkSessionID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `CassandraConflict`.`Tasks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CassandraConflict`.`Tasks` ;

CREATE  TABLE IF NOT EXISTS `CassandraConflict`.`Tasks` (
  `pkTaskID` INT NOT NULL AUTO_INCREMENT ,
  `fkSessionID` INT NOT NULL ,
  `Description` VARCHAR(145) NOT NULL ,
  `Preference` INT NOT NULL ,
  PRIMARY KEY (`pkTaskID`) ,
  INDEX `fk_Tasks_Session1` (`fkSessionID` ASC) ,
  CONSTRAINT `fk_Tasks_Session1`
    FOREIGN KEY (`fkSessionID` )
    REFERENCES `CassandraConflict`.`Session` (`pkSessionID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CassandraConflict`.`Files`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CassandraConflict`.`Files` ;

CREATE  TABLE IF NOT EXISTS `CassandraConflict`.`Files` (
  `pkFileID` INT NOT NULL AUTO_INCREMENT ,
  `Path` VARCHAR(245) NOT NULL ,
  PRIMARY KEY (`pkFileID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CassandraConflict`.`Tasks2File`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CassandraConflict`.`Tasks2File` ;

CREATE  TABLE IF NOT EXISTS `CassandraConflict`.`Tasks2File` (
  `pkId` INT NOT NULL AUTO_INCREMENT ,
  `fkFileID` INT NOT NULL ,
  `fkTaskID` INT NOT NULL ,
  `SelectionType` CHAR NOT NULL ,
  PRIMARY KEY (`pkId`) ,
  INDEX `fk_Tasks2File_Files` (`fkFileID` ASC) ,
  INDEX `fk_TaskstoFile_Tasks1` (`fkTaskID` ASC) ,
  CONSTRAINT `fk_Tasks2File_Files`
    FOREIGN KEY (`fkFileID` )
    REFERENCES `CassandraConflict`.`Files` (`pkFileID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TaskstoFile_Tasks1`
    FOREIGN KEY (`fkTaskID` )
    REFERENCES `CassandraConflict`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `CassandraConflict`.`Conflicts`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `CassandraConflict`.`Conflicts` ;

CREATE  TABLE IF NOT EXISTS `CassandraConflict`.`Conflicts` (
  `pkConflictId` INT NOT NULL AUTO_INCREMENT ,
  `Type` VARCHAR(45) NOT NULL ,
  `fkTaskID1` INT NOT NULL ,
  `fkTaskID2` INT NOT NULL ,
  PRIMARY KEY (`pkConflictId`) ,
  INDEX `fk_Conflicts_Tasks1` (`fkTaskID1` ASC) ,
  INDEX `fk_Conflicts_Tasks2` (`fkTaskID2` ASC) ,
  CONSTRAINT `fk_Conflicts_Tasks1`
    FOREIGN KEY (`fkTaskID1` )
    REFERENCES `CassandraConflict`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Conflicts_Tasks2`
    FOREIGN KEY (`fkTaskID2` )
    REFERENCES `CassandraConflict`.`Tasks` (`pkTaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
