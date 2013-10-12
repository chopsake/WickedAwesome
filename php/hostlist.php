<?php
include('connection.php');
include('auth.php');

// check for valid uid
if($uid >= 0)
{
    // get the host id from the game joined
    $sql = "SELECT uid FROM players WHERE gid = '$uid';";
    $result = mysql_query($sql);
    $row = mysql_fetch_array($result);
    if ($row) {
        $uid = $row['uid'];

        $data = array();
        $sql = "SELECT lname, fname FROM users WHERE uid = '$uid';";
        $result = mysql_query($sql);
        $counter = 0;
        while($row = mysql_fetch_array($result)) {
            $data['name'][] = $row['fname'] . " " . $row['lname'];
            $counter++;
        }
        $data['response'][] = $counter; // count of users in game
    } else {
        $data['response'][] = 0; // no one in game
    }
}
else {
    $data['response'][] = -1; // invalid login
}

echo json_encode($data);

mysql_close($con);

?>