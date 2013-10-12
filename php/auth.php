<?php

$email =    $_POST['email'];
$pass =     $_POST['password'];

$query = "SELECT uid FROM users WHERE email='$email' AND password=MD5('$pass');";

$result = mysql_query($query);
$rows = mysql_num_rows($result);

if($rows == 1) {
    $row = mysql_fetch_array($result);
    $uid = $row['uid'];
}
else {
    $uid = -1;
}
//    die('No User Found');

?>