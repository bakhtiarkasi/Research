#************************************************************
# Sequel Pro SQL dump
# Version 4096
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: cse.unl.edu (MySQL 5.5.33-MariaDB)
# Database: rleano
# Generation Time: 2014-07-02 21:13:33 +0000
# ************************************************************

use bkasi;


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_actions`;

CREATE TABLE `derby_actions` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `issue_code` varchar(15) NOT NULL DEFAULT '',
  `author` varchar(255) DEFAULT '',
  `date` varchar(255) DEFAULT '',
  `action` varchar(255) NOT NULL DEFAULT '',
  `field` varchar(255) NOT NULL DEFAULT '',
  `oldValue` varchar(255) NOT NULL DEFAULT '',
  `newValue` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `issue_code_index` (`issue_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_comment`;

CREATE TABLE `derby_comment` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `issue_id` int(11) NOT NULL,
  `author` varchar(255) NOT NULL DEFAULT '',
  `date` varchar(255) DEFAULT NULL,
  `comment` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_commit`;

CREATE TABLE `derby_commit` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `hash` varchar(255) NOT NULL DEFAULT '',
  `projectPath` varchar(255) NULL,
  `date` varchar(255) NOT NULL DEFAULT '',
  `author` varchar(255) NOT NULL,
  `author_mail` varchar(255) NOT NULL DEFAULT '',
  `committer` varchar(255) NOT NULL DEFAULT '',
  `committer_mail` varchar(255) NOT NULL DEFAULT '',
  `comment` longtext,
  PRIMARY KEY (`id`),
  KEY `hash_index` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_file`;
CREATE TABLE `derby_file` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `commit_id` int(11) NOT NULL,
  `filename` varchar(255) NOT NULL DEFAULT '',
  `extension` varchar(255) DEFAULT NULL,
  `isSource` binary(1) NOT NULL DEFAULT '0',
  `creationDate` varchar(255) DEFAULT NULL,
  `createdCommit` varchar(255) DEFAULT NULL,
  `gitProject` varchar(700) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Filename` (`filename`),
  KEY `isSource` (`isSource`)
) ENGINE=InnoDB AUTO_INCREMENT=58318 DEFAULT CHARSET=utf8;



# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_filedata`;

CREATE TABLE `derby_filedata` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `commit_id` int(11) NOT NULL,
  `commit_hash` varchar(255) NOT NULL DEFAULT '',
  `issue_id` int(11) DEFAULT NULL,
  `issue_key` varchar(255) DEFAULT NULL,
  `filename` varchar(255) NOT NULL DEFAULT '',
  `last_author` varchar(255) NOT NULL DEFAULT '',
  `last_committer` varchar(255) NOT NULL,
  `date` varchar(255) NOT NULL DEFAULT '',
  `authors` int(11) NOT NULL,
  `committers` int(11) NOT NULL,
  `LOC` int(11) NOT NULL,
  `code` int(11) NOT NULL,
  `comment` int(11) NOT NULL,
  `comment_ratio` float DEFAULT NULL,
  `centrality` float DEFAULT NULL,
  `frequency` int(11) NOT NULL,
  `complexity` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hashfile_unique` (`commit_hash`,`filename`),
  KEY `hashfile_index` (`commit_hash`,`filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_issue`;

CREATE TABLE `derby_issue` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `key` varchar(15) NOT NULL DEFAULT '',
  `title` varchar(1000) NOT NULL DEFAULT '',
  `description` longtext,
  `reporter_name` varchar(255) DEFAULT NULL,
  `assignee_name` varchar(255) DEFAULT NULL,
  `component` varchar(255) DEFAULT NULL,
  `date_issued` varchar(255) DEFAULT NULL,
  `date_resolved` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `priority` varchar(255) DEFAULT NULL,
  `resolution` varchar(255) DEFAULT NULL,
  `reporter` varchar(255) DEFAULT NULL,
  `assignee` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_unique` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# ------------------------------------------------------------

DROP TABLE IF EXISTS `derby_parent`;

CREATE TABLE `derby_parent` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `commit_id` int(11) NOT NULL,
  `parent_hash` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `commit_index` (`commit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `derby_issue2commit`;

CREATE TABLE `derby_issue2commit` (
  `issue_id` int(11) NOT NULL,
  `commit_id` int(11) NOT NULL,
  PRIMARY KEY (`issue_id`,`commit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;





/*

Delete from derby_actions;
Delete from derby_comment;
Delete from derby_commit;
Delete from derby_file;
Delete from derby_filedata;
Delete from derby_issue;
Delete from derby_issue2commit;
Delete from derby_parent;

ALTER TABLE derby_actions AUTO_INCREMENT = 1;
ALTER TABLE derby_comment AUTO_INCREMENT = 1;
ALTER TABLE derby_commit AUTO_INCREMENT = 1;
ALTER TABLE derby_file AUTO_INCREMENT = 1;
ALTER TABLE derby_filedata AUTO_INCREMENT = 1;
ALTER TABLE derby_issue AUTO_INCREMENT = 1;
ALTER TABLE derby_issue2commit AUTO_INCREMENT = 1;
ALTER TABLE derby_parent AUTO_INCREMENT = 1;










*/



