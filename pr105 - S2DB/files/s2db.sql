-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema s2db
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `s2db` ;

-- -----------------------------------------------------
-- Schema s2db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `s2db` DEFAULT CHARACTER SET utf8 ;
USE `s2db` ;

-- -----------------------------------------------------
-- Table `s2db`.`path`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `s2db`.`path` ;

CREATE TABLE IF NOT EXISTS `s2db`.`path` (
  `seq` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`seq`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `s2db`.`device`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `s2db`.`device` ;

CREATE TABLE IF NOT EXISTS `s2db`.`device` (
  `seq` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `mac` VARCHAR(45) NOT NULL,
  `name` VARCHAR(90) NULL,
  PRIMARY KEY (`seq`, `mac`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `s2db`.`session`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `s2db`.`session` ;

CREATE TABLE IF NOT EXISTS `s2db`.`session` (
  `seq` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `seq_device` BIGINT(20) NOT NULL,
  `start` DATETIME NOT NULL,
  `ip_address` VARCHAR(15) NOT NULL,
  `class` VARCHAR(80) NOT NULL,
  PRIMARY KEY (`seq`),
  INDEX `fk_session_device_idx` (`seq_device` ASC),
  CONSTRAINT `fk_session_device`
    FOREIGN KEY (`seq_device`)
    REFERENCES `s2db`.`device` (`seq`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `s2db`.`page`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `s2db`.`page` ;

CREATE TABLE IF NOT EXISTS `s2db`.`page` (
  `seq` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `seq_path` BIGINT(20) NOT NULL,
  `seq_session` BIGINT(20) NULL,
  `last_modified` DATETIME NULL,
  `state` CHAR(1) NULL,
  PRIMARY KEY (`seq`),
  INDEX `fk_page_path_idx` (`seq_path` ASC),
  INDEX `fk_page_session_idx` (`seq_session` ASC),
  CONSTRAINT `fk_page_path`
    FOREIGN KEY (`seq_path`)
    REFERENCES `s2db`.`path` (`seq`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_page_session`
    FOREIGN KEY (`seq_session`)
    REFERENCES `s2db`.`session` (`seq`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `s2db`.`prop`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `s2db`.`prop` ;

CREATE TABLE IF NOT EXISTS `s2db`.`prop` (
  `seq` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `seq_page` BIGINT(20) NOT NULL,
  `name` VARCHAR(80) NOT NULL,
  `value` VARCHAR(1024) NULL,
  PRIMARY KEY (`seq`),
  INDEX `fk_prop_page_idx` (`seq_page` ASC),
  CONSTRAINT `fk_prop_page`
    FOREIGN KEY (`seq_page`)
    REFERENCES `s2db`.`page` (`seq`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `s2db`.`log`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `s2db`.`log` ;

CREATE TABLE IF NOT EXISTS `s2db`.`log` (
  `seq` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `seq_session` BIGINT(20) NULL,
  `time` BIGINT(20) NOT NULL,
  `text` VARCHAR(255) NULL,
  `value` BIGINT(20) NULL,
  PRIMARY KEY (`seq`),
  INDEX `fk_log_session_idx` (`seq_session` ASC),
  CONSTRAINT `fk_log_session`
    FOREIGN KEY (`seq_session`)
    REFERENCES `s2db`.`session` (`seq`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
