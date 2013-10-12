<?php
include('connection.php');

$sql="SELECT * from players;";
$result = mysql_query($sql);
while($row = mysql_fetch_array($result)) {
    echo "gid: " . $row['gid'] . " | uid: " . $row['uid'] . "<br>";
}

mysql_close($con);
?>