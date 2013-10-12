<?php
include('connection.php');
include('auth.php');
include('gamecheck.php');

/*
 *	option 1 return list of games avaliable
 *		order for wifi return
 *			responce = 10
 *			macid
 *			hid 			host id
 *			pref			game pref
 *		order for GPS return
 *			response = 11
 *			hid
 *			lat
 *			lon
 *			pref
 *	otion 2 insert the sclected game to join
 *		return response
 *	return 0 invalid login
 *	return 1 valid operation
 *	return 2 already in a game
 *
 */

$condition = $_POST['cond'];

// check for valid uid
if($uid >= 0)
{
    $return = gameTest($uid);
    // Already in a game
    if ($return == 1 || $return == 2)
    $data['response'][] = $return;
    else {
        // display list of posible games
        if ($condition == 1) {
            $data = array();
            $data = NULL;
            // number of wifi signals recived at curent location
            $numWifi = $_POST['numWifi'];
            if ($numWifi > 0) {
                $query = "SELECT uid, macid FROM wifi;";
                $result = mysql_query($query);
                $rows = mysql_num_rows($result);
                while($row = mysql_fetch_array($result))
                {	// insert only those with the same wifi id
                    foreach ($data['hid'] as &$val) {
                        if ($val == $row['uid'])
                        $found = true;
                    }
                    if (!$found) {
                        for ($i = 0; $i < $numWifi; $i++) {
                            // $_POST['macid' . ($i + 1)] is the first macid = post name for first is macid1
                            if ($_POST['macid' . ($i + 1)] == $row['macid']) {
                                // responce 10 means it is a wifi connection
                                $data['response'][] = 10;
                                $data['hid'][] = $row['uid'];
                                $temp = $row['uid'];
                                // get the pref info from games
                                $sql = "SELECT pref FROM games WHERE uid = '$temp';";
                                $result1 = mysql_query($sql);
                                $row1 = mysql_fetch_array($result1);
                                $data['pref'][] = $row1['pref'];
                            }
                        }
                    }
                    else {
                        $found = false;
                    }
                }
            }
            // insert the satelite data
            $lat = $_POST['lat'];
            if ($lat) {
                $lon = $_POST['lon'];
                $query = "SELECT uid, lat, lon, pref FROM games;";
                $result = mysql_query($query);
                $rows = mysql_num_rows($result);
                if($rows > 0)
                {
                    $found = false;
                    while($row = mysql_fetch_array($result))
                    {	// insert only those within one mile
                        foreach ($data['hid'] as &$val) {
                            if ($val == $row['uid'])
                            $found = true;
                        }
                        if (!$found) {
                            if (sqrt(pow((69.1*($lat - $row['lat'])),2) + pow((69.1*($lon - $row['lon'])*cos($row['lat']/57.3)),2)) < 1) {
                                // response 11 indicates sate data follows
                                $data['response'][] = 11;
                                $data['hid'][] = $row['uid'];
                                $data['pref'][] = $row['pref'];
                            }
                        }
                        else {
                            $found = false;
                        }
                    }
                }
            }
            // no games to join
            if (!$data)
            $data['response'][] = 5;
        }
        // update database for the game selected
        else if ($condition == 2) {
            $gid = $_POST['hid'];
            $insert = "INSERT INTO players VALUES ('$gid','$uid');";
            $result = mysql_query($insert);
            $data['response'][] = 1;
        }
    }
}
// invalid login
else
$data['response'][] = 0;

echo json_encode($data);

mysql_close($con);

?>