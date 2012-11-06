USE `arsonae`;
DROP procedure IF EXISTS `getAllEvents`;

DELIMITER $$
USE `arsonae`$$
CREATE PROCEDURE `arsonae`.`getAllEvents` ()

BEGIN
Select * from enmeventtype;
END
$$
DELIMITER ;