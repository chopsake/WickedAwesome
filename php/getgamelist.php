<?php
include('connection.php');
include('auth.php');

// check for valid uid
if($uid >= 0) {
    // get the host id from the game joined
    $sql = "SELECT gid From players where uid = '$uid';";
    $result = mysql_query($sql);
    $row = mysql_fetch_array($result);
    if ($row) {
        $gid = $row['gid'];

        $data = array();
        // grab the blue tooth mac id and set the return
        $sql = "SELECT bid From users where uid = '$gid';";
        $result = mysql_query($sql);
        $row = mysql_fetch_array($result);
        $data['bid'][] = $row['bid'];

        // grab the game data
        $sql = "SELECT pref, lat, lon From games where uid = '$gid';";
        $result = mysql_query($sql);
        $row = mysql_fetch_array($result);
        $data['pref'][] = $row['pref'];
        $data['lat'][] = $row['lat'];
        $data['lon'][] = $row['lon'];


        $query = "SELECT macid, snr FROM wifi WHERE uid = '$gid';";
        $result = mysql_query($query);
        $rows = mysql_num_rows($result);
        while($row = mysql_fetch_array($result)) {
            $data['macid'][] = $row['macid'];
            $data['snr'][] = $row['snr'];
        }
        $data['response'][] = 1;
    }
    // check to see if game was droped
    else {
        $data['response'][] = 2;
    }
}
// invalid login
else {
    $data['response'][] = 0;
}

echo json_encode($data);

mysql_close($con);

?>