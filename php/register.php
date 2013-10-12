<?php
include('keycheck.php');   //verify the key
include('connection.php'); //connection to the database

/* 	return codes
 * 1 = registration ok
 *	0 = email address already registered
 *	2 = invalid email address
 */

function check_email_address($email)
{
    // First, we check that there's one @ symbol, and that the lengths are right
    if (!ereg("^[^@]{1,64}@[^@]{1,255}$", $email)) {
        return false;
    }
    // Split it into sections to make life easier
    $email_array = explode("@", $email);
    $local_array = explode(".", $email_array[0]);

    for ($i = 0; $i < sizeof($local_array); $i++)
    {
        if (!ereg("^(([A-Za-z0-9!#$%&'*+/=?^_`{|}~-][A-Za-z0-9!#$%&'*+/=?^_`{|}~\.-]{0,63})|(\"[^(\\|\")]{0,62}\"))$", $local_array[$i])) {
            return false;
        }
    }

    if (!ereg("^\[?[0-9\.]+\]?$", $email_array[1]))
    {
        // Check if domain is IP. If not, it should be valid domain name
        $domain_array = explode(".", $email_array[1]);

        if (sizeof($domain_array) < 2) {
            return false; // Not enough parts to domain
        }

        for ($i = 0; $i < sizeof($domain_array); $i++)
        {
            if (!ereg("^(([A-Za-z0-9][A-Za-z0-9-]{0,61}[A-Za-z0-9])|([A-Za-z0-9]+))$", $domain_array[$i])) {
                return false;
            }
        }
    }
    return true;
}

$email = $_POST['email'];

if(check_email_address($email))
{
    //$sql = "SELECT * FROM users WHERE email='$email';";
    $sql = "SELECT * FROM users WHERE email=\"" . $email . "\";";

    $result = mysql_query($sql);
    $rows = mysql_num_rows($result);
    $data = array();

    if($rows == 0) { //free to register that person
        $fname = $_POST['fname'];
        $lname = $_POST['lname'];
        $password = $_POST['password'];
        $bid = $_POST['bid'];

        $insert = "INSERT INTO users (fname, lname, password, email, bid) "
        . "VALUES('$fname','$lname', MD5('$password'),'$email', '$bid');";

        mysql_query($insert);
        $data['response'][] = 1;
    }
    else { //there is already someone registered with that email
        $data['response'][] = 0;
    }
}
else {   // invalid email address
    $data['response'][] = 2;
}

echo json_encode($data);

mysql_close($con);

?>