<?php
$key = '5bnLHqqqZLw60uctCYh1';

if (isset($_POST['key'])) {
    if ($_POST['key'] == $key) ;
    else {
        die('Key Not Authorized');
    }
}
?>