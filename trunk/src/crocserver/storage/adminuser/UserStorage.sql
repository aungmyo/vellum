
-- insert
insert into user_ (user_name, display_name, email, subject, role_)
values (?, ?, ?, ?, ?);

-- update_display_name_subject
update user_ 
set 
  display_name = ?, 
  subject = ?,
  updated = now()
where user_name = ?
;

-- exists_username
select count(1) from user_ where user_name = ?
;

-- exists_email
select count(1) from user_ where email = ?
;

-- find_username
select * from user_ where user_name = ?
;

-- find_email
select * from user_ where email = ?
;

-- list
select * from user_ order by user_name
;


