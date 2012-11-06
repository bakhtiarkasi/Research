Select taskid, min(startdate)S, max(startdate)E, DATEDIFF(max(enddate),min(startdate))day, Timediff(max(endtime),min(starttime))time  from event group by taskid;

Select min(author), count(author) from task group by author order by 2 desc;

Select count(T.taskid), min(startdate) from (Select taskid, startdate from event group by startdate, taskid)T group by T.startdate order by 2 asc;






Select taskid, createdate, completedate, DATEDIFF(completedate, createdate)day, Timediff(completetime,createtime)time  from alltasks where completetime is not null;

select count(S.T)C, taskid, T from (select SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)T, taskid from event)S group by taskid, S.T order by C desc;


select count(1), S.taskid from (select distinct SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)T, taskid from event)S group by S.taskid order by 1 desc;


select distinct SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)T, taskid from event where taskid = '245343' order by 1;


Select count(1) from event where type not in ('propagation', 'selection', 'prediction');

Select count(1) from event;


Select E1.id, E2.id, E1.taskid, E1.author, E1.file, E1.start, E2.taskid, E2.author, E2.file, E2.start from 
    (select taskid, id,
            SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file, 
            min(startdate)start, 
            max(enddate)end,
            (Select author from task where taskid = e.taskid)author  
    from event e
    group by taskid, file)E1 
    
    inner join (select taskid, id, 
                       SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file, 
                       min(startdate)start, 
                       max(enddate)end,
                       (Select author from task where taskid = e.taskid)author
                from event e
                group by taskid, file)E2 
    
    on E1.file = E2.file 
       and E1.taskid <> E2.taskid 
       and DATEDIFF(E1.start, E2.start) = 0
       and E1.author <> E2.author; 
       
       
       
       
       
select taskid, id,
       SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file, 
       type, startdate, starttime,
       (Select author from task where taskid = e.taskid)author
       from event e
       where id in ('16185','16151','10802','10808','9629', '6293', '19571','8352')
       order by file, startdate, starttime;
       


Select E1.taskid E1taskid, E2.taskid E2taskid, E1.file E1file #,E1.id E1id, E2.id E2id, E1.author E1author, E1.file E1file, E1.start E1Start, E2.author E2author, E2.file E2file, E2.start E2start 
from 
    (select taskid, id,
            SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file, 
            min(startdate)start, 
            max(enddate)end,
            (Select author from task where taskid = e.taskid)author  
    from event e
    where e.taskid  in ('271069' ,'263318' ,'256809' ,'275121' ,'245234' ,'245002' ,'242997' ,'321054' ,'252297' ,'320923' ,'305167' ,'291343' ,'298607' ,'251266' ,'294957' ,'251506' ,'303234' ,'283514' ,'253932' ,'265346' ,'295105' ,'301870' ,'288448' ,'278552' ,'259782' ,'251987' ,'279746' ,'250771' ,'308534' ,'287572' ,'269803' ,'295745' ,'285969' ,'298364' ,'277254' ,'264930' ,'290599' ,'263202' ,'287832' ,'254424' ,'241790' ,'294910' ,'269407' ,'265015' ,'284559' ,'304171' ,'245740' ,'243131' ,'290490' ,'243655' ,'261363' ,'305169' ,'281007' ,'288441' ,'294847' ,'327396' ,'268524' ,'278485' ,'321948' ,'294364' ,'279267' ,'241524' ,'274878' ,'278913' ,'284370' ,'251235' ,'292662' ,'246724' ,'263126' ,'243975' ,'281241' ,'287895' ,'295906' ,'280539' ,'264503' ,'251409' ,'266869' ,'290860' ,'264612' ,'249572' ,'289155' ,'255003' ,'266142' ,'238371' ,'295234' ,'269323' ,'266496' ,'278569' ,'282436' ,'288210' ,'189689' ,'274152' ,'254862' ,'332074' ,'277974' ,'249947' ,'309057' ,'249021' ,'291770' ,'327262' ,'312207' ,'266910' ,'266752' ,'295764' ,'276113' ,'295763' ,'280988' ,'264611' ,'274798' ,'285985' ,'280834' ,'275774' ,'281699' ,'286609' ,'297450' ,'254695' ,'298493' ,'278602' ,'247182' ,'288347' ,'280973' ,'255430' ,'244017' ,'316261' ,'320198' ,'256784' ,'274390' ,'281002' ,'288427' ,'285965' ,'303509' ,'297878' ,'280561' ,'284166' ,'285921' ,'276361' ,'271487' ,'265682')
    group by taskid, file)E1 
    
    inner join (select taskid, id, 
                       SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file, 
                       min(startdate)start, 
                       max(enddate)end,
                       (Select author from task where taskid = e.taskid)author
                from event e
                where e.taskid  in ('271069' ,'263318' ,'256809' ,'275121' ,'245234' ,'245002' ,'242997' ,'321054' ,'252297' ,'320923' ,'305167' ,'291343' ,'298607' ,'251266' ,'294957' ,'251506' ,'303234' ,'283514' ,'253932' ,'265346' ,'295105' ,'301870' ,'288448' ,'278552' ,'259782' ,'251987' ,'279746' ,'250771' ,'308534' ,'287572' ,'269803' ,'295745' ,'285969' ,'298364' ,'277254' ,'264930' ,'290599' ,'263202' ,'287832' ,'254424' ,'241790' ,'294910' ,'269407' ,'265015' ,'284559' ,'304171' ,'245740' ,'243131' ,'290490' ,'243655' ,'261363' ,'305169' ,'281007' ,'288441' ,'294847' ,'327396' ,'268524' ,'278485' ,'321948' ,'294364' ,'279267' ,'241524' ,'274878' ,'278913' ,'284370' ,'251235' ,'292662' ,'246724' ,'263126' ,'243975' ,'281241' ,'287895' ,'295906' ,'280539' ,'264503' ,'251409' ,'266869' ,'290860' ,'264612' ,'249572' ,'289155' ,'255003' ,'266142' ,'238371' ,'295234' ,'269323' ,'266496' ,'278569' ,'282436' ,'288210' ,'189689' ,'274152' ,'254862' ,'332074' ,'277974' ,'249947' ,'309057' ,'249021' ,'291770' ,'327262' ,'312207' ,'266910' ,'266752' ,'295764' ,'276113' ,'295763' ,'280988' ,'264611' ,'274798' ,'285985' ,'280834' ,'275774' ,'281699' ,'286609' ,'297450' ,'254695' ,'298493' ,'278602' ,'247182' ,'288347' ,'280973' ,'255430' ,'244017' ,'316261' ,'320198' ,'256784' ,'274390' ,'281002' ,'288427' ,'285965' ,'303509' ,'297878' ,'280561' ,'284166' ,'285921' ,'276361' ,'271487' ,'265682')
                group by taskid, file)E2 
    
    on E1.file = E2.file 
       and E1.taskid <> E2.taskid 
       order by E1.taskid, E1.file, E2.taskid, E2.file;
       

select taskid, id,
            SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file, 
            min(startdate)start, 
            max(enddate)end,
            (Select author from task where taskid = e.taskid)author  ;
    



    
    Select distinct taskid, SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1)file
    from event e
    where e.taskid in 

(286609,	251235,	290599,	244017,	294364,	189689)
order by taskid, filename
    
    


      
    
   

   