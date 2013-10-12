<?php
include('connection.php');
include('auth.php');

if($uid >= 0) {
    $query = "DELETE FROM games WHERE uid = '$uid';";
    $result = mysql_query($query);
    $query = "DELETE FROM players WHERE gid = '$uid';";
    $result = mysql_query($query);
    $query = "DELETE FROM players WHERE uid = '$uid';";
    $result = mysql_query($query);
    $query = "DELETE FROM wifi WHERE uid = '$uid';";
    $result = mysql_query($query);
}
?>