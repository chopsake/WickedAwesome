<?php
include('connection.php');
include('auth.php');
include('gamecheck.php');

/*
 *	insert into game table
 *	uid, lat, lon, and pref
 *	return 0 invalid login
 *	return 1 upon completion of insert
 *	return 2 if already in a game
 */

//if ($_POST['input'] == 0) { //query if in game
//  $data['response'][] = gameTest($uid);
//} else { // create game

// check for valid uid
if($uid >= 0)
{
    $return = gameTest($uid);
    // Already in a game
    if ($return == 1 || $return == 2)
    $data['response'][] = $return;
    else {
        $lat = $_POST['lat'];
        $lon = $_POST['lon'];
        $pref = $_POST['pref'];
        $query = "INSERT INTO games (uid, lat, lon, pref) VALUES ('$uid','$lat','$lon','$pref');";
        $result = mysql_query($query);
        $data['response'][] = $return;
    }
}
// invalid login
else {
    $data['response'][] = 0;
}
//}

echo json_encode($data);

mysql_close($con);

?>