<?php
include('connection.php');
include('auth.php');

/*
 * 	insert wifi mac address
 * 	return 0 invalid login
 * 	return 1 upon insertion
 */

// check for valid uid
if($uid >= 0) {
    // insert data into wifi
    $macid = $_POST['macid'];
    $snr = $_POST['snr'];
    $query = "INSERT INTO wifi (uid, macid, snr) VALUES ('$uid','$macid', '$snr');";
    $result = mysql_query($query);
    $data['response'][] = 1;
}
// invalid login
else {
    $data['response'][] = 0;
}

echo json_encode($data);

mysql_close($con);

?>