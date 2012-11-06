START TRANSACTION; 
INSERT INTO `cass`.`Files`
(
`Name`,
`Path`)

Select filenames as Name, min(path) as Path from (SELECT distinct SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1) as filenames, filename as Path from mytask.event )T
group by filenames order by 1 limit  30000;

START TRANSACTION;
SET @rank=0;

INSERT INTO `cass`.`User`
(`UID`,
`Name`)

SELECT @rank:=@rank+1 AS rank,  author from
(select distinct author from mytask.alltasks order by 1) t;

#COMMIT
#rollback
 

ALTER TABLE cass.user AUTO_INCREMENT = 1;

select * from cass.user;

Delete  from cass.user where pkuserid > 0;