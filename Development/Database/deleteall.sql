start transaction;
Delete from cass.Task2User;	
ALTER TABLE cass.Task2User AUTO_INCREMENT=1;
Delete from cass.Tasks2File;	
ALTER TABLE cass.Tasks2File AUTO_INCREMENT=1;
Delete from cass.conflicts;	
ALTER TABLE cass.conflicts AUTO_INCREMENT=1;
Delete from cass.tasks;	
ALTER TABLE cass.tasks AUTO_INCREMENT=1;
Delete from cass.session;	
ALTER TABLE cass.session AUTO_INCREMENT=1;
#commit