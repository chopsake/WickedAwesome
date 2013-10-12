<?php
include('keycheck.php');
include('connection.php');
include('gamecheck.php');

$email =    $_POST['email'];
$pass =     $_POST['password'];

$query =    "SELECT uid FROM users WHERE email='$email' AND password=MD5('$pass');";
$result = mysql_query($query);
$rows = mysql_num_rows($result);

$data = array();

if($rows == 1) {
    $row = mysql_fetch_array($result);
    $data['response'][] = gameTest($row['uid']);
}
// invalid login
else {
    $data['response'][] = 0;
}

echo json_encode($data);

mysql_close($con);

?>