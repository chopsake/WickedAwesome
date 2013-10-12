<?php
function gameTest($uid) {
    $query = "SELECT uid From games where uid = '$uid';";
    $result = mysql_query($query);
    $rows = mysql_num_rows($result);
    // Already host of a game
    if ($rows != 0) {
        return 1;
    }

    $query = "SELECT uid From players where uid = '$uid';";
    $result = mysql_query($query);
    $rows = mysql_num_rows($result);
    // Already player in a game
    if ($rows != 0) {
        return 2;
    }

    return 3; // not in game
}
?>