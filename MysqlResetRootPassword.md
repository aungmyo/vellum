http://dev.mysql.com/doc/refman/5.0/en/resetting-permissions.html

```
mysqld_safe --skip-grant-tables 

mysql --user=root mysql

update user set Password=PASSWORD('mysqlrootpassword') where user='root';
flush privileges;
exit;
```