<?php
$con = mysql_connect('mmayfieldcom.fatcowmysql.com', 'ecs152', 'ecs152c');
if (!$con) {
    die('Could not connect: ' . mysql_error());
}
mysql_select_db(ecs152);
?>