use cass;

select * from session where pksessionid = 10;

set @userId = 0;
#returns the set of all taks with there wsid, also the user they are assigned to along
# with the username (create T->D) mapping
Select t.pktaskid, t.ws_Id, t.Description, t.isclean, tu.Preference, u.Name, u.pkUserId, u.UID
from tasks t
inner join Task2User tu 
on t.pkTaskId = tu.fkTaskId
inner join user u
on tu.fkUserId = u.pkUserId
where t.fkSessionId = 10
order by t.ws_id;

#returns the set of files assigned to a task/developer.
#create the T->F mapping from this data set
Select t.pktaskid, t.ws_Id, f.Name
from tasks t
inner join Tasks2File tf 
on t.pkTaskId = tf.fkTaskId
inner join files f
on tf.fkFileId = f.pkFileId
where t.fkSessionId = 10;


#returns task1 and task2 id's along with conflict type and the direction
#create conflict set T also T->T mapping with conflict type and weight, direction
select t.ws_Id as task1, t2.ws_Id as task2, c.Type, c.Direction  
from conflicts c
inner join tasks t 
on (t.pkTaskId = c.fkTaskid1)
inner join tasks t2 
on (t2.pkTaskId = c.fkTaskid2)
and t.fksessionId = c.fkSessionId
and t2.fkSessionId = c.fkSessionId
where c.fksessionid = 10;

call SP_GetDataforScheduler(10);


Select *, fkTaskId - (Select min(fkTaskId) - 1 
                       from task2USER ti 
                       inner join tasks t on t.pktaskid = ti.fktaskid
                        where ti.fkuserId = t2u.fkuserId
                        group by t.fkSessionId 
                       )Pref2
from task2USER t2u 
order by fkTaskId, pref2,Preference;


Select min(fkuserid), count(1)
from task2user
group by fkuserid;


Select t2u.*, t.fkSessionId from tasks t
inner join task2user t2u 
on t2u.fktaskid = t.pktaskid
group by fkSessionId,  fkuserid
  

 where table = session

select * from session where pksessionid = 72;

Select Count(Type), fksessionid from conflicts
group by fksessionid, Type order by 1 desc;



select @date := CURDATE();

INSERT INTO `cass`.`session`(`Source`,`Description`,`StartTime`,`EndTime`,`taskStartId`,`taskEndId`,`p_devs`,
                                        `p_avgTask`,`p_avgFile`,`p_MF`,`p_BF`,`p_TF`,`p_DR`)
                       values('Jenkins','Low Scenario',@date,@date, 1, 1, 42, 
                                          4, 3, 0.0,0.0, 0.0, 'W');





