<?php
include('connection.php');
include('auth.php');

/*
 *  update bluetooth macid
 *  return 0 invalid login
 *  return 1 upon update
 */

// check for valid uid
if($uid >= 0) {
    $macid = $_POST['macid'];
    $query = "UPDATE wifi SET macid='$macid' WHERE uid='$uid';";
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